package UI

import java.awt.BorderLayout
import java.awt.Font
import java.io.File
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class MacroManagerUI : JPanel() {

    private val macroFolder = File("macros")
    private val macrosPanel: JPanel

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

        loadMacros()
    }

    private fun loadMacros() {
        macrosPanel.removeAll()

        if (!macroFolder.exists()) {
            macroFolder.mkdir()
        }

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
        init {
            val nameLabel = JLabel(macroFile.nameWithoutExtension)
            val playButton = JButton("Play")
            val editButton = JButton("Edit")
            val deleteButton = JButton("Delete")

            add(nameLabel)
            add(editButton)
            add(playButton)
            add(deleteButton)
        }
    }
}