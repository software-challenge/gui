package sc.gui.view

import javafx.geometry.Pos
import sc.gui.AppStyle
import sc.gui.serverAddress
import sc.gui.serverPort
import tornadofx.*
import tornadofx.Stylesheet.Companion.legend

class GameLoadingView: View() {
    override val root = vbox {
        alignment = Pos.CENTER
        
        vbox {
            alignment = Pos.TOP_CENTER
            label {
                addClass(AppStyle.heading)
                text = "Das Spiel startet..."
            }
            label {
                addClass(legend)
                text = "Bitte verbinde gestartete Spieler auf $serverAddress:$serverPort"
            }
        }
    }
}

