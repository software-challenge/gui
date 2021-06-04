package sc.gui.view

import javafx.geometry.Pos
import sc.gui.AppStyle
import sc.gui.controller.CreateGame
import tornadofx.*

class StartView: View() {
    override val root = vbox {
        alignment = Pos.CENTER
        spacing = AppStyle.spacing * 2
        label {
            style {
                fontSize = AppStyle.fontSizeHeader
            }
            text = "Willkommen bei der Software-Challenge!"
        }
        button {
            text = "Neues Spiel starten"
            action {
                fire(CreateGame)
            }
        }
    }
}

