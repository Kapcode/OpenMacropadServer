# Open Macropad Server - Development Notes

This document outlines significant development challenges encountered during the project's iterative refinement, along with their solutions. These notes serve as a reference for understanding specific design decisions and workarounds implemented.

## 1. Dynamic Font Sizing and Text Truncation in Macro Items

**Challenge:** Initially, `JLabel`s within `MacroItem`s would display an ellipsis (...) or clip text when the container was too small, even with attempts to dynamically resize fonts. The `JLabel`'s internal rendering logic often preempted custom font adjustments. Inconsistent font styles (bold vs. plain) also arose during resizing.

**Solution:**
*   **Custom `DynamicFontSizeLabel`**: A custom `JLabel` subclass was created (`UI/DynamicFontSizeLabel.kt`). This class overrides `paintComponent(g: Graphics)` and takes full control of text rendering. It explicitly calculates the required font size (down to a `minSize`) to fit the available width, *preserving the original font style* (`font.deriveFont(font.style, newFontSize)`), and then manually draws the string using `g.drawString()`. This bypasses `JLabel`'s default truncation behavior.
*   **Layout Refinement**: The `MacroItem`'s internal layout was changed from `BoxLayout` to `BorderLayout` to ensure the `keyLabel` and `commandLabel` received appropriate horizontal space, preventing premature shrinking.

## 2. JNativeHook Modifier Key Detection (Unresolved References)

**Challenge:** When implementing global hotkey triggers, attempts to use `NativeKeyEvent.VC_CONTROL_L`, `VC_SHIFT_L`, etc., resulted in `Unresolved reference` compilation errors. The specific `JNativeHook` version in use did not expose these constants directly.

**Solution:**
*   **Integer Literals for Modifier Keys**: In `ActiveMacroManager.kt`, the `checkForTriggers` method was modified to use hardcoded integer literals for the left and right modifier key codes (e.g., `29` for Left Ctrl, `157` for Right Ctrl). These values were determined through testing or `JNativeHook` documentation for the specific environment. A `Technical Notes` section was added to `README.md` to document this workaround.

## 3. `java.awt.Robot` Timing Issues (e.g., Ctrl+V typing 'v')

**Challenge:** Key combinations involving modifier keys (like `Ctrl+V` for paste) would often fail, resulting in only the character key being typed (e.g., 'v'). This occurred because the `Robot` was sending key events too quickly for the operating system to register the modifier key as "down" before the character key was pressed. Initial attempts with `robot.delay()` were insufficient or inconsistent.

**Solution:**
*   **`robot.isAutoWaitForIdle = true`**: The most robust solution was to set `robot.isAutoWaitForIdle = true` on the `Robot` instance in `MacroPlayer.kt`. This forces the `Robot` to wait for the operating system's event queue to be idle after each event, guaranteeing that modifier keys are processed before subsequent key presses. All manual `robot.delay()` calls for key events were removed.

## 4. Tab Title Editing and Tab Switching Interference

**Challenge:** Implementing editable tab titles using a `JTextField` within the `JTabbedPane`'s custom tab component (`TabTitle`) caused several issues:
    *   Single clicks on the tab title would not reliably switch tabs.
    *   Double-click to edit was inconsistent or non-functional.
    *   The `JTextField` was initially non-editable due to `isFocusable = false`.

**Solution:**
*   **Explicit "Edit" Button**: The double-click-to-edit functionality was replaced with an explicit "Edit" `JButton` placed next to the tab's close button. This provides a clear user action and avoids event consumption conflicts.
*   **`JTextField` Focusability**: The `textField.isFocusable = false` was removed from `TabTitle`'s `init` block to ensure the `JTextField` could receive focus and input when visible.
*   **Event Handling**: `MouseListeners` were carefully managed to ensure single clicks on the tab header are handled by the `JTabbedPane` for selection, while the "Edit" button's `ActionListener` triggers the rename.

## 5. "New Document" Button and `JTabbedPane` Crash (`ArrayIndexOutOfBoundsException`)

**Challenge:** Attempting to add/remove a "New Document" button directly to/from the `JTabbedPane` when no tabs were open led to `ArrayIndexOutOfBoundsException` and other layout issues. The `JTabbedPane` is not designed to have arbitrary components added to its main area.

**Solution:**
*   **`CardLayout` for `TabbedUI`**: The `TabbedUI` component (which extends `JPanel`) was refactored to use a `CardLayout`.
    *   One "card" holds the actual `JTabbedPane`.
    *   Another "card" holds a `JPanel` containing the centered "New Document" button.
*   **Delegation**: `TabbedUI` now delegates `addTab`, `remove`, `tabCount`, `selectedComponent`, etc., to its internal `JTabbedPane` instance. The `checkShowNewDocumentButton` method simply switches between the "Tabs" and "NewButton" cards.
*   **Explicit `this@TabbedUI.remove(i)`**: In the `TabButton`'s `mouseClicked` listener, calls to `remove(i)` were explicitly qualified as `this@TabbedUI.remove(i)` to ensure the overridden method in `TabbedUI` (which correctly delegates to the internal `JTabbedPane`) is called, preventing `ArrayIndexOutOfBoundsException`.

## 6. MacroBar Layout and Visibility

**Challenge:** The `MacroBar` (containing macro items) and the JSON editor were not always visible or correctly sized, often being squashed or disappearing. This was due to complex interactions between `JSplitPane`s and `BoxLayout`s.

**Solution:**
*   **Simplified `MacroJsonEditorUI` Layout**: The `JSplitPane` separating the JSON editor and `MacroBar` within `MacroJsonEditorUI` was removed. Instead, a simple `JPanel` with `BorderLayout` was used: the `RTextScrollPane` (editor) in `CENTER` and the `MacroBar` in `SOUTH`.
*   **Explicit Sizing for `MacroBar`**: `MacroBar` was given explicit `minimumSize` and `preferredSize` to ensure it demands and receives adequate vertical space from its parent layout.
*   **`MacroBar`'s Internal Layout**: The `MacroBar` itself was set to `BorderLayout` to correctly position the `triggerSlot` (WEST) and the `itemsPanelWithToolbar` (CENTER), allowing the latter to expand horizontally.

## 7. Drag-and-Drop Refinements

**Challenge:** Initial drag-and-drop implementation suffered from:
    *   `NotSerializableException`: Attempting to transfer entire `MacroItem` components.
    *   Inconsistent Drop Indicator: The red line would get stuck or not accurately reflect the drop position.
    *   Lack of Visual Feedback: No ghost image, and the drop zone highlight was incorrect or non-existent.
    *   Incorrect Drop Zones: Dropping directly onto items was allowed, and the visual feedback for valid drop areas was poor.

**Solution:**
*   **Index-Based Transfer**: The `TransferHandler` was refactored to transfer only the integer index of the `DraggableMacroItem`, resolving serialization issues.
*   **`getDragImage()` Implementation**: The `getDragImage()` method was correctly implemented in `MacroItemTransferHandler` to create a `BufferedImage` of the dragged component, providing the ghost image.
*   **Unified Drop Logic**: A single `getTargetIndex(support: TransferSupport)` helper function was created to consistently calculate the insertion index for both `canImport` (visual feedback) and `importData` (actual drop).
*   **Dedicated Drop Zone**: A `dropZoneHeight` (30 pixels) was defined at the top of the `macroItemsPanel`.
    *   `canImport` now returns `false` (no-drop zone) if `dropPoint.y > dropZoneHeight`.
    *   The `paintComponent` of `macroItemsPanel` was modified to draw a distinct highlight rectangle only within this `dropZoneHeight` area when `isDragInProgress` is true, providing clear visual feedback.
    *   `macroItemsPanel.isOpaque = true` was set to ensure background painting.

## 8. Key Input Parsing for "COMMA"

**Challenge:** The `validateAndCleanKeys` function in `NewEventDialog.kt` was too aggressive in filtering empty strings after splitting by commas, making it impossible to specify a literal "COMMA" key. `ctrl,,` would become `["ctrl"]` instead of `["ctrl", "COMMA"]`.

**Solution:**
*   **Clear Separator Rule**: A new rule was established: both spaces and commas are treated *only* as separators. To specify a literal comma key, the user must type the word "COMMA".
*   **Refined `validateAndCleanKeys`**: The function was updated to replace all commas with spaces, then split by spaces, and finally filter out empty tokens. This ensures that `ctrl,alt` and `ctrl alt` both result in `["ctrl", "alt"]`, and `ctrl comma` is required for `["ctrl", "COMMA"]`.

## 9. "New Trigger" Button Default State

**Challenge:** When clicking the "New Trigger" button, the `NewEventDialog` would open, but the "Is Trigger" checkbox and "ON-RELEASE" command were not pre-selected, requiring extra user steps.

**Solution:**
*   **`isTriggerDefault` Constructor Parameter**: `NewEventDialog` was given an `isTriggerDefault: Boolean = false` constructor parameter.
*   **Conditional Pre-selection**: In `NewEventDialog.kt`, if `isTriggerDefault` is true, the "Is Trigger" checkbox is selected, and the `keyCommandComboBox` is set to "ON-RELEASE" upon dialog initialization.
*   **`MacroBar` Integration**: The `newTriggerButton`'s `ActionListener` in `MacroBar.kt` now passes `isTriggerDefault = true` to the `NewEventDialog` constructor.
