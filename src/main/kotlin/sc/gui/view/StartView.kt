package sc.gui.view

import javafx.geometry.Pos
import javafx.scene.text.TextAlignment
import sc.gui.AppStyle
import sc.gui.controller.CreateGame
import tornadofx.*

class StartView: View() {
    override val root = vbox(AppStyle.spacing, Pos.CENTER) {
        paddingAll = AppStyle.spacing
        label("Willkommen bei der Software-Challenge!") {
            addClass(AppStyle.heading)
            isWrapText = true
            textAlignment = TextAlignment.CENTER
        }
        button("Neues Spiel starten") {
            action {
                fire(CreateGame)
            }
        }
    }
}

