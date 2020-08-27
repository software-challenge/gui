package sc.gui.view

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.Parent
import javafx.util.StringConverter
import javafx.util.converter.IntegerStringConverter
import org.slf4j.LoggerFactory
import sc.gui.controller.*
import sc.gui.model.GameCreationModel
import sc.gui.model.UndeployedPiecesModel
import sc.plugin2021.Color
import sc.plugin2021.Piece
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

class TestFragment() : Fragment() {
    override val root = label {
        text = "Test"
    }
    override fun onDock() {
        super.onDock()
        logger.debug("test fragment docked")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TestFragment::class.java)
    }
}

class PiecesScope(val pieces: ObservableList<Piece>): Scope()

class GameView : View() {
    val input = SimpleStringProperty()
    private val boardView: BoardView by inject()
    private val clientController: ClientController by inject()
    private val gameController: GameController by inject()
    private val redUndeployedPieces = UndeployedPiecesModel(Color.RED)

    init {
        subscribe<StartGameRequest> { event ->
            clientController.startGame("localhost", 13050, event.gameCreationModel)
        }
    }

    override val root = borderpane {
        left = borderpane {
            top {
                this += find<TestFragment>()
                val pieces = "pieces" to redUndeployedPieces.undeployedPieces
                this += find<PiecesListFragment>(pieces)
            }
            bottom {
                //add(blueUndeployedPieces)
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
                    textProperty().bind(Bindings.concat("Selected: ", gameController.currentColorProperty(), " ", gameController.currentPieceShapeProperty()))
                }
                button {
                    text = "Pause / Play"
                    setOnMouseClicked {
                        clientController.togglePause()
                    }
                }
                button {
                    text = "Previous"
                    setOnMouseClicked {
                        clientController.previous()
                    }
                }
                label {
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
        right = borderpane {
            top {
                //add(yellowUndeployedPieces)
            }
            bottom {
                //add(greenUndeployedPieces)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GameView::class.java)
    }
}