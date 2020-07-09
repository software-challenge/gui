package sc.gui.controller

import sc.server.Lobby
import tornadofx.Controller
import java.io.IOException

class ServerController : Controller() {
    fun startServer() {
        val server = Lobby()
        try {
            server.start()
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
    }
}
