package sc.gui.view

import javafx.animation.Animation
import javafx.animation.FadeTransition
import javafx.animation.KeyFrame
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableDoubleValue
import javafx.beans.value.ObservableValue
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.transform.Rotate
import javafx.util.Duration
import mu.KotlinLogging
import sc.api.plugins.Team
import sc.gui.AppStyle
import sc.gui.controller.HumanMoveAction
import sc.gui.model.AppModel
import sc.gui.model.GameModel
import sc.plugin2022.*
import sc.plugin2022.util.Constants
import sc.util.listenImmediately
import tornadofx.*
import java.lang.ref.WeakReference
import kotlin.math.pow
import kotlin.random.Random

private val logger = KotlinLogging.logger { }

val animationDuration = Duration.seconds(0.1)
val transitionDuration = animationDuration.multiply(8.0)
val animationInterval = timeline {
    cycleCount = Animation.INDEFINITE
    this += KeyFrame(animationDuration, {
        animationFunctions.removeIf {
            // Remove invalid WeakReferences
            it.get()?.let {
                it()
                false
            } ?: true
        }
    })
}
private val animationFunctions = ArrayList<WeakReference<() -> Unit>>()

// this custom class is required to be able to shrink upsized images back to smaller sizes
// see: https://stackoverflow.com/a/35202191/9127322
class ResizableImageView(sizeProperty: ObservableValue<Number>): ImageView() {
    init {
        fitWidthProperty().bind(sizeProperty)
        isPreserveRatio = true
    }
    
    override fun minHeight(width: Double): Double = 16.0
    override fun minWidth(height: Double): Double = 16.0
    override fun isResizable(): Boolean = true
    
    override fun toString(): String =
            "${styleClass.joinToString(".")}@${Integer.toHexString(hashCode())}${pseudoClassStates.joinToString("") { ":$it" }}"
}

class PieceImage(private val sizeProperty: ObservableDoubleValue, private val content: String): StackPane() {
    var height = 0
        private set
    private val animateFn = ::animate
    
    init {
        addChild(content)
        animationFunctions.add(WeakReference(animateFn))
        viewOrder = 1.0
    }
    
    val frameCount = if(content == "seestern" || content == "herzmuschel") 20 else 16
    var frame = Random.nextInt(1, frameCount)
    fun animate() {
        if(AppModel.animate.value || frame > 0)
            frame = nextFrame()
    }
    
    fun nextFrame(prefix: String = "idle", oldFrame: Int = frame, randomize: Boolean = true, remove: Boolean = false): Int {
        val img = children.lastOrNull() as? ResizableImageView
        img?.removePseudoClass("$prefix$oldFrame")
        return if(!remove)
            (oldFrame.inc() + if(randomize) Random.nextInt(1, 5).div(5) else 0)
                    .mod(frameCount).also { newFrame ->
                        img?.addPseudoClass("$prefix$newFrame")
                    }
        else -1
    }
    
    fun updateHeight(newHeight: Int) {
        while(height < newHeight) {
            addChild("blank")
        }
        if(newHeight < height) {
            children.subList(0, height - newHeight).forEach { node ->
                node.fade(transitionDuration, 0.0).setOnFinished {
                    children.remove(node)
                }
            }
            height = newHeight
        }
        animate()
    }
    
    fun addChild(graphic: String) {
        height++
        logger.trace { "$this: Adding $graphic for height $height" }
        children.add(0, ResizableImageView(sizeProperty).apply {
            addClass(graphic)
            if(graphic == "herzmuschel")
                sizeProperty.listenImmediately {
                    translateX = -it.toDouble() / 8
                    translateY = -it.toDouble() / 3
                }
            if(graphic == "robbe")
                sizeProperty.listenImmediately {
                    translateX = it.toDouble() / 11
                }
        })
    }
    
    override fun toString(): String =
            "$content@${Integer.toHexString(hashCode())}" +
            pseudoClassStates.joinToString("") { ":$it" } +
            children
}

class BoardView: View() {
    private val gameModel: GameModel by inject()
    private val pieces = HashMap<Coordinates, PieceImage>()
    
    private val size = doubleProperty(16.0)
    private val gridSize
        get() = size.value / Constants.BOARD_SIZE
    private val calculatedBlockSize = size.doubleBinding { gridSize * 0.9 }
    
    private val ambers = Team.values().associateWith { ArrayList<Node>() }
    private val rootStack: Pane by lazy { root.parent.run { if(parent != null) parent else this } as Pane }
    
