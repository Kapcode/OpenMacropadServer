package UI

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.TransferHandler

abstract class DraggableMacroItem : MacroItem() {

    init {
        val listener = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val component = e.source as? JComponent ?: return
                val handler = component.transferHandler ?: return
                handler.exportAsDrag(component, e, TransferHandler.MOVE)
            }
        }
        addMouseListener(listener)
    }
}