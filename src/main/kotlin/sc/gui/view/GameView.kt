package sc.gui.view

import javafx.beans.property.SimpleStringProperty
import javafx.util.StringConverter
import javafx.util.converter.NumberStringConverter
import sc.gui.controller.ClientController
import sc.gui.controller.GameController
import sc.gui.model.UndeployedPiecesModel
import sc.gui.view.BoardView
import sc.gui.view.PiecesFragment
import sc.plugin2021.Color
import sc.plugin2021.PieceShape
import tornadofx.*
import java.text.Format

class ColorConverter: StringConverter<Color>() {
    override fun toString(color: Color?): String {
        return "Color: " + color.toString()
    }

    override fun fromString(string: String?): Color {
        TODO("Not yet implemented")
    }
}

class ShapeConverter: StringConverter<PieceShape>() {
    override fun toString(shape: PieceShape?): String {
        return "Shape: " + shape.toString()
    }

    override fun fromString(string: String?): PieceShape {
        TODO("Not yet implemented")
    }

}
class GameView: View() {
    val input = SimpleStringProperty()
    private val boardView: BoardView by inject()
    private val clientController: ClientController by inject()
    private val gameController: GameController by inject()
    val redUndeployedPieces = PiecesFragment(UndeployedPiecesModel(Color.RED))
    val blueUndeployedPieces = PiecesFragment(UndeployedPiecesModel(Color.BLUE))
    val yellowUndeployedPieces = PiecesFragment(UndeployedPiecesModel(Color.YELLOW))
    val greenUndeployedPieces = PiecesFragment(UndeployedPiecesModel(Color.GREEN))
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
                    clientController.startGame()
                }
            }
            label {
                textProperty().bindBidirectional(gameController.currentColorProperty(), ColorConverter())
            }
            label {
                textProperty().bindBidirectional(gameController.currentPieceShapeProperty(), ShapeConverter())
            }
        }
    }
}