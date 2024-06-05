package sc.gui.controller

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.stage.FileChooser
import javafx.util.Duration
import mu.KotlinLogging
import sc.api.plugins.IGameState
import sc.framework.HelperMethods
import sc.framework.ReplayLoader
import sc.gui.GamePausedEvent
import sc.gui.GameReadyEvent
import sc.gui.NewGameState
import sc.gui.events.*
import sc.gui.model.GameModel
import sc.networking.clients.IGameController
import tornadofx.*
import java.io.IOException

fun View.selectReplay(onConfirm: () -> Unit = {}) {
    chooseFile(
            "Replay laden",
            arrayOf(FileChooser.ExtensionFilter("XML", "*.xml", "*.xml.gz")),
            HelperMethods.replayFolder.takeIf { it.exists() },
            mode = FileChooserMode.Single,
    ).forEach {
        onConfirm()
        try {
            find(GameFlowController::class).loadReplay(ReplayLoader(it))
        } catch(e: Exception) {
            warning("Replay laden fehlgeschlagen", "Das Replay $it konnte nicht geladen werden:\n" + e.stackTraceToString())
        }
    }
}

class GameFlowController: Controller() {
    private val logger = KotlinLogging.logger {}
    
    private val gameModel: GameModel by inject()
    /** Whether to request a new Move when stepping forward. */
    private var stepController = true
    private val interval = Timeline(KeyFrame(Duration.seconds(gameModel.stepSpeed.value * 3), {
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
            logger.debug { "Received $event" }
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
            val state = event.gameState
            history.add(state)
            logger.debug("New state: {}", state)
            gameModel.run {
                if(stepController || gameState.value == null || gameResult.value != null) {
                    gameResult.set(null)
                    gameState.set(state)
                } else {
                    updateAvailableTurns(state.turn)
                }
            }
        }
    }
    
    @Throws(ReplayLoaderException::class)
    fun loadReplay(loader: ReplayLoader) {
        if(history.isNotEmpty())
            throw ReplayLoaderException("Trying to load replay into a running game")
        val result = loader.loadHistory()
        history.addAll(result.first)
        logger.debug("Loaded {} states from {}", history.size, loader)
        if(history.isEmpty())
            throw ReplayLoaderException("Replay history from $loader is empty")
        fire(GameReadyEvent())
        gameModel.availableTurns.set(history.last().turn)
        gameModel.gameResult.set(result.second)
        gameModel.playerNames.setAll(
                result.second?.scores?.keys
                        ?.sortedBy { it.team.index }
                        ?.map { it.displayName }.orEmpty())
        gameModel.gameState.set(history.first())
    }
}

class ReplayLoaderException(message: String): IOException(message)