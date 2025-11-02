package UI

import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent

class TabbedUI : JTabbedPane() {

    override fun add(title: String, component: Component): Component {
        super.add(title, component)
        val index = indexOfComponent(component)
        setTabComponentAt(index, TabTitle(title))
        return component // Return the added component
    }

    fun setTitleForComponent(component: Component, title: String) {
        val index = indexOfComponent(component)
        if (index != -1) {
            val tabTitle = getTabComponentAt(index) as? TabTitle
            tabTitle?.editableLabel?.text = title
            // Also update the actual tab title in JTabbedPane
            setTitleAt(index, title)
        }
    }

    fun getTitleForComponent(component: Component): String? {
        val index = indexOfComponent(component)
        return if (index != -1) {
            (getTabComponentAt(index) as? TabTitle)?.editableLabel?.text
        } else {
            null
        }
    }

    inner class TabTitle(initialTitle: String) : JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)) {
        val editableLabel: JLabel = JLabel(initialTitle)
        private val textField: JTextField = JTextField(initialTitle)

        init {
            isOpaque = false
            isFocusable = false // Make the TabTitle JPanel itself non-focusable

            // Make children non-focusable to ensure parent (TabTitle) doesn't interfere with JTabbedPane clicks
            editableLabel.isFocusable = false
            // Removed: textField.isFocusable = false; JTextField needs to be focusable to be editable

            add(editableLabel)
            add(textField)

            // Add the new Edit button
            val editIcon = SvgIconRenderer.getIcon("/pencil-icon.svg", 12, 12)
            val editButton = if (editIcon != null) JButton(editIcon) else JButton("Edit")
            editButton.toolTipText = "Edit Tab Title"
            editButton.isContentAreaFilled = false
            editButton.isFocusable = false
            editButton.border = BorderFactory.createEmptyBorder()
            editButton.addActionListener { startEditing() }
            add(editButton)

            add(TabButton())

            // Initially show the label, hide the text field
            editableLabel.isVisible = true
            textField.isVisible = false

            textField.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent?) {
                    finishEditing()
                }
            })

            textField.addActionListener { finishEditing() }
        }

        fun startEditing() {
            editableLabel.isVisible = false
            textField.isVisible = true
            textField.text = editableLabel.text
            revalidate()
            repaint()
            textField.requestFocusInWindow()
            textField.selectAll() // Highlight the text
        }

        private fun finishEditing() {
            val newTitle = textField.text
            editableLabel.text = newTitle
            editableLabel.isVisible = true
            textField.isVisible = false
            revalidate()
            repaint()

            // Update the actual tab title in the JTabbedPane
            val tabIndex = this@TabbedUI.indexOfTabComponent(this)
            if (tabIndex != -1) {
                this@TabbedUI.setTitleAt(tabIndex, newTitle)
            }
        }
    }

    inner class TabButton : JButton(SvgIconRenderer.getIcon("/close-icon.svg", 12, 12)), SwingConstants {
        init {
            isContentAreaFilled = false
            isFocusable = false
            border = BorderFactory.createEmptyBorder()
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val tab = this@TabbedUI
                    val i = tab.indexOfTabComponent(this@TabButton.parent as Component)
                    if (i != -1) {
                        val editor = tab.getComponentAt(i) as? MacroJsonEditorUI
                        if (editor?.hasUnsavedChanges == true) {
                            val choice = JOptionPane.showConfirmDialog(
                                tab,
                                "Save changes to ${tab.getTitleForComponent(editor)}?",
                                "Unsaved Changes",
                                JOptionPane.YES_NO_CANCEL_OPTION
                            )
                            when (choice) {
                                JOptionPane.YES_OPTION -> {
                                    editor.save(tab.getTitleForComponent(editor))
                                    tab.remove(i)
                                }
                                JOptionPane.NO_OPTION -> tab.remove(i)
                                // JOptionPane.CANCEL_OPTION or closing the dialog does nothing
                            }
                        } else {
                            tab.remove(i)
                        }
                    }
                }
            })
        }
    }
}