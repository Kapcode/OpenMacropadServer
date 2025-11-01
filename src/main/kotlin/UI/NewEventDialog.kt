package UI

import org.json.JSONObject
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.*
import java.util.LinkedHashMap

class NewEventDialog(parent: JFrame) : JDialog(parent, "Create New Macro Event", true) {

    private val eventTypeComboBox: JComboBox<String>
    private val cardsPanel: JPanel
    private val cardLayout = CardLayout()

    private val keyEventCard = "Key Event"
    private val mouseEventCard = "Mouse Event"

    private lateinit var keyTextField: JTextField
    private lateinit var keyCommandComboBox: JComboBox<String>
    private lateinit var isTriggerCheckBox: JCheckBox

    private lateinit var mouseCommandComboBox: JComboBox<String>
    private lateinit var xCoordinateField: JTextField
    private lateinit var yCoordinateField: JTextField

    var createdEvent: JSONObject? = null
        private set

    init {
        layout = BorderLayout()

        val topPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        topPanel.add(JLabel("Event Type:"))
        eventTypeComboBox = JComboBox(arrayOf(keyEventCard, mouseEventCard))
        topPanel.add(eventTypeComboBox)
        add(topPanel, BorderLayout.NORTH)

        cardsPanel = JPanel(cardLayout)
        createKeyEventPanel()
        createMouseEventPanel()
        add(cardsPanel, BorderLayout.CENTER)

        eventTypeComboBox.addActionListener { 
            cardLayout.show(cardsPanel, eventTypeComboBox.selectedItem as String)
        }

        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val createButton = JButton("Create")
        val cancelButton = JButton("Cancel")
        buttonPanel.add(createButton)
        buttonPanel.add(cancelButton)
        add(buttonPanel, BorderLayout.SOUTH)

        createButton.addActionListener { 
            if (createEvent()) {
                dispose()
            }
        }
        cancelButton.addActionListener { dispose() }

        pack()
        setLocationRelativeTo(parent)
    }

    private fun createKeyEventPanel() {
        val panel = JPanel(GridLayout(0, 2, 5, 5))
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        panel.add(JLabel("Key(s):"))
        keyTextField = JTextField()
        panel.add(keyTextField)

        panel.add(JLabel("Command:"))
        keyCommandComboBox = JComboBox(arrayOf("PRESS", "RELEASE", "ON-RELEASE"))
        panel.add(keyCommandComboBox)

        panel.add(JLabel("Is Trigger:"))
        isTriggerCheckBox = JCheckBox()
        panel.add(isTriggerCheckBox)

        cardsPanel.add(panel, keyEventCard)
    }

    private fun createMouseEventPanel() {
        val panel = JPanel(GridLayout(0, 2, 5, 5))
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        panel.add(JLabel("Command:"))
        mouseCommandComboBox = JComboBox(MacroMouseItem.MouseCommandType.values().map { it.name }.toTypedArray())
        panel.add(mouseCommandComboBox)

        panel.add(JLabel("X Coordinate:"))
        xCoordinateField = JTextField("0")
        panel.add(xCoordinateField)

        panel.add(JLabel("Y Coordinate:"))
        yCoordinateField = JTextField("0")
        panel.add(yCoordinateField)

        cardsPanel.add(panel, mouseEventCard)
    }

    private fun createEvent(): Boolean {
        val selectedType = eventTypeComboBox.selectedItem as String
        val map = LinkedHashMap<String, Any>() // Use LinkedHashMap to preserve order

        return when (selectedType) {
            keyEventCard -> {
                val keyInput = keyTextField.text.trim()
                if (keyInput.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Key(s) cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE)
                    return false
                }

                if (isTriggerCheckBox.isSelected) {
                    map["type"] = "trigger"
                    map["command"] = keyCommandComboBox.selectedItem as String
                    val keys = keyInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    map["keys"] = keys
                } else {
                    map["type"] = "key"
                    map["command"] = keyCommandComboBox.selectedItem as String
                    if (keyInput.contains(",")) {
                        JOptionPane.showMessageDialog(this, "Only triggers can have multiple keys.", "Input Error", JOptionPane.ERROR_MESSAGE)
                        return false
                    }
                    map["key"] = keyInput
                }
                createdEvent = JSONObject(map)
                true
            }
            mouseEventCard -> {
                try {
                    val x = xCoordinateField.text.toInt()
                    val y = yCoordinateField.text.toInt()
                    map["type"] = "mouse"
                    map["command"] = mouseCommandComboBox.selectedItem as String
                    map["x"] = x
                    map["y"] = y
                    createdEvent = JSONObject(map)
                    true
                } catch (e: NumberFormatException) {
                    JOptionPane.showMessageDialog(this, "Coordinates must be valid integers.", "Input Error", JOptionPane.ERROR_MESSAGE)
                    false
                }
            }
            else -> false
        }
    }
}