package sc.gui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sc.framework.plugins.Player
import sc.networking.clients.*
import sc.plugin2021.AbstractClient
import sc.plugin2021.GamePlugin
import sc.protocol.responses.PrepareGameProtocolMessage
import sc.protocol.responses.ProtocolErrorMessage
import sc.server.Configuration
import sc.shared.GameResult
import sc.shared.SlotDescriptor
import java.net.ConnectException
import java.util.*
import kotlin.system.exitProcess
import kotlin.concurrent.schedule

class LobbyListener(val logger: Logger) : ILobbyClientListener {

    var numberJoined = 0

    override fun onNewState(roomId: String?, state: Any?) {
        logger.debug("lobby: new state for $roomId")
    }

    override fun onError(roomId: String?, error: ProtocolErrorMessage?) {
        logger.debug("lobby: new error for $roomId")
    }

    override fun onRoomMessage(roomId: String?, data: Any?) {
        logger.debug("lobby: new message for $roomId")
    }

    override fun onGamePrepared(response: PrepareGameProtocolMessage?) {
        logger.debug("lobby: game was prepared")
    }

    override fun onGameLeft(roomId: String?) {
        logger.debug("lobby: $roomId game was left")
    }

    override fun onGameJoined(roomId: String?) {
        numberJoined++
        logger.debug("lobby: $roomId game was joined ($numberJoined)")
    }

    override fun onGameOver(roomId: String?, data: GameResult?) {
        logger.debug("lobby: $roomId game is over")
    }

    override fun onGamePaused(roomId: String?, nextPlayer: Player?) {
        logger.debug("lobby: $roomId game was paused")
    }

    override fun onGameObserved(roomId: String?) {
        logger.debug("lobby: $roomId game was observed")
    }

}

class AdminListener(val logger: Logger) : IAdministrativeListener {
    override fun onGamePaused(roomId: String?, nextPlayer: Player?) {
        logger.debug("admin: game paused")
    }

}

class ControllingClient(host: String, port: Int) {

    var game: IControllableGame? = null
    private lateinit var playerOne: AbstractClient
    private lateinit var playerTwo: AbstractClient
    private lateinit var listener: IUpdateListener
    private val lobbyListener: LobbyListener
    private val adminListener: AdminListener

    private val lobby: LobbyClient = try {
        LobbyClient(Configuration.getXStream(), sc.plugin2021.util.Configuration.classesToRegister, host, port)
    } catch (e: ConnectException) {
        logger.error("Could not connect to Server: " + e.message)
        exitProcess(1)
    }

    init {
        lobby.start()
        lobby.authenticate(Configuration.get(Configuration.PASSWORD_KEY))
        // these listeners are just there to see which events we get (seems like we get not many)
        lobbyListener = LobbyListener(logger)
        adminListener = AdminListener(logger)
        lobby.addListener(lobbyListener)
        lobby.addListener(adminListener)
    }

    fun startNewGame(playerOne: AbstractClient, playerTwo: AbstractClient, listener: IUpdateListener) {
        this.playerOne = playerOne
        this.playerTwo = playerTwo
        this.listener = listener
        val requestResult = lobby.prepareGameAndWait(
                GamePlugin.PLUGIN_UUID,
                SlotDescriptor("One", false, false),
                SlotDescriptor("Two", false, false)
        )

        if (requestResult.isSuccessful) {
            val preparation = requestResult.result!!
            game = lobby.observeAndControl(preparation)
            game!!.addListener(listener)
            playerOne.joinPreparedGame(preparation.reservations[0])
            playerTwo.joinPreparedGame(preparation.reservations[1])
        } else {
            logger.error("Could not prepare game!" + requestResult.error)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ControllingClient::class.java)
    }
}