package sc.gui.controller

import org.slf4j.LoggerFactory
import sc.framework.plugins.Player
import sc.gui.ControllingClient
import sc.gui.TestClient
import sc.gui.model.BoardModel
import sc.gui.model.GameCreationModel
import sc.networking.clients.ILobbyClientListener
import sc.networking.clients.IUpdateListener
import sc.plugin2021.*
import sc.protocol.responses.PrepareGameProtocolMessage
import sc.protocol.responses.ProtocolErrorMessage
import sc.shared.GameResult
import tornadofx.Controller
import tornadofx.EventBus
import tornadofx.FXEvent

// This is the listener to update the global state of the server (lobby)
class UILobbyListener() : ILobbyClientListener {
    companion object {
        val logger = LoggerFactory.getLogger(ClientController::class.java)
    }

    override fun onNewState(roomId: String?, state: Any?) {
        logger.debug("listener: onNewState")
        val gameState = state as GameState
        logger.debug("This is what I got: $gameState")
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

    override fun onUpdate(sender: Any?) {
        logger.debug("game listener: onUpdate")
        onUpdateHandler()
    }

    override fun onError(sender: String?) {
        logger.debug("game listener: onError")
    }

}

class StartGameRequest(val gameCreationModel: GameCreationModel): FXEvent(EventBus.RunOn.BackgroundThread)
class UpdateGameState(val gameState: GameState): FXEvent()

class ClientController : Controller() {

    companion object {
        val logger = LoggerFactory.getLogger(ClientController::class.java)
    }

    val boardModel: BoardModel by inject()
    var controllingClient: ControllingClient? = null
    val listener: UIGameListener = UIGameListener(::newGameState)

    // Do NOT call this directly in the UI thread, use fire(StartGameRequest(gameCreationModel)). This way, the game starting is done in the background
    fun startGame(host: String = "localhost", port: Int = 13050, gameCreationModel: GameCreationModel = GameCreationModel()) {
        // starting the game in the UI thread blocks the UI

            logger.debug("creating and observing")

            // NOTE that testClientOne and testClientTwo are currently *internal* clients to wire the logic of the GameHandlers to the server. When external clients should join the game, these are not needed.
            // TODO: implement client for HUMAN, MANUELL and COMPUTER
            val player1 = when(gameCreationModel.selectedPlayerType1.value) {
                sc.gui.model.PlayerType.HUMAN -> TestClient(PlayerType.PLAYER_ONE, host, port)
                sc.gui.model.PlayerType.MANUELL -> TestClient(PlayerType.PLAYER_ONE, host, port)
                sc.gui.model.PlayerType.COMPUTER -> TestClient(PlayerType.PLAYER_ONE, host, port)
                else -> throw Exception("invalid playerType for player 1, cannot create game")
            }
            val player2 = when(gameCreationModel.selectedPlayerType2.value) {
                sc.gui.model.PlayerType.HUMAN -> TestClient(PlayerType.PLAYER_TWO, host, port)
                sc.gui.model.PlayerType.MANUELL -> TestClient(PlayerType.PLAYER_TWO, host, port)
                sc.gui.model.PlayerType.COMPUTER -> TestClient(PlayerType.PLAYER_TWO, host, port)
                else -> throw Exception("invalid playerType for player 2, cannot create game")
            }

            controllingClient = ControllingClient(host, port)
            controllingClient!!.startNewGame(player1, player2, listener)
    }

    fun newGameState() {
        logger.debug("ClientController got new update")
        val gameControl = controllingClient?.game
        if (gameControl != null) {
            val gameState = gameControl.currentState as? GameState
            if (gameState != null) {
                logger.debug("gamestate is $gameState")
                fire(UpdateGameState(gameState))
            } else {
                logger.debug("no gamestate, but $gameControl")
            }
        } else {
            logger.debug("no controlling client yet")
        }
    }

    fun updateGameState() {
        val gameState = controllingClient?.game?.currentState as? GameState
        if (gameState != null) {
            logger.debug("gamestate is $gameState")
            fire(UpdateGameState(gameState))
        }
    }

    fun previous() {
        controllingClient?.game?.previous()
        updateGameState()
    }

    fun next() {
        controllingClient?.game?.next()
        updateGameState()
    }
}
