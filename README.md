# Open Macropad Server

A server application for the Open Macropad device, allowing users to record, manage, and execute macros.

## Features

*   **Macro Management**: Create, save, edit, and delete macros through a user-friendly interface.
*   **Live Directory Watching**: The macro manager automatically updates when you add, remove, or change macro files on disk.
*   **Editable Tab Titles**: Double-click a tab's title to rename it for better organization.
*   **Intelligent Saving**: The application intelligently handles "Save" and "Save As" operations, using the tab title as a suggested filename and preventing accidental overwrites.
*   **Batch Deletion**: Easily select and delete multiple macros at once.
*   **Themed UI**: A modern, themed interface for a consistent look and feel.

## UI Structure

The user interface is built using Java Swing and is organized into several key components:

*   **`Main.kt`**: The main entry point of the application. It initializes the window, menu bar, and assembles all the different UI panels into a nested layout using `JSplitPane`.

*   **`ServerStatusUI.kt`**: A panel at the top of the window that displays the server's status (running/stopped), IP address, and port.

*   **`ConsoleUI.kt`**: A panel that provides a console for viewing server messages, client connections, and for sending raw data to connected devices.

*   **`ConnectedDevicesUI.kt`**: A panel that lists all the clients currently connected to the server.

*   **`MacroManagerUI.kt`**: A powerful panel for managing your macros. It lists all available `.json` files from the macro directory (`Documents/OpenMacropadServer/Macros` by default). From here, you can:
    *   **Edit**: Open a macro in a new tab.
    *   **Delete**: Remove a single macro (with confirmation).
    *   **Batch Delete**: Enter a selection mode to remove multiple macros at once.

*   **`TabbedUI.kt`**: A container that holds different editor views in tabs. Each tab has a close button and an editable title.
    *   **`MacroJsonEditorUI.kt`**: The primary tab, which provides a text editor for viewing and editing the JSON structure of a macro. It also contains the `MacroBar`.
    *   **`MacroBar.kt`**: A visual representation of the macro's steps, displayed as a sequence of items (`MacroKeyItem`, `MacroMouseItem`). It also features a toolbar with "Record", "Undo", and "Redo" buttons.

*   **Toolbars**: The application features several toolbars for quick access to common actions:
    *   **Macro Manager Toolbar**: "Add" and "Remove" buttons for managing macros.
    *   **Editor Toolbar**: "Save", "Save As", "Undo", and "Redo" buttons for the active editor tab.

![Screen-Shot-Of-Application](image_url)
