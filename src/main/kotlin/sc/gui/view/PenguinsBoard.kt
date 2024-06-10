package sc.gui.view

import javafx.animation.KeyFrame
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.ChangeListener
import javafx.geometry.Orientation
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.effect.ColorAdjust
import javafx.scene.effect.Glow
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.transform.Rotate
import mu.KotlinLogging
import sc.api.plugins.Coordinates
import sc.api.plugins.IGameState
import sc.api.plugins.Team
import sc.gui.AppStyle
import sc.gui.controller.HumanMoveAction
import sc.gui.model.GameModel
import sc.plugin2023.Field
import sc.plugin2023.GameState
import sc.plugin2023.Move
import sc.plugin2023.util.PluginConstants
import sc.util.listenImmediately
import tornadofx.*

private val logger = KotlinLogging.logger { }

class BoardView: View() {
    private val gameModel: GameModel by inject()
    private val gameState: GameState?
        get() = gameModel.gameState.value as? GameState
    
    private val pieces = HashMap<Coordinates, PieceImage>()
    private val ice = HashMap<Coordinates, PieceImage>()
    
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
                    //children.remove(PluginConstants.BOARD_SIZE.toDouble().pow(2).toInt(), children.size)
                    grid.children.clear()
                    pieces.clear()
                    ice.clear()
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
                            val moveType =
                                    if((oldState?.board?.getOrEmpty(move.to)?.fish ?: 0) > 1) "consume"
                                    else "move"
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
                                    // TODO hack to fix ordering
                                    // set viewOrder in transition
                                    grid.children.remove(piece)
                                    addPiece(piece, move.to)
                                    logger.trace { "Tile $piece finished transition to ${state.board[move.to]} covering $coveredPiece at ${move.to} (highlight: $currentHighlight)" }
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
                state.board.forEach<Coordinates, Field> { (coordinates, field) ->
                    if(field.isEmpty) {
                        removePiece(pieces.remove(coordinates))
                        removePiece(ice.remove(coordinates))
                    } else {
                        val ice = ice.getOrPut(coordinates) {
                            createPiece("ice").also { addPiece(it, coordinates) }
                        }
                        val piece = field.penguin
                        if(field.fish > 0) {
                            removePiece(pieces.remove(coordinates))
                            if(ice.children.size < 2) {
                                ice.children.add(FlowPane(Orientation.HORIZONTAL, *(0 until field.fish).map {
                                    ResizableImageView(calculatedBlockSize.div(2)).addClass("fish")
                                }.toTypedArray()).apply {
                                    this.alignment = Pos.CENTER
                                    this.maxWidthProperty().bind(calculatedBlockSize.div(1))
                                    this.translateYProperty().bind(calculatedBlockSize.divide(-10))
                                    /* TODO why do they turn into lines?
                                       findings:
                                       - has to do with increasing y position
                                       - reducing the image width makes the issue much worse
                                       - unrelated to minHeight/minWidth of ImageView as well as "managed" property
                                    */
                                })
                            }
                            ice.onLeftClick {
                                if(gameState?.canPlacePenguin(coordinates) == true)
                                    humanMove(Move.set(coordinates))
                                else currentHighlightCoords?.let {
                                    if(state.board[it].penguin == state.currentTeam &&
                                       state.penguinsPlaced && targetHighlights.contains(ice))
                                        humanMove(Move(it, coordinates))
                                }
                            }
                            ice.setOnMouseEntered {
                                if(gameState?.canPlacePenguin(coordinates) == true)
                                    ice.addHover(team = gameState?.currentTeam)
                                (ice.effect as? ColorAdjust ?: ColorAdjust()).run {
                                    ice.effect = this
                                    brightness = -0.2
                                }
                            }
                            ice.setOnMouseExited {
                                (ice.effect as? ColorAdjust)?.takeIf { ice in targetHighlights }?.run {
                                    brightness = 0.0
                                } ?: ice.removeHover()
                            }
                            ice.children.first().effect = null
                        } else if(piece != null) {
                            if(ice.children.size > 1)
                                removePiece(ice.children.last(), parent = ice)
                            ice.children.first().effect = ColorAdjust(piece.colorAdjust, 0.0, 0.0, 0.0)
                            val penguin = pieces.getOrPut(coordinates) {
                                addPiece(createPiece("penguin"), coordinates)
                            }
                            penguin.scaleX = -(piece.index.xor(state.startTeam.index) * 2 - 1.0)
                            penguin.fade(transitionDuration, AppStyle.pieceOpacity * when {
                                piece != state.currentTeam -> 0.7
                                gameModel.atLatestTurn.value && gameState?.isOver == false -> 1.0
                                else -> 0.9
                            })
                            penguin.nextFrame()
                            penguin.setClass("inactive", piece != state.currentTeam)
                            
                            penguin.setOnMouseEntered { event ->
                                if(lockedHighlight == null) {
                                    highlight(penguin, updateTargetHighlights = coordinates)
                                    event.consume()
                                } else if(lockedHighlight != coordinates) {
                                    penguin.addHover()
                                }
                            }
                            penguin.onLeftClick {
                                lockedHighlight =
                                        if(coordinates == lockedHighlight || !isSelectable(coordinates)) {
                                            pieces[coordinates]?.let { highlight(it, false, coordinates) }
                                            null
                                        } else {
                                            coordinates.also { pieces[coordinates]?.let { highlight(it, true, coordinates) } }
                                        }
                                logger.trace { "Clicked $coordinates (lock at $lockedHighlight, current $currentHighlight)" }
                            }
                            penguin.setOnMouseExited { event ->
                                if(lockedHighlight == null) {
                                    if(!isSelectable(coordinates)) {
                                        clearTargetHighlights()
                                        penguin.removeHover()
                                        currentHighlight = null
                                    }
                                } else if(lockedHighlight != coordinates) {
                                    penguin.removeHover()
                                }
                                event.consume()
                            }
                        } else {
                            logger.error("Invalid State!")
                        }
                    }
                    //image.viewOrder = PluginConstants.BOARD_SIZE - coordinates.y.toDouble()
                }
            }
            Platform.runLater {
                @Suppress("UNCHECKED_CAST")
                gameModel.gameState.addListener(stateListener as ChangeListener<in IGameState?>)
                stateListener.changed(null, null, gameModel.gameState.value)
            }
        }
    }
    
    val grid: Pane = root.children.first() as Pane
    
    private fun removePiece(piece: Node?, durationMultiplier: Double = 1.0, parent: Pane = grid) =
            piece?.fade(transitionDuration.multiply(durationMultiplier), 0.0) {
                setOnFinished {
                    parent.children.remove(piece)
                }
            }
    
    /** Whether the piece at [coords] could be selected for a human move.. */
    private fun isSelectable(coords: Coordinates) =
            pieces[coords]?.opacity == AppStyle.pieceOpacity && gameModel.isHumanTurn.value && gameState?.penguinsPlaced == true
    
    private var lockedHighlight: Coordinates? = null
    private var currentHighlight: Node? = null
    private var currentHighlightCoords: Coordinates? = null
    private var targetHighlights = ArrayList<Node>()
    
    private fun createPiece(type: String): PieceImage =
            PieceImage(calculatedBlockSize, type)
    
    private fun highlight(node: PieceImage, lock: Boolean = false, updateTargetHighlights: Coordinates? = null) {
        currentHighlight?.removeHover()
        updateTargetHighlights?.takeIf { it != lockedHighlight }?.let { highlightTargets(it) }
        node.addHover(lock)
        currentHighlight = node
    }
    
    private fun Node.removeHover() {
        //removeClass(AppStyle.gridHover, AppStyle.gridLock)
        effect = null
    }
    
    private fun Node.addHover(lock: Boolean = false, team: Team? = null): Node {
        //addClass(if(lock) AppStyle.gridLock else AppStyle.gridHover)
        effect =
                if(team == null) Glow(if(lock) 0.7 else 0.4)
                else ColorAdjust(team.colorAdjust, -0.6, 0.0, 0.0)
        return this
    }
    
    private fun clearTargetHighlights() {
        currentHighlightCoords = null
        currentHighlight?.removeHover()
        lockedHighlight?.let { pieces[it] }?.removeHover()
        lockedHighlight = null
        targetHighlights.forEach { it.removeHover() }
        targetHighlights.clear()
    }
    
    private fun highlightTargets(position: Coordinates) {
        clearTargetHighlights()
        currentHighlightCoords = position
        
        gameState?.let { state ->
            targetHighlights.addAll(
                    state.board.possibleMovesFrom(position)
                            .also { logger.debug { "highlighting possible moves from $position: $it" } }
                            .mapNotNull { move ->
                                ice[move.to]?.addHover(team = state.board[position].penguin)
                            })
        }
    }
    
    private fun humanMove(move: Move) {
        if(gameModel.atLatestTurn.value && gameModel.isHumanTurn.value) {
            fire(HumanMoveAction(move.also { logger.debug("Human Move: $it") }))
        }
    }
    
    private fun <T: Region> addPiece(node: T, coordinates: Coordinates): T {
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
        return node
    }
}

private fun Node.setClass(className: String, add: Boolean = true) =
        if(add) addClass(className) else removeClass(className)

val Team.colorAdjust
    get() = this.index - 0.4