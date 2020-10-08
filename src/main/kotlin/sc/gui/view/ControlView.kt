package sc.gui.view

import javafx.beans.binding.Bindings
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import sc.gui.AppStyle
import sc.gui.controller.AppController
import sc.gui.controller.ClientController
import sc.gui.controller.GameController
import sc.gui.controller.GameCreationController
import sc.plugin2021.Color
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
    private val selected = hbox {
        visibleProperty().bind(gameCreationController.hasHumanPlayerProperty)
        addClass(AppStyle.pieceUnselectable)
        label("Auswahl: ")
        hbox {
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

    override val root = borderpane {
        padding = Insets(10.0, 0.0, 10.0, 0.0)
		left = selected
        center {
            hbox {
                alignment = Pos.TOP_CENTER
                hbox {
                    padding = Insets(0.0, 10.0, 0.0, 10.0)
                    this += playPauseButton
                }
                button {
                    disableWhen(gameController.currentTurnProperty().isEqualTo(0))
                    text = "⏮"
                    setOnMouseClicked {
                        clientController.previous()
                    }
                }
                label {
                    padding = Insets(0.0, 10.0, 0.0, 10.0)
                    textProperty().bind(Bindings.concat(gameController.currentTurnProperty(), " / ", gameController.availableTurnsProperty()))
                }
                button {
                    disableWhen(gameController.currentTurnProperty().isEqualTo(gameController.availableTurnsProperty()))
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
            if(clientController.controllingClient?.game?.isPaused == true) {
                playPauseButton.text = if(start) "Start" else "Weiter"
            } else {
                playPauseButton.text = "Anhalten"
            }
        }
        playPauseButton.setOnMouseClicked {
            if (gameController.gameEndedProperty().get()) {
                appController.changeViewTo(StartView::class)
                gameController.clearGame()
            } else {
                clientController.togglePause()
				updatePauseState(false)
            }
        }
    
        // When the game is paused externally e.g. when rewinding
        gameController.currentTurnProperty().addListener { _, _, turn ->
            updatePauseState(turn == 0)
        }
        gameController.gameEndedProperty().addListener { _, _, ended ->
            if (ended) {
                playPauseButton.text = "Spiel beenden"
            }
        }
        gameController.isHumanTurnProperty().addListener { _, _, humanTurn ->
            if (humanTurn) {
                selected.removeClass(AppStyle.pieceUnselectable)
            } else if (!humanTurn && !selected.hasClass(AppStyle.pieceUnselectable)) {
                selected.addClass(AppStyle.pieceUnselectable)
            }
        }
    }
}