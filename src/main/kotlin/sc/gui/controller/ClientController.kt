package sc.gui.controller

import org.slf4j.LoggerFactory
import sc.framework.plugins.Player
import sc.gui.ControllingClient
import sc.gui.TestClient
import sc.gui.TestGameHandler
import sc.gui.model.BoardModel
import sc.networking.clients.ILobbyClientListener
import sc.networking.clients.IUpdateListener
import sc.networking.clients.LobbyClient
import sc.plugin2021.*
import sc.plugin2021.util.GameRuleLogic
import sc.protocol.responses.PrepareGameProtocolMessage
import sc.protocol.responses.ProtocolErrorMessage
import sc.server.Configuration
import sc.shared.GameResult
import tornadofx.Controller

// This is the listener to update the global state of the server (lobby)
class UILobbyListener() : ILobbyClientListener {
    companion object {
        val logger = LoggerFactory.getLogger(ClientController::class.java)
    }

    override fun onNewState(roomId: String?, state: Any?) {
        logger.debug("listener: onNewState")
        val gameState = state as GameState
        logger.debug("This is what I got: " + gameState.toString())
    }

    override fun onError(roomId: String?, error: ProtocolErrorMessage?) {
        logger.debug("listener: onError")
    }

    override fun onRoomMessage(roomId: String?, data: Any?) {
        logger.debug("listener: onRoomMessage")
    }

    override fun onGamePrepared(response: PrepareGameProtocolMessage?) {
        logger.debug("listener: onGamePrepared")
    }

    override fun onGameLeft(roomId: String?) {
        logger.debug("listener: onGameLeft")
    }

    override fun onGameJoined(roomId: String?) {
        logger.debug("listener: onGameJoined")
    }

    override fun onGameOver(roomId: String?, data: GameResult?) {
        logger.debug("listener: onGameOver")
    }

    override fun onGamePaused(roomId: String?, nextPlayer: Player?) {
        logger.debug("listener: onGamePaused")
    }

    override fun onGameObserved(roomId: String?) {
        logger.debug("listener: onGameObserved")
    }
}

class UIGameListener(val onUpdateHandler: () -> Unit): IUpdateListener {
    companion object {
        val logger = LoggerFactory.getLogger(ClientController::class.java)
    }

    override fun onUpdate(p0: Any?) {
        logger.debug("game listener: onUpdate")
        onUpdateHandler()
    }

    override fun onError(p0: String?) {
        logger.debug("game listener: onError")
    }

}

class ClientController : Controller() {

    companion object {
        val logger = LoggerFactory.getLogger(ClientController::class.java)
    }

    val boardModel: BoardModel by inject()
    var controllingClient: ControllingClient? = null
    val listener: UIGameListener = UIGameListener(::newGameState)

    fun startGame() {
        val host = "localhost"
        val port = 13050
        logger.debug("creating and observing")
        // NOTE that testClientOne and testClientTwo are currently *internal* clients to wire the logic of the GameHandlers to the server. When external clients should join the game, these are not needed.
        val testClientOne = TestClient(PlayerType.PLAYER_ONE, host, port)
        val testClientTwo = TestClient(PlayerType.PLAYER_TWO, host, port)
        controllingClient = ControllingClient(host, port, testClientOne, testClientTwo, listener)
    }

    fun newGameState() {
        logger.debug("ClientController got new update")
        val gameControl = controllingClient?.game
        if (gameControl != null) {
            val gameState = gameControl!!.currentState as GameState
            if (gameState != null) {
                logger.debug("gamestate is " + gameState)
                boardModel.setField(0, 0, Field(Coordinates(0, 0), FieldContent.GREEN))
            } else {
                logger.debug("no gamestate, but " + gameControl.toString())
            }
        } else {
            logger.debug("no controlling client yet")
        }
    }
}
