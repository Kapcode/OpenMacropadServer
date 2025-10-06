import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeInputEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener
import org.json.JSONArray
import org.json.JSONObject

class MacroRecorder : NativeKeyListener, NativeMouseInputListener {

    private val events = mutableListOf<String>()

    fun startRecording() {
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(this)
        GlobalScreen.addNativeMouseListener(this)
        GlobalScreen.addNativeMouseMotionListener(this)
    }

    fun stopRecording() {
        GlobalScreen.removeNativeKeyListener(this)
        GlobalScreen.removeNativeMouseListener(this)
        GlobalScreen.removeNativeMouseMotionListener(this)
        GlobalScreen.unregisterNativeHook()
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

    override fun nativeKeyPressed(e: NativeKeyEvent) {
        events.add("Key Pressed: ${NativeKeyEvent.getKeyText(e.keyCode)}")
    }

    override fun nativeKeyReleased(e: NativeKeyEvent) {
        events.add("Key Released: ${NativeKeyEvent.getKeyText(e.keyCode)}")
    }

    override fun nativeKeyTyped(e: NativeKeyEvent) {
        // Not used
    }

    override fun nativeMouseClicked(e: NativeMouseEvent) {
        events.add("Mouse Clicked: ${e.x}, ${e.y}")
    }

    override fun nativeMousePressed(e: NativeMouseEvent) {
        events.add("Mouse Pressed: ${e.x}, ${e.y}")
    }

    override fun nativeMouseReleased(e: NativeMouseEvent) {
        events.add("Mouse Released: ${e.x}, ${e.y}")
    }

    override fun nativeMouseMoved(e: NativeMouseEvent) {
        events.add("Mouse Moved: ${e.x}, ${e.y}")
    }

    override fun nativeMouseDragged(e: NativeMouseEvent) {
        events.add("Mouse Dragged: ${e.x}, ${e.y}")
    }
}