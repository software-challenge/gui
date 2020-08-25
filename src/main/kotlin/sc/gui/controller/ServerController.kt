package sc.gui.controller

import sc.plugin2021.Game
import sc.protocol.requests.PrepareGameRequest
import sc.server.Configuration
import sc.server.Lobby
import tornadofx.*
import java.io.IOException

class ServerController : Controller() {
    val server = Lobby()

    fun startServer() {
        Configuration.loadServerProperties()
        try {
            server.start()
            //server.gameManager.pluginManager.loadPlugin(Game.javaClass, server.gameManager.pluginApi)
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
    }

    fun stopServer() {
        // TODO
    }
}
