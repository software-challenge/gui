package sc.gui

import sc.server.network.NewClientListener

const val serverAddress = "localhost"

val serverPort: Int
    get() = NewClientListener.lastUsedPort
