package sc.gui.view

import javafx.beans.property.SimpleStringProperty
import javafx.util.StringConverter
import sc.gui.controller.ClientController
import sc.gui.controller.GameController
import sc.gui.controller.StartGameRequest
import sc.gui.controller.UpdateGameState
import sc.gui.model.GameCreationModel
import sc.gui.model.UndeployedPiecesModel
import sc.plugin2021.Color
import sc.plugin2021.PieceShape
import tornadofx.*

class ColorConverter : StringConverter<Color>() {
    override fun toString(color: Color?): String {
        return "Color: " + color.toString()
    }

    override fun fromString(string: String?): Color {
        TODO("Not yet implemented")
    }
}

class ShapeConverter : StringConverter<PieceShape>() {
    override fun toString(shape: PieceShape?): String {
        return "Shape: " + shape.toString()
    }

    override fun fromString(string: String?): PieceShape {
        TODO("Not yet implemented")
    }

}

class GameView : View() {
    val input = SimpleStringProperty()
    private val boardView: BoardView by inject()
    private val clientController: ClientController by inject()
    private val gameController: GameController by inject()
    private val redUndeployedPieces = PiecesListFragment(UndeployedPiecesModel(Color.RED))
    private val blueUndeployedPieces = PiecesListFragment(UndeployedPiecesModel(Color.BLUE))
    private val yellowUndeployedPieces = PiecesListFragment(UndeployedPiecesModel(Color.YELLOW))
    private val greenUndeployedPieces = PiecesListFragment(UndeployedPiecesModel(Color.GREEN))

    init {
        subscribe<StartGameRequest> { event ->
            clientController.startGame("localhost", 13050, event.gameCreationModel)
        }
    }

    override val root = borderpane {
        left = borderpane {
            top {
                add(redUndeployedPieces)
            }
            bottom {
                add(blueUndeployedPieces)
            }
        }
        center = borderpane {
            center(boardView::class)

            bottom = hbox {
                button {
                    text = "Start Client"
                    setOnMouseClicked {
                        fire(StartGameRequest(GameCreationModel()))
                    }
                }
                label {
                    textProperty().bindBidirectional(gameController.currentColorProperty(), ColorConverter())
                }
                label {
                    textProperty().bindBidirectional(gameController.currentPieceShapeProperty(), ShapeConverter())
                }
                button {
                    text = "Previous"
                    setOnMouseClicked {
                        clientController.previous()
                    }
                }
                button {
                    text = "Next"
                    setOnMouseClicked {
                        clientController.next()
                    }
                }
            }
        }
        right = borderpane {
            top {
                add(yellowUndeployedPieces)
            }
            bottom {
                add(greenUndeployedPieces)
            }
        }
    }
}