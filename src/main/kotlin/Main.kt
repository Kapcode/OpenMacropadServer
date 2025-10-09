import ConnectionListener
import UI.*
import WifiServer
import java.awt.Dimension
import javax.swing.*

fun main() {
    // Always create Swing UI on the Event Dispatch Thread
    SwingUtilities.invokeLater {
        createAndShowGUI()
    }
}

fun createAndShowGUI() {
    val wifiServer = WifiServer()

    val frame = JFrame("Open Macropad Server")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(1280, 800)
    frame.setLocationRelativeTo(null) // center on screen

    // Create a menu bar
    val menuBar = JMenuBar()
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

    // --- Create UI components ---
    val serverStatusUI = ServerStatusUI()
    val consoleUI = ConsoleUI(wifiServer)
    val macroManagerUI = MacroManagerUI()
    val tabbedUI = TabbedUI()

    // Set minimum sizes to prevent them from disappearing
    serverStatusUI.minimumSize = Dimension(0, 50)
    consoleUI.minimumSize = Dimension(200, 100)
    macroManagerUI.minimumSize = Dimension(200, 100)
    tabbedUI.minimumSize = Dimension(400, 100)

    // --- Create the nested layout ---

    // 3. Innermost split: MacroManager (left) and TabbedUI (right)
    val centerSplit = JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        macroManagerUI,
        tabbedUI
    )
    centerSplit.resizeWeight = 0.3 // MacroManager gets 30%, TabbedUI gets 70%

    // 2. Middle split: Console (left) and the centerSplit (right)
    val bottomHorizontalSplit = JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        consoleUI,
        centerSplit
    )
    bottomHorizontalSplit.resizeWeight = 0.2 // Console gets 20%

    // 1. Top-level split: ServerStatus (top) and the rest (bottom)
    val mainSplitPane = JSplitPane(
        JSplitPane.VERTICAL_SPLIT,
        serverStatusUI,
        bottomHorizontalSplit
    )
    mainSplitPane.resizeWeight = 0.1 // ServerStatus gets 10%

    // Add to frame
    frame.add(mainSplitPane)

    // Add listeners
    startItem.addActionListener {
        wifiServer.startListening()
        serverStatusUI.updateStatus(wifiServer.isListening(), 9999)
    }

    stopItem.addActionListener {
        wifiServer.stopListening()
        serverStatusUI.updateStatus(
            wifiServer.isListening(),
            port = 9999
        )
    }

    settingsItem.addActionListener {
        val settingsDialog = SettingsUI(frame)
        settingsDialog.isVisible = true
    }

    wifiServer.setConnectionListener(object : ConnectionListener {
        override fun onClientConnected(clientId: String) {
            val message = "Client connected: $clientId"
            consoleUI.appendMessage(message)
        }

        override fun onClientDisconnected(clientId: String) {
            val message = "Client disconnected: $clientId"
            consoleUI.appendMessage(message)
        }

        override fun onDataReceived(clientId: String, data: ByteArray) {
            val message = "Received from $clientId: ${data.toString(Charsets.UTF_8)}"
            consoleUI.appendMessage(message)
        }

        override fun onError(error: String) {
            val message = "Server error: $error"
            consoleUI.appendMessage(message)
            serverStatusUI.updateStatus(
                false,
                port = 9999
            )
        }
    })
    val macroJsonEditorUI = MacroJsonEditorUI()
    tabbedUI.add("Macro Editor", macroJsonEditorUI)
    frame.isVisible = true

    // Start listening and update status
    wifiServer.startListening()
    serverStatusUI.updateStatus(wifiServer.isListening(), 9999)
}

fun copy() {}
