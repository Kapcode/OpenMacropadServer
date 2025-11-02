package UI

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.KeyboardFocusManager
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.beans.PropertyChangeListener
import javax.swing.*
import java.awt.Point
import java.awt.Image
import java.awt.image.BufferedImage
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent

class MacroBar(private val frame: JFrame, private val tabbedUI: TabbedUI) : JPanel() {

    private val toolbar = ToolBarUI()
    val macroItemsPanel = JPanel().apply { layout = BoxLayout(this, BoxLayout.X_AXIS) }
    private val triggerSlot = JPanel(BorderLayout())
    private val transferHandler = MacroItemTransferHandler()

    init {
        val theme = Theme()
        layout = BoxLayout(this, BoxLayout.X_AXIS) // Use BoxLayout for the main panel
        background = theme.SecondaryBackgroundColor

        triggerSlot.border = BorderFactory.createTitledBorder("Trigger")
        triggerSlot.transferHandler = transferHandler
        triggerSlot.preferredSize = Dimension(200, 0)
        triggerSlot.maximumSize = Dimension(200, Integer.MAX_VALUE)
        add(triggerSlot)

        val itemsPanelWithToolbar = JPanel(BorderLayout())
        
        val newEventIcon = SvgIconRenderer.getIcon("/add-file-icon.svg", 24, 24)
        if (newEventIcon != null) {
            toolbar.addButton(newEventIcon, "New Event") { 
                val focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner
                val wasEditorInFocus = focusOwner is JTextArea

                val selectedComponent = tabbedUI.selectedComponent
                if (selectedComponent is MacroJsonEditorUI) {
                    val dialog = NewEventDialog(frame)
                    dialog.isVisible = true
                    dialog.createdEvent?.let { event ->
                        selectedComponent.insertNewEvent(event, dialog.isTriggerEvent, wasEditorInFocus)
                    }
                }
            }
        }

        val recordIcon = SvgIconRenderer.getIcon("/green-circle-shape-icon.svg", 24, 24)
        val stopIcon = SvgIconRenderer.getIcon("/red-circle-shape-icon.svg", 24, 24)
        val recordButton = if (recordIcon != null) {
            ToolBarButton(recordIcon, "Start Recording") {}
        } else {
            ToolBarButton("Record", "Start Recording") {}
        }
        var isRecording = false
        recordButton.addActionListener { 
            isRecording = !isRecording
            if (isRecording) {
                if (stopIcon != null) recordButton.setIcon(stopIcon)
                recordButton.setToolTipText("Stop Recording")
                println("Recording Started") // Placeholder
            } else {
                if (recordIcon != null) recordButton.setIcon(recordIcon)
                recordButton.setToolTipText("Start Recording")
                println("Recording Stopped") // Placeholder
            }
        }
        toolbar.add(recordButton)

        val undoIcon = SvgIconRenderer.getIcon("/undo-circle-outline-icon.svg", 24, 24)
        if (undoIcon != null) {
            toolbar.addButton(undoIcon, "Undo last action") {}
        }

        val redoIcon = SvgIconRenderer.getIcon("/redo-circle-outline-icon.svg", 24, 24)
        if (redoIcon != null) {
            toolbar.addButton(redoIcon, "Redo last action") {}
        }

        itemsPanelWithToolbar.add(toolbar, BorderLayout.NORTH)

        val scrollPane = JScrollPane(macroItemsPanel).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER
            border = null
            // Increase the horizontal scroll speed
            horizontalScrollBar.unitIncrement = 20
        }
        itemsPanelWithToolbar.add(scrollPane, BorderLayout.CENTER)
        add(itemsPanelWithToolbar)

        repaint()
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        super.addPropertyChangeListener(listener)
    }

    fun addMacroItem(item: MacroItem) {
        item.transferHandler = transferHandler
        macroItemsPanel.add(item)
    }

    fun setTriggerItem(item: MacroItem) {
        triggerSlot.removeAll()
        item.transferHandler = transferHandler
        triggerSlot.add(item, BorderLayout.CENTER)
        triggerSlot.revalidate()
        triggerSlot.repaint()
    }

    fun getTriggerItem(): MacroItem? {
        return if (triggerSlot.componentCount > 0) triggerSlot.getComponent(0) as? MacroItem else null
    }

    fun clear() {
        macroItemsPanel.removeAll()
        triggerSlot.removeAll()
        revalidate()
        repaint()
    }

    fun notifyItemsReordered() {
        firePropertyChange("component.reordered", null, null)
    }

    inner class MacroItemTransferHandler : TransferHandler() {

        private val macroItemIndexFlavor = DataFlavor(Integer::class.java, "Macro Item Index")
        private var draggedComponent: MacroItem? = null // Store the component being dragged

        override fun getSourceActions(c: JComponent): Int {
            return COPY_OR_MOVE
        }

        override fun createTransferable(c: JComponent): Transferable? {
            if (c is MacroItem) {
                draggedComponent = c // Store the component
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

        override fun getDragImage(): Image? {
            val c = draggedComponent ?: return null
            val image = BufferedImage(c.width, c.height, BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()
            c.paint(g)
            g.dispose()
            return image
        }

        override fun getDragImageOffset(): Point? {
            val c = draggedComponent as? DraggableMacroItem ?: return null
            return c.dragStartEvent?.point
        }

        override fun canImport(support: TransferSupport): Boolean {
            return support.isDataFlavorSupported(macroItemIndexFlavor)
        }

        override fun importData(support: TransferSupport): Boolean {
            if (!canImport(support)) {
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

            if (support.component == triggerSlot) {
                val newTrigger = MacroKeyItem(draggedItem.getText(), draggedItem.getCommand())
                setTriggerItem(newTrigger)
                notifyItemsReordered()
                return true
            }

            if (support.component == macroItemsPanel) {
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
            return false
        }

        override fun exportDone(source: JComponent?, data: Transferable?, action: Int) {
            draggedComponent = null
        }
    }
}