    val grid = gridpane {
        paddingAll = AppStyle.spacing
        maxHeightProperty().bind(size)
        maxWidthProperty().bind(size)
        val stateListener = ChangeListener<GameState?> { _, oldState, state ->
            clearTargetHighlights()
            if(state == null) {
                ambers.values.flatten().forEach { rootStack.children.remove(it) }
                ambers.values.forEach { it.clear() }
                children.remove(Constants.BOARD_SIZE.toDouble().pow(2).toInt(), children.size)
                pieces.clear()
                return@ChangeListener
            }
            rotate = state.startTeam.index * 180.0
            rotationAxis = Rotate.Y_AXIS
            // TODO finish pending movements
            logger.trace("New state for board: ${state.longString()}")
            val lastMove = arrayOf(state to state.lastMove, oldState to oldState?.lastMove?.reversed()).maxByOrNull {
                it.first?.turn ?: -1
            }!!.second
            // TODO tornadofx: nested CSS, Color.derive with defaults, configProperty, CSS important, selectClass/Pseudo
            // TODO sounds for figure movements
            lastMove?.let { move ->
                pieces.remove(move.from)?.let { piece ->
                    val coveredPiece = pieces.remove(move.to)
                    val newHeight = state.board[move.to]?.count
                    if(newHeight != null) {
                        pieces[move.to] = piece
                        if(newHeight < piece.height)
                            piece.updateHeight(newHeight)
                    }
                    val robbe = oldState?.board?.get(move.from)?.type == PieceType.Robbe
                    parallelTransition {
                        var cur = -1
                        val moveType = if(coveredPiece != null) "consume" else "move"
                        timeline {
                            cycleCount = if(robbe) 12 else 8
                            this += KeyFrame(animationDuration, {
                                cur = piece.nextFrame(moveType, cur, randomize = false)
                            })
                        }.apply { setOnFinished { piece.nextFrame(moveType, cur, remove = true) } }
                        children += piece.move(transitionDuration - animationDuration.multiply(2.0), Point2D(move.delta.dx * gridSize, move.delta.dy * gridSize), play = false) {
                            delay = animationDuration.multiply(if(robbe) 4.0 else 1.0)
                            setOnFinished {
                                piece.translateX = 0.0
                                piece.translateY = 0.0
                                piece.gridpaneConstraints { columnRowIndex(move.to.x, move.to.y) }
                                logger.trace("Tile $piece finished transition to ${state.board[move.to]} covering $coveredPiece at ${move.to} (highlight: $currentHighlight)")
                                if(currentHighlight != null && currentHighlight in arrayOf(piece, coveredPiece)) {
                                    highlightTargets(move.to)
                                    lockedHighlight = move.to
                                }
                                this@gridpane.children.remove(coveredPiece)
                                if(lockedHighlight == move.to)
                                    lockedHighlight = null
                                if(newHeight == null) {
                                    Platform.runLater {
                                        val bounds = piece.localToScene(piece.layoutBounds)
                                        val teamAmbers = oldState?.getPointsForTeam(state.otherTeam) ?: return@runLater
                                        (teamAmbers until state.getPointsForTeam(state.otherTeam)).forEach { position ->
                                            Group(PieceImage(calculatedBlockSize, "amber")).apply {
                                                opacity = 0.0
                                                rootStack.add(this)
                                                val alignLeft = oldState.board[move.from]?.team == Team.ONE
                                                StackPane.setAlignment(this, if(alignLeft) Pos.TOP_LEFT else Pos.TOP_RIGHT)
                                                translateX = bounds.centerX - (calculatedBlockSize.value * 0.5).let { if(alignLeft) it else rootStack.width - it }
                                                translateY = bounds.centerY - calculatedBlockSize.value / 2
                                                fade(transitionDuration, AppStyle.pieceOpacity).setOnFinished {
                                                    val xOffset = { size: Number -> (size.toDouble() / 2 + position * size.toDouble() / 3).let { if(alignLeft) it else -it } }
                                                    ambers[state.otherTeam]?.takeIf { it.size <= position }
                                                            ?.let { ambers ->
                                                                ambers.add(this)
                                                                move(transitionDuration.multiply(2.0), Point2D(xOffset(calculatedBlockSize.value), 0.0)).setOnFinished {
                                                                    translateXProperty().bind(calculatedBlockSize.doubleBinding { xOffset(it!!) })
                                                                }
                                                            } ?: run {
                                                        fade(transitionDuration, 0).setOnFinished {
                                                            rootStack.children.remove(this)
                                                        }
                                                    }
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
            while(iter.hasNext()) {
                val (c, image) = iter.next()
                val piece = state.board[c]
                if(piece == null) {
                    removePiece(image)
                    iter.remove()
                } else {
                    image.addClass(piece.team.color)
                    image.scaleX = -(piece.team.index.xor(state.startTeam.index) * 2 - 1.0)
                    image.fade(transitionDuration, AppStyle.pieceOpacity * when {
                        piece.team != state.currentTeam -> 0.6
                        gameModel.atLatestTurn.value && gameModel.gameState.value?.isOver == false -> 1.0
                        else -> 0.8
                    })
                }
            }
        }
        (0 until Constants.BOARD_SIZE).forEach { index ->
            constraintsForRow(index).percentHeight = 100.0 / Constants.BOARD_SIZE
            constraintsForColumn(index).percentWidth = 100.0 / Constants.BOARD_SIZE
            (0 until Constants.BOARD_SIZE).forEach { row ->
                add(Pane().addClass("grid").apply {
                    setOnMouseEntered { event ->
                        pieces[gridCoordinates]?.run {
                            if(lockedHighlight == null) {
                                highlight(this)
                                event.consume()
                            } else if(lockedHighlight != gridCoordinates) {
                                addClass(AppStyle.gridHover)
                            }
                        }
                    }
                    setOnMouseExited { event ->
                        pieces[gridCoordinates]?.run {
                            if(lockedHighlight == null) {
                                if(!isSelectable(gridCoordinates)) {
                                    clearTargetHighlights()
                                    removeClass(AppStyle.gridHover)
                                    currentHighlight = null
                                }
                            } else if(lockedHighlight != gridCoordinates) {
                                removeClass(AppStyle.gridHover)
                            }
                            event.consume()
                        }
                    }
                    onLeftClick {
                        val coords = gridCoordinates
                        lockedHighlight =
                                if(coords == lockedHighlight || !isSelectable(coords)) {
                                    pieces[coords]?.let { highlight(it, lock = false, updateTargetHighlights = coords != lockedHighlight) }
                                    null
                                } else {
                                    coords.also { pieces[coords]?.let { highlight(it, lock = true) } }
                                }
                        logger.trace { "Clicked $coords (lock at $lockedHighlight, current $currentHighlight)" }
                    }
                }, index, row)
            }
        }
        Platform.runLater {
            gameModel.gameState.addListener(stateListener)
            stateListener.changed(null, null, gameModel.gameState.value)
        }
    }
    override val root = stackpane {
        alignment = Pos.CENTER
        size.bind(Bindings.min(widthProperty(), heightProperty()))
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
            pieces[coords]?.opacity == AppStyle.pieceOpacity && gameModel.isHumanTurn.value
    
    private var lockedHighlight: Coordinates? = null
    private var currentHighlight: Node? = null
    private var targetHighlights = ArrayList<Node>()
    
    private fun createPiece(type: PieceType): PieceImage =
            PieceImage(calculatedBlockSize, type.name.lowercase())
    
    private fun highlight(node: Node, lock: Boolean = false, updateTargetHighlights: Boolean = true) {
        currentHighlight?.removeClass(AppStyle.gridHover, AppStyle.gridLock)
        if(updateTargetHighlights)
            highlightTargets(node.gridCoordinates)
        node.addClass(if(lock) AppStyle.gridLock else AppStyle.gridHover)
        currentHighlight = node
    }
    
    private fun clearTargetHighlights() {
        lockedHighlight?.let { pieces[it] }?.removeClass(AppStyle.gridHover)
        lockedHighlight = null
        grid.children.removeAll(targetHighlights)
        targetHighlights.clear()
    }
    
    private fun highlightTargets(position: Coordinates) {
        clearTargetHighlights()
        gameModel.gameState.value?.board?.possibleMovesFrom(position)?.map {
            val target = position + it
            val node = Region().addClass(AppStyle.gridHover).apply {
                onLeftClick {
                    if(gameModel.atLatestTurn.value && gameModel.isHumanTurn.value && gameModel.gameState.value?.board?.get(position)?.team == gameModel.gameState.value?.currentTeam) {
                        fire(HumanMoveAction(Move(position, target).also { logger.debug("Human Move: $it") }))
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
