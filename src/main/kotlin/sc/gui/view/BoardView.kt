package sc.gui.view

import javafx.animation.FadeTransition
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableDoubleValue
import javafx.beans.value.ObservableValue
import javafx.geometry.Insets
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.*
import javafx.util.Duration
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.GameController
import sc.plugin2022.Coordinates
import sc.plugin2022.GameState
import sc.plugin2022.PieceType
import sc.plugin2022.util.Constants
import tornadofx.*

private val logger = LoggerFactory.getLogger(BoardView::class.java)

// this custom class is required to be able to shrink upsized images back to smaller sizes
// see: https://stackoverflow.com/a/35202191/9127322
class ResizableImageView(sizeProperty: ObservableValue<Number>, resource: String, scaling: Double = 1.0): ImageView() {
    init {
        imageProperty().bind(sizeProperty.objectBinding {
            val size = it!!.toDouble()
            val scaledSize = size * scaling
            translateY = -size * (scaling - 1.0) / 5
            Image(resource, scaledSize, scaledSize, true, true)
        })
    }
    
    override fun prefHeight(width: Double): Double = image.height
    override fun minHeight(width: Double): Double = 16.0
    override fun prefWidth(height: Double): Double = image.width
    override fun minWidth(width: Double): Double = 16.0
    override fun isResizable(): Boolean = true
}

class PieceImage(private val sizeProperty: ObservableDoubleValue, private val content: PieceType): StackPane() {
    val height
        get() = children.size
    
    init {
        addChild(content.toString().toLowerCase())
    }
    
    fun setHeight(newHeight: Int) {
        while (height < newHeight) {
            addChild("blank")
        }
        if (newHeight < height) {
            children.remove(0, height - newHeight)
        }
    }
    
    fun addChild(graphic: String) {
        children.add(0, ResizableImageView(
                sizeProperty,
                ResourceLookup(this)["/graphics/$graphic.png"],
                if (graphic == "moewe") 1.5 else 1.0,
        ))
    }
    
    override fun toString(): String = "PieceImage@${Integer.toHexString(hashCode())}(content = $content)"
}

class BoardView: View() {
    
    private val gameController: GameController by inject()
    val pieces = HashMap<Coordinates, PieceImage>()
    
    val size = doubleProperty(16.0)
    val gridSize
        get() = size.value / Constants.BOARD_SIZE
    val calculatedBlockSize = size.doubleBinding { gridSize * 0.9 }
    
    val transitionDuration = Duration(500.0)
    
    val grid = gridpane {
        isGridLinesVisible = true
        paddingAll = AppStyle.spacing
        maxHeightProperty().bind(size)
        maxWidthProperty().bind(size)
        val listener = ChangeListener<GameState?> { _, oldState, state ->
            if (state == null)
                return@ChangeListener
            // TODO finish pending animations
            logger.trace("New state for board: ${state.longString()}")
            val lastMove = arrayOf(state to state.lastMove, oldState to oldState?.lastMove?.reverse()).maxByOrNull { it.first?.turn ?: -1 }!!.second
            lastMove?.let { move ->
                pieces.remove(move.start)?.let { piece ->
                    val coveredPiece = pieces.remove(move.destination)
                    val newHeight = state.board[move.destination]?.count
                    if(newHeight != null) {
                        pieces[move.destination] = piece
                        if(newHeight < piece.height)
                            piece.setHeight(newHeight)
                    }
                    piece.move(transitionDuration, Point2D(move.delta.dx * gridSize, move.delta.dy * gridSize)) {
                        setOnFinished {
                            piece.translateX = 0.0
                            piece.translateY = 0.0
                            piece.gridpaneConstraints { columnRowIndex(move.destination.x, move.destination.y) }
                            logger.trace("Piece $piece finished animating to ${state.board[move.destination]}")
                            children.remove(coveredPiece)
                            if(newHeight == null) {
                                piece.setHeight(0)
                                piece.addChild("amber")
                                removePiece(piece, 3.0)
                            } else {
                                piece.setHeight(newHeight)
                            }
                        }
                    }
                }
            }
            state.board.forEach { (coords, piece) ->
                pieces.computeIfAbsent(coords) {
                    createPiece(coords, piece.type).also { pieceImage ->
                        pieceImage.opacity = 0.0
                        pieceImage.setHeight(piece.count)
                        pieceImage.background = Background(BackgroundFill(c(if(piece.team.index == 0) "red" else "blue",0.5), CornerRadii.EMPTY, Insets.EMPTY))
                        add(pieceImage, coords.x, coords.y)
                    }
                }
            }
            val iter = pieces.iterator()
            while (iter.hasNext()) {
                val (c, image) = iter.next()
                val piece = state.board[c]
                if (piece == null) {
                    removePiece(image)
                    iter.remove()
                } else {
                    // TODO image.disableProperty().set(piece.team != state.currentTeam)
                    image.fade(transitionDuration, if (piece.team == state.currentTeam) 0.9 else 0.5)
                }
            }
        }
        gameController.gameState.addListener(listener)
        listener.changed(null, null, gameController.gameState.value)
        for (x in 0 until Constants.BOARD_SIZE) {
            constraintsForRow(x).percentHeight = 100.0 / Constants.BOARD_SIZE
            constraintsForColumn(x).percentWidth = 100.0 / Constants.BOARD_SIZE
        }
    }
    override val root = vbox(alignment = Pos.CENTER) {
        size.bind(Bindings.min(widthProperty(), heightProperty()))
        grid.vgrow = Priority.ALWAYS
        add(grid)
    }
    
    private fun removePiece(piece: Node, durationMultiplier: Double = 1.0): FadeTransition =
            piece.fade(transitionDuration.multiply(durationMultiplier), 0.0) {
                setOnFinished {
                    grid.children.remove(piece)
                }
            }
    
    private fun createPiece(coordinates: Coordinates, type: PieceType): PieceImage =
            PieceImage(calculatedBlockSize, type).apply {
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
            }
    
    fun handleClick(coordinates: Coordinates) {
        /* TODO human move actions
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
