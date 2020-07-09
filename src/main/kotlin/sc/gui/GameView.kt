package sc.gui

import javafx.beans.property.SimpleStringProperty
import sc.gui.controller.ClientController
import sc.gui.model.UndeployedPiecesModel
import sc.gui.view.BoardView
import sc.gui.view.PiecesFragment
import sc.plugin2021.PlayerColor
import tornadofx.View
import tornadofx.button
import tornadofx.hbox
import tornadofx.vbox

class GameView: View() {
    val input = SimpleStringProperty()
    private val boardView: BoardView by inject()
    private val clientController: ClientController by inject()
    val redUndeployedPieces = PiecesFragment(UndeployedPiecesModel(PlayerColor.RED))
    val blueUndeployedPieces = PiecesFragment(UndeployedPiecesModel(PlayerColor.BLUE))
    val yellowUndeployedPieces = PiecesFragment(UndeployedPiecesModel(PlayerColor.YELLOW))
    val greenUndeployedPieces = PiecesFragment(UndeployedPiecesModel(PlayerColor.GREEN))
    override val root = vbox {
        hbox {
            vbox {
                add(redUndeployedPieces)
                add(blueUndeployedPieces)
            }
            add(boardView)
            vbox {
                add(yellowUndeployedPieces)
                add(greenUndeployedPieces)
            }
        }
        hbox {
            button {
                text = "Start Client"
                setOnMouseClicked {
                    clientController.startClient()
                }
            }
        }
    }
}