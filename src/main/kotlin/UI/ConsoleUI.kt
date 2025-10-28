package UI

import ConnectionUIBridge
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.*

class ConsoleUI(private val connectionUIBridge: ConnectionUIBridge) : JPanel() {
    private val textArea: JTextArea
    private val inputField: JTextField
    private val sendButton: JButton

    init {
        val theme = Theme()
        layout = BorderLayout()
        background = theme.SecondaryBackgroundColor

        // Header Panel
        val headerPanel = JPanel(BorderLayout())
        headerPanel.background = theme.SecondaryBackgroundColor
        val consoleTitleLabel = JLabel("Console")
        consoleTitleLabel.font = consoleTitleLabel.font.deriveFont(Font.BOLD, 14f)
        consoleTitleLabel.foreground = theme.SecondaryFontColor
        headerPanel.add(consoleTitleLabel, BorderLayout.WEST)
        add(headerPanel, BorderLayout.NORTH)

        // Text area for displaying messages
        textArea = JTextArea()
        textArea.isEditable = false
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        textArea.background = theme.SecondaryBackgroundColor
        textArea.foreground = theme.SecondaryFontColor
        val scrollPane = JScrollPane(textArea)
        scrollPane.border = BorderFactory.createLineBorder(theme.SecondaryBorderColor)

        // Input field
        inputField = JTextField()
        inputField.addActionListener { sendMessage() } // Allow sending with Enter key
        inputField.background = theme.BackgroundColor
        inputField.foreground = theme.FontColor
        inputField.caretColor = theme.FontColor
        inputField.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.BorderColor),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )

        // Send button
        sendButton = JButton("Send")
        sendButton.addActionListener {
            sendMessage()
        }
        sendButton.background = theme.PrimaryButtonColor
        sendButton.foreground = theme.PrimaryButtonFont
        sendButton.isFocusPainted = false
        sendButton.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.PrimaryButtonBorder, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        )

        // Panel for input field and button
        val inputPanel = JPanel(BorderLayout())
        inputPanel.background = theme.SecondaryBackgroundColor
        inputPanel.add(inputField, BorderLayout.CENTER)
        inputPanel.add(sendButton, BorderLayout.EAST)

        // Add components to main panel
        add(scrollPane, BorderLayout.CENTER)
        add(inputPanel, BorderLayout.SOUTH)
    }

    private fun sendMessage() {
        val message = inputField.text
        if (message.isNotBlank()) {
            connectionUIBridge.sendData(message.toByteArray(Charsets.UTF_8))
            appendMessage("Broadcast: $message")
            inputField.text = ""
        }
    }

    fun appendMessage(message: String) {
        SwingUtilities.invokeLater {
            textArea.append(message + "\n")
            textArea.caretPosition = textArea.document.length // Auto-scroll to bottom
        }
    }
}
