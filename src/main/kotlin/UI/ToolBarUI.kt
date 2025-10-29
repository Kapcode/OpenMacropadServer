package UI

import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.JToolBar

class ToolBarUI : JToolBar() {
    init {
        isFloatable = false
        isRollover = true

        background = Theme().PrimaryToolBarBackgroundColor
        border = BorderFactory.createLineBorder(Theme().PrimaryToolBarBorderColor) // Add border to the toolbar itself
    }

    fun addButton(text: String, tooltip: String, actionListener: ActionListener) {
        val button = ToolBarButton(text, tooltip, actionListener)
        add(button)
    }
}