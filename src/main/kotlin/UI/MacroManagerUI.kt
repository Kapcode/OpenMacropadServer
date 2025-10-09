package UI

import java.io.File
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class MacroManagerUI : JPanel() {

    private val macroFolder = File("macros")

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        loadMacros()
    }

    private fun loadMacros() {
        if (!macroFolder.exists()) {
            macroFolder.mkdir()
        }

        macroFolder.listFiles { _, name -> name.endsWith(".json") }?.forEach { file ->
            val item = MacroManagerItem(file)
            add(item)
        }
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