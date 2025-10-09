import UI.Theme
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import javax.swing.*
import javax.swing.border.EmptyBorder

class ServerStatusUI : JPanel() {
    private val statusLabel: JLabel
    private val portLabel: JLabel
    private val ipLabel: JLabel?

    init {
        val theme : Theme = Theme()
        setLayout(BorderLayout())
        setBorder(EmptyBorder(10, 10, 10, 10))

        // Title panel
        val titlePanel = JPanel()
        titlePanel.layout = BorderLayout()
        titlePanel.setBackground(theme.BackgroundColor) // Light Blue

        val titleLabel = JLabel("Server Status")
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER)
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f))
        titleLabel.setForeground(theme.FontColor) // Black
        titlePanel.add(titleLabel, BorderLayout.CENTER)

        add(JScrollPane(titlePanel), BorderLayout.NORTH)

        // Status panel
        val statusPanel = JPanel()
        statusPanel.setLayout(BoxLayout(statusPanel, BoxLayout.Y_AXIS))

        statusLabel = JLabel("Status: Stopped")
        portLabel = JLabel("Port: N/A")
        ipLabel = JLabel("IP: " + Utility().getSystemIP())
        statusPanel.add(statusLabel)
        statusPanel.add(Box.createVerticalStrut(5))
        statusPanel.add(portLabel)
        statusPanel.add(Box.createVerticalStrut(5))
        statusPanel.add(ipLabel)

        add(JScrollPane(statusPanel), BorderLayout.CENTER)
    }

    fun updateStatus(isRunning: Boolean, port: Int) {
        SwingUtilities.invokeLater(Runnable {
            statusLabel.setText("Status: " + (if (isRunning) "Running" else "Stopped"))
            portLabel.setText(if (isRunning) "Port: " + port else "Port: N/A")
        })
    }
}