package sc.gui.view

import javafx.beans.binding.Bindings
import javafx.geometry.Insets
import sc.gui.AppStyle
import sc.gui.controller.ClientController
import sc.gui.controller.GameController
import sc.plugin2021.Color
import tornadofx.*

class ControlView() : View() {
    private val gameController: GameController by inject()
    private val clientController: ClientController by inject()

    override val root = hbox {
        label("Selected: ")
        hbox {
            addClass(AppStyle.undeployedPiece, when (gameController.selectedColor.value) {
                Color.BLUE -> AppStyle.borderBLUE
                Color.GREEN -> AppStyle.borderGREEN
                Color.YELLOW -> AppStyle.borderYELLOW
                else -> AppStyle.borderRED
            })
            gameController.selectedColor.addListener { _, _, newValue ->
                if (hasClass(AppStyle.borderRED)) {
                    removeClass(AppStyle.borderRED)
                }
                if (hasClass(AppStyle.borderBLUE)) {
                    removeClass(AppStyle.borderBLUE)
                }
                if (hasClass(AppStyle.borderGREEN)) {
                    removeClass(AppStyle.borderGREEN)
                }
                if (hasClass(AppStyle.borderYELLOW)) {
                    removeClass(AppStyle.borderYELLOW)
                }
                if (newValue != null) {
                    addClass(when (newValue) {
                        Color.RED -> AppStyle.borderRED
                        Color.BLUE -> AppStyle.borderBLUE
                        Color.GREEN -> AppStyle.borderGREEN
                        Color.YELLOW -> AppStyle.borderYELLOW
                    })
                }
            }
            this += PiecesFragment(gameController.selectedColor, gameController.selectedShape, gameController.selectedRotation, gameController.selectedFlip)
        }
        hbox {
            padding = Insets(0.0, 10.0, 0.0, 10.0)
            button {
                text = "Pause / Play"
                setOnMouseClicked {
                    clientController.togglePause()
                }
            }
        }
        button {
            text = "Previous"
            setOnMouseClicked {
                clientController.previous()
            }
        }
        label {
            padding = Insets(0.0, 10.0, 0.0, 10.0)
            textProperty().bind(Bindings.concat(gameController.currentTurnProperty(), " / ", gameController.availableTurnsProperty()))
        }
        button {
            text = "Next"
            setOnMouseClicked {
                clientController.next()
            }
        }
    }
}