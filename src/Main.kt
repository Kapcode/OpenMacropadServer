import UI.ConnectedDevicesUI
import UI.ConsoleUI
import UI.ServerStatusUI
import UI.TabbedUI
import java.awt.Dimension
import javax.swing.*

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    // Always create Swing UI on the Event Dispatch Thread
    SwingUtilities.invokeLater {
        createAndShowGUI()
    }
}

fun createAndShowGUI() {
    val frame = JFrame("My Application")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(800, 600)
    frame.setLocationRelativeTo(null) // center on screen

    // Create a menu bar
    val menuBar = JMenuBar()

    // Create Server menu
    val serverMenu = JMenu("Server")

    val startItem = JMenuItem("Start")
    startItem.addActionListener {
        // TODO: Start server
        println("Server starting...")
    }

    val stopItem = JMenuItem("Stop")
    stopItem.addActionListener {
        // TODO: Stop server
        println("Server stopping...")
    }

    val settingsItem = JMenuItem("Settings")
    settingsItem.addActionListener {
        val settingsDialog = SettingsUI(frame)
        settingsDialog.isVisible = true
    }

    serverMenu.add(startItem)
    serverMenu.add(stopItem)
    serverMenu.addSeparator()
    serverMenu.add(settingsItem)

    menuBar.add(serverMenu)
    frame.jMenuBar = menuBar

    // Create UI components
    val connectedDevicesUI = ConnectedDevicesUI()
    connectedDevicesUI.minimumSize = Dimension(300, 300)
    connectedDevicesUI.addDevice("Test-Device-001")
    connectedDevicesUI.addDevice("Test-Device-002")
    connectedDevicesUI.addDevice("Test-Device-003")
    connectedDevicesUI.addDevice("Test-Device-004")
    connectedDevicesUI.addDevice("Test-Device-005")


    val consoleUI = ConsoleUI()
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

    frame.isVisible = true
}