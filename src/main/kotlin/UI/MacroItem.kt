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

    init {
        val theme : Theme = Theme()
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = theme.BackgroundColor // Set background to black
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10) // Internal padding
        )


        // Main key label (e.g., "Mouse", "C")
        keyLabel = JLabel().apply {
            font = Font("Arial", Font.BOLD, 16)
            alignmentX = Component.CENTER_ALIGNMENT
            foreground = theme.FontColor // Set text color to light blue
        }

        // Command label (e.g., "PRESS", "SNAP_TO (100, 200)")
        commandLabel = JLabel().apply {
            font = Font("Arial", Font.PLAIN, 10)
            alignmentX = Component.CENTER_ALIGNMENT
            foreground = theme.FontColor // Set text color to light blue
        }

        add(keyLabel)
        add(commandLabel)

        // Set a maximum size to prevent items from becoming too tall
        maximumSize = Dimension(Integer.MAX_VALUE, preferredSize.height)
    }

    fun setText(text: String, command: String) {
        keyLabel.text = text
        commandLabel.text = command
        // The layout manager will handle resizing automatically
    }

    fun getText(): String {
        return keyLabel.text
    }

    fun getCommand(): String {
        return commandLabel.text
    }
}