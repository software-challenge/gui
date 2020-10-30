package sc.gui.view

import javafx.beans.binding.Bindings
import javafx.geometry.Pos
import javafx.scene.control.Button
import sc.gui.AppStyle
import sc.gui.controller.*
import sc.gui.model.ViewType
import sc.plugin2021.Color
import sc.plugin2021.SkipMove
import tornadofx.*

val Color.borderStyle
    get() = when(this) {
        Color.BLUE -> AppStyle.borderBLUE
        Color.GREEN -> AppStyle.borderGREEN
        Color.YELLOW -> AppStyle.borderYELLOW
        Color.RED -> AppStyle.borderRED
    }

class ControlView : View() {
    private val gameController: GameController by inject()
    private val clientController: ClientController by inject()
    private val appController: AppController by inject()
    private val gameCreationController: GameCreationController by inject()
    private val playPauseButton: Button = button {
        text = "Start"
    }

    override val root = hbox {
        spacing = 8.0
        hbox {
            spacing = 8.0
            visibleProperty().bind(gameCreationController.hasHumanPlayer)
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
                alignment = Pos.TOP_CENTER
                button {
                    enableWhen(gameController.canSkip)
                    text = "Passen"
                    setOnMouseClicked {
                        fire(HumanMoveAction(SkipMove(gameController.currentColor.get())))
                    }
                }
            }
            hbox {
                spacing = 8.0
                this += playPauseButton
                button {
                    disableWhen(gameController.currentTurn.isEqualTo(0))
                    text = "⏮"
                    setOnMouseClicked {
                        clientController.previous()
                    }
                }
                label {
                    textProperty().bind(Bindings.concat(gameController.currentTurn, " / ", gameController.availableTurns))
                }
                button {
                    disableWhen(gameController.currentTurn.isEqualTo(gameController.availableTurns))
                    text = "⏭"
                    setOnMouseClicked {
                        clientController.next()
                    }
                }
            }
        }
    }

    init {
        val updatePauseState = { start: Boolean ->
            if(clientController.lobbyManager?.game?.isPaused == true) {
                playPauseButton.text = if(start) "Start" else "Weiter"
            } else {
                playPauseButton.text = "Anhalten"
            }
        }
        playPauseButton.setOnMouseClicked {
            if (gameController.gameEnded.get()) {
                appController.changeViewTo(ViewType.START)
                gameController.clearGame()
            } else {
                clientController.togglePause()
                updatePauseState(false)
            }
        }

        // When the game is paused externally e.g. when rewinding
        gameController.currentTurn.addListener { _, _, turn ->
                                                               updatePauseState(turn == 0)
        }
        gameController.gameEnded.addListener { _, _, ended ->
                                                             if (ended) {
                                                                 playPauseButton.text = "Spiel beenden"
                                                             }
        }
    }
}
