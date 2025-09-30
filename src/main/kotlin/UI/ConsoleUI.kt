package UI

import java.awt.BorderLayout
import javax.swing.*

class ConsoleUI : JPanel() {
    private val textArea: JTextArea
    private val inputField: JTextField
    private val sendButton: JButton

    init {
        layout = BorderLayout()

        // Text area for displaying messages
        textArea = JTextArea()
        textArea.isEditable = false
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        val scrollPane = JScrollPane(textArea)

        // Input field
        inputField = JTextField()

        // Send button
        sendButton = JButton("Send")
        sendButton.addActionListener {
            sendMessage()
        }

        // Panel for input field and button
        val inputPanel = JPanel(BorderLayout())
        inputPanel.add(inputField, BorderLayout.CENTER)
        inputPanel.add(sendButton, BorderLayout.EAST)

        // Add components to main panel
        add(scrollPane, BorderLayout.CENTER)
        add(inputPanel, BorderLayout.SOUTH)
    }

    private fun sendMessage() {
        val message = inputField.text
        if (message.isNotBlank()) {
            // TODO: Send message via .ConnectionUIBridge
            appendMessage("Sent: $message")
            inputField.text = ""
        }
    }

    fun appendMessage(message: String) {
        textArea.append(message + "\n")
        textArea.caretPosition = textArea.document.length // Auto-scroll to bottom
    }
}
