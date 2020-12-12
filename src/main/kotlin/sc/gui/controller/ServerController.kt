package sc.gui.controller

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import sc.server.Configuration
import sc.server.Lobby
import tornadofx.*
import java.io.IOException
import org.slf4j.LoggerFactory


class ServerController : Controller() {
    private val server = Lobby()

    fun startServer() {
        // output logback diagnostics to see if a logback.xml config was found
        val  lc = LoggerFactory.getILoggerFactory() as LoggerContext
        StatusPrinter.print(lc)

        Configuration.loadServerProperties()
        try {
            server.start()
            // TODO get address & port from server
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
    }

    fun stopServer() {
        server.close()
    }
}
