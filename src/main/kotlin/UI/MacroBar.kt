package UI

import java.awt.BorderLayout
import java.awt.KeyboardFocusManager
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.beans.PropertyChangeListener
import javax.swing.*
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea

class MacroBar(private val frame: JFrame, private val tabbedUI: TabbedUI) : JPanel() {

    private val toolbar = ToolBarUI()
    val macroItemsPanel = JPanel().apply { layout = BoxLayout(this, BoxLayout.X_AXIS) }
    private val transferHandler = MacroItemTransferHandler()

    init {
        val theme = Theme()
        layout = BorderLayout()
        background = theme.SecondaryBackgroundColor
        macroItemsPanel.background = theme.SecondaryBackgroundColor
        macroItemsPanel.transferHandler = transferHandler

        val newEventIcon = SvgIconRenderer.getIcon("/add-file-icon.svg", 24, 24)
        if (newEventIcon != null) {
            toolbar.addButton(newEventIcon, "New Event") { 
                val focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner
                val wasEditorInFocus = focusOwner is RSyntaxTextArea

                val selectedComponent = tabbedUI.selectedComponent
                if (selectedComponent is MacroJsonEditorUI) {
                    val dialog = NewEventDialog(frame)
                    dialog.isVisible = true
                    dialog.createdEvent?.let { event ->
                        selectedComponent.insertNewEvent(event, wasEditorInFocus)
                    }
                }
            }
        }

        val recordIcon = SvgIconRenderer.getIcon("/play-button-outline-green-icon.svg", 24, 24)
        if (recordIcon != null) {
            toolbar.addButton(recordIcon, "Start/Stop Recording") {}
        }

        val undoIcon = SvgIconRenderer.getIcon("/undo-circle-outline-icon.svg", 24, 24)
        if (undoIcon != null) {
            toolbar.addButton(undoIcon, "Undo last action") {}
        }

        val redoIcon = SvgIconRenderer.getIcon("/redo-circle-outline-icon.svg", 24, 24)
        if (redoIcon != null) {
            toolbar.addButton(redoIcon, "Redo last action") {}
        }

        add(toolbar, BorderLayout.NORTH)
        add(macroItemsPanel, BorderLayout.CENTER)

        repaint()
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        super.addPropertyChangeListener(listener)
    }

    fun addMacroItem(item: MacroItem) {
        item.transferHandler = transferHandler
        macroItemsPanel.add(item)
    }

    fun clear() {
        macroItemsPanel.removeAll()
        macroItemsPanel.revalidate()
        macroItemsPanel.repaint()
    }

    fun notifyItemsReordered() {
        firePropertyChange("component.reordered", null, null)
    }

    inner class MacroItemTransferHandler : TransferHandler() {

        private val macroItemIndexFlavor = DataFlavor(Integer::class.java, "Macro Item Index")

        override fun getSourceActions(c: JComponent?): Int {
            return MOVE
        }

        override fun createTransferable(c: JComponent?): Transferable? {
            if (c is MacroItem) {
                val index = macroItemsPanel.components.indexOf(c)
                if (index != -1) {
                    return object : Transferable {
                        override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(macroItemIndexFlavor)
                        override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean = flavor == macroItemIndexFlavor
                        override fun getTransferData(flavor: DataFlavor?): Any = if (isDataFlavorSupported(flavor)) index else throw UnsupportedFlavorException(flavor)
                    }
                }
            }
            return null
        }

        override fun canImport(support: TransferSupport?): Boolean {
            return support?.isDataFlavorSupported(macroItemIndexFlavor) == true && support.component == macroItemsPanel
        }

        override fun importData(support: TransferSupport?): Boolean {
            if (!canImport(support) || support == null) {
                return false
            }

            val transferable = support.transferable
            val sourceIndex = try {
                transferable.getTransferData(macroItemIndexFlavor) as Int
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

            val draggedItem = macroItemsPanel.getComponent(sourceIndex) as? MacroItem ?: return false

            val dropPoint = support.dropLocation.dropPoint
            var targetIndex = macroItemsPanel.componentCount

            for (i in 0 until macroItemsPanel.componentCount) {
                val component = macroItemsPanel.getComponent(i)
                if (dropPoint.x < component.x + component.width / 2) {
                    targetIndex = i
                    break
                }
            }

            if (sourceIndex == targetIndex || (sourceIndex != -1 && targetIndex == sourceIndex + 1)) {
                return false
            }

            macroItemsPanel.remove(draggedItem)
            if (sourceIndex < targetIndex) {
                targetIndex--
            }

            macroItemsPanel.add(draggedItem, targetIndex)

            macroItemsPanel.revalidate()
            macroItemsPanel.repaint()

            this@MacroBar.notifyItemsReordered()

            return true
        }
    }
}