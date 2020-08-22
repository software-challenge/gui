package sc.gui

import org.slf4j.LoggerFactory
import sc.networking.clients.IControllableGame
import sc.networking.clients.ILobbyClientListener
import sc.networking.clients.LobbyClient
import sc.plugin2021.AbstractClient
import sc.plugin2021.GamePlugin
import sc.server.Configuration
import sc.shared.SlotDescriptor
import java.net.ConnectException
import kotlin.system.exitProcess

class ControllingClient {

    var control: IControllableGame? = null

    private val client: LobbyClient

    constructor(host: String, port: Int, clientOne: AbstractClient, clientTwo: AbstractClient, listener: ILobbyClientListener) {
        try {
            client = LobbyClient(Configuration.getXStream(), sc.plugin2021.util.Configuration.classesToRegister, host, port)
        } catch (e: ConnectException) {
            logger.error("Could not connect to Server: " + e.message)
            exitProcess(1)
        }
        client.start()
        client.addListener(listener)
        client.authenticate(Configuration.get(Configuration.PASSWORD_KEY))
        val requestResult = client.prepareGameAndWait(
                GamePlugin.PLUGIN_UUID,
                SlotDescriptor("One", false, true),
                SlotDescriptor("Two", false, true)
        )

        if (requestResult.isSuccessful) {
            val preparation = requestResult.result!!
            control = client.observeAndControl(preparation)
            clientOne.joinPreparedGame(preparation.reservations[0])
            clientTwo.joinPreparedGame(preparation.reservations[1])
        } else {
            logger.error("Could not prepare game!" + requestResult.error)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ControllingClient::class.java)
    }

    fun nextStep() {
        control?.next()
    }
}