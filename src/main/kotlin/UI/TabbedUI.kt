package UI

import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTabbedPane

class TabbedUI : JTabbedPane() {

    override fun add(title: String, component: Component): Component {
        super.add(title, component)
        val index = indexOfComponent(component)

        val tabHeader = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        tabHeader.isOpaque = false

        val editableLabel = EditableTabComponent(this, title)
        val closeButton = JButton("x")
        closeButton.isContentAreaFilled = false
        closeButton.border = null
        closeButton.addActionListener { 
            remove(component)
        }

        tabHeader.add(editableLabel)
        tabHeader.add(closeButton)

        // Listener with custom double-click timing
        tabHeader.addMouseListener(object : MouseAdapter() {
            var lastClickTime: Long = 0
            val DOUBLE_CLICK_THRESHOLD = 300 // Milliseconds

            override fun mouseClicked(e: MouseEvent) {
                val tabIndex = indexOfTabComponent(tabHeader)
                if (tabIndex == -1) return

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime <= DOUBLE_CLICK_THRESHOLD) {
                    // Double-click detected
                    editableLabel.startEditing()
                } else {
                    // Single-click
                    selectedIndex = tabIndex
                }
                lastClickTime = currentTime
            }
        })

        setTabComponentAt(index, tabHeader)

        return component
    }

    fun getTitleForComponent(component: Component): String? {
        val index = indexOfComponent(component)
        if (index != -1) {
            return getTitleAt(index)
        }
        return null
    }

    fun setTitleForComponent(component: Component, newTitle: String) {
        val index = indexOfComponent(component)
        if (index != -1) {
            val tabHeader = getTabComponentAt(index) as? JPanel
            val editableTab = tabHeader?.getComponent(0) as? EditableTabComponent
            editableTab?.updateTitle(newTitle)
            setTitleAt(index, newTitle)
        }
    }
}