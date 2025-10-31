package UI

import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JPanel

class ToolBarButton : JPanel { // Removed super() call from here

    private lateinit var button: JButton // Use lateinit

    // Constructor for Icon-based buttons
    constructor(icon: Icon, toolTipText: String, actionListener: ActionListener) : super(BorderLayout()) { // Call super() here
        button = JButton(icon)
        setup(toolTipText, actionListener)
    }

    // Constructor for Text-based buttons
    constructor(text: String, toolTipText: String, actionListener: ActionListener) : super(BorderLayout()) { // Call super() here
        button = JButton(text)
        setup(toolTipText, actionListener)
    }

    private fun setup(toolTipText: String, actionListener: ActionListener) {
        // 1. Configure the container panel
        isOpaque = false // Make the container transparent
        border = BorderFactory.createEmptyBorder(2, 5, 2, 5) // This provides the padding

        // 2. Configure the actual button
        button.toolTipText = toolTipText
        button.addActionListener(actionListener)

        // 3. Style the button itself
        button.isFocusable = false
        button.background = Theme().PrimaryButtonColor
        button.foreground = Theme().PrimaryButtonFont
        button.border = BorderFactory.createLineBorder(Theme().PrimaryButtonBorder)

        // 4. Add the button to the container panel
        add(button, BorderLayout.CENTER)
    }
}