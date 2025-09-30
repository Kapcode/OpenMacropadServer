package UI

import Utility
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.*

class ServerStatusUI : JPanel() {
    private val statusLabel: JLabel
    private val portLabel: JLabel
    private val ipLabel: JLabel


    init {
        layout = BorderLayout()
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Title
        val titleLabel = JLabel("Server Status")

        titleLabel.horizontalAlignment = SwingConstants.CENTER
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 16f)

        // Status panel
        val statusPanel = JPanel()
        statusPanel.layout = BoxLayout(statusPanel, BoxLayout.Y_AXIS)

        statusLabel = JLabel("Status: Stopped")
        portLabel = JLabel("Port: N/A")
        ipLabel = JLabel("IP: "+Utility().getSystemIP())
        statusPanel.add(statusLabel)
        statusPanel.add(Box.createVerticalStrut(5))
        statusPanel.add(portLabel)
        statusPanel.add(Box.createVerticalStrut(5))
        statusPanel.add(ipLabel)

        add(titleLabel, BorderLayout.NORTH)
        add(statusPanel, BorderLayout.CENTER)
    }

    fun updateStatus(isRunning: Boolean, port: Int = 0) {
        SwingUtilities.invokeLater {
            statusLabel.text = "Status: ${if (isRunning) "Running" else "Stopped"}"
            portLabel.text = if (isRunning) "Port: $port" else "Port: N/A"
        }
    }
}