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
    protected val keyLabel: JLabel
    protected val commandLabel: JLabel

    companion object {
        const val ITEM_HEIGHT = 40 // Define a standard height for all macro items
        const val DEFAULT_KEY_FONT_SIZE = 16f
        const val DEFAULT_COMMAND_FONT_SIZE = 10f
        const val MIN_FONT_SIZE = 8f
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
            font = Font("Arial", Font.BOLD, DEFAULT_KEY_FONT_SIZE.toInt())
            alignmentX = Component.LEFT_ALIGNMENT // Align text to the left
            foreground = theme.SecondaryButtonFont // Use theme font color
        }

        // Command label (e.g., "PRESS", "SNAP_TO (100, 200)")
        commandLabel = JLabel().apply {
            font = Font("Arial", Font.PLAIN, DEFAULT_COMMAND_FONT_SIZE.toInt())
            alignmentX = Component.LEFT_ALIGNMENT // Align text to the left
            foreground = theme.SecondaryButtonFont // Use theme font color
        }

        add(keyLabel)
        add(commandLabel)

        // Explicitly set preferred and maximum size to enforce a consistent height
        preferredSize = Dimension(150, ITEM_HEIGHT)
        maximumSize = Dimension(Integer.MAX_VALUE, ITEM_HEIGHT)
    }

    fun setText(text: String, command: String) {
        keyLabel.text = text
        commandLabel.text = command
        adjustLabelFontSize(keyLabel, DEFAULT_KEY_FONT_SIZE)
        adjustLabelFontSize(commandLabel, DEFAULT_COMMAND_FONT_SIZE)
        revalidate()
        repaint()
    }

    private fun adjustLabelFontSize(label: JLabel, defaultSize: Float) {
        val originalFont = label.font.deriveFont(defaultSize)
        label.font = originalFont
        var currentFontSize = defaultSize

        var fontMetrics = label.getFontMetrics(originalFont)
        var textWidth = fontMetrics.stringWidth(label.text)

        // Check if the component has a valid width yet
        val componentWidth = if (width > 0) width else preferredSize.width

        // Reduce font size until it fits or hits the minimum
        while (textWidth > (componentWidth - 20) && currentFontSize > MIN_FONT_SIZE) { // 20 for padding
            currentFontSize -= 1f
            val newFont = originalFont.deriveFont(currentFontSize)
            label.font = newFont
            fontMetrics = label.getFontMetrics(newFont)
            textWidth = fontMetrics.stringWidth(label.text)
        }
    }

    fun getText(): String {
        return keyLabel.text
    }

    fun getCommand(): String {
        return commandLabel.text
    }
}