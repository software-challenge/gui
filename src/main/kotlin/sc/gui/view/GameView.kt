package sc.gui.view

import javafx.animation.FadeTransition
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.ObservableList
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.util.Duration
import javafx.util.StringConverter
import javafx.util.converter.IntegerStringConverter
import org.slf4j.LoggerFactory
import sc.gui.controller.*
import sc.gui.model.GameCreationModel
import sc.gui.model.UndeployedPiecesModel
import sc.plugin2021.Color
import sc.plugin2021.Piece
import sc.plugin2021.PieceShape
import sc.plugin2021.Rotation
import tornadofx.*

class ColorConverter : StringConverter<Color>() {
    override fun toString(color: Color?): String {
        return color!!.name.toLowerCase()
    }

    override fun fromString(string: String?): Color {
        TODO("Not yet implemented")
    }
}

class PiecesScope(val pieces: ObservableList<Piece>) : Scope()

class ShapeConverter : StringConverter<PieceShape>() {
    override fun toString(piece: PieceShape?): String {
        return piece!!.name.toLowerCase()
    }

    override fun fromString(string: String?): PieceShape {
        TODO("Not yet implemented")
    }

}

class PathBinding(val color: Property<Color>, val pieceShape: Property<PieceShape>) : StringBinding() {
    init {
        bind(color)
        bind(pieceShape)
    }

    override fun computeValue(): String {
        return String.format("file:resources/graphics/blokus/%s/%s.png", color.value.toString().toLowerCase(), pieceShape.value.toString().toLowerCase())
    }
}

class GameView : View() {
    val input = SimpleStringProperty()
    private val boardView: BoardView by inject()
    private val clientController: ClientController by inject()
    private val gameController: GameController by inject()
    private val redUndeployedPieces = UndeployedPiecesModel(Color.RED)
    private val blueUndeployedPieces = UndeployedPiecesModel(Color.BLUE)
    private val yellowUndeployedPieces = UndeployedPiecesModel(Color.YELLOW)
    private val greenUndeployedPieces = UndeployedPiecesModel(Color.GREEN)

    lateinit var imageView: ImageView

    val statusLabel = Label("whooohooo")

    init {
        subscribe<StartGameRequest> { event ->
            clientController.startGame("localhost", 13050, event.gameCreationModel)
        }
        subscribe<HumanMoveRequest> { event ->
            val player = event.gameState.currentPlayer.displayName
            val color = event.gameState.currentColor.name
            statusLabel.text = "Spieler ${player} mit ${color} ist am Zug!"
            statusLabel.isVisible = true
            val ft = FadeTransition(7.seconds, statusLabel)
            ft.fromValue = 1.0
            ft.toValue = 0.0
            ft.play()
            ft.setOnFinished { statusLabel.isVisible = false }
        }

    }

    override val root = borderpane {
        left = borderpane {
            top {
                val pieces = "undeployedPiecesModel" to blueUndeployedPieces
                this += find<PiecesListFragment>(pieces)
            }
            bottom {
                val pieces = "undeployedPiecesModel" to redUndeployedPieces
                this += find<PiecesListFragment>(pieces)
            }
        }
        center = borderpane {
            center = stackpane {
                this += find<BoardView>()
                statusLabel.style = "-fx-text-fill: white; -fx-font-size: 32pt;"
                statusLabel.isVisible = false
                this += statusLabel
            }

            bottom = hbox {
                label {
                    textProperty().bind(Bindings.concat("Selected: ", gameController.currentColorProperty(), " ", gameController.currentPieceShapeProperty()))
                }
                val path = PathBinding(gameController.currentColorProperty(), gameController.currentPieceShapeProperty())
                imageview(path) {
                    style {
                        backgroundColor += c("#000000")
                    }
                    isSmooth = false
                    rotate(Duration.seconds(3.0), 90)
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
                val pieces = "undeployedPiecesModel" to yellowUndeployedPieces
                this += find<PiecesListFragment>(pieces)
            }
            bottom {
                val pieces = "undeployedPiecesModel" to greenUndeployedPieces
                this += find<PiecesListFragment>(pieces)
            }
        }

        setOnMouseClicked {
            if (it.button == MouseButton.SECONDARY) {
                logger.debug("Right-click, flipping piece")
                gameController.selectFlip(!gameController.currentFlipProperty().get())
            }
            it.consume()
        }
        setOnScroll {
            logger.debug("Scrolling detected: rotating selected piece")
            gameController.selectRotation(
                    gameController.currentRotationProperty().get().rotate(
                            when {
                                it.deltaY > 0.0 -> Rotation.LEFT
                                it.deltaY == 0.0 -> Rotation.NONE
                                it.deltaY < 0.0 -> Rotation.RIGHT
                                else -> Rotation.MIRROR
                            }
                    )
            )
            it.consume()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GameView::class.java)
    }
}