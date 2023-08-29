package sc.gui.controller

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.util.Duration
import mu.KotlinLogging
import sc.api.plugins.IGameState
import sc.gui.GamePausedEvent
import sc.gui.GameReadyEvent
import sc.gui.NewGameState
import sc.gui.events.*
import sc.gui.model.GameModel
import sc.networking.clients.GameLoaderClient
import sc.networking.clients.IGameController
import tornadofx.*
import java.io.IOException

class GameFlowController: Controller() {
    private val logger = KotlinLogging.logger {}
    
    private val gameModel: GameModel by inject()
    /** Whether to request a new Move when stepping forward. */
    private var stepController = true
    private val interval = Timeline(KeyFrame(Duration.seconds(gameModel.stepSpeed.value), {
        fire(StepGame(1))
    })).apply {
        cycleCount = Animation.INDEFINITE
        rateProperty().bind(gameModel.stepSpeed)
    }
    
    private val history = ArrayList<IGameState>()
    /** Used to control running Game - is null for completed Game/Replay. */
    var controller: IGameController? = null
    
    init {
        subscribe<PauseGame> { event ->
            controller?.let {
                it.pause(event.pause)
                stepController = false
            } ?: fire(GamePausedEvent(event.pause))
            if(event.pause) {
                interval.pause()
            } else {
                interval.play()
            }
        }
        subscribe<StepGame> { event ->
            val turn = gameModel.currentTurn.value + event.steps
            val state: IGameState? =
                    if(event.steps > 0)
                        history.firstOrNull { it.turn >= turn } ?: run {
                            if(stepController) {
                                // At latest available turn
                                controller?.step()
                                history.lastOrNull()
                            } else {
                                // First Step after PauseGame Event
                                interval.pause()
                                stepController = true
                                null
                            }
                        }
                    else
                        history.lastOrNull { it.turn <= turn } ?: history.first()
            if(state != null)
                gameModel.gameState.set(state)
        }
        gameModel.gameOver.onChange {
            if(it)
                controller = null
        }
        subscribe<TerminateGame> {
            interval.pause()
            history.clear()
            controller?.cancel()
            controller = null
        }
        subscribe<NewGameState> { event ->
            history.add(event.gameState)
        }
    }
    
    @Throws(ReplayLoaderException::class)
    fun loadReplay(loader: GameLoaderClient) {
        if(history.isNotEmpty())
            throw ReplayLoaderException("Trying to load replay into a running game")
        history.addAll(loader.getHistory())
        logger.debug("Loaded {} states from {}", history.size, loader)
        if(history.isEmpty())
            throw ReplayLoaderException("Replay history from $loader is empty")
        fire(GameReadyEvent())
        gameModel.availableTurns.set(history.last().turn)
        gameModel.gameResult.set(loader.result)
        gameModel.playerNames.setAll(
                loader.result?.scores?.keys
                        ?.sortedBy { it.team.index }
                        ?.map { it.displayName }.orEmpty())
        gameModel.gameState.set(history.first())
    }
}

class ReplayLoaderException(message: String): IOException(message)