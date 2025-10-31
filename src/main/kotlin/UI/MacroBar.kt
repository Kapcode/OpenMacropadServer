package UI

import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JPanel

class MacroBar : JPanel() {

    private val toolbar = ToolBarUI()
    private val macroItemsPanel = JPanel(FlowLayout(FlowLayout.LEFT))

    init {
        val theme = Theme()
        layout = BorderLayout()
        background = theme.SecondaryBackgroundColor // Use theme background color
        macroItemsPanel.background = theme.SecondaryBackgroundColor

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

    /**
     * Adds a MacroItem (like a MacroKeyItem or MacroMouseItem) to the bar.
     */
    fun addMacroItem(item: MacroItem) {
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
}