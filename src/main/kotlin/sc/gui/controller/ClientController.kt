package sc.gui.controller

import javafx.application.Platform
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sc.api.plugins.IGameState
import sc.api.plugins.exceptions.GameLogicException
import sc.gui.LobbyManager
import sc.gui.controller.client.ExecClient
import sc.gui.controller.client.ExternalClient
import sc.gui.controller.client.InternalClient
import sc.gui.model.PlayerType
import sc.gui.model.TeamSettings
import sc.gui.model.ViewType
import sc.gui.serverAddress
import sc.gui.serverPort
import sc.plugin2021.GameState
import sc.plugin2021.Move
import sc.plugin2021.util.GameRuleLogic
import sc.shared.GameResult
import tornadofx.Controller
import tornadofx.EventBus
import tornadofx.FXEvent
import java.util.concurrent.CompletableFuture

class StartGameRequest(val playerOneSettings: TeamSettings, val playerTwoSettings: TeamSettings): FXEvent(EventBus.RunOn.BackgroundThread)
class NewGameState(val gameState: GameState): FXEvent()
class HumanMoveRequest(val gameState: GameState): FXEvent()
class HumanMoveAction(val move: Move): FXEvent()
class GameOverEvent(val result: GameResult): FXEvent()

class ClientController: Controller() {
    private val appController: AppController by inject()
    
    var lobbyManager: LobbyManager? = null
    
    private val listener = object: IGameListener {
        override fun onGameStarted(error: Throwable?) {
            if (error != null) {
                // TODO proper error screen
                logger.error("Failed to start game!", error)
            } else {
                Platform.runLater {
                    appController.changeViewTo(ViewType.GAME)
                }
            }
        }
        override fun onNewState(state: IGameState) {
            updateGameState()
        }
        override fun onGameOver(gameResult: GameResult) {
            fire(GameOverEvent(gameResult))
        }
    }
    
    // Do NOT call this directly in the UI thread, use fire(StartGameRequest(gameCreationModel))
    // This way, the game starting is done in the background - otherwise the UI will be blocked
    // TODO put everything triggered by events in a different class and call these from the controller using events
    fun startGame(playerSettings: Array<TeamSettings>, host: String = serverAddress, port: Int = serverPort) {
        logger.debug("Creating and observing game on $host:$port")
        
        val players = playerSettings.map { teamSettings ->
            when (val type = teamSettings.type.value) {
                PlayerType.HUMAN -> InternalClient(host, port, type, ::humanMoveRequest)
                PlayerType.COMPUTER_EXAMPLE -> InternalClient(host, port, type, ::testClientMoveRequest)
                PlayerType.COMPUTER -> ExecClient(host, port, teamSettings.executable.get())
                PlayerType.EXTERNAL -> ExternalClient(host, port)
                else -> throw IllegalArgumentException("Cannot create game: Invalid playerType $type")
            }
        }
        // TODO handle client start failures
    
        lobbyManager?.game?.cancel()
        lobbyManager = LobbyManager(host, port).apply {
            startNewGame(players, playerSettings.map { it.name.get() }, players.none { it.type == PlayerType.EXTERNAL }, players.none { it.type == PlayerType.HUMAN }, listener)
        }
    }
    
    fun humanMoveRequest(gameState: GameState): CompletableFuture<Move> {
        val future = CompletableFuture<Move>()
        subscribe<HumanMoveAction>(1) {
            future.complete(it.move)
        }
        fire(HumanMoveRequest(gameState))
        return future
    }
    
    fun testClientMoveRequest(state: GameState): CompletableFuture<Move> {
        val possibleMoves = GameRuleLogic.getPossibleMoves(state)
        if (possibleMoves.isEmpty())
            throw GameLogicException("No possible Moves found!")
        return CompletableFuture.completedFuture(possibleMoves.random())
    }
    
    fun updateGameState() {
        val gameState = lobbyManager?.game?.currentState as? GameState
        if (gameState != null) {
            logger.debug("gamestate is $gameState")
            fire(NewGameState(gameState))
        }
    }
    
    fun previous() {
        lobbyManager?.game?.previous()
        updateGameState()
    }
    
    fun next() {
        lobbyManager?.game?.next()
        updateGameState()
    }
    
    fun togglePause() {
        val game = lobbyManager?.game
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
