package sc.gui.view

import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.geometry.Pos
import mu.KotlinLogging
import sc.gui.AppStyle
import sc.gui.GamePausedEvent
import sc.gui.GameReadyEvent
import sc.gui.controller.GameController
import sc.gui.view.GameControlState.*
import tornadofx.*

sealed class GameControlEvent: FXEvent()
data class PauseGame(val pause: Boolean): GameControlEvent()
data class StepGame(val steps: Int): GameControlEvent()
/** Signals that the current game should be terminated.
 * @param close whether to return to start screen */
data class TerminateGame(val close: Boolean = true): GameControlEvent()

/** Encapsulates the different actions of the GameControlButton.
 * @param action the event to fire when this state is invoked */
enum class GameControlState(val text: String, val action: FXEvent) {
    START("Start", PauseGame(false)),
    PLAYING("Anhalten", PauseGame(true)),
    PAUSED("Weiter", PauseGame(false)),
    FINISHED("Spiel beenden", TerminateGame());
}

class ControlView: View() {
    private val logger = KotlinLogging.logger {}
    
    private val gameController: GameController by inject()
    private val gameControlState: Property<GameControlState> = objectProperty(START)
    
    override val root =
            hbox {
                alignment = Pos.CENTER
                spacing = AppStyle.formSpacing
                button {
                    gameControlState.listenImmediately { controlState ->
                        logger.debug { "Updating $this to State $controlState" }
                        isDisable = controlState == null
                        controlState?.let { text = it.text }
                    }
                    action {
                        isDisable = true
                        fire(gameControlState.value.action)
                    }
                }
                button {
                    disableWhen(gameController.currentTurn.isEqualTo(0))
                    text = "⏮"
                    setOnMouseClicked {
                        if (gameController.atLatestTurn.value)
                            fire(PauseGame(true))
                        fire(StepGame(-1))
                    }
                }
                label {
                    textProperty().bind(Bindings.concat(gameController.currentTurn, " / ", gameController.availableTurns))
                }
                button {
                    disableProperty().bind(
                            gameController.atLatestTurn.booleanBinding(gameController.gameEnded) {
                                it == true && gameController.gameEnded.value
                            }
                    )
                    text = "⏭"
                    setOnMouseClicked {
                        fire(StepGame(1))
                    }
                }
            }
    
    init {
        gameController.gameStarted.onChange {
            if (it)
                gameControlState.value = PLAYING
        }
        arrayOf(gameController.atLatestTurn, gameController.gameEnded).forEach { observable ->
            observable.onChange {
                logger.debug { "updating control state on change of $observable" }
                if (gameController.gameEnded.value && gameController.atLatestTurn.value)
                    gameControlState.value = FINISHED
            }
        }
        subscribe<GameReadyEvent> {
            gameControlState.value = START
        }
        subscribe<GamePausedEvent> { event ->
            gameControlState.value = if (event.paused) PAUSED else PLAYING
        }
    }
    
}
