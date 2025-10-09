package UI

import java.awt.FlowLayout
import javax.swing.JPanel

class MacroBar : JPanel() {

    init {
        layout = FlowLayout(FlowLayout.LEFT)
    }

    /**
     * Adds a MacroItem (like a MacroKeyItem or MacroMouseItem) to the bar.
     */
    fun addMacroItem(item: MacroItem) {
        add(item)
    }

    /**
     * Clears all items from the macro bar.
     */
    fun clear() {
        removeAll()
        revalidate()
        repaint()
    }
}