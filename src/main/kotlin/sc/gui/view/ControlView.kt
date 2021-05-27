package sc.gui.view

import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import mu.KotlinLogging
import sc.gui.AppStyle
import sc.gui.GamePausedEvent
import sc.gui.GameReadyEvent
import sc.gui.controller.GameController
import sc.gui.controller.GameCreationController
import sc.gui.controller.HumanMoveAction
import sc.gui.view.GameControlState.*
import sc.plugin2021.Color
import tornadofx.*

val Color.borderStyle
    get() = when(this) {
        Color.BLUE -> AppStyle.borderBLUE
        Color.GREEN -> AppStyle.borderGREEN
        Color.YELLOW -> AppStyle.borderYELLOW
        Color.RED -> AppStyle.borderRED
    }

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
    SKIP("Passen", HumanMoveAction(null)),
    FINISHED("Spiel beenden", TerminateGame());
}

class ControlView : View() {
    private val logger = KotlinLogging.logger {}
    
    private val gameController: GameController by inject()
    private val gameControlState: Property<GameControlState> = objectProperty(START)
    private val hasHuman = find(GameCreationController::class).hasHumanPlayer

    override val root = hbox {
        spacing = 8.0
        hbox {
            spacing = 8.0
            visibleProperty().bind(hasHuman)
            addClass(AppStyle.pieceUnselectable)
            gameController.isHumanTurn.addListener { _, _, humanTurn ->
                if(humanTurn) {
                    removeClass(AppStyle.pieceUnselectable)
                } else if(!humanTurn && !hasClass(AppStyle.pieceUnselectable)) {
                    addClass(AppStyle.pieceUnselectable)
                }
            }
            label("Auswahl: ")
            pane {
                addClass(AppStyle.undeployedPiece, gameController.selectedColor.value.borderStyle)
                gameController.selectedColor.addListener { _, old, new ->
                    if(old != null)
                        removeClass(old.borderStyle)
                    if(new != null)
                        addClass(new.borderStyle)
                }
                this += PiecesFragment(gameController.selectedColor, gameController.selectedShape, gameController.selectedRotation, gameController.selectedFlip)
            }
        }
        vbox {
            spacing = 8.0
            hbox {
                spacing = 8.0
                button {
                    fun updateState(state: GameControlState?) {
                        logger.debug { "Updating $this to State $state" }
                        isDisable = state == null
                        state?.let { text = it.text }
                    }
                    updateState(gameControlState.value)
                    gameControlState.onChange(::updateState)
                    action {
                        fire(gameControlState.value.action)
                    }
                    gameController.canSkip.onChange {
                        if(it) {
                            gameControlState.value = SKIP
                            isDisable = false
                        } else if(gameControlState.value == SKIP) {
                            isDisable = true
                        }
                    }
                }
                button {
                    disableWhen(gameController.currentTurn.isEqualTo(0))
                    text = "⏮"
                    setOnMouseClicked {
                        fire(StepGame(-1))
                    }
                }
                label {
                    textProperty().bind(Bindings.concat(gameController.currentTurn, " / ", gameController.availableTurns))
                }
                button {
                    disableWhen(gameController.currentTurn.isEqualTo(gameController.availableTurns))
                    text = "⏭"
                    setOnMouseClicked {
                        fire(StepGame(1))
                    }
                }
            }
        }
    }
    
    init {
        gameController.gameStarted.onChange {
            logger.debug { "Game started: $it, state ${gameControlState.value}, hasHuman ${hasHuman.get()}" }
            if (it) {
                if(hasHuman.get()) {
                    gameControlState.value = SKIP
                    gameControlState.value = null
                } else {
                    gameControlState.value = PLAYING
                }
            }
        }
        gameController.gameEnded.onChange {
            if(it)
                gameControlState.value = FINISHED
        }
        subscribe<GameReadyEvent> {
            logger.debug { "Game ready, state ${gameControlState.value}, hasHuman ${hasHuman.get()}" }
            gameControlState.value = START
        }
        subscribe<GamePausedEvent> { event ->
            gameControlState.value = if(event.paused) PAUSED else PLAYING
        }
    }
    
}
