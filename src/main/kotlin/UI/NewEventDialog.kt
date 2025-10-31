package UI

import org.json.JSONObject
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.*

class NewEventDialog(parent: JFrame) : JDialog(parent, "Create New Macro Event", true) {

    private val eventTypeComboBox: JComboBox<String>
    private val cardsPanel: JPanel
    private val cardLayout = CardLayout()

    private val keyEventCard = "Key Event"
    private val mouseEventCard = "Mouse Event"

    private lateinit var keyTextField: JTextField
    private lateinit var keyCommandComboBox: JComboBox<String>

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

        panel.add(JLabel("Key:"))
        keyTextField = JTextField()
        panel.add(keyTextField)

        panel.add(JLabel("Command:"))
        keyCommandComboBox = JComboBox(arrayOf("PRESS", "RELEASE"))
        panel.add(keyCommandComboBox)

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
        val jsonEvent = JSONObject()

        return when (selectedType) {
            keyEventCard -> {
                if (keyTextField.text.isBlank()) {
                    JOptionPane.showMessageDialog(this, "Key cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE)
                    return false
                }
                jsonEvent.put("type", "key")
                jsonEvent.put("key", keyTextField.text)
                jsonEvent.put("command", keyCommandComboBox.selectedItem as String)
                createdEvent = jsonEvent
                true
            }
            mouseEventCard -> {
                try {
                    val x = xCoordinateField.text.toInt()
                    val y = yCoordinateField.text.toInt()
                    jsonEvent.put("type", "mouse")
                    jsonEvent.put("command", mouseCommandComboBox.selectedItem as String)
                    jsonEvent.put("x", x)
                    jsonEvent.put("y", y)
                    createdEvent = jsonEvent
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