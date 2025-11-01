package UI

import java.awt.Color

class MacroKeyItem(keyText: String, command: String) : DraggableMacroItem() {

    init {
        var displayText = keyText
        if (command == "ON-RELEASE") {
            // This is a trigger, so set the foreground to red and make the text uppercase
            super.keyLabel.foreground = Color.RED
            super.commandLabel.foreground = Color.RED
            displayText = keyText.uppercase()
        }
        setText(displayText, command)
    }
}