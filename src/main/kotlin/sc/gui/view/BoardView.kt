package sc.gui.view

import javafx.animation.FadeTransition
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableDoubleValue
import javafx.beans.value.ObservableValue
import javafx.geometry.Insets
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.util.Duration
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.GameController
import sc.gui.controller.HumanMoveAction
import sc.plugin2022.*
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
        addChild(content.toString().lowercase())
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
        val moewe = graphic == "moewe"
        children.add(0, ResizableImageView(
                sizeProperty,
                ResourceLookup(this)["/graphics/$graphic.png"],
                if (moewe) 1.5 else 1.0,
        ).also {
            if (moewe)
                it.scaleX = -1.0
        })
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
    
    var lockedHighlight: Coordinates? = null
    var targetHighlights = ArrayList<Node>()
    
    val grid = gridpane {
        isGridLinesVisible = true
        paddingAll = AppStyle.spacing
        maxHeightProperty().bind(size)
        maxWidthProperty().bind(size)
        val listener = ChangeListener<GameState?> { _, oldState, state ->
            clearTargetHighlights()
            if (state == null) {
                children.removeAll(pieces.values)
                pieces.clear()
                return@ChangeListener
            }
            // TODO finish pending animations
            logger.trace("New state for board: ${state.longString()}")
            val lastMove = arrayOf(state to state.lastMove, oldState to oldState?.lastMove?.reversed()).maxByOrNull {
                it.first?.turn ?: -1
            }!!.second
            lastMove?.let { move ->
                pieces.remove(move.from)?.let { piece ->
                    val coveredPiece = pieces.remove(move.to)
                    val newHeight = state.board[move.to]?.count
                    if (newHeight != null) {
                        pieces[move.to] = piece
                        if (newHeight < piece.height)
                            piece.setHeight(newHeight)
                    }
                    piece.move(transitionDuration, Point2D(move.delta.dx * gridSize, move.delta.dy * gridSize)) {
                        setOnFinished {
                            piece.translateX = 0.0
                            piece.translateY = 0.0
                            piece.gridpaneConstraints { columnRowIndex(move.to.x, move.to.y) }
                            logger.trace("Piece $piece finished animating to ${state.board[move.to]}")
                            children.remove(coveredPiece)
                            if (newHeight == null) {
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
                    createPiece(piece.type).also { pieceImage ->
                        pieceImage.opacity = 0.0
                        pieceImage.setHeight(piece.count)
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
                    image.scaleX = piece.team.direction.toDouble()
                    image.background = Background(BackgroundFill(c(if (piece.team.index == 0) "red" else "blue", 0.5), CornerRadii.EMPTY, Insets.EMPTY))
                    image.fade(transitionDuration, if (piece.team == state.currentTeam) 0.9 else 0.5)
                }
            }
        }
        Platform.runLater {
            gameController.gameState.addListener(listener)
            listener.changed(null, null, gameController.gameState.value)
        }
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
    
    private fun createPiece(type: PieceType): PieceImage =
            PieceImage(calculatedBlockSize, type).apply {
                fun coords() = Coordinates(GridPane.getColumnIndex(this), GridPane.getRowIndex(this))
                setOnMouseEntered {
                    if (lockedHighlight == null) {
                        addClass(AppStyle.hoverColor)
                        highlightTargets(coords())
                        it.consume()
                    } else if (lockedHighlight != coords()) {
                        addClass(AppStyle.softHoverColor)
                    }
                }
                setOnMouseExited {
                    if (lockedHighlight != coords())
                        removeClass(AppStyle.hoverColor, AppStyle.softHoverColor)
                    it.consume()
                }
                onLeftClick {
                    val coords = coords()
                    lockedHighlight = if (lockedHighlight == coords) {
                        null
                    } else {
                        addClass(AppStyle.hoverColor)
                        highlightTargets(coords)
                        coords
                    }
                }
            }
    
    private fun clearTargetHighlights() {
        lockedHighlight?.let { pieces[it] }?.removeClass(AppStyle.hoverColor)
        lockedHighlight = null
        grid.children.removeAll(targetHighlights)
        targetHighlights.clear()
    }
    
    private fun highlightTargets(position: Coordinates) {
        clearTargetHighlights()
        gameController.gameState.value?.board?.possibleMovesFrom(position)?.map {
            val target = position + it
            val node = Region().addClass(AppStyle.hoverColor).apply {
                onLeftClick {
                    if (gameController.isHumanTurn.value && gameController.gameState.value?.board?.get(position)?.team == gameController.gameState.value?.currentTeam) {
                        fire(HumanMoveAction(Move(position, target).also { logger.debug("Human move: $it") }))
                    }
                }
            }
            grid.add(node, target.x, target.y)
            node
        }?.let { targetHighlights.addAll(it) }
    }
}
