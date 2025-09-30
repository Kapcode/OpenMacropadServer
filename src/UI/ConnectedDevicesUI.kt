package UI//
import java.awt.BorderLayout
import javax.swing.*

class ConnectedDevicesUI : JPanel() {
    private val devicesPanel: JPanel
    private val devicePanels = mutableMapOf<String, ConnectedDeviceLabel>()

    init {
        layout = BorderLayout()

        // Title
        val titleLabel = JLabel("Connected Devices")
        titleLabel.horizontalAlignment = SwingConstants.CENTER

        // Panel to hold device labels
        devicesPanel = JPanel()
        devicesPanel.layout = BoxLayout(devicesPanel, BoxLayout.Y_AXIS)
        val scrollPane = JScrollPane(devicesPanel)

        add(titleLabel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
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