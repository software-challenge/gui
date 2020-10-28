package sc.gui.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import sc.gui.controller.AppController
import sc.gui.model.ViewType
import tornadofx.*

class StartView : View() {
    private val controller: AppController by inject()
    override val root = vbox {
        alignment = Pos.CENTER

        vbox {
            alignment = Pos.TOP_CENTER
            label {
                style {
                    fontSize = 32.px
                }
                text = "Willkommen bei der Software-Challenge!"
            }
            vbox {
                alignment = Pos.TOP_CENTER
                padding = Insets(40.0, 0.0, 0.0, 0.0)

                button {
                    text = "Neues Spiel starten"
                    setOnMouseClicked {
                        controller.changeViewTo(ViewType.GAME_CREATION)
                    }
                }
            }
        }
    }
}

