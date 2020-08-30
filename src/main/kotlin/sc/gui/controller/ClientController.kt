package sc.gui.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sc.framework.plugins.Player
import sc.gui.ControllingClient
import sc.gui.HumanClient
import sc.gui.TestClient
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
class UILobbyListener : ILobbyClientListener {
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

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ClientController::class.java)
    }
}

class UIGameListener(val onUpdateHandler: () -> Unit) : IUpdateListener {
    override fun onUpdate(sender: Any?) {
        logger.debug("game listener: onUpdate")
        onUpdateHandler()
    }

    override fun onError(sender: String?) {
        logger.debug("game listener: onError")
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ClientController::class.java)
    }
}

class StartGameRequest(val gameCreationModel: GameCreationModel) : FXEvent(EventBus.RunOn.BackgroundThread)
class NewGameState(val gameState: GameState) : FXEvent()
class HumanMoveRequest(val gameState: GameState) : FXEvent()


class ClientController : Controller() {
    var controllingClient: ControllingClient? = null
    private val listener: UIGameListener = UIGameListener(::newGameState)

    // Do NOT call this directly in the UI thread, use fire(StartGameRequest(gameCreationModel)). This way, the game starting is done in the background
    // TODO put everything which is activated by events in a different class and call these from the controller by events
    fun startGame(host: String = "localhost", port: Int = 13050, gameCreationModel: GameCreationModel = GameCreationModel()) {
        // starting the game in the UI thread blocks the UI

        logger.debug("creating and observing")

        // NOTE that testClientOne and testClientTwo are currently *internal* clients to wire the logic of the GameHandlers to the server. When external clients should join the game, these are not needed.
        // TODO: implement client for HUMAN, MANUELL and COMPUTER
        val player1 = when (gameCreationModel.selectedPlayerType1.value) {
            sc.gui.model.PlayerType.HUMAN -> HumanClient(PlayerType.PLAYER_ONE, host, port, ::humanMoveRequest)
            sc.gui.model.PlayerType.MANUELL -> TestClient(PlayerType.PLAYER_ONE, host, port)
            sc.gui.model.PlayerType.COMPUTER -> TestClient(PlayerType.PLAYER_ONE, host, port)
            else -> throw Exception("invalid playerType for player 1, cannot create game")
        }
        val player2 = when (gameCreationModel.selectedPlayerType2.value) {
            sc.gui.model.PlayerType.HUMAN -> HumanClient(PlayerType.PLAYER_TWO, host, port, ::humanMoveRequest)
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
                fire(NewGameState(gameState))
            } else {
                logger.debug("no gamestate, but $gameControl")
            }
        } else {
            logger.debug("no controlling client yet")
        }
    }

    fun humanMoveRequest(gameState: GameState) {
        fire(HumanMoveRequest(gameState))
    }

    fun updateGameState() {
        val gameState = controllingClient?.game?.currentState as? GameState
        if (gameState != null) {
            logger.debug("gamestate is $gameState")
            fire(NewGameState(gameState))
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

    fun togglePause() {
        val game = controllingClient?.game
        if (game != null) {
            if (game.isPaused) {
                game.unpause()
            } else {
                game.pause()
            }
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ClientController::class.java)
    }
}
