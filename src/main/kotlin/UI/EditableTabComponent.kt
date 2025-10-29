package UI

import java.awt.CardLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JTabbedPane

class EditableTabComponent(private val tabbedPane: JTabbedPane, initialTitle: String) : JPanel() {

    private val cardLayout = CardLayout()
    private val label: JLabel
    private val textField: JTextField

    private val labelCard = "label"
    private val textFieldCard = "textField"

    init {
        layout = cardLayout
        isOpaque = false // Make the panel transparent

        // Create the label for displaying the title
        label = JLabel(initialTitle)
        label.border = javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 5) // Some padding

        // Create the text field for editing the title
        textField = JTextField(initialTitle)

        // Add components to the panel
        add(label, labelCard)
        add(textField, textFieldCard)

        // Show the label by default
        cardLayout.show(this, labelCard)

        // Add a mouse listener to the label to start editing on double-click
        label.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    textField.text = label.text
                    cardLayout.show(this@EditableTabComponent, textFieldCard)
                    textField.requestFocusInWindow()
                    textField.selectAll()
                }
            }
        })

        // Add an action listener to the text field to stop editing when Enter is pressed
        textField.addActionListener { stopEditing() }

        // Add a focus listener to the text field to stop editing when it loses focus
        textField.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
                stopEditing()
            }
        })
    }

    private fun stopEditing() {
        val newTitle = textField.text
        label.text = newTitle

        // Find the index of this component's parent in the tabbed pane
        val parentPanel = this.parent as? JPanel
        if (parentPanel != null) {
            val index = tabbedPane.indexOfTabComponent(parentPanel)
            if (index != -1) {
                tabbedPane.setTitleAt(index, newTitle)
            }
        }

        // Switch back to the label view
        cardLayout.show(this, labelCard)
    }

    /**
     * Programmatically updates the title of the tab.
     */
    fun updateTitle(newTitle: String) {
        label.text = newTitle
        textField.text = newTitle
    }
}