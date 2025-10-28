package UI

import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel

class MacroBar : JPanel() {

    private var featuredButton: JButton = JButton("Record Macro")

    init {
        val theme = Theme()
        layout = FlowLayout(FlowLayout.LEFT)
        background = theme.SecondaryBackgroundColor // Use theme background color
        createFeaturedButton() // Call the function to create and add the button
        repaint()
    }

    /**
     * Adds a MacroItem (like a MacroKeyItem or MacroMouseItem) to the bar.
     */
    fun addMacroItem(item: MacroItem) {
        add(item)
    }

    /**
     * Clears all items from the macro bar, except the featured button.
     */
    fun clear() {
        val children = components
        for (i in children.indices) {
            if (children[i] != featuredButton) {
                remove(children[i])
            }
        }
        revalidate()
        repaint()
    }

    private fun createFeaturedButton() {
        featuredButton = JButton("Record Macro")
        featuredButton.addActionListener { println("boo") }
        featuredButton.foreground = Theme().PrimaryButtonFont // Use the font color from the theme
        featuredButton.background = Theme().PrimaryButtonColor // Use the secondary background color from the theme
        featuredButton.border = BorderFactory.createLineBorder(Theme().PrimaryButtonBorder) // Use the secondary border color from the theme
        add(featuredButton)
    }
}