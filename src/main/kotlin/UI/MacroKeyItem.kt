package UI

class MacroKeyItem(keyText: String, command: String) : DraggableMacroItem() {

    init {
        // The keyText can now be a comma-separated list
        setText(keyText, command)
    }
}