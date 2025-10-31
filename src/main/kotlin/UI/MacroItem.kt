package UI

import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

open class MacroItem : JPanel() {
    private val keyLabel: JLabel
    private val commandLabel: JLabel

    companion object {
        const val ITEM_HEIGHT = 40 // Define a standard height for all macro items
    }

    init {
        val theme = Theme() // Instantiate the theme

        // Use BoxLayout.Y_AXIS for vertical arrangement of labels
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = theme.SecondaryButtonColor // Use theme background color
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.SecondaryButtonBorder, 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10) // Internal padding
        )

        // Main key label (e.g., "Mouse", "C")
        keyLabel = JLabel().apply {
            font = Font("Arial", Font.BOLD, 16)
            alignmentX = Component.LEFT_ALIGNMENT // Align text to the left
            foreground = theme.SecondaryButtonFont // Use theme font color
        }

        // Command label (e.g., "PRESS", "SNAP_TO (100, 200)")
        commandLabel = JLabel().apply {
            font = Font("Arial", Font.PLAIN, 10)
            alignmentX = Component.LEFT_ALIGNMENT // Align text to the left
            foreground = theme.SecondaryButtonFont // Use theme font color
        }

        add(keyLabel)
        add(commandLabel)

        // Explicitly set preferred and maximum size to enforce a consistent height
        preferredSize = Dimension(150, ITEM_HEIGHT) // Give it a default width and fixed height
        maximumSize = Dimension(Integer.MAX_VALUE, ITEM_HEIGHT)
    }

    fun setText(text: String, command: String) {
        keyLabel.text = text
        commandLabel.text = command
        // Revalidate and repaint to ensure layout updates after text change
        revalidate()
        repaint()
    }

    fun getText(): String {
        return keyLabel.text
    }

    fun getCommand(): String {
        return commandLabel.text
    }
}