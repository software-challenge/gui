package sc.gui.view

import javafx.beans.binding.Bindings
import sc.gui.AppStyle
import sc.gui.GamePausedEvent
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
    private val gameController: GameController by inject()
    private val gameControlState = objectProperty(START)
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
                button(gameControlState.get().text) {
                    gameControlState.addListener { _, _, state ->
                        isDisable = false
                        text = state.text
                    }
                    setOnMouseClicked {
                        if(gameControlState.value == PAUSED)
                            gameControlState.set(PLAYING)
                        else
                            isDisable = true
                        fire(gameControlState.value.action)
                    }
                    gameController.canSkip.onChange {
                        if(it) {
                            gameControlState.set(SKIP)
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
            if(it)
                if(!hasHuman.get())
                    gameControlState.set(PLAYING)
            else
                gameControlState.set(START)
        }
        gameController.gameEnded.onChange {
            if(it)
                gameControlState.set(FINISHED)
        }
        subscribe<GamePausedEvent> {
            gameControlState.set(PAUSED)
        }
    }
    
}
