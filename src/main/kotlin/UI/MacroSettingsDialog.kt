package UI

import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

class MacroSettingsDialog(parent: JFrame) : JDialog(parent, "Macro Settings", true) {

    private val macroDirectoryField: JTextField

    init {
        layout = BorderLayout()
        val defaultPath = System.getProperty("user.home") + "\\Documents\\OpenMacropadServer\\Macros"

        // Panel for the directory selection
        val directoryPanel = JPanel(BorderLayout())
        directoryPanel.border = BorderFactory.createTitledBorder("Macro Directory")

        macroDirectoryField = JTextField(defaultPath, 50)
        val browseButton = JButton("Browse")

        directoryPanel.add(macroDirectoryField, BorderLayout.CENTER)
        directoryPanel.add(browseButton, BorderLayout.EAST)

        add(directoryPanel, BorderLayout.CENTER)

        // Panel for the Save/Cancel buttons
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val saveButton = JButton("Save")
        val cancelButton = JButton("Cancel")
        buttonPanel.add(saveButton)
        buttonPanel.add(cancelButton)

        add(buttonPanel, BorderLayout.SOUTH)

        // Action Listeners
        browseButton.addActionListener {
            val chooser = JFileChooser()
            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            chooser.dialogTitle = "Select Macro Directory"
            chooser.selectedFile = java.io.File(macroDirectoryField.text)

            val result = chooser.showOpenDialog(this)
            if (result == JFileChooser.APPROVE_OPTION) {
                macroDirectoryField.text = chooser.selectedFile.absolutePath
            }
        }

        saveButton.addActionListener {
            // TODO: Save the setting
            dispose()
        }

        cancelButton.addActionListener {
            dispose()
        }

        pack()
        setLocationRelativeTo(parent)
    }
}