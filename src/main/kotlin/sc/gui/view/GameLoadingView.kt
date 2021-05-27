package sc.gui.view

import javafx.geometry.Pos
import javafx.scene.text.FontPosture
import sc.gui.serverAddress
import sc.gui.serverPort
import tornadofx.*

class GameLoadingView: View() {
    override val root = vbox {
        alignment = Pos.CENTER
        
        vbox {
            alignment = Pos.TOP_CENTER
            label {
                style {
                    fontSize = 32.px
                }
                text = "Das Spiel startet..."
            }
            label {
                style {
                    fontStyle = FontPosture.ITALIC
                    fontSize = 24.px
                }
                text = "Bitte verbinde manuell gestartete Clients auf $serverAddress:$serverPort"
            }
        }
    }
}

