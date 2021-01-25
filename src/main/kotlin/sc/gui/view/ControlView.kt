package sc.gui.view

import javafx.beans.binding.Bindings
import javafx.scene.control.Button
import org.slf4j.LoggerFactory
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
    private val playPauseSkipButton: Button = button {
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
                spacing = 8.0
                this += playPauseSkipButton
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
        // TODO properly implement State pattern for start/pause/play/skip/finish-button
        val updatePauseState = { start: Boolean ->
            val paused = clientController.lobbyManager?.game?.isPaused
            logger.trace("Button updatePauseState: $paused (start: $start)")
            if (paused == true) {
                playPauseSkipButton.text = if (start) "Start" else "Weiter"
            } else {
                playPauseSkipButton.text = "Anhalten"
            }
        }
        gameController.canSkip.addListener { _, _, canSkip ->
            if(canSkip) {
                playPauseSkipButton.text = "Passen"
            }
        }
        playPauseSkipButton.setOnMouseClicked {
            when {
                gameController.canSkip.get() -> {
                    fire(HumanMoveAction(SkipMove(gameController.currentColor.value)))
                }
                gameController.gameEnded.value -> {
                    appController.changeViewTo(ViewType.START)
                    gameController.clearGame()
                }
                else -> {
                    updatePauseState(false)
                    clientController.togglePause()
                }
            }
        }
    
        // When the game is paused externally e.g. when rewinding
        arrayOf(gameController.currentTurn, gameController.started, gameController.gameResult).forEach {
            it.addListener { _, _, _ ->
                if (gameController.gameEnded.value) {
                    playPauseSkipButton.text = "Spiel beenden"
                } else {
                    updatePauseState(!gameController.started.value)
                }
            }
        }
    }
    
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
