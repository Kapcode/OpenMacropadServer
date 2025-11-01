package UI

import java.awt.Color

class MacroKeyItem(keyText: String, command: String) : DraggableMacroItem() {

    init {
        var displayText = keyText
        // The color change must happen *after* the parent constructor has run.
        // We can do this by checking the command and then setting the color.
        if (command == "ON-RELEASE") {
            displayText = keyText.uppercase()
            // We need to set the foreground on the labels themselves, not the panel.
            super.keyLabel.foreground = Color.RED
            super.commandLabel.foreground = Color.RED
        }
        setText(displayText, command)
    }
}