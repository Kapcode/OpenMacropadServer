import ConnectionListener
import UI.ConnectedDevicesUI
import UI.ConsoleUI
import UI.ServerStatusUI
import UI.TabbedUI
import WifiServer
import java.awt.Dimension
import java.awt.Robot
import javax.swing.*

fun main() {
    // Always create Swing UI on the Event Dispatch Thread
    SwingUtilities.invokeLater {
        createAndShowGUI()
    }
}

fun createAndShowGUI() {
    val wifiServer = WifiServer()

    val frame = JFrame("My Application")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(800, 600)
    frame.setLocationRelativeTo(null) // center on screen

    // Create a menu bar
    val menuBar = JMenuBar()

    // Create Server menu
    val serverMenu = JMenu("Server")

    val startItem = JMenuItem("Start")
    val stopItem = JMenuItem("Stop")
    val settingsItem = JMenuItem("Settings")

    serverMenu.add(startItem)
    serverMenu.add(stopItem)
    serverMenu.addSeparator()
    serverMenu.add(settingsItem)

    menuBar.add(serverMenu)
    frame.jMenuBar = menuBar

    // Create UI components
    val connectedDevicesUI = ConnectedDevicesUI()
    connectedDevicesUI.minimumSize = Dimension(300, 300)

    val consoleUI = ConsoleUI(wifiServer)
    consoleUI.minimumSize = Dimension(300, 300)

    // Create the left split pane (vertical) with devices on top and console on bottom
    val leftSplitPane = JSplitPane(
        JSplitPane.VERTICAL_SPLIT,
        connectedDevicesUI,
        consoleUI
    )
    leftSplitPane.resizeWeight = 0.5 // Equal space for both panels
    leftSplitPane.dividerLocation = 300

    // Create server status UI and tabbed UI for the right side
    val serverStatusUI = ServerStatusUI()
    serverStatusUI.minimumSize = Dimension(400, 100)

    val tabbedUI = TabbedUI()
    tabbedUI.minimumSize = Dimension(400, 300)

    // Create right split pane (vertical) with server status on top and tabbed UI on bottom
    val rightSplitPane = JSplitPane(
        JSplitPane.VERTICAL_SPLIT,
        serverStatusUI,
        tabbedUI
    )
    rightSplitPane.resizeWeight = 0.3 // 30% for server status, 70% for tabbed UI
    rightSplitPane.dividerLocation = 150

    // Create the main split pane (horizontal) with left panels and right panels
    val mainSplitPane = JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        leftSplitPane,
        rightSplitPane
    )
    mainSplitPane.resizeWeight = 0.3 // Give more space to the right side
    mainSplitPane.dividerLocation = 350

    // Add to frame
    frame.add(mainSplitPane)

    // Add listeners
    startItem.addActionListener {
        wifiServer.startListening()
        serverStatusUI.updateStatus(wifiServer.isListening(), 9999)
    }

    stopItem.addActionListener {
        wifiServer.stopListening()
        serverStatusUI.updateStatus(wifiServer.isListening())
        connectedDevicesUI.clearDevices()
    }

    settingsItem.addActionListener {
        val settingsDialog = SettingsUI(frame)
        settingsDialog.isVisible = true
    }

    wifiServer.setConnectionListener(object : ConnectionListener {
        override fun onClientConnected(clientId: String) {
            val message = "Client connected: $clientId"
            consoleUI.appendMessage(message)
            connectedDevicesUI.addDevice(clientId)
        }

        override fun onClientDisconnected(clientId: String) {
            val message = "Client disconnected: $clientId"
            consoleUI.appendMessage(message)
            connectedDevicesUI.removeDevice(clientId)
        }

        override fun onDataReceived(clientId: String, data: ByteArray) {
            val message = "Received from $clientId: ${data.toString(Charsets.UTF_8)}"
            consoleUI.appendMessage(message)
        }

        override fun onError(error: String) {
            val message = "Server error: $error"
            consoleUI.appendMessage(message)
            serverStatusUI.updateStatus(false)
        }
    })

    frame.isVisible = true

    // Start listening and update status
    wifiServer.startListening()
    serverStatusUI.updateStatus(wifiServer.isListening(), 9999)
}
fun copy(){



}

