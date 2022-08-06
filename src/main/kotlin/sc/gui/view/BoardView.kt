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
import javafx.scene.Node
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.transform.Rotate
import javafx.util.Duration
import mu.KotlinLogging
import sc.api.plugins.Coordinates
import sc.api.plugins.Team
import sc.gui.AppStyle
import sc.gui.controller.HumanMoveAction
import sc.gui.model.AppModel
import sc.gui.model.GameModel
import sc.plugin2023.*
import sc.plugin2023.Field
import sc.plugin2023.util.PluginConstants
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
    private val animateFn = ::animate
    
    init {
        alignment = Pos.BOTTOM_CENTER
        addChild(content)
        animationFunctions.add(WeakReference(animateFn))
        viewOrder = 1.0
    }
    
    val frameCount = 20
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
    
    fun addChild(graphic: String, index: Int? = null) {
        logger.trace { "$this: Adding $graphic" }
        children.add(index ?: children.size, ResizableImageView(sizeProperty).apply {
            addClass(graphic)
            if(graphic == "penguin")
                sizeProperty.listenImmediately {
                    this.translateY = -it.toDouble() / 5
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
    private val boardSize = PluginConstants.BOARD_SIZE * 2
    private val gridSize
        get() = size.value / boardSize * 0.8
    private val calculatedBlockSize = size.doubleBinding { gridSize * 1.9 }
    
    override val root = hbox {
        this.alignment = Pos.CENTER
        size.bind(Bindings.min(widthProperty(), heightProperty().multiply(1.6)))
        anchorpane {
            this.paddingAll = AppStyle.spacing
            val stateListener = ChangeListener<GameState?> { _, oldState, state ->
                clearTargetHighlights()
                if(state == null) {
                    children.remove(PluginConstants.BOARD_SIZE.toDouble().pow(2).toInt(), children.size)
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
                        pieces[move.to] = piece
                        parallelTransition {
                            var cur = -1
                            val moveType = if((oldState?.board?.getOrEmpty(move.to)?.fish ?: 0) > 1) "consume" else "move"
                            timeline {
                                cycleCount = 8
                                this += KeyFrame(animationDuration, {
                                    cur = piece.nextFrame(moveType, cur, randomize = false)
                                })
                            }.apply { setOnFinished { piece.nextFrame(moveType, cur, remove = true) } }
                            children += piece.move(transitionDuration - animationDuration.multiply(2.0), Point2D(move.delta!!.dx * gridSize, move.delta!!.dy * gridSize), play = false) {
                                delay = animationDuration.multiply(1.0)
                                setOnFinished {
                                    piece.translateX = 0.0
                                    piece.translateY = 0.0
                                    addPiece(piece, move.to)
                                    logger.trace("Tile $piece finished transition to ${state.board[move.to]} covering $coveredPiece at ${move.to} (highlight: $currentHighlight)")
                                    if(currentHighlight != null && currentHighlight in arrayOf(piece, coveredPiece)) {
                                        highlightTargets(move.to)
                                        lockedHighlight = move.to
                                    }
                                    grid.children.remove(coveredPiece)
                                    if(lockedHighlight == move.to)
                                        lockedHighlight = null
                                }
                            }
                        }
                    }
                }
                state.board.forEach<Coordinates, Field> { (hexCoords, piece) ->
                    if(piece.isEmpty)
                        return@forEach
                    pieces.computeIfAbsent(hexCoords) { coordinates ->
                        createPiece("ice").apply {
                            if(piece.fish > 0) {
                                addChild("fish")
                                label(piece.fish.toString())
                            }
                            addPiece(this, coordinates)
                            
                            setOnMouseExited { event ->
                                if(lockedHighlight == null) {
                                    if(!isSelectable(coordinates)) {
                                        clearTargetHighlights()
                                        removeClass(AppStyle.gridHover)
                                        currentHighlight = null
                                    }
                                } else if(lockedHighlight != coordinates) {
                                    removeClass(AppStyle.gridHover)
                                }
                                event.consume()
                            }
                        }
                    }
                }
                val iter = pieces.iterator()
                while(iter.hasNext()) {
                    val (coordinates, image) = iter.next()
                    val field = state.board[coordinates]
                    val piece = field.penguin
                    when {
                        piece != null -> {
                            if(!image.children.last().hasClass("penguin")) {
                                image.children.remove(1, image.children.size)
                                image.addChild("penguin")
                            }
                            image.addClass(piece.color)
                            image.scaleX = -(piece.index.xor(state.startTeam.index) * 2 - 1.0)
                            image.fade(transitionDuration, AppStyle.pieceOpacity * when {
                                piece != state.currentTeam -> 0.6
                                gameModel.atLatestTurn.value && gameModel.gameState.value?.isOver == false -> 1.0
                                else -> 0.8
                            })
                            
                            image.setOnMouseEntered { event ->
                                if(lockedHighlight == null) {
                                    highlight(image, updateTargetHighlights = coordinates)
                                    event.consume()
                                } else if(lockedHighlight != coordinates) {
                                    image.addClass(AppStyle.gridHover)
                                }
                            }
                            image.onLeftClick {
                                lockedHighlight =
                                        if(coordinates == lockedHighlight || !isSelectable(coordinates)) {
                                            pieces[coordinates]?.let { highlight(it, false, coordinates) }
                                            null
                                        } else {
                                            coordinates.also { pieces[coordinates]?.let { highlight(it, true, coordinates) } }
                                        }
                                logger.trace { "Clicked $coordinates (lock at $lockedHighlight, current $currentHighlight)" }
                            }
                        }
                        field.fish > 0 -> {
                            if(!image.children.last().hasClass("fish")) {
                                image.children.remove(1, image.children.size)
                                image.addChild("fish")
                                image.label(field.fish.toString())
                                Team.values().forEach { image.removeClass(it.color, true) }
                                image.opacity = 1.0
                                image.scaleX = 1.0
                            }
                            image.setOnMouseEntered { event ->
                                if(lockedHighlight == null) {
                                    highlight(image)
                                    event.consume()
                                } else if(lockedHighlight != coordinates) {
                                    image.addClass(AppStyle.gridHover)
                                }
                            }
                            if(state.canPlacePenguin(coordinates)) {
                                image.onLeftClick {
                                    humanMove(Move.set(coordinates))
                                }
                            }
                        }
                        else -> {
                            removePiece(image)
                            iter.remove()
                        }
                    }
                }
            }
            Platform.runLater {
                gameModel.gameState.addListener(stateListener)
                stateListener.changed(null, null, gameModel.gameState.value)
            }
        }
    }
    
    val grid: Pane = root.children.first() as Pane
    
    private fun removePiece(piece: Node, durationMultiplier: Double = 1.0): FadeTransition =
            piece.fade(transitionDuration.multiply(durationMultiplier), 0.0) {
                setOnFinished {
                    grid.children.remove(piece)
                }
            }
    
    /** Whether the piece at [coords] could be selected for a human move.. */
    private fun isSelectable(coords: Coordinates) =
            pieces[coords]?.opacity == AppStyle.pieceOpacity && gameModel.isHumanTurn.value && gameModel.gameState.value?.penguinsPlaced == true
    
    private var lockedHighlight: Coordinates? = null
    private var currentHighlight: Node? = null
    private var targetHighlights = ArrayList<Node>()
    
    private fun createPiece(type: String): PieceImage =
            PieceImage(calculatedBlockSize, type)
    
    private fun highlight(node: PieceImage, lock: Boolean = false, updateTargetHighlights: Coordinates? = null) {
        currentHighlight?.removeClass(AppStyle.gridHover, AppStyle.gridLock)
        updateTargetHighlights?.takeIf { it != lockedHighlight }?.let { highlightTargets(it) }
        node.addClass(if(lock) AppStyle.gridLock else AppStyle.gridHover)
        currentHighlight = node
    }
    
    private fun clearTargetHighlights() {
        lockedHighlight?.let { pieces[it] }?.removeClass(AppStyle.gridHover)
        lockedHighlight = null
        targetHighlights.forEach {
            it.removeClass(AppStyle.gridHover)
        }
        targetHighlights.clear()
    }
    
    private fun highlightTargets(position: Coordinates) {
        clearTargetHighlights()
        
        gameModel.gameState.value?.let { state ->
            state.board.possibleMovesFrom(position)
                    .also { logger.debug { "highlighting possible moves from $position: $it" } }
                    .mapNotNull { move ->
                pieces[move.to]?.apply {
                    addClass(AppStyle.gridHover)
                    if(state.board[position].penguin == state.currentTeam && state.penguinsPlaced) {
                        onLeftClick {
                            humanMove(move)
                        }
                    }
                }
            }.let { targetHighlights.addAll(it) }
        }
    }
    
    private fun humanMove(move: Move) {
        if(gameModel.atLatestTurn.value && gameModel.isHumanTurn.value) {
            fire(HumanMoveAction(move.also { logger.debug("Human Move: $it") }))
        }
    }
    
    fun addPiece(node: Region, coordinates: Coordinates) {
        if(grid.children.contains(node))
            logger.warn("Attempting to add duplicate grid child at $coordinates: $node")
        else
            grid.add(node)
        size.listenImmediately {
            //logger.trace("$node at $coordinates block size: $it")
            node.anchorpaneConstraints {
                leftAnchor = coordinates.x * gridSize
                bottomAnchor = (PluginConstants.BOARD_SIZE - coordinates.y) * gridSize
            }
        }
    }
}
