package UI

import java.awt.BorderLayout
import java.awt.Font
import javax.swing.*

class ConnectedDevicesUI : JPanel() {
    private val devicesPanel: JPanel
    private val devicePanels = mutableMapOf<String, ConnectedDeviceLabel>()

    init {
        val theme = Theme()
        layout = BorderLayout()
        background = theme.SecondaryBackgroundColor

        // Header Panel
        val headerPanel = JPanel(BorderLayout())
        headerPanel.background = theme.SecondaryBackgroundColor
        val titleLabel = JLabel("Connected Devices")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 14f)
        titleLabel.foreground = theme.SecondaryFontColor
        headerPanel.add(titleLabel, BorderLayout.WEST)

        // Panel to hold device labels
        devicesPanel = JPanel()
        devicesPanel.layout = BoxLayout(devicesPanel, BoxLayout.Y_AXIS)
        devicesPanel.background = theme.SecondaryBackgroundColor
        val scrollPane = JScrollPane(devicesPanel)
        scrollPane.border = BorderFactory.createLineBorder(theme.SecondaryBorderColor)

        // Main container panel
        val containerPanel = JPanel(BorderLayout())
        containerPanel.background = theme.SecondaryBackgroundColor
        containerPanel.add(headerPanel, BorderLayout.NORTH)
        containerPanel.add(scrollPane, BorderLayout.CENTER)

        add(containerPanel, BorderLayout.CENTER)
    }

    fun addDevice(clientId: String) {
        SwingUtilities.invokeLater {
            if (!devicePanels.containsKey(clientId)) {
                val devicePanel = ConnectedDeviceLabel(clientId)
                devicePanels[clientId] = devicePanel
                devicesPanel.add(devicePanel)
                devicesPanel.revalidate()
                devicesPanel.repaint()
            }
        }
    }

    fun removeDevice(clientId: String) {
        SwingUtilities.invokeLater {
            devicePanels[clientId]?.let { panel ->
                devicesPanel.remove(panel)
                devicePanels.remove(clientId)
                devicesPanel.revalidate()
                devicesPanel.repaint()
            }
        }
    }

    fun clearDevices() {
        SwingUtilities.invokeLater {
            devicesPanel.removeAll()
            devicePanels.clear()
            devicesPanel.revalidate()
            devicesPanel.repaint()
        }
    }
}