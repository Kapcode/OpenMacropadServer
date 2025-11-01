import MacroPlayer
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener

class ActiveMacroManager(private val macroPlayer: MacroPlayer) : NativeKeyListener, NativeMouseInputListener {

    // Map to store active macros: MacroFile -> Parsed Macro JSON
    private val activeMacros = ConcurrentHashMap<File, JSONObject>()

    // Map to store currently pressed keys for trigger detection
    private val pressedKeys = ConcurrentHashMap<Int, Boolean>()

    // Configuration file for persisting active macro states
    private val configFilePath = Paths.get(System.getProperty("user.home"), "Documents", "OpenMacropadServer", "active_macros.json")

    init {
        // Disable JNativeHook's default logger level to INFO for debugging
        Logger.getLogger(GlobalScreen::class.java.packageName).level = Level.INFO 
        Logger.getLogger(GlobalScreen::class.java.packageName).useParentHandlers = false // Prevent duplicate console output

        try {
            GlobalScreen.registerNativeHook()
        } catch (ex: NativeHookException) {
            System.err.println("There was a problem registering the native hook.")
            System.err.println(ex.message)
        }
        GlobalScreen.addNativeKeyListener(this)
        GlobalScreen.addNativeMouseListener(this)
        GlobalScreen.addNativeMouseMotionListener(this)

        // Ensure config directory exists
        Files.createDirectories(configFilePath.parent)
        loadActiveMacroStates()
    }

    fun addActiveMacro(macroFile: File, macroJson: JSONObject) {
        activeMacros[macroFile] = macroJson
        saveActiveMacroStates()
        println("Added active macro: ${macroFile.name}. Current active macros: ${activeMacros.keys.map { it.name }}")
    }

    fun removeActiveMacro(macroFile: File) {
        activeMacros.remove(macroFile)
        saveActiveMacroStates()
        println("Removed active macro: ${macroFile.name}. Current active macros: ${activeMacros.keys.map { it.name }}")
    }

    fun isMacroActive(macroFile: File): Boolean {
        return activeMacros.containsKey(macroFile)
    }

    // Cleanup method to unregister the native hook
    fun shutdown() {
        try {
            GlobalScreen.removeNativeKeyListener(this)
            GlobalScreen.removeNativeMouseListener(this)
            GlobalScreen.removeNativeMouseMotionListener(this)
            GlobalScreen.unregisterNativeHook()
            println("JNativeHook unregistered successfully.")
        } catch (ex: NativeHookException) {
            System.err.println("There was a problem unregistering the native hook.")
            System.err.println(ex.message)
        }
    }

