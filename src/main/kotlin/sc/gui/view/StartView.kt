package sc.gui.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import sc.gui.controller.CreateGame
import tornadofx.*

class StartView : View() {
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
                    action {
                        fire(CreateGame)
                    }
                }
            }
        }
    }
}

