import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener
import org.json.JSONArray
import org.json.JSONObject
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

class MacroRecorder : NativeKeyListener, NativeMouseInputListener {

    private val events = mutableListOf<String>()
    private var isRecording = false

    private val awtKeyCodeMap = mapOf(
        "Backspace" to KeyEvent.VK_BACK_SPACE,
        "Tab" to KeyEvent.VK_TAB,
        "Enter" to KeyEvent.VK_ENTER,
        "Shift" to KeyEvent.VK_SHIFT,
        "Ctrl" to KeyEvent.VK_CONTROL,
        "Alt" to KeyEvent.VK_ALT,
        "Caps Lock" to KeyEvent.VK_CAPS_LOCK,
        "Escape" to KeyEvent.VK_ESCAPE,
        "Space" to KeyEvent.VK_SPACE,
        "Page Up" to KeyEvent.VK_PAGE_UP,
        "Page Down" to KeyEvent.VK_PAGE_DOWN,
        "End" to KeyEvent.VK_END,
        "Home" to KeyEvent.VK_HOME,
        "Left" to KeyEvent.VK_LEFT,
        "Up" to KeyEvent.VK_UP,
        "Right" to KeyEvent.VK_RIGHT,
        "Down" to KeyEvent.VK_DOWN,
        "Insert" to KeyEvent.VK_INSERT,
        "Delete" to KeyEvent.VK_DELETE,
        "0" to KeyEvent.VK_0,
        "1" to KeyEvent.VK_1,
        "2" to KeyEvent.VK_2,
        "3" to KeyEvent.VK_3,
        "4" to KeyEvent.VK_4,
        "5" to KeyEvent.VK_5,
        "6" to KeyEvent.VK_6,
        "7" to KeyEvent.VK_7,
        "8" to KeyEvent.VK_8,
        "9" to KeyEvent.VK_9,
        "A" to KeyEvent.VK_A,
        "B" to KeyEvent.VK_B,
        "C" to KeyEvent.VK_C,
        "D" to KeyEvent.VK_D,
        "E" to KeyEvent.VK_E,
        "F" to KeyEvent.VK_F,
        "G" to KeyEvent.VK_G,
        "H" to KeyEvent.VK_H,
        "I" to KeyEvent.VK_I,
        "J" to KeyEvent.VK_J,
        "K" to KeyEvent.VK_K,
        "L" to KeyEvent.VK_L,
        "M" to KeyEvent.VK_M,
        "N" to KeyEvent.VK_N,
        "O" to KeyEvent.VK_O,
        "P" to KeyEvent.VK_P,
        "Q" to KeyEvent.VK_Q,
        "R" to KeyEvent.VK_R,
        "S" to KeyEvent.VK_S,
        "T" to KeyEvent.VK_T,
        "U" to KeyEvent.VK_U,
        "V" to KeyEvent.VK_V,
        "W" to KeyEvent.VK_W,
        "X" to KeyEvent.VK_X,
        "Y" to KeyEvent.VK_Y,
        "Z" to KeyEvent.VK_Z,
        "F1" to KeyEvent.VK_F1,
        "F2" to KeyEvent.VK_F2,
        "F3" to KeyEvent.VK_F3,
        "F4" to KeyEvent.VK_F4,
        "F5" to KeyEvent.VK_F5,
        "F6" to KeyEvent.VK_F6,
        "F7" to KeyEvent.VK_F7,
        "F8" to KeyEvent.VK_F8,
        "F9" to KeyEvent.VK_F9,
        "F10" to KeyEvent.VK_F10,
        "F11" to KeyEvent.VK_F11,
        "F12" to KeyEvent.VK_F12,
        "Semicolon" to KeyEvent.VK_SEMICOLON,
        "Equals" to KeyEvent.VK_EQUALS,
        "Comma" to KeyEvent.VK_COMMA,
        "Minus" to KeyEvent.VK_MINUS,
        "Period" to KeyEvent.VK_PERIOD,
        "Slash" to KeyEvent.VK_SLASH,
        "Back Quote" to KeyEvent.VK_BACK_QUOTE,
        "Open Bracket" to KeyEvent.VK_OPEN_BRACKET,
        "Back Slash" to KeyEvent.VK_BACK_SLASH,
        "Close Bracket" to KeyEvent.VK_CLOSE_BRACKET,
        "Quote" to KeyEvent.VK_QUOTE,
        "NumPad-0" to KeyEvent.VK_NUMPAD0,
        "NumPad-1" to KeyEvent.VK_NUMPAD1,
        "NumPad-2" to KeyEvent.VK_NUMPAD2,
        "NumPad-3" to KeyEvent.VK_NUMPAD3,
        "NumPad-4" to KeyEvent.VK_NUMPAD4,
        "NumPad-5" to KeyEvent.VK_NUMPAD5,
        "NumPad-6" to KeyEvent.VK_NUMPAD6,
        "NumPad-7" to KeyEvent.VK_NUMPAD7,
        "NumPad-8" to KeyEvent.VK_NUMPAD8,
        "NumPad-9" to KeyEvent.VK_NUMPAD9,
        "NumPad *" to KeyEvent.VK_MULTIPLY,
        "NumPad +" to KeyEvent.VK_ADD,
        "NumPad ," to KeyEvent.VK_SEPARATOR,
        "NumPad -" to KeyEvent.VK_SUBTRACT,
        "NumPad ." to KeyEvent.VK_DECIMAL,
        "NumPad /" to KeyEvent.VK_DIVIDE
    )

    fun startRecording() {
        isRecording = true
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(this)
        GlobalScreen.addNativeMouseListener(this)
        GlobalScreen.addNativeMouseMotionListener(this)
    }

    fun stopRecording() {
        isRecording = false
        GlobalScreen.removeNativeKeyListener(this)
        GlobalScreen.removeNativeMouseListener(this)
        GlobalScreen.removeNativeMouseMotionListener(this)
        GlobalScreen.unregisterNativeHook()
    }

    private fun stopRecordingOnEscapeOrTilde(e: NativeKeyEvent) {
        if (isRecording) {
            when (NativeKeyEvent.getKeyText(e.keyCode)) {
                "Escape", "Back Quote" -> stopRecording()
            }
        }
    }

    fun recordToJSON(): String {
        val json = JSONObject()
        val eventsArray = JSONArray()

        for (eventString in events) {
            val eventObject = JSONObject()
            when {
                eventString.startsWith("Key Pressed:") -> {
                    val keyText = eventString.substringAfter("Key Pressed:").trim()
                    eventObject.put("type", "key_press")
                    eventObject.put("key", keyText)
                }
                eventString.startsWith("Key Released:") -> {
                    val keyText = eventString.substringAfter("Key Released:").trim()
                    eventObject.put("type", "key_release")
                    eventObject.put("key", keyText)
                }
                eventString.startsWith("Mouse Clicked:") -> {
                    val coords = eventString.substringAfter("Mouse Clicked:").trim().split(",").map { it.trim().toInt() }
                    eventObject.put("type", "mouse_click")
                    eventObject.put("x", coords[0])
                    eventObject.put("y", coords[1])
                }
                eventString.startsWith("Mouse Pressed:") -> {
                    val coords = eventString.substringAfter("Mouse Pressed:").trim().split(",").map { it.trim().toInt() }
                    eventObject.put("type", "mouse_press")
                    eventObject.put("x", coords[0])
                    eventObject.put("y", coords[1])
                }
                eventString.startsWith("Mouse Released:") -> {
                    val coords = eventString.substringAfter("Mouse Released:").trim().split(",").map { it.trim().toInt() }
                    eventObject.put("type", "mouse_release")
                    eventObject.put("x", coords[0])
                    eventObject.put("y", coords[1])
                }
                eventString.startsWith("Mouse Moved:") -> {
                    val coords = eventString.substringAfter("Mouse Moved:").trim().split(",").map { it.trim().toInt() }
                    eventObject.put("type", "mouse_move")
                    eventObject.put("x", coords[0])
                    eventObject.put("y", coords[1])
                }
                eventString.startsWith("Mouse Dragged:") -> {
                    val coords = eventString.substringAfter("Mouse Dragged:").trim().split(",").map { it.trim().toInt() }
                    eventObject.put("type", "mouse_drag")
                    eventObject.put("x", coords[0])
                    eventObject.put("y", coords[1])
                }
            }
            eventsArray.put(eventObject)
        }
        json.put("events", eventsArray)
        return json.toString(4)
    }

    fun macroRepeater(jsonString: String) {
        val json = JSONObject(jsonString)
        val eventsArray = json.getJSONArray("events")
        val robot = Robot()

        for (i in 0 until eventsArray.length()) {
            val eventObject = eventsArray.getJSONObject(i)
            when (eventObject.get("type") as String) {
                "key_press" -> {
                    val keyText = eventObject.get("key") as String
                    awtKeyCodeMap[keyText]?.let { robot.keyPress(it) }
                }
                "key_release" -> {
                    val keyText = eventObject.get("key") as String
                    awtKeyCodeMap[keyText]?.let { robot.keyRelease(it) }
                }
                "mouse_press" -> {
                    val x = eventObject.get("x") as Int
                    val y = eventObject.get("y") as Int
                    robot.mouseMove(x, y)
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                }
                "mouse_release" -> {
                    val x = eventObject.get("x") as Int
                    val y = eventObject.get("y") as Int
                    robot.mouseMove(x, y)
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                }
                "mouse_move" -> {
                    val x = eventObject.get("x") as Int
                    val y = eventObject.get("y") as Int
                    robot.mouseMove(x, y)
                }
            }
        }
    }

    override fun nativeKeyPressed(e: NativeKeyEvent) {
        stopRecordingOnEscapeOrTilde(e)
        if (isRecording) {
            events.add("Key Pressed: ${NativeKeyEvent.getKeyText(e.keyCode)}")
        }
    }

    override fun nativeKeyReleased(e: NativeKeyEvent) {
        if (isRecording) {
            events.add("Key Released: ${NativeKeyEvent.getKeyText(e.keyCode)}")
        }
    }

    override fun nativeKeyTyped(e: NativeKeyEvent) {
        // Not used
    }

    override fun nativeMouseClicked(e: NativeMouseEvent) {
        if (isRecording) {
            events.add("Mouse Clicked: ${e.x}, ${e.y}")
        }
    }

    override fun nativeMousePressed(e: NativeMouseEvent) {
        if (isRecording) {
            events.add("Mouse Pressed: ${e.x}, ${e.y}")
        }
    }

    override fun nativeMouseReleased(e: NativeMouseEvent) {
        if (isRecording) {
            events.add("Mouse Released: ${e.x}, ${e.y}")
        }
    }

    override fun nativeMouseMoved(e: NativeMouseEvent) {
        if (isRecording) {
            events.add("Mouse Moved: ${e.x}, ${e.y}")
        }
    }

    override fun nativeMouseDragged(e: NativeMouseEvent) {
        if (isRecording) {
            events.add("Mouse Dragged: ${e.x}, ${e.y}")
        }
    }
}