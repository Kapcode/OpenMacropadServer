package UI

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.awt.BorderLayout
import java.awt.Point
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.File
import java.io.IOException
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.util.LinkedHashMap

class MacroJsonEditorUI(private val frame: JFrame) : JPanel(), PropertyChangeListener {

    private val textArea: RSyntaxTextArea
    private lateinit var macroBar: MacroBar
    private var currentFile: File? = null
    private var isUpdatingFromText = false
    private var isUpdatingFromBar = false

    init {
        layout = BorderLayout()

        textArea = RSyntaxTextArea(20, 60)
        textArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JSON
        textArea.isCodeFoldingEnabled = true

        try {
            val theme = org.fife.ui.rsyntaxtextarea.Theme.load(javaClass.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"))
            theme.apply(textArea)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val sp = RTextScrollPane(textArea)
        add(sp, BorderLayout.CENTER)

        addPropertyChangeListener("ancestor") { e ->
            if (e.newValue != null && !::macroBar.isInitialized) {
                val tabbedPane = SwingUtilities.getAncestorOfClass(JTabbedPane::class.java, this) as? TabbedUI
                if (tabbedPane != null) {
                    macroBar = MacroBar(frame, tabbedPane)
                    macroBar.addPropertyChangeListener(this@MacroJsonEditorUI)
                    val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, sp, macroBar)
                    splitPane.resizeWeight = 0.7
                    remove(sp)
                    add(splitPane, BorderLayout.CENTER)
                    revalidate()
                    repaint()
                    updateMacroBarFromText()
                }
            }
        }

        textArea.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) { if (!isUpdatingFromBar) updateMacroBarFromText() }
            override fun removeUpdate(e: DocumentEvent?) { if (!isUpdatingFromBar) updateMacroBarFromText() }
            override fun changedUpdate(e: DocumentEvent?) { if (!isUpdatingFromBar) updateMacroBarFromText() }
        })

        setText(createDefaultMacroJson(), null)
    }

    override fun propertyChange(evt: PropertyChangeEvent?) {
        if (::macroBar.isInitialized && evt?.source == macroBar && evt.propertyName == "component.reordered") {
            if (!isUpdatingFromText) {
                updateTextFromMacroBar()
            }
        }
    }

    fun insertNewEvent(newEvent: JSONObject, wasEditorInFocus: Boolean) {
        val originalText = getText()
        val caretPosition = textArea.caretPosition

        try {
            val currentJson = JSONObject(originalText)
            val events = currentJson.getJSONArray("events")
            var insertIndex = -1

            if (wasEditorInFocus) {
                val eventsArrayContentStartIndex = originalText.indexOf('[', originalText.indexOf("\"events\""))
                if (caretPosition > eventsArrayContentStartIndex) {
                    val textBeforeCaretInArray = originalText.substring(eventsArrayContentStartIndex, caretPosition)
                    insertIndex = textBeforeCaretInArray.count { it == '}' }
                }
            }

            if (insertIndex != -1 && insertIndex <= events.length()) {
                events.put(insertIndex, newEvent)
            } else {
                events.put(newEvent)
            }
            
            setText(currentJson.toString(4), currentFile)
            
        } catch (e: JSONException) {
            val newEventText = newEvent.toString(4)
            SwingUtilities.invokeLater {
                textArea.insert(newEventText, caretPosition)
            }
        }
    }

    fun hasTextFocus(): Boolean {
        return textArea.hasFocus()
    }

    private fun updateTextFromMacroBar() {
        if (!::macroBar.isInitialized) return
        isUpdatingFromBar = true
        val events = JSONArray()
        for (component in macroBar.macroItemsPanel.components) {
            val map = LinkedHashMap<String, Any>()
            when (component) {
                is MacroKeyItem -> {
                    map["type"] = "key"
                    map["command"] = component.getCommand()
                    map["key"] = component.getText()
                    events.put(JSONObject(map))
                }
                is MacroMouseItem -> {
                    map["type"] = "mouse"
                    map["command"] = component.commandType.name
                    map["x"] = component.point.x
                    map["y"] = component.point.y
                    events.put(JSONObject(map))
                }
            }
        }
        val root = LinkedHashMap<String, Any>()
        root["events"] = events
        setText(JSONObject(root).toString(4), currentFile)
        isUpdatingFromBar = false
    }

    private fun updateMacroBarFromText() {
        if (!::macroBar.isInitialized) return
        isUpdatingFromText = true
        macroBar.clear()

        try {
            val json = JSONObject(getText())
            val events = json.getJSONArray("events")

            for (i in 0 until events.length()) {
                val eventObject = events.getJSONObject(i)
                when (eventObject.getString("type")) {
                    "key" -> {
                        val keyText = eventObject.optString("key", eventObject.optJSONArray("keys")?.join(","))
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
            // JSON is likely malformed, which is expected during typing.
        }

        macroBar.revalidate()
        macroBar.repaint()
        isUpdatingFromText = false
    }

    private fun createDefaultMacroJson(): String {
        return """{
    "events": [
        {
            "type": "key",
            "command": "PRESS",
            "key": "Ctrl"
        },
        {
            "type": "key",
            "command": "PRESS",
            "key": "C"
        },
        {
            "type": "key",
            "command": "RELEASE",
            "key": "C"
        },
        {
            "type": "key",
            "command": "RELEASE",
            "key": "Ctrl"
        },
        {
            "type": "key",
            "command": "PRESS",
            "key": "Ctrl"
        },
        {
            "type": "key",
            "command": "PRESS",
            "key": "V"
        },
        {
            "type": "key",
            "command": "RELEASE",
            "key": "V"
        },
        {
            "type": "key",
            "command": "RELEASE",
            "key": "Ctrl"
        }
    ]
}"""
    }

    fun setText(text: String, file: File?) {
        textArea.text = text
        textArea.caretPosition = 0
        this.currentFile = file
        if (::macroBar.isInitialized) {
            updateMacroBarFromText()
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
            val finalSuggestedName = if (suggestedName.lowercase().endsWith(".json")) suggestedName else "$suggestedName.json"
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