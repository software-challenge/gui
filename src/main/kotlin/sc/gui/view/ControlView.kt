package sc.gui.view

import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import mu.KotlinLogging
import sc.gui.AppStyle
import sc.gui.GamePausedEvent
import sc.gui.GameReadyEvent
import sc.gui.controller.GameController
import sc.gui.view.GameControlState.*
import sc.util.binding
import sc.util.listen
import sc.util.listenImmediately
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
                    prefWidth = AppStyle.fontSizeRegular.value * 15
                    gameControlState.listenImmediately { controlState ->
                        logger.debug { "GameControlState $controlState" }
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
                    alignment = Pos.CENTER
                    prefWidth = AppStyle.fontSizeRegular.value * 7
                    textProperty().bind(
                            arrayOf<ObservableValue<Number>>(gameController.currentTurn, gameController.availableTurns).binding
                            { (cur, all) -> "Zug " + if (cur != all || gameController.gameEnded.value) "$cur/$all" else cur }
                    )
                }
                button {
                    disableProperty().bind(
                            arrayOf(gameController.atLatestTurn, gameController.isHumanTurn, gameController.gameEnded).binding
                            { (latest, human, end) -> logger.trace("latest: $latest, human: $human, end: $end"); latest && (human || end) }
                    )
                    text = "⏭"
                    setOnMouseClicked {
                        fire(StepGame(1))
                        if (gameControlState.value == START)
                            gameControlState.value = PAUSED
                    }
                }
            }
    
    init {
        subscribe<GameReadyEvent> {
            gameControlState.value = START
        }
        subscribe<GamePausedEvent> { event ->
            gameControlState.value = when {
                event.paused -> PAUSED
                gameController.isHumanTurn.value -> null
                else -> PLAYING
            }
        }
        gameController.isHumanTurn.onChange {
            if (it && gameControlState.value != PAUSED) {
                gameControlState.value = PLAYING
                gameControlState.value = null
            }
        }
        arrayOf<ObservableValue<Boolean>>(gameController.atLatestTurn, gameController.gameEnded).listen { (latestTurn, end) ->
            if (latestTurn && end) gameControlState.value = FINISHED
        }
    }
    
}
