package UI

import java.awt.Font
import java.awt.Graphics
import javax.swing.JLabel

class DynamicFontSizeLabel(private val defaultSize: Float, private val minSize: Float) : JLabel() {

    override fun paintComponent(g: Graphics) {
        // Guard against running when the component has no width yet.
        if (width == 0) {
            super.paintComponent(g)
            return
        }

        val originalFont = font.deriveFont(defaultSize)
        this.font = originalFont

        val fontMetrics = g.getFontMetrics(font)
        val textWidth = fontMetrics.stringWidth(text)
        val availableWidth = width - 6 // Padding

        if (textWidth > availableWidth) {
            var newFontSize = defaultSize
            var newFont = originalFont

            while (g.getFontMetrics(newFont).stringWidth(text) > availableWidth && newFontSize > minSize) {
                newFontSize -= 1f
                newFont = newFont.deriveFont(newFontSize)
            }
            this.font = newFont
        }

        super.paintComponent(g)
    }
}