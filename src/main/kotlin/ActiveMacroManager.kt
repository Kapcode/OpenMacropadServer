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

    private val activeMacros = ConcurrentHashMap<File, JSONObject>()
    private val pressedKeys = ConcurrentHashMap<Int, Boolean>()
    private val configFilePath = Paths.get(System.getProperty("user.home"), "Documents", "OpenMacropadServer", "active_macros.json")

    init {
        Logger.getLogger(GlobalScreen::class.java.packageName).level = Level.WARNING
        Logger.getLogger(GlobalScreen::class.java.packageName).useParentHandlers = false

        try {
            GlobalScreen.registerNativeHook()
        } catch (ex: NativeHookException) {
            System.err.println("There was a problem registering the native hook.")
            System.err.println(ex.message)
        }
        GlobalScreen.addNativeKeyListener(this)
        GlobalScreen.addNativeMouseListener(this)
        GlobalScreen.addNativeMouseMotionListener(this)

        Files.createDirectories(configFilePath.parent)
        loadActiveMacroStates()
    }

    fun addActiveMacro(macroFile: File, macroJson: JSONObject) {
        activeMacros[macroFile] = macroJson
        saveActiveMacroStates()
    }

    fun removeActiveMacro(macroFile: File) {
        activeMacros.remove(macroFile)
        saveActiveMacroStates()
    }

    fun isMacroActive(macroFile: File): Boolean {
        return activeMacros.containsKey(macroFile)
    }

    fun shutdown() {
        try {
            GlobalScreen.removeNativeKeyListener(this)
            GlobalScreen.removeNativeMouseListener(this)
            GlobalScreen.removeNativeMouseMotionListener(this)
            GlobalScreen.unregisterNativeHook()
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
            } catch (e: Exception) {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun nativeKeyPressed(nativeEvent: NativeKeyEvent) {
        synchronized(pressedKeys) {
            pressedKeys[nativeEvent.keyCode] = true
        }
    }

    override fun nativeKeyReleased(nativeEvent: NativeKeyEvent) {
        val releasedKeyCode = nativeEvent.keyCode
        val pressedKeysSnapshot = ConcurrentHashMap(pressedKeys)
        synchronized(pressedKeys) {
            pressedKeys.remove(releasedKeyCode)
        }
        checkForTriggers(releasedKeyCode, pressedKeysSnapshot)
    }

    override fun nativeKeyTyped(nativeEvent: NativeKeyEvent) {}
    override fun nativeMouseClicked(nativeEvent: NativeMouseEvent) {}
    override fun nativeMousePressed(nativeEvent: NativeMouseEvent) {}
    override fun nativeMouseReleased(nativeEvent: NativeMouseEvent) {}
    override fun nativeMouseMoved(nativeEvent: NativeMouseEvent) {}
    override fun nativeMouseDragged(nativeEvent: NativeMouseEvent) {}

    private fun checkForTriggers(releasedKeyCode: Int, pressedKeysSnapshot: ConcurrentHashMap<Int, Boolean>) {
        activeMacros.forEach { (macroFile, macroJson) ->
            val triggerObj = macroJson.optJSONObject("trigger")
            if (triggerObj != null && triggerObj.optString("command") == "ON-RELEASE") {
                val triggerKeysJson = triggerObj.optJSONArray("keys")
                val triggerKey = triggerObj.optString("key")

                val requiredKeyCodes = mutableSetOf<Int>()
                if (triggerKeysJson != null) {
                    for (i in 0 until triggerKeysJson.length()) {
                        val keyName = triggerKeysJson.getString(i).uppercase()
                        KeyMap.stringToNativeKeyCodeMap[keyName]?.let { requiredKeyCodes.add(it) }
                    }
                } else if (triggerKey.isNotEmpty()) {
                    KeyMap.stringToNativeKeyCodeMap[triggerKey.uppercase()]?.let { requiredKeyCodes.add(it) }
                }

                if (requiredKeyCodes.contains(releasedKeyCode)) {
                    val remainingRequiredKeys = requiredKeyCodes.toMutableSet()
                    remainingRequiredKeys.remove(releasedKeyCode)

                    val pressedCodes = pressedKeysSnapshot.keys.toSet()

                    val allModifiersHeld = remainingRequiredKeys.all { requiredKey ->
                        when (requiredKey) {//todo test this, i have no idea if l shift right shift will work, try updating jnative hook?
                            // Using integer literals as a fallback since named constants for L/R modifiers are unavailable.
                            // 29=VC_CONTROL_L, 157=VC_CONTROL_R
                            NativeKeyEvent.VC_CONTROL -> pressedCodes.contains(29) || pressedCodes.contains(157)
                            // 42=VC_SHIFT_L, 54=VC_SHIFT_R
                            NativeKeyEvent.VC_SHIFT -> pressedCodes.contains(42) || pressedCodes.contains(54)
                            // 56=VC_ALT_L, 184=VC_ALT_R
                            NativeKeyEvent.VC_ALT -> pressedCodes.contains(56) || pressedCodes.contains(184)
                            else -> pressedCodes.contains(requiredKey)
                        }
                    }

                    if (allModifiersHeld) {
                        Thread { 
                            try {
                                macroPlayer.playMacro(macroJson.toString(), startIndex = 0)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }.start()
                    }
                }
            }
        }
    }
}