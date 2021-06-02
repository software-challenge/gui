package sc.gui.view

import javafx.beans.property.Property
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.AppController
import sc.gui.controller.GameController
import sc.plugin2022.Coordinates
import sc.plugin2022.PieceType
import sc.plugin2022.util.Constants
import tornadofx.*

// this custom class is required to be able to shrink upsized images back to smaller sizes
// see: https://stackoverflow.com/a/35202191/9127322
class PieceImage(sizeProperty: Property<Double>, private val content: PieceType): ImageView() {
    
    init {
        sizeProperty.listenImmediately { size ->
            image = createImage(size)
        }
    }
    
    private fun createImage(size: Double) =
            Image(ResourceLookup(this)["/graphics/${content.toString().toLowerCase()}.png"], size, size, true, false)
    
    override fun minHeight(width: Double): Double {
        return 16.0
    }
    
    override fun prefHeight(width: Double): Double {
        image ?: return minHeight(width)
        return image.height
    }
    
    override fun minWidth(height: Double): Double {
        return 16.0
    }
    
    override fun prefWidth(height: Double): Double {
        image ?: return minWidth(height)
        return image.width
    }
    
    override fun isResizable(): Boolean {
        return true
    }
}

class BoardView: View() {
    private val logger = LoggerFactory.getLogger(BoardView::class.java)
    
    private val gameController: GameController by inject()
    private val appController: AppController by inject()
    val pieces = HashMap<Coordinates, Node>()
    val calculatedBlockSize = objectProperty(16.0)
    
    val grid = gridpane {
        isGridLinesVisible = true
        gameController.gameState.onChange { state ->
            state?.lastMove?.let { move ->
                val piece = pieces[move.start] ?: return@let
                piece.gridpaneConstraints {
                    columnRowIndex(move.destination.x, move.destination.y)
                }
                // TODO animate
                //  children.remove(piece)
                //  piece.move(Duration.seconds(1.0), move)
            }
        }
        gameController.gameState.listenImmediately {
            if(pieces.isEmpty() && it != null) {
                it.currentPieces.forEach { (coords, piece) ->
                    pieces[coords] = createPiece(coords, piece.type)
                    // TODO this doesn't work with movement
                }
            }
        }
        for (x in 0 until Constants.BOARD_SIZE) {
            constraintsForRow(x).percentHeight = 100.0 / Constants.BOARD_SIZE
            constraintsForColumn(x).percentWidth = 100.0 / Constants.BOARD_SIZE
        }
    }
    override val root = hbox {
        alignment = Pos.CENTER
        this += grid
    }
    
    private fun getPane(x: Int, y: Int): Node =
            grid.children.find { node ->
                GridPane.getColumnIndex(node) == x && GridPane.getRowIndex(node) == y
            } ?: throw Exception("Pane of ($x, $y) is not part of the BoardView")
    
    private fun createPiece(coordinates: Coordinates, type: PieceType): HBox {
        val image = PieceImage(calculatedBlockSize, type)
        image.fitWidthProperty().bind(calculatedBlockSize)
        image.fitHeightProperty().bind(calculatedBlockSize)
        
        return hbox {
            setOnMouseEntered {
                addClass(AppStyle.hoverColor)
                it.consume()
            }
            setOnMouseExited {
                removeClass(AppStyle.hoverColor)
                it.consume()
            }
            setOnMouseClicked {
                if (it.button == MouseButton.PRIMARY) {
                    logger.debug("Clicked on pane $coordinates")
                    handleClick(coordinates)
                    it.consume()
                }
            }
            
            this += image
        }
    }
    
    fun handleClick(coordinates: Coordinates) {
        /* TODO
        if(!game.atLatestTurn.value)
            return
        if (isHoverable(x, y, game.selectedCalculatedShape.get()) && isPlaceable(x, y, game.selectedCalculatedShape.get())) {
            logger.debug("Set-Move from GUI at [$x,$y] seems valid")
            val color = game.selectedColor.get()
            
            val move = SetMove(Piece(color, game.selectedShape.get(), game.selectedRotation.get(), game.selectedFlip.get(), Coordinates(x, y)))
            GameRuleLogic.validateSetMove(board.get(), move)
            fire(HumanMoveAction(move))
        } else {
            logger.debug("Set-Move from GUI at [$x,$y] seems invalid")
        }*/
    }
}