    private fun loadActiveMacroStates() {
        if (Files.exists(configFilePath)) {
            try {
                val content = Files.readString(configFilePath)
                val jsonArray = JSONArray(content)
                for (i in 0 until jsonArray.length()) {
                    val filePath = jsonArray.getString(i)
                    val file = File(filePath)
                    if (file.exists()) {
                        val macroJson = JSONObject(file.readText())
                        activeMacros[file] = macroJson
                    }
                }
                println("Loaded active macro states: ${activeMacros.keys.map { it.name }}")
            } catch (e: Exception) {
                System.err.println("Error loading active macro states: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun saveActiveMacroStates() {
        try {
            val jsonArray = JSONArray()
            activeMacros.keys.forEach { file ->
                jsonArray.put(file.absolutePath)
            }
            Files.writeString(configFilePath, jsonArray.toString(4))
            println("Saved active macro states.")
        } catch (e: Exception) {
            System.err.println("Error saving active macro states: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun nativeKeyPressed(nativeEvent: NativeKeyEvent) {
        synchronized(pressedKeys) {
            pressedKeys[nativeEvent.keyCode] = true
            println("Key Pressed: ${NativeKeyEvent.getKeyText(nativeEvent.keyCode)} (Code: ${nativeEvent.keyCode}), Pressed Keys: ${pressedKeys.keys.map { NativeKeyEvent.getKeyText(it) }}")
        }
    }

    override fun nativeKeyReleased(nativeEvent: NativeKeyEvent) {
        val releasedKeyCode = nativeEvent.keyCode
        // Create a snapshot of currently pressed keys *before* removing the released key
        val pressedKeysSnapshot = ConcurrentHashMap(pressedKeys)
        synchronized(pressedKeys) {
            pressedKeys.remove(releasedKeyCode)
            println("Key Released: ${NativeKeyEvent.getKeyText(releasedKeyCode)} (Code: ${releasedKeyCode}), Remaining Pressed Keys: ${pressedKeys.keys.map { NativeKeyEvent.getKeyText(it) }}")
        }
        // Check for triggers on key release, passing the released key and the snapshot
        checkForTriggers(releasedKeyCode, pressedKeysSnapshot)
    }

    override fun nativeKeyTyped(nativeEvent: NativeKeyEvent) {
        // Not used for hotkey detection
    }

    // NativeMouseInputListener methods (not currently used for triggers, but required by interface)
    override fun nativeMouseClicked(nativeEvent: NativeMouseEvent) {}
    override fun nativeMousePressed(nativeEvent: NativeMouseEvent) {}
    override fun nativeMouseReleased(nativeEvent: NativeMouseEvent) {}
    override fun nativeMouseMoved(nativeEvent: NativeMouseEvent) {}
    override fun nativeMouseDragged(nativeEvent: NativeMouseEvent) {}

    private fun checkForTriggers(releasedKeyCode: Int, pressedKeysSnapshot: ConcurrentHashMap<Int, Boolean>) {
        println("\n--- Checking for Triggers ---")
        println("Released Key Code: ${NativeKeyEvent.getKeyText(releasedKeyCode)}")
        println("Pressed Keys Snapshot (before release): ${pressedKeysSnapshot.keys.map { NativeKeyEvent.getKeyText(it) }}")

        activeMacros.forEach { (macroFile, macroJson) ->
            println("  Evaluating macro: ${macroFile.name}")
            val eventsArray = macroJson.optJSONArray("events")
            if (eventsArray != null && eventsArray.length() > 0) {
                val firstEvent = eventsArray.getJSONObject(0)
                if (firstEvent.optString("type") == "trigger" && firstEvent.optString("command") == "ON-RELEASE") {
                    val triggerKeysJson = firstEvent.optJSONArray("keys")
                    val triggerKey = firstEvent.optString("key")

                    val requiredKeyCodes = mutableSetOf<Int>()
                    if (triggerKeysJson != null) {
                        for (i in 0 until triggerKeysJson.length()) {
                            val keyName = triggerKeysJson.getString(i).uppercase()
                            val jnhCode = KeyMap.stringToNativeKeyCodeMap[keyName]
                            if (jnhCode != null) {
                                requiredKeyCodes.add(jnhCode)
                            } else {
                                println("    WARNING: KeyMap.stringToNativeKeyCodeMap does not contain: $keyName")
                            }
                        }
                    } else if (triggerKey.isNotEmpty()) {
                        val keyName = triggerKey.uppercase()
                        val jnhCode = KeyMap.stringToNativeKeyCodeMap[keyName]
                        if (jnhCode != null) {
                            requiredKeyCodes.add(jnhCode)
                        } else {
                            println("    WARNING: KeyMap.stringToNativeKeyCodeMap does not contain: $keyName")
                        }
                    }

                    println("    Trigger Type: ON-RELEASE, Required Key Codes: ${requiredKeyCodes.map { NativeKeyEvent.getKeyText(it) }}")

                    // Check if the released key is part of the trigger combination
                    if (requiredKeyCodes.contains(releasedKeyCode)) {
                        println("    Released key (${NativeKeyEvent.getKeyText(releasedKeyCode)}) is part of the trigger.")
                        // Remove the released key from the required set for checking against the snapshot
                        val remainingRequiredKeys = requiredKeyCodes.toMutableSet()
                        remainingRequiredKeys.remove(releasedKeyCode)

                        println("    Remaining required keys (excluding released): ${remainingRequiredKeys.map { NativeKeyEvent.getKeyText(it) }}")

                        // Check if all *other* required keys were pressed when the released key was released
                        if (pressedKeysSnapshot.keys.containsAll(remainingRequiredKeys)) {
                            println("    ALL REMAINING REQUIRED KEYS WERE PRESSED IN SNAPSHOT! Trigger detected for ${macroFile.name}!")
                            // Trigger detected, play macro on a new thread
                            Thread { 
                                try {
                                    // Play macro starting from the second event (skip the trigger itself)
                                    macroPlayer.playMacro(macroJson.toString(), startIndex = 1)
                                } catch (e: Exception) {
                                    System.err.println("Error playing active macro ${macroFile.name}: ${e.message}")
                                    e.printStackTrace()
                                }
                            }.start()
                        } else {
                            println("    Snapshot does NOT contain all remaining required keys.")
                        }
                    } else {
                        println("    Released key (${NativeKeyEvent.getKeyText(releasedKeyCode)}) is NOT part of the trigger.")
                    }
                } else {
                    println("    First event is not an ON-RELEASE trigger.")
                }
            } else {
                println("    Macro has no events or no trigger event.")
            }
        }
        println("--- End Trigger Check ---\n")
    }
}