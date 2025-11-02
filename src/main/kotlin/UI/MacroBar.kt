package UI

import java.awt.* 
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.beans.PropertyChangeListener
import javax.swing.*
import java.awt.image.BufferedImage

class MacroBar(private val frame: JFrame, private val tabbedUI: TabbedUI) : JPanel() {

    private val toolbar = ToolBarUI()
    private var dropLineX: Int? = null
    private val dropZoneHeight = 30 // The height of the drop zone at the top
    private var isDragInProgress = false

    val macroItemsPanel = object : JPanel() {
        override fun paintComponent(g: Graphics) {
            // Set background based on drag state
            background = if (isDragInProgress) {
                Theme().SecondaryBackgroundColor.brighter()
            } else {
                Theme().SecondaryBackgroundColor
            }
            super.paintComponent(g)

            dropLineX?.let {
                g.color = Color.RED
                // Draw the line only within the top drop zone area
                g.fillRect(it - 1, 0, 2, dropZoneHeight)
            }
        }
    }.apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        isOpaque = true // Ensure the panel paints its background
        // Create a physical drop zone at the top with an empty border
        border = BorderFactory.createEmptyBorder(dropZoneHeight, 0, 0, 0)
    }

    private val triggerSlot = JPanel(BorderLayout())
    private val transferHandler = MacroItemTransferHandler()

    init {
        val theme = Theme()
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        background = theme.SecondaryBackgroundColor

        triggerSlot.border = BorderFactory.createTitledBorder("Trigger")
        triggerSlot.transferHandler = transferHandler
        triggerSlot.preferredSize = Dimension(200, 0)
        triggerSlot.maximumSize = Dimension(200, Integer.MAX_VALUE)
        add(triggerSlot)

        val itemsPanelWithToolbar = JPanel(BorderLayout())
        
        val newEventIcon = SvgIconRenderer.getIcon("/add-file-icon.svg", 24, 24)
        if (newEventIcon != null) {
            toolbar.addButton(newEventIcon, "New Event") { 
                val focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner
                val wasEditorInFocus = focusOwner is JTextArea

                val selectedComponent = tabbedUI.selectedComponent
                if (selectedComponent is MacroJsonEditorUI) {
                    val dialog = NewEventDialog(frame)
                    dialog.isVisible = true
                    dialog.createdEvent?.let { event ->
                        selectedComponent.insertNewEvent(event, dialog.isTriggerEvent, wasEditorInFocus)
                    }
                }
            }
        }

        val recordIcon = SvgIconRenderer.getIcon("/green-circle-shape-icon.svg", 24, 24)
        val stopIcon = SvgIconRenderer.getIcon("/red-circle-shape-icon.svg", 24, 24)
        val recordButton = if (recordIcon != null) {
            ToolBarButton(recordIcon, "Start Recording") {}
        } else {
            ToolBarButton("Record", "Start Recording") {}
        }
        var isRecording = false
        recordButton.addActionListener { 
            isRecording = !isRecording
            if (isRecording) {
                if (stopIcon != null) recordButton.setIcon(stopIcon)
                recordButton.setToolTipText("Stop Recording")
            } else {
                if (recordIcon != null) recordButton.setIcon(recordIcon)
                recordButton.setToolTipText("Start Recording")
            }
        }
        toolbar.add(recordButton)

        val undoIcon = SvgIconRenderer.getIcon("/undo-circle-outline-icon.svg", 24, 24)
        if (undoIcon != null) {
            toolbar.addButton(undoIcon, "Undo last action") {}
        }

        val redoIcon = SvgIconRenderer.getIcon("/redo-circle-outline-icon.svg", 24, 24)
        if (redoIcon != null) {
            toolbar.addButton(redoIcon, "Redo last action") {}
        }

        itemsPanelWithToolbar.add(toolbar, BorderLayout.NORTH)

        val scrollPane = JScrollPane(macroItemsPanel).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER
            border = null
            horizontalScrollBar.unitIncrement = 20
        }
        itemsPanelWithToolbar.add(scrollPane, BorderLayout.CENTER)
        add(itemsPanelWithToolbar)

        macroItemsPanel.transferHandler = transferHandler
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        super.addPropertyChangeListener(listener)
    }

    fun addMacroItem(item: MacroItem) {
        item.transferHandler = transferHandler
        macroItemsPanel.add(item)
    }

    fun setTriggerItem(item: MacroItem) {
        triggerSlot.removeAll()
        item.transferHandler = transferHandler
        triggerSlot.add(item, BorderLayout.CENTER)
        triggerSlot.revalidate()
        triggerSlot.repaint()
    }

    fun getTriggerItem(): MacroItem? {
        return if (triggerSlot.componentCount > 0) triggerSlot.getComponent(0) as? MacroItem else null
    }

    fun clear() {
        macroItemsPanel.removeAll()
        triggerSlot.removeAll()
        revalidate()
        repaint()
    }

    fun notifyItemsReordered() {
        firePropertyChange("component.reordered", null, null)
    }

    inner class MacroItemTransferHandler : TransferHandler() {

        private val indexFlavor = DataFlavor(Int::class.java, "java/lang/Integer")
        private var sourceComponent: JComponent? = null

        private fun getTargetIndex(support: TransferSupport): Int {
            val dropPoint = support.dropLocation.dropPoint
            var targetIndex = 0
            for (i in 0 until macroItemsPanel.componentCount) {
                val component = macroItemsPanel.getComponent(i)
                if (dropPoint.x > component.x + component.width / 2) {
                    targetIndex = i + 1
                }
            }
            return targetIndex
        }

        override fun getSourceActions(c: JComponent): Int {
            sourceComponent = c
            isDragInProgress = true
            macroItemsPanel.repaint()
            return MOVE
        }

        override fun createTransferable(c: JComponent): Transferable? {
            val index = macroItemsPanel.components.indexOf(c)
            if (index != -1) {
                return object : Transferable {
                    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(indexFlavor)
                    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = flavor.equals(indexFlavor)
                    override fun getTransferData(flavor: DataFlavor): Any = index
                }
            }
            return null
        }

        override fun getDragImage(): Image? {
            val c = sourceComponent ?: return null
            val image = BufferedImage(c.width, c.height, BufferedImage.TYPE_INT_ARGB)
            val g2 = image.createGraphics()
            c.paint(g2)
            g2.dispose()
            return image
        }

        override fun canImport(support: TransferSupport): Boolean {
            if (!support.isDrop || !support.isDataFlavorSupported(indexFlavor)) {
                return false
            }

            val dropPoint = support.dropLocation.dropPoint

            // Check if the drop is in the valid drop zone (the top border area)
            if (dropPoint.y > dropZoneHeight) {
                dropLineX = null
                macroItemsPanel.repaint()
                return false // It's a no-drop zone
            }

            val targetIndex = getTargetIndex(support)
            
            if (targetIndex < macroItemsPanel.componentCount) {
                dropLineX = macroItemsPanel.getComponent(targetIndex).x
            } else {
                if (macroItemsPanel.componentCount > 0) {
                    val lastComp = macroItemsPanel.getComponent(macroItemsPanel.componentCount - 1)
                    dropLineX = lastComp.x + lastComp.width
                } else {
                    dropLineX = 0 // If panel is empty
                }
            }

            macroItemsPanel.repaint()
            return true
        }

        override fun importData(support: TransferSupport): Boolean {
            if (!canImport(support)) {
                return false
            }

            val sourceIndex = try {
                support.transferable.getTransferData(indexFlavor) as Int
            } catch (e: Exception) {
                return false
            }

            val comp = macroItemsPanel.getComponent(sourceIndex) as? DraggableMacroItem ?: return false
            val targetIndex = getTargetIndex(support)

            if (sourceIndex == targetIndex) {
                return false // No change
            }

            macroItemsPanel.remove(comp)
            val newIndex = if (sourceIndex < targetIndex) targetIndex - 1 else targetIndex
            macroItemsPanel.add(comp, newIndex)

            macroItemsPanel.revalidate()
            macroItemsPanel.repaint()
            notifyItemsReordered()
            return true
        }

        override fun exportDone(source: JComponent, data: Transferable, action: Int) {
            dropLineX = null
            isDragInProgress = false
            macroItemsPanel.repaint()
            sourceComponent = null
        }
    }
}