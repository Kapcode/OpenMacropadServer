package UI

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import org.json.JSONException
import org.json.JSONObject
import java.awt.BorderLayout
import java.awt.Point
import java.io.File
import java.io.IOException
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class MacroJsonEditorUI : JPanel() {

    private val textArea: RSyntaxTextArea
    private val macroBar: MacroBar
    private var currentFile: File? = null // To keep track of the file being edited

    init {
        layout = BorderLayout()

        // 1. Create and configure the text area for JSON
        textArea = RSyntaxTextArea(20, 60)
        textArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JSON
        textArea.isCodeFoldingEnabled = true

        // Apply the dark theme
        try {
            val theme = org.fife.ui.rsyntaxtextarea.Theme.load(
                javaClass.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml")
            )
            theme.apply(textArea)
        } catch (e: IOException) {
            e.printStackTrace() // Theme file not found
        }

        val sp = RTextScrollPane(textArea)

        // 2. Create the MacroBar
        macroBar = MacroBar()

        // 3. Create a split pane to hold the editor and the macro bar
        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, sp, macroBar)
        splitPane.resizeWeight = 0.7 // Editor gets 70% of the space

        add(splitPane, BorderLayout.CENTER)

        // 4. Add a listener to update the macro bar when the text changes
        textArea.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                updateMacroBarFromText()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                updateMacroBarFromText()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                updateMacroBarFromText()
            }
        })

        // Load a default macro and trigger the initial update
        setText(createDefaultMacroJson(), null)
    }

    private fun updateMacroBarFromText() {
        macroBar.clear()

        try {
            val json = JSONObject(getText())
            val events = json.getJSONArray("events")

            for (i in 0 until events.length()) {
                val eventObject = events.getJSONObject(i)
                when (eventObject.getString("type")) {
                    "key" -> {
                        val keyText = eventObject.getString("key")
                        val command = eventObject.getString("command")
                        macroBar.addMacroItem(MacroKeyItem(keyText, command))
                    }
                    "mouse" -> {
                        val command = eventObject.getString("command")
                        val commandType = MacroMouseItem.MouseCommandType.valueOf(command)
                        val x = eventObject.optInt("x", 0)
                        val y = eventObject.optInt("y", 0)
                        macroBar.addMacroItem(MacroMouseItem(commandType, Point(x, y)))
                    }
                }
            }
        } catch (e: JSONException) {
            // JSON is likely malformed while the user is typing. This is expected.
        }

        macroBar.revalidate()
        macroBar.repaint()
    }

    private fun createDefaultMacroJson(): String {
        return """{
    "events": []
}"""
    }

    fun setText(text: String, file: File?) {
        SwingUtilities.invokeLater {
            textArea.text = text
            textArea.caretPosition = 0
            this.currentFile = file
        }
    }

    fun getText(): String {
        return textArea.text
    }

    fun getCurrentFile(): File? {
        return currentFile
    }

    fun save(suggestedName: String? = null) {
        val suggestedNameWithoutExt = suggestedName?.removeSuffix(".json")
        val fileHasChanged = currentFile != null && suggestedNameWithoutExt != null && currentFile!!.nameWithoutExtension != suggestedNameWithoutExt

        if (currentFile == null || fileHasChanged) {
            saveAs(suggestedName)
        } else {
            saveToFile(currentFile!!)
        }
    }

    fun saveAs(suggestedName: String? = null) {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Save Macro As"
        fileChooser.fileFilter = javax.swing.filechooser.FileNameExtensionFilter("JSON Macro Files", "json")

        val defaultPath = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "OpenMacropadServer" + File.separator + "Macros"
        val defaultDir = File(defaultPath)
        if (!defaultDir.exists()) defaultDir.mkdirs()
        fileChooser.currentDirectory = defaultDir

        if (suggestedName != null) {
            val finalSuggestedName = if (suggestedName.lowercase().endsWith(".json")) {
                suggestedName
            } else {
                suggestedName + ".json"
            }
            fileChooser.selectedFile = File(defaultDir, finalSuggestedName)
        }

        val userSelection = fileChooser.showSaveDialog(this)

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            var fileToSave = fileChooser.selectedFile
            if (!fileToSave.name.lowercase().endsWith(".json")) {
                fileToSave = File(fileToSave.absolutePath + ".json")
            }
            saveToFile(fileToSave)
        }
    }

    private fun saveToFile(file: File) {
        try {
            file.writeText(getText())
            currentFile = file
            
            val tabbedPane = SwingUtilities.getAncestorOfClass(JTabbedPane::class.java, this) as? TabbedUI
            tabbedPane?.setTitleForComponent(this, file.name)

            JOptionPane.showMessageDialog(this, "Macro saved successfully to ${file.name}", "Save Successful", JOptionPane.INFORMATION_MESSAGE)
        } catch (e: IOException) {
            JOptionPane.showMessageDialog(this, "Error saving macro: ${e.message}", "Save Error", JOptionPane.ERROR_MESSAGE)
            e.printStackTrace()
        }
    }

    fun getMacroBar(): MacroBar {
        return macroBar
    }
}