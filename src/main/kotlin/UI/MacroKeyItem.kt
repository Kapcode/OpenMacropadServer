package UI

class MacroKeyItem(keyText: String, command: String) : MacroItem() {

    init {
        // Use the parent's setText method to display the key and command.
        // command mean press // release
        setText(keyText, command)
    }
}//