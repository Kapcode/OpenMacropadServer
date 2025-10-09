package UI

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import org.json.JSONException
import org.json.JSONObject
import java.awt.BorderLayout
import java.awt.Point
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class MacroJsonEditorUI : JPanel() {

    private val textArea: RSyntaxTextArea
    private val macroBar: MacroBar

    init {
        layout = BorderLayout()

        // 1. Create and configure the text area for JSON
        textArea = RSyntaxTextArea(20, 60)
        textArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JSON
        textArea.isCodeFoldingEnabled = true
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
        setText(createDefaultMacroJson())
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
            // The macro bar will simply be empty or partially updated, which is fine.
        }

        macroBar.revalidate()
        macroBar.repaint()
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

    /**
     * Sets the text content of the JSON editor.
     */
    fun setText(text: String) {
        // Run on the Event Dispatch Thread to avoid concurrency issues with the DocumentListener
        SwingUtilities.invokeLater {
            textArea.text = text
            textArea.caretPosition = 0 // Reset caret to the beginning
        }
    }

    /**
     * Gets the text content from the JSON editor.
     */
    fun getText(): String {
        return textArea.text
    }

    /**
     * Gets a reference to the MacroBar instance.
     */
    fun getMacroBar(): MacroBar {
        return macroBar
    }
}