package sc.gui

import org.slf4j.LoggerFactory
import sc.networking.clients.IControllableGame
import sc.networking.clients.ILobbyClientListener
import sc.networking.clients.LobbyClient
import sc.plugin2021.AbstractClient
import sc.plugin2021.GamePlugin
import sc.plugin2021.IGameHandler
import sc.server.Configuration
import sc.shared.SlotDescriptor
import java.net.ConnectException
import kotlin.system.exitProcess

class ControllingClient(host: String, port: Int, playerOne: AbstractClient, playerTwo: AbstractClient, listener: ILobbyClientListener) {

    var game: IControllableGame? = null

    private val control: LobbyClient = try {
        LobbyClient(Configuration.getXStream(), sc.plugin2021.util.Configuration.classesToRegister, host, port)
    } catch (e: ConnectException) {
        logger.error("Could not connect to Server: " + e.message)
        exitProcess(1)
    }

    init {
        control.start()
        control.addListener(listener)
        control.authenticate(Configuration.get(Configuration.PASSWORD_KEY))
        val requestResult = control.prepareGameAndWait(
                GamePlugin.PLUGIN_UUID,
                SlotDescriptor("One", false, false),
                SlotDescriptor("Two", false, false)
        )

        if (requestResult.isSuccessful) {
            val preparation = requestResult.result!!
            game = control.observeAndControl(preparation)
            playerOne.joinPreparedGame(preparation.reservations[0])
            playerTwo.joinPreparedGame(preparation.reservations[1])
            game!!.unpause()
        } else {
            logger.error("Could not prepare game!" + requestResult.error)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ControllingClient::class.java)
    }
}