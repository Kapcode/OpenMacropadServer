package UI

import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel

class ToolBarButton(
    text: String,
    toolTipText: String,
    actionListener: ActionListener
) : JPanel(BorderLayout()) { // Inherit from JPanel to act as a container

    private val button: JButton

    init {
        // 1. Configure the container panel
        isOpaque = false // Make the container transparent
        border = BorderFactory.createEmptyBorder(2, 5, 2, 5) // This provides the padding

        // 2. Create the actual button that will be nested inside
        button = JButton(text)
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