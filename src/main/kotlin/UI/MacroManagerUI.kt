package UI

import java.awt.BorderLayout
import java.awt.Font
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import java.nio.file.WatchService
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MacroManagerUI(private val tabbedUI: TabbedUI) : JPanel() {

    private val defaultMacroPath = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "OpenMacropadServer" + File.separator + "Macros"
    private var macroFolder = File(defaultMacroPath) // Initialize with default path
    private val macrosPanel: JPanel

    private var watcher: WatchService? = null
    private var watchThread: Thread? = null
    var isSelectionMode = false
        private set

    init {
        val theme = Theme()
        layout = BorderLayout()
        background = theme.SecondaryBackgroundColor

        // Header
        val headerPanel = JPanel(BorderLayout())
        headerPanel.background = theme.SecondaryBackgroundColor
        val titleLabel = JLabel("Macro Manager")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 14f)
        titleLabel.foreground = theme.SecondaryFontColor
        headerPanel.add(titleLabel, BorderLayout.WEST)
        add(headerPanel, BorderLayout.NORTH)

        // Panel to hold the list of macros
        macrosPanel = JPanel()
        macrosPanel.layout = BoxLayout(macrosPanel, BoxLayout.Y_AXIS)
        macrosPanel.background = theme.SecondaryBackgroundColor
        add(macrosPanel, BorderLayout.CENTER)

        // Ensure the macro directory exists and load macros
        ensureMacroDirectoryExists()
        loadMacros()

        // Start watching the directory
        startWatchingMacroDirectory()
    }

    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        // Reload the macros to show/hide checkboxes
        loadMacros()
    }

    fun deleteSelectedMacros() {
        val selectedItems = macrosPanel.components.filterIsInstance<MacroManagerItem>().filter { it.isSelected() }

        if (selectedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No macros selected for deletion.", "Delete Macros", JOptionPane.INFORMATION_MESSAGE)
            return
        }

        val fileNames = selectedItems.joinToString("\n") { "- ${it.getMacroFile().name}" }
        val confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the following files?\n$fileNames",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION
        )

        if (confirm == JOptionPane.YES_OPTION) {
            selectedItems.forEach { item ->
                val macroFile = item.getMacroFile()
                // Close the tab if it's open
                for (i in 0 until tabbedUI.tabCount) {
                    val component = tabbedUI.getComponentAt(i)
                    if (component is MacroJsonEditorUI) {
                        if (component.getCurrentFile()?.absolutePath == macroFile.absolutePath) {
                            tabbedUI.remove(i)
                            break
                        }
                    }
                }
                // Delete the file
                macroFile.delete()
            }
            // Exit selection mode after deletion
            setSelectionMode(false)
        }
    }

    private fun ensureMacroDirectoryExists() {
        if (!macroFolder.exists()) {
            macroFolder.mkdirs() // Use mkdirs to create parent directories if they don't exist
        }
    }

    private fun startWatchingMacroDirectory() {
        try {
            watcher = FileSystems.getDefault().newWatchService()
            val path = macroFolder.toPath()
            path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY)

            watchThread = Thread { 
                while (!Thread.currentThread().isInterrupted) {
                    val key: WatchKey
                    try {
                        key = watcher!!.take()
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        return@Thread
                    }

                    for (event in key.pollEvents()) {
                        val kind = event.kind()

                        // Ignore OVERFLOW event
                        if (kind === StandardWatchEventKinds.OVERFLOW) {
                            continue
                        }

                        // A change occurred, reload macros on the EDT
                        SwingUtilities.invokeLater { loadMacros() }
                    }

                    val valid = key.reset()
                    if (!valid) {
                        println("WatchKey no longer valid, exiting watcher.")
                        break
                    }
                }
            }
            watchThread!!.isDaemon = true // Allow application to exit even if this thread is running
            watchThread!!.start()
        } catch (e: Exception) {
            System.err.println("Error setting up directory watcher: ${e.message}")
            e.printStackTrace()
        }
    }

    // Stop the watcher when the component is removed (e.g., application closes)
    override fun removeNotify() {
        super.removeNotify()
        watchThread?.interrupt()
        watcher?.close()
    }

    private fun loadMacros() {
        macrosPanel.removeAll()

        val macroFiles = macroFolder.listFiles { _, name -> name.endsWith(".json") }

        if (macroFiles.isNullOrEmpty()) {
            val emptyLabel = JLabel("No macros found.")
            emptyLabel.foreground = Theme().SecondaryFontColor
            macrosPanel.add(emptyLabel)
        } else {
            macroFiles.forEach { file ->
                val item = MacroManagerItem(file)
                macrosPanel.add(item)
            }
        }
        macrosPanel.revalidate()
        macrosPanel.repaint()
    }

    inner class MacroManagerItem(private val macroFile: File) : JPanel() {
        private val checkBox = JCheckBox()

        init {
            val theme = Theme()
            background = theme.SecondaryBackgroundColor

            checkBox.isVisible = isSelectionMode
            add(checkBox)

            val nameLabel = JLabel(macroFile.nameWithoutExtension)
            nameLabel.foreground = theme.SecondaryFontColor

            val playButton = JButton("Play")
            playButton.background = theme.SecondaryButtonColor
            playButton.foreground = theme.SecondaryButtonFont
            playButton.border = BorderFactory.createLineBorder(theme.SecondaryButtonBorder)

            val editButton = JButton("Edit")
            editButton.background = theme.SecondaryButtonColor
            editButton.foreground = theme.SecondaryButtonFont
            editButton.border = BorderFactory.createLineBorder(theme.SecondaryButtonBorder)
            editButton.addActionListener { 
                val newEditor = MacroJsonEditorUI()
                newEditor.setText(macroFile.readText(), macroFile)
                tabbedUI.add(macroFile.name, newEditor)
                tabbedUI.setSelectedComponent(newEditor)
            }

            val deleteButton = JButton("Delete")
            deleteButton.background = theme.ThirdButtonColor
            deleteButton.foreground = theme.ThirdButtonFont
            deleteButton.border = BorderFactory.createLineBorder(theme.ThirdButtonBorder)
            deleteButton.addActionListener { 
                val confirm = JOptionPane.showConfirmDialog(
                    this, 
                    "Are you sure you want to delete '${macroFile.name}'?", 
                    "Confirm Deletion", 
                    JOptionPane.YES_NO_OPTION
                )

                if (confirm == JOptionPane.YES_OPTION) {
                    // Close the tab if it's open
                    for (i in 0 until tabbedUI.tabCount) {
                        val component = tabbedUI.getComponentAt(i)
                        if (component is MacroJsonEditorUI) {
                            if (component.getCurrentFile()?.absolutePath == macroFile.absolutePath) {
                                tabbedUI.remove(i)
                                break
                            }
                        }
                    }
                    // Delete the file
                    macroFile.delete()
                }
            }

            add(nameLabel)
            add(editButton)
            add(playButton)
            add(deleteButton)
        }

        fun isSelected(): Boolean = checkBox.isSelected
        fun getMacroFile(): File = macroFile
    }
}