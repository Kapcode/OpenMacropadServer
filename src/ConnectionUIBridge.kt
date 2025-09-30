//purpose is to bridge the gap between the UI and the connection library I have not created yet.
//this will also handle communication between usb and bluetooth as well as Wi-Fi. (the network library will, this is the stand in for that)
//UI will communicate with this interface


interface ConnectionUIBridge {
    fun startListening()
    fun stopListening()
    fun sendData(data: ByteArray)
    fun isListening(): Boolean
    fun getConnectedClients(): List<String> // or return client info
    fun disconnectClient(clientId: String)
    // Add callback/listener for when clients connect/disconnect
    fun setConnectionListener(listener: ConnectionListener)
}

interface ConnectionListener {
    fun onClientConnected(clientId: String)
    fun onClientDisconnected(clientId: String)
    fun onDataReceived(clientId: String, data: ByteArray)
    fun onError(error: String)
}


abstract class Wifi : ConnectionUIBridge {
    protected var listener: ConnectionListener? = null
    protected val connectedClients = mutableSetOf<String>()

    override fun setConnectionListener(listener: ConnectionListener) {//runs on connect or disconnect
        this.listener = listener
    }

    override fun getConnectedClients(): List<String> {
        return connectedClients.toList()
    }

    protected fun handleNewConnection(clientId: String) {
        connectedClients.add(clientId)
        listener?.onClientConnected(clientId)
    }

    protected fun handleDisconnection(clientId: String) {
        connectedClients.remove(clientId)
        listener?.onClientDisconnected(clientId)
    }

    override fun disconnectClient(clientId: String) {
        if (connectedClients.contains(clientId)) {
            // Subclass should implement actual disconnection logic
            handleDisconnection(clientId)
        }
    }

    // Then in your subclasses, you can call the listener methods like:
    // listener?.onClientConnected(clientId)
    // listener?.onDataReceived(clientId, data)
}