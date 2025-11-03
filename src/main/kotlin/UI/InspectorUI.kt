package UI

import java.awt.*
import java.awt.image.BufferedImage
import javax.swing.*
import javax.swing.Timer

class InspectorUI : JPanel() {

    private val locationLabel = JLabel("Location: (X, Y)")
    private val argbLabel = JLabel("ARGB: (A, R, G, B)")
    private val hexLabel = JLabel("Hex: #RRGGBB")
    private val colorSwatch = JPanel()
    private val screenshotLabel = JLabel()
    private val captureHotkeyComboBox: JComboBox<String>
    private val liveUpdateCheckBox: JCheckBox

    private var captureTimer: Timer
    private var isFrozen = false

    init {
        layout = GridBagLayout()
        border = BorderFactory.createTitledBorder("Inspector")

        val gbc = GridBagConstraints()
        gbc.insets = Insets(2, 5, 2, 5)
        gbc.anchor = GridBagConstraints.WEST

        // --- Row 0: Location ---
        gbc.gridx = 0
        gbc.gridy = 0
        add(locationLabel, gbc)

        // --- Row 1: ARGB Color ---
        gbc.gridy = 1
        add(argbLabel, gbc)

        // --- Row 2: Hex Color ---
        gbc.gridy = 2
        add(hexLabel, gbc)

        // --- Row 3: Color Swatch & Screenshot ---
        gbc.gridy = 3
        gbc.gridwidth = 2
        val previewPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        colorSwatch.preferredSize = Dimension(24, 24)
        colorSwatch.border = BorderFactory.createLineBorder(Color.BLACK)
        previewPanel.add(colorSwatch)
        screenshotLabel.border = BorderFactory.createLineBorder(Color.BLACK)
        previewPanel.add(screenshotLabel)
        add(previewPanel, gbc)
        gbc.gridwidth = 1 // Reset gridwidth

        // --- Row 4: Capture Hotkey & Live Update ---
        gbc.gridy = 4
        val controlPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        controlPanel.add(JLabel("Capture Hotkey:"))
        captureHotkeyComboBox = JComboBox(arrayOf("F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8"))
        controlPanel.add(captureHotkeyComboBox)

        liveUpdateCheckBox = JCheckBox("Live Update", true)
        liveUpdateCheckBox.addActionListener { 
            if (liveUpdateCheckBox.isSelected) {
                captureTimer.start()
            } else {
                captureTimer.stop()
            }
        }
        controlPanel.add(liveUpdateCheckBox)
        add(controlPanel, gbc)

        // Timer to update the inspector info
        captureTimer = Timer(100) { updateInspector() }
        captureTimer.start()
    }

    private fun updateInspector() {
        if (isFrozen) return

        val robot = Robot()
        val mousePos = MouseInfo.getPointerInfo().location
        val color = robot.getPixelColor(mousePos.x, mousePos.y)

        // Update labels
        locationLabel.text = "Location: (${mousePos.x}, ${mousePos.y})"
        argbLabel.text = "ARGB: (${color.alpha}, ${color.red}, ${color.green}, ${color.blue})"
        hexLabel.text = String.format("Hex: #%02x%02x%02x", color.red, color.green, color.blue)

        // Update color swatch
        colorSwatch.background = color

        // Update screenshot
        val rect = Rectangle(mousePos.x - 12, mousePos.y - 12, 24, 24)
        val capture = robot.createScreenCapture(rect)
        screenshotLabel.icon = ImageIcon(capture.getScaledInstance(48, 48, Image.SCALE_DEFAULT))
    }

    fun toggleFreeze() {
        isFrozen = !isFrozen
    }

    fun getSelectedHotkey(): String {
        return captureHotkeyComboBox.selectedItem as String
    }
}