import ConnectionListener
import UI.*
import WifiServer
import com.formdev.flatlaf.FlatDarkLaf
import java.awt.AWTEvent
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.KeyboardFocusManager
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.MouseEvent
import javax.swing.*
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea

fun main() {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(FlatDarkLaf())
        } catch (ex: Exception) {
            System.err.println("Failed to initialize LaF")
        }

        createAndShowGUI()
    }
}

fun createAndShowGUI() {
    val wifiServer = WifiServer()
    val frame = JFrame("Open Macropad Server")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.extendedState = JFrame.MAXIMIZED_BOTH // Maximize the window
    frame.setLocationRelativeTo(null)

    val menuBar = JMenuBar()
    val serverMenu = JMenu("Server")
    val startItem = JMenuItem("Start")
    startItem.toolTipText = "Start the server"
    val stopItem = JMenuItem("Stop")
    stopItem.toolTipText = "Stop the server"
    val settingsItem = JMenuItem("Settings")
    settingsItem.toolTipText = "Open server settings"
    serverMenu.add(startItem)
    serverMenu.add(stopItem)
    serverMenu.addSeparator()
    serverMenu.add(settingsItem)
    menuBar.add(serverMenu)

    val macroMenu = JMenu("Macro Manager")
    val macroSettingsItem = JMenuItem("Settings")
    macroSettingsItem.toolTipText = "Open macro manager settings"
    macroMenu.add(macroSettingsItem)
    menuBar.add(macroMenu)

    frame.jMenuBar = menuBar

    val serverStatusUI = ServerStatusUI()
    val consoleUI = ConsoleUI(wifiServer)
    val connectedDevicesUI = ConnectedDevicesUI()
    val tabbedUI = TabbedUI()
    val macroManagerUI = MacroManagerUI(tabbedUI)

    val macroManagerToolbar = ToolBarUI()
    val addIcon = SvgIconRenderer.getIcon("/add-file-icon.svg", 24, 24)
    if (addIcon != null) {
        macroManagerToolbar.addButton(addIcon, "Add Macro") { println("add") }
    }

    val removeIcon = SvgIconRenderer.getIcon("/remove-file-icon.svg", 24, 24)
    val deleteIcon = SvgIconRenderer.getIcon("/trash-bin-icon.svg", 24, 24)
    val removeButton = if (removeIcon != null) {
        ToolBarButton(removeIcon, "Remove Macro") {}
    } else {
        ToolBarButton("Remove", "Remove Macro") {}
    }
    macroManagerToolbar.add(removeButton)

    removeButton.addActionListener { 
        if (macroManagerUI.isSelectionMode) {
            macroManagerUI.deleteSelectedMacros()
            if (removeIcon != null) removeButton.setIcon(removeIcon)
            removeButton.setToolTipText("Remove Macro")
        } else {
            macroManagerUI.setSelectionMode(true)
            if (deleteIcon != null) removeButton.setIcon(deleteIcon)
            removeButton.setToolTipText("Delete Selected")
        }
    }

    serverStatusUI.minimumSize = Dimension(0, 50)
    consoleUI.minimumSize = Dimension(200, 100)
    connectedDevicesUI.minimumSize = Dimension(200, 100)
    macroManagerUI.minimumSize = Dimension(200, 100)
    tabbedUI.minimumSize = Dimension(400, 100)

    val consoleAndDevicesSplit = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, consoleUI, connectedDevicesUI)
    consoleAndDevicesSplit.resizeWeight = 0.5

    val tabbedUIToolbar = ToolBarUI()
    val newEventIcon = SvgIconRenderer.getIcon("/add-file-icon.svg", 24, 24)
    if (newEventIcon != null) {
        tabbedUIToolbar.addButton(newEventIcon, "New Event") { 
            val focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner
            val wasEditorInFocus = focusOwner is RSyntaxTextArea

            val selectedComponent = tabbedUI.selectedComponent
            if (selectedComponent is MacroJsonEditorUI) {
                val dialog = NewEventDialog(frame)
                dialog.isVisible = true
                dialog.createdEvent?.let { event ->
                    selectedComponent.insertNewEvent(event, wasEditorInFocus)
                }
            }
        }
    }
    val saveIcon = SvgIconRenderer.getIcon("/save-file-icon.svg", 24, 24)
    if (saveIcon != null) {
        tabbedUIToolbar.addButton(saveIcon, "Save") {
            val selectedComponent = tabbedUI.selectedComponent
            if (selectedComponent is MacroJsonEditorUI) {
                selectedComponent.save(tabbedUI.getTitleForComponent(selectedComponent))
            }
        }
    }

    val saveAsIcon = SvgIconRenderer.getIcon("/save-as-icon.svg", 24, 24)
    if (saveAsIcon != null) {
        tabbedUIToolbar.addButton(saveAsIcon, "Save As") {
            val selectedComponent = tabbedUI.selectedComponent
            if (selectedComponent is MacroJsonEditorUI) {
                selectedComponent.saveAs(tabbedUI.getTitleForComponent(selectedComponent))
            }
        }
    }

    val undoIcon = SvgIconRenderer.getIcon("/undo-circle-outline-icon.svg", 24, 24)
    if (undoIcon != null) {
        tabbedUIToolbar.addButton(undoIcon, "Undo") {}
    }

    val redoIcon = SvgIconRenderer.getIcon("/redo-circle-outline-icon.svg", 24, 24)
    if (redoIcon != null) {
        tabbedUIToolbar.addButton(redoIcon, "Redo") {}
    }

    val macroManagerPanel = JPanel(BorderLayout())
    macroManagerPanel.add(macroManagerToolbar, BorderLayout.NORTH)
    macroManagerPanel.add(macroManagerUI, BorderLayout.CENTER)

    val tabbedUIPanel = JPanel(BorderLayout())
    tabbedUIPanel.add(tabbedUIToolbar, BorderLayout.NORTH)
    tabbedUIPanel.add(tabbedUI, BorderLayout.CENTER)

    val centerSplit = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, macroManagerPanel, tabbedUIPanel)
    centerSplit.resizeWeight = 0.3

    val bottomHorizontalSplit = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, consoleAndDevicesSplit, centerSplit)
    bottomHorizontalSplit.resizeWeight = 0.3

    val mainSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, serverStatusUI, bottomHorizontalSplit)
    mainSplitPane.resizeWeight = 0.1

    frame.add(mainSplitPane)

    Toolkit.getDefaultToolkit().addAWTEventListener(AWTEventListener { event ->
        if (event is MouseEvent && event.id == MouseEvent.MOUSE_PRESSED) {
            if (macroManagerUI.isSelectionMode) {
                val source = event.source as? JComponent ?: return@AWTEventListener
                
                val isClickOnRemoveButton = SwingUtilities.isDescendingFrom(source, removeButton)
                val isClickInMacroManager = SwingUtilities.isDescendingFrom(source, macroManagerUI)

                if (!isClickOnRemoveButton && !isClickInMacroManager) {
                    macroManagerUI.cancelSelectionMode()
                    if (removeIcon != null) removeButton.setIcon(removeIcon)
                    removeButton.setToolTipText("Remove Macro")
                }
            }
        }
    }, AWTEvent.MOUSE_EVENT_MASK)

    startItem.addActionListener { wifiServer.startListening(); serverStatusUI.updateStatus(wifiServer.isListening(), 9999) }
    stopItem.addActionListener { wifiServer.stopListening(); serverStatusUI.updateStatus(wifiServer.isListening(), 9999) }
    settingsItem.addActionListener { SettingsUI(frame).isVisible = true }
    macroSettingsItem.addActionListener { MacroSettingsDialog(frame).isVisible = true }

    wifiServer.setConnectionListener(object : ConnectionListener {
        override fun onClientConnected(clientId: String) {
            consoleUI.appendMessage("Client connected: $clientId")
            connectedDevicesUI.addDevice(clientId)
        }

        override fun onClientDisconnected(clientId: String) {
            consoleUI.appendMessage("Client disconnected: $clientId")
            connectedDevicesUI.removeDevice(clientId)
        }

        override fun onDataReceived(clientId: String, data: ByteArray) {
            consoleUI.appendMessage("Received from $clientId: ${data.toString(Charsets.UTF_8)}")
        }

        override fun onError(error: String) {
            consoleUI.appendMessage("Server error: $error")
            serverStatusUI.updateStatus(false, 9999)
        }
    })

    tabbedUI.add("Macro Editor", MacroJsonEditorUI(frame))
    frame.isVisible = true

    wifiServer.startListening()
    serverStatusUI.updateStatus(wifiServer.isListening(), 9999)
}
