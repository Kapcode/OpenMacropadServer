# Open Macropad Server

A server application for the Open Macropad device, allowing users to record, manage, and execute macros.

## UI Structure

The user interface is built using Java Swing and is organized into several key components:

*   **`Main.kt`**: The main entry point of the application. It initializes the window, menu bar, and assembles all the different UI panels into a nested layout using `JSplitPane`.

*   **`ServerStatusUI.kt`**: A panel at the top of the window that displays the server's status (running/stopped), IP address, and port.

*   **`ConsoleUI.kt`**: A panel that provides a console for viewing server messages, client connections, and for sending raw data to connected devices.

*   **`ConnectedDevicesUI.kt`**: A panel that lists all the clients currently connected to the server.

*   **`MacroManagerUI.kt`**: A panel for viewing, playing, editing, and deleting saved macro files (which are stored as `.json` files in the `macros/` directory).

*   **`TabbedUI.kt`**: A container that holds different editor views in tabs.
    *   **`MacroJsonEditorUI.kt`**: The primary tab, which provides a text editor for viewing and editing the JSON structure of a macro. It also contains the `MacroBar`.
    *   **`MacroBar.kt`**: A visual representation of the macro's steps, displayed as a sequence of items (`MacroKeyItem`, `MacroMouseItem`).
