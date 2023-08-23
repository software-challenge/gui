package sc.gui.view

import javafx.animation.KeyFrame
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.effect.ColorAdjust
import javafx.scene.effect.Glow
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.transform.Rotate
import javafx.stage.Stage
import mu.KotlinLogging
import sc.api.plugins.*
import sc.gui.AppStyle
import sc.gui.controller.HumanMoveAction
import sc.gui.model.GameModel
import sc.plugin2024.GameState
import sc.util.listenImmediately
import tornadofx.*
import kotlin.math.sqrt

private val logger = KotlinLogging.logger { }

class MississippiBoard: View() {
    private val gameModel: GameModel by inject()
    private val gameState: GameState?
        get() = gameModel.gameState.value as? GameState
    
    private val gridSize: Double
        get() = gameState?.board?.rectangleSize?.let { minOf(root.width.minus(AppStyle.spacing * 2) / it.x, root.height / it.y) } ?: 20.0
    
    val grid: Pane = AnchorPane().apply { this.paddingAll = AppStyle.spacing }
    
    override val root = vbox {
        alignment = Pos.CENTER
        children.add(grid)
    }
    
    private val calculatedBlockSize = gameModel.gameState.doubleBinding(grid.widthProperty(), grid.heightProperty()) { gridSize }
    
    init {
        val stateListener = ChangeListener<GameState?> { _, oldState, state ->
            clearTargetHighlights()
            grid.children.clear()
            if(state == null) {
                return@ChangeListener
            }
            logger.trace("New state for board: ${state.longString()}")
            state.board.forEachField { cubeCoordinates, field ->
                createPiece(field.toString().lowercase()).also { addPiece(it, cubeCoordinates) }
                /*
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
                Team.values().forEach { ice.children.first().effect = null }
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
                */
            }
            state.ships.forEach { ship ->
                createPiece("ship").also {
                    it.rotate = ship.direction.angle.toDouble()
                    addPiece(it, ship.position)
                }
            }
        }
        Platform.runLater {
            @Suppress("UNCHECKED_CAST")
            gameModel.gameState.addListener(stateListener as ChangeListener<in IGameState?>)
            stateListener.changed(null, null, gameModel.gameState.value)
        }
    }
    
    private fun removePiece(piece: Node?, durationMultiplier: Double = 1.0, parent: Pane = grid) =
            piece?.fade(transitionDuration.multiply(durationMultiplier), 0.0) {
                setOnFinished {
                    parent.children.remove(piece)
                }
            }
    
    private var lockedHighlight: Coordinates? = null
    private var currentHighlight: Node? = null
    private var currentHighlightCoords: Coordinates? = null
    private var targetHighlights = ArrayList<Node>()
    
    private fun createPiece(type: String): PieceImage =
            PieceImage(calculatedBlockSize, type)
    
    private fun highlight(node: PieceImage, lock: Boolean = false, updateTargetHighlights: Coordinates? = null) {
        currentHighlight?.removeHover()
        updateTargetHighlights?.takeIf { it != lockedHighlight }?.let { highlightTargets(it) }
        //node.addHover(lock)
        currentHighlight = node
    }
    
    private fun Node.removeHover() {
        //removeClass(AppStyle.gridHover, AppStyle.gridLock)
        effect = null
    }
    
    private fun clearTargetHighlights() {
        currentHighlightCoords = null
        currentHighlight?.removeHover()
        lockedHighlight = null
        targetHighlights.forEach { it.removeHover() }
        targetHighlights.clear()
    }
    
    private fun highlightTargets(position: Coordinates) {
        clearTargetHighlights()
        currentHighlightCoords = position
        
        gameState?.let { state ->
            /*
            targetHighlights.addAll(
                    state.board.possibleMovesFrom(position)
                            .also { logger.debug { "highlighting possible moves from $position: $it" } }
                            .mapNotNull { move ->
                                ice[move.to]?.addHover(team = state.board[position].penguin)
                            })*/
        }
    }
    
    private fun <T: Region> addPiece(node: T, coordinates: CubeCoordinates): T {
        if(grid.children.contains(node))
            logger.warn("Attempting to add duplicate grid child at $coordinates: $node")
        else
            grid.add(node)
        calculatedBlockSize.listenImmediately {
            //logger.trace("$node at $coordinates block size: $it")
            val size = it.toDouble()
            node.anchorpaneConstraints {
                leftAnchor = (coordinates.x / 2.0 + 2) * size
                topAnchor = (coordinates.r - (gameState?.board?.bounds?.second?.first ?: -2) + 2) * size * 0.862
            }
        }
        return node
    }
}

private fun Node.setClass(className: String, add: Boolean = true) =
        if(add) addClass(className) else removeClass(className)
