package sc.gui.controller

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.util.Duration
import sc.gui.GamePausedEvent
import sc.gui.GameReadyEvent
import sc.gui.NewGameState
import sc.gui.model.GameModel
import sc.gui.view.PauseGame
import sc.gui.view.StepGame
import sc.gui.view.TerminateGame
import sc.networking.clients.GameLoaderClient
import sc.networking.clients.IGameController
import sc.plugin2022.GameState
import tornadofx.*
import java.io.File
import java.io.IOException

class GameFlowController: Controller() {
    private val gameModel: GameModel by inject()
    private var stepController = true
    private val interval = Timeline(KeyFrame(Duration.seconds(1.0), {
        fire(StepGame(1))
    })).apply { cycleCount = Animation.INDEFINITE }
    
    private val history = ArrayList<GameState>()
    var controller: IGameController? = null
    
    init {
        subscribe<PauseGame> { event ->
            if(controller?.pause(event.pause) == null) {
                fire(GamePausedEvent(event.pause))
            } else {
                stepController = false
            }
            if (event.pause) {
                interval.pause()
            } else {
                interval.play()
            }
        }
        subscribe<StepGame> { event ->
            val turn = gameModel.currentTurn.value + event.steps
            val state: GameState? = history.firstOrNull { it.turn >= turn } ?: run {
                if(stepController) {
                    controller?.step()
                    history.lastOrNull()
                } else {
                    interval.pause()
                    stepController = true
                    null
                }
            }
            if(state != null)
                gameModel.gameState.set(state)
        }
        gameModel.gameEnded.onChange {
            if (it)
                controller = null
        }
        subscribe<TerminateGame> {
            interval.pause()
            history.clear()
            controller?.cancel()
            controller = null
        }
        subscribe<NewGameState> { event ->
            history.add(event.gameState as GameState)
        }
    }
    
    fun loadReplay(file: File) {
        val loader = GameLoaderClient(file)
        history.addAll(loader.getHistory().filterIsInstance<GameState>())
        if(history.isEmpty())
            throw IOException("")
        fire(GameReadyEvent())
        gameModel.availableTurns.set(history.last().turn)
        gameModel.gameResult.set(loader.result)
        gameModel.gameState.set(history.first())
    }
}