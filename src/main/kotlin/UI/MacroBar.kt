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

        toolbar.addButton("Record (START)", "Start/Stop Recording") {}
        toolbar.addButton("Undo", "Undo last action") {}
        toolbar.addButton("Redo", "Redo last action") {}
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