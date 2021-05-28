package sc.gui.controller

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.util.Duration
import sc.gui.GamePausedEvent
import sc.gui.GameReadyEvent
import sc.gui.NewGameState
import sc.gui.view.PauseGame
import sc.gui.view.StepGame
import sc.gui.view.TerminateGame
import sc.networking.clients.GameLoaderClient
import sc.networking.clients.IGameController
import sc.plugin2021.GameState
import tornadofx.Controller
import tornadofx.onChange
import java.io.File
import java.io.IOException

class GameFlowController: Controller() {
    private val gameController by inject<GameController>()
    private val interval = Timeline(KeyFrame(Duration.seconds(1.0), {
        fire(StepGame(1))
    })).apply {
        cycleCount = Animation.INDEFINITE
    }
    
    private val history = ArrayList<GameState>()
    var controller: IGameController? = null
        set(value) {
            if (value != null) {
                fire(GameReadyEvent())
            } else {
                controller?.cancel()
            }
            field = value
        }
    
    init {
        subscribe<PauseGame> { event ->
            controller?.pause(event.pause) ?: run {
                if (event.pause) {
                    interval.pause()
                } else {
                    interval.play()
                }
                fire(GamePausedEvent(event.pause))
            }
        }
        subscribe<StepGame> { event ->
            val turn = gameController.currentTurn.value + event.steps
            val state: GameState? = history.firstOrNull { it.turn >= turn } ?: run {
                controller?.step()
                history.lastOrNull()
            }
            if(state != null)
                gameController.gameState.set(state)
        }
        gameController.gameEnded.onChange {
            if (it)
                controller = null
        }
        subscribe<TerminateGame> {
            history.clear()
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
        gameController.availableTurns.set(history.last().turn)
        gameController.gameResult.set(loader.result)
    }
}