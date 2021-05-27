package sc.gui.controller

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import org.slf4j.LoggerFactory
import sc.server.Configuration
import sc.server.Lobby
import tornadofx.Controller

class ServerController : Controller() {
    private val server = Lobby()

    fun startServer() {
        // output logback diagnostics to see if a logback.xml config was found
        val lc = LoggerFactory.getILoggerFactory() as LoggerContext
        StatusPrinter.print(lc)

        Configuration.loadServerProperties()
        Configuration.set(Configuration.SAVE_REPLAY, true)
        server.start()
        // TODO get address & port from server
        // TODO do we have to communicate via network at all?
    }

    fun stopServer() {
        server.close()
    }
}
