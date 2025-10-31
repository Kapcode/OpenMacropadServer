package UI

import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.beans.PropertyChangeListener
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.TransferHandler
import javax.swing.SwingUtilities

class MacroBar : JPanel() {

    private val toolbar = ToolBarUI()
    val macroItemsPanel = JPanel().apply { layout = BoxLayout(this, BoxLayout.X_AXIS) }
    private val transferHandler = MacroItemTransferHandler()

    init {
        val theme = Theme()
        layout = BorderLayout()
        background = theme.SecondaryBackgroundColor // Use theme background color
        macroItemsPanel.background = theme.SecondaryBackgroundColor
        macroItemsPanel.transferHandler = transferHandler

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

    // Change this to add listener to MacroBar itself
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        super.addPropertyChangeListener(listener)
    }

    /**
     * Adds a MacroItem (like a MacroKeyItem or MacroMouseItem) to the bar.
     */
    fun addMacroItem(item: MacroItem) {
        item.transferHandler = transferHandler
        macroItemsPanel.add(item)
    }

    /**
     * Clears all items from the macro bar, except the toolbar.
     */
    fun clear() {
        macroItemsPanel.removeAll()
        macroItemsPanel.revalidate()
        macroItemsPanel.repaint()
    }

    // Public method to notify listeners of reorder, called by the TransferHandler
    fun notifyItemsReordered() {
        // Fire the property change on MacroBar itself
        firePropertyChange("component.reordered", null, null)
    }

    // Inner class for handling drag and drop of MacroItems
    inner class MacroItemTransferHandler : TransferHandler() {

        // DataFlavor to identify our dragged MacroItem by its index
        private val macroItemIndexFlavor = DataFlavor(Integer::class.java, "Macro Item Index")

        override fun getSourceActions(c: JComponent?): Int {
            return MOVE // We are moving items
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
            // Only allow import if the data flavor is supported and it's a drop onto our macroItemsPanel
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

            // Get the actual MacroItem being dragged from its original position
            val draggedItem = macroItemsPanel.getComponent(sourceIndex) as? MacroItem ?: return false

            val dropPoint = support.dropLocation.dropPoint
            var targetIndex = macroItemsPanel.componentCount // Default to end

            // Calculate the target index for insertion in a BoxLayout (X_AXIS)
            for (i in 0 until macroItemsPanel.componentCount) {
                val component = macroItemsPanel.getComponent(i)
                // If the drop point is before the middle of this component, insert here
                if (dropPoint.x < component.x + component.width / 2) {
                    targetIndex = i
                    break
                }
            }

            // If the item is being dropped at its current position or just after it, do nothing.
            // This prevents unnecessary re-adds and potential issues.
            if (sourceIndex == targetIndex || (sourceIndex != -1 && targetIndex == sourceIndex + 1)) {
                return false
            }

            // Remove the item from its original position
            macroItemsPanel.remove(draggedItem)
            // Adjust targetIndex if removing an item from before the target
            if (sourceIndex < targetIndex) {
                targetIndex--
            }

            // Add the item at the new target index
            macroItemsPanel.add(draggedItem, targetIndex)

            macroItemsPanel.revalidate()
            macroItemsPanel.repaint()

            // Notify the outer class that items have been reordered
            this@MacroBar.notifyItemsReordered()

            return true
        }

        override fun exportDone(source: JComponent?, data: Transferable?, action: Int) {
            // This method is called after the drag is complete. We don't need to do anything here
            // as the importData already handled the reordering.
        }
    }
}