package UI

class MacroKeyItem(keyText: String, command: String) : DraggableMacroItem() {

    init {
        // Use the parent's setText method to display the key and command.
        setText(keyText, command)
    }
}