import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener
import org.json.JSONArray
import org.json.JSONObject
import java.awt.Robot
import java.awt.event.InputEvent

class MacroRecorder : NativeKeyListener, NativeMouseInputListener {

    private val events = mutableListOf<String>()
    private var isRecording = false

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
                    KeyMap.awtKeyCodeMap[keyText]?.let { robot.keyPress(it) }
                }
                "key_release" -> {
                    val keyText = eventObject.get("key") as String
                    KeyMap.awtKeyCodeMap[keyText]?.let { robot.keyRelease(it) }
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

    fun convertTextToMacro(macroText: String): String {
        val eventsArray = JSONArray()
        val shiftChars = "~!@#$%^&*()_+{}|:\"<>?"

        for (char in macroText) {
            val isUpperCase = char.isUpperCase()
            val needsShift = isUpperCase || shiftChars.contains(char)

            val keyText = KeyMap.charToKeyTextMap[char.lowercaseChar()] ?: when (char) {
                '~' -> "Back Quote"
                '!' -> "1"
                '@' -> "2"
                '#' -> "3"
                '$' -> "4"
                '%' -> "5"
                '^' -> "6"
                '&' -> "7"
                '*' -> "8"
                '(' -> "9"
                ')' -> "0"
                '_' -> "Minus"
                '+' -> "Equals"
                '{' -> "Open Bracket"
                '}' -> "Close Bracket"
                '|' -> "Back Slash"
                ':' -> "Semicolon"
                '\"' -> "Quote"
                '<' -> "Comma"
                '>' -> "Period"
                '?' -> "Slash"
                else -> null
            }

            if (keyText != null) {
                if (needsShift) {
                    val shiftPress = JSONObject()
                    shiftPress.put("type", "key_press")
                    shiftPress.put("key", "Shift")
                    eventsArray.put(shiftPress)
                }

                val pressEvent = JSONObject()
                pressEvent.put("type", "key_press")
                pressEvent.put("key", keyText)
                eventsArray.put(pressEvent)

                val releaseEvent = JSONObject()
                releaseEvent.put("type", "key_release")
                releaseEvent.put("key", keyText)
                eventsArray.put(releaseEvent)

                if (needsShift) {
                    val shiftRelease = JSONObject()
                    shiftRelease.put("type", "key_release")
                    shiftRelease.put("key", "Shift")
                    eventsArray.put(shiftRelease)
                }
            }
        }

        val json = JSONObject()
        json.put("events", eventsArray)
        return json.toString(4)
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