package UI

import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

open class MacroItem : JPanel() {
    protected val keyLabel: DynamicFontSizeLabel
    protected val commandLabel: DynamicFontSizeLabel

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
        keyLabel = DynamicFontSizeLabel(DEFAULT_KEY_FONT_SIZE, MIN_FONT_SIZE).apply {
            font = Font("Arial", Font.BOLD, DEFAULT_KEY_FONT_SIZE.toInt())
            alignmentX = Component.LEFT_ALIGNMENT // Align text to the left
            foreground = theme.SecondaryButtonFont // Use theme font color
        }

        // Command label (e.g., "PRESS", "SNAP_TO (100, 200)")
        commandLabel = DynamicFontSizeLabel(DEFAULT_COMMAND_FONT_SIZE, MIN_FONT_SIZE).apply {
            font = Font("Arial", Font.PLAIN, DEFAULT_COMMAND_FONT_SIZE.toInt())
            alignmentX = Component.LEFT_ALIGNMENT // Align text to the left
            foreground = theme.SecondaryButtonFont // Use theme font color
        }

        add(keyLabel)
        add(commandLabel)

        // Explicitly set preferred and maximum size to enforce a consistent height
        preferredSize = Dimension(200, ITEM_HEIGHT) // Increased preferred width
        maximumSize = Dimension(Integer.MAX_VALUE, ITEM_HEIGHT)

        // Add a component listener to adjust font size when the component is resized
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                keyLabel.repaint() // Trigger a repaint on the labels to re-calculate font size
                commandLabel.repaint()
            }
        })
    }

    fun setText(text: String, command: String) {
        keyLabel.text = text
        commandLabel.text = command
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