package sc.gui.view

import javafx.animation.Animation
import javafx.animation.FadeTransition
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableDoubleValue
import javafx.beans.value.ObservableValue
import javafx.geometry.Insets
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.util.Duration
import org.slf4j.LoggerFactory
import sc.api.plugins.Team
import sc.gui.AppStyle
import sc.gui.controller.HumanMoveAction
import sc.gui.model.GameModel
import sc.plugin2022.*
import sc.plugin2022.util.Constants
import sc.util.listenImmediately
import tornadofx.*
import java.lang.ref.WeakReference
import kotlin.random.Random

private val logger = LoggerFactory.getLogger(BoardView::class.java)

const val pieceOpacity = 0.9
val transitionDuration = Duration(500.0)

val animationInterval = Timeline(KeyFrame(Duration.seconds(0.1), {
    animationFunctions.removeIf {
        // Remove invalid WeakReferences
        it.get()?.let {
            it(null)
            false
        } ?: true
    }
})).apply {
    cycleCount = Animation.INDEFINITE
    play()
}
private val animationFunctions = ArrayList<WeakReference<(Int?) -> Unit>>()

// this custom class is required to be able to shrink upsized images back to smaller sizes
// see: https://stackoverflow.com/a/35202191/9127322
class ResizableImageView(sizeProperty: ObservableValue<Number>): ImageView() {
    init {
        fitWidthProperty().bind(sizeProperty)
        fitHeightProperty().bind(imageProperty().doubleBinding(sizeProperty) {
            val size = sizeProperty.value.toDouble()
            it?.let { it.height * size / it.width } ?: size
        })
    }
    
    override fun minHeight(width: Double): Double = 16.0
    override fun minWidth(height: Double): Double = 16.0
    override fun isResizable(): Boolean = true
}

class PieceImage(private val sizeProperty: ObservableDoubleValue, private val content: String): StackPane() {
    var height = 0
        private set
    private val animate = ::nextFrame
    
    init {
        addChild(content)
        animationFunctions.add(WeakReference(animate))
    }
    
    val frameCount = if(content == "seestern" || content == "herzmuschel") 20 else 16
    var frame = Random.nextInt(0, frameCount)
    fun nextFrame(forceFrame: Int?) {
        (children.firstOrNull() as? ResizableImageView)?.removePseudoClass("frame$frame")
        frame = forceFrame ?: (frame + 1).mod(frameCount)
        (children?.firstOrNull() as? ResizableImageView)?.addPseudoClass("frame$frame")
    }
    
    fun updateHeight(newHeight: Int) {
        while (height < newHeight) {
            addChild("blank")
        }
        if (newHeight < height) {
            children.subList(0, height - newHeight).forEach { node ->
                node.fade(transitionDuration, 0.0).setOnFinished {
                    children.remove(node)
                }
            }
            height = newHeight
        }
    }
    
    fun addChild(graphic: String) {
        height++
        children.add(0, ResizableImageView(sizeProperty).apply {
            addClass(graphic)
            if(graphic == "herzmuschel")
                sizeProperty.listenImmediately {
                    translateX = -it.toDouble() / 11
                    translateY = -it.toDouble() / 3
                }
            if(graphic == "robbe")
                sizeProperty.listenImmediately {
                    translateX = it.toDouble() / 11
                }
        })
    }
    
    override fun toString(): String = "PieceImage@${Integer.toHexString(hashCode())}(content = $content)"
}

class BoardView: View() {
    
    private val gameModel: GameModel by inject()
    private val pieces = HashMap<Coordinates, PieceImage>()
    
    private val size = doubleProperty(16.0)
    private val gridSize
        get() = size.value / Constants.BOARD_SIZE
    private val calculatedBlockSize = size.doubleBinding { gridSize * 0.9 }
    
    private val ambers = Team.values().associateWith { ArrayList<Node>() }
    private val rootStack: StackPane by lazy { (grid.scene.root as BorderPane).center as StackPane }
    
    val grid = gridpane {
        isGridLinesVisible = true
        paddingAll = AppStyle.spacing
        maxHeightProperty().bind(size)
        maxWidthProperty().bind(size)
        val stateListener = ChangeListener<GameState?> { _, oldState, state ->
            clearTargetHighlights()
            if (state == null) {
                ambers.values.flatten().forEach { rootStack.children.remove(it) }
                ambers.values.forEach { it.clear() }
                isGridLinesVisible = false
                children.clear()
                pieces.clear()
                isGridLinesVisible = true
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
                            piece.updateHeight(newHeight)
                    }
                    piece.move(transitionDuration, Point2D(move.delta.dx * gridSize, move.delta.dy * gridSize)) {
                        setOnFinished {
                            piece.translateX = 0.0
                            piece.translateY = 0.0
                            piece.gridpaneConstraints { columnRowIndex(move.to.x, move.to.y) }
                            logger.trace("Piece $piece finished animating to ${state.board[move.to]} at ${move.to}")
                            println("Moving $piece while highlighting $currentHighlight onto $coveredPiece")
                            if (currentHighlight != null && currentHighlight in arrayOf(piece, coveredPiece)) {
                                highlightTargets(move.to)
                                lockedHighlight = move.to
                            }
                            children.remove(coveredPiece)
                            if (lockedHighlight == move.to)
                                lockedHighlight = null
                            if (newHeight == null) {
                                Platform.runLater {
                                    val bounds = piece.localToScene(piece.boundsInLocal)
                                    val teamAmbers = ambers[state.otherTeam] ?: return@runLater
                                    while (teamAmbers.size < state.getPointsForTeam(state.otherTeam))
                                        Group(PieceImage(calculatedBlockSize, "amber")).apply {
                                            opacity = 0.0
                                            rootStack.add(this)
                                            val alignLeft = oldState?.board?.get(move.from)?.team == Team.ONE
                                            StackPane.setAlignment(this, if (alignLeft) Pos.TOP_LEFT else Pos.TOP_RIGHT)
                                            translateX = bounds.centerX - (calculatedBlockSize.value * 0.5).let { if (alignLeft) it else scene.width - it }
                                            translateY = bounds.centerY - calculatedBlockSize.value / 2 - 56
                                            val position = teamAmbers.size
                                            teamAmbers.add(this)
                                            fade(transitionDuration, pieceOpacity).setOnFinished {
                                                val xOffset = { size: Number -> (position * (size.toDouble() / 3) + AppStyle.spacing).let { if (alignLeft) it else -it } }
                                                move(transitionDuration.multiply(2.0), Point2D(xOffset(calculatedBlockSize.value), 0.0)).setOnFinished {
                                                    translateXProperty().bind(calculatedBlockSize.doubleBinding { xOffset(it!!) })
                                                }
                                            }
                                        }
                                }
                                piece.updateHeight(0)
                                removePiece(piece)
                            } else {
                                piece.updateHeight(newHeight)
                            }
                        }
                    }
                }
            }
            state.board.forEach { (coords, piece) ->
                pieces.computeIfAbsent(coords) {
                    createPiece(piece.type).also { pieceImage ->
                        pieceImage.opacity = 0.0
                        pieceImage.updateHeight(piece.count)
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
                    image.fade(transitionDuration, pieceOpacity * when {
                        piece.team != state.currentTeam -> 0.6
                        gameModel.atLatestTurn.value -> 1.0
                        else -> 0.8
                    })
                }
            }
        }
        Platform.runLater {
            gameModel.gameState.addListener(stateListener)
            stateListener.changed(null, null, gameModel.gameState.value)
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
    
    /** Whether the piece at [coords] could be selected for a human move.. */
    private fun isSelectable(coords: Coordinates) =
            pieces[coords]?.opacity == pieceOpacity && gameModel.isHumanTurn.value
    
    private var lockedHighlight: Coordinates? = null
    private var currentHighlight: Node? = null
    private var targetHighlights = ArrayList<Node>()
    
    private fun createPiece(type: PieceType): PieceImage =
            PieceImage(calculatedBlockSize, type.name.lowercase()).apply {
                setOnMouseEntered {
                    if (lockedHighlight == null) {
                        addClass(AppStyle.hoverColor)
                        highlightTargets(gridCoordinates)
                        currentHighlight = this
                        it.consume()
                    } else if (lockedHighlight != gridCoordinates) {
                        addClass(AppStyle.softHoverColor)
                    }
                }
                setOnMouseExited {
                    if (lockedHighlight != gridCoordinates)
                        removeClass(AppStyle.hoverColor, AppStyle.softHoverColor)
                    if (lockedHighlight == null && !isSelectable(gridCoordinates))
                        clearTargetHighlights()
                    currentHighlight = null
                    it.consume()
                }
                onLeftClick {
                    lockedHighlight = gridCoordinates.takeUnless { it == lockedHighlight || !isSelectable(it) }?.also {
                        addClass(AppStyle.hoverColor)
                        highlightTargets(it)
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
        gameModel.gameState.value?.board?.possibleMovesFrom(position)?.map {
            val target = position + it
            val node = Region().addClass(AppStyle.hoverColor).apply {
                onLeftClick {
                    if (gameModel.atLatestTurn.value && gameModel.isHumanTurn.value && gameModel.gameState.value?.board?.get(position)?.team == gameModel.gameState.value?.currentTeam) {
                        fire(HumanMoveAction(Move(position, target).also { logger.debug("Human move: $it") }))
                    }
                }
            }
            grid.add(node, target.x, target.y)
            node
        }?.let { targetHighlights.addAll(it) }
    }
}

val Node.gridCoordinates
    get() = Coordinates(GridPane.getColumnIndex(this), GridPane.getRowIndex(this))
