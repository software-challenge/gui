package sc.gui.view

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import mu.KotlinLogging
import sc.api.plugins.Coordinates
import sc.api.plugins.CubeCoordinates
import sc.api.plugins.CubeDirection
import sc.api.plugins.IGameState
import sc.gui.AppStyle
import sc.gui.controller.HumanMoveAction
import sc.gui.model.GameModel
import sc.plugin2024.Action
import sc.plugin2024.GameState
import sc.plugin2024.Move
import sc.plugin2024.actions.Accelerate
import sc.plugin2024.actions.Advance
import sc.plugin2024.actions.Push
import sc.plugin2024.actions.Turn
import sc.plugin2024.util.PluginConstants
import sc.util.listenImmediately
import tornadofx.*

private val logger = KotlinLogging.logger { }

class MississippiBoard: View() {
    private val gameModel: GameModel by inject()
    private val gameState: GameState?
        get() = gameModel.gameState.value as? GameState
    
    private val gridSize: Double
        get() = gameState?.board?.rectangleSize?.let {
            minOf(
                    (root.width - AppStyle.spacing) / (it.x + 1),
                    (root.height - AppStyle.spacing * (if(gameModel.gameOver.value && gameModel.atLatestTurn.value) 4 else 2)) / it.y
            )
        } ?: 10.0
    
    val grid: Pane = AnchorPane().apply { this.paddingAll = AppStyle.spacing }
    
    override val root = vbox {
        alignment = Pos.CENTER
        children.add(grid)
    }
    
    private val calculatedBlockSize = gameModel.gameState.doubleBinding(gameModel.gameResult, gameModel.atLatestTurn, grid.widthProperty(), grid.heightProperty()) { gridSize }
    private val fontSizeBinding = calculatedBlockSize.stringBinding { "-fx-font-size: ${it?.toDouble()?.times(0.3)}" }
    
    private var originalState: GameState? = null
    private val humanMove = ArrayList<Action>()
    
    init {
        val stateListener = ChangeListener<GameState?> { _, oldState, state ->
            clearTargetHighlights()
            grid.children.clear()
            if(state?.lastMove != oldState?.lastMove)
                humanMove.clear()
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
                    addPiece(Label("C${ship.coal}\nS${ship.speed}" +
                                   "\nM${ship.movement}"
                                           .takeIf { state.currentTeam == ship.team && humanMove.isNotEmpty() }
                                           .orEmpty()).apply {
                        styleProperty().bind(fontSizeBinding)
                    }, ship.position)
                }
            }
        }
        Platform.runLater {
            @Suppress("UNCHECKED_CAST")
            gameModel.gameState.addListener(stateListener as ChangeListener<in IGameState?>)
            stateListener.changed(null, null, gameModel.gameState.value)
            
            root.scene.setOnKeyPressed { keyEvent ->
                val state = gameState ?: return@setOnKeyPressed
                if(humanMove.isEmpty())
                    originalState = state
                val ship = state.currentShip
                val action: Action? = when(keyEvent.code) {
                    KeyCode.UP, KeyCode.W ->
                        Advance(1).takeIf {
                            ship.coal + ship.movement + ship.freeAcc > 0 &&
                            ship.speed - ship.movement < 6
                        }
                    
                    KeyCode.LEFT, KeyCode.A ->
                        Turn(state.currentShip.direction - 1)
                    
                    KeyCode.RIGHT, KeyCode.D ->
                        Turn(state.currentShip.direction + 1)
                    
                    KeyCode.BACK_SPACE, KeyCode.C -> {
                        humanMove.clear()
                        gameModel.gameState.set(originalState)
                        null
                    }
                    
                    KeyCode.ACCEPT, KeyCode.ENTER, KeyCode.S, KeyCode.SPACE -> {
                        keyEvent.consume()
                        if(humanMove.isEmpty()) {
                            alert(Alert.AlertType.ERROR, "Unvollständiger Zug!")
                        } else {
                            if(state.currentShip.movement != 0) {
                                humanMove.add(0, Accelerate(-state.currentShip.movement))
                            }
                            if(!humanMove(Move(ArrayList(humanMove))))
                                gameModel.gameState.set(originalState)
                            humanMove.clear()
                            originalState = null
                        }
                        null
                    }
                    
                    else -> {
                        keyEvent.text.toIntOrNull()?.let {
                            if(it < CubeDirection.values().size)
                                Push(CubeDirection.values()[it])
                            else null
                        }
                    }
                }
                logger.debug("Adding Human Action {}", action)
                if(action != null) {
                    keyEvent.consume()
                    if(state.mustPush && action !is Push) {
                        alert(Alert.AlertType.ERROR, "Abdrängaktion mit Richtung 0 (Rechts) - 5 (Oben Rechts) festlegen!")
                        return@setOnKeyPressed
                    }
                    val newState = state.clone()
                    newState.currentShip.movement += PluginConstants.MAX_SPEED
                    action.perform(newState)?.let {
                        alert(Alert.AlertType.ERROR, it.message)
                    } ?: run {
                        newState.currentShip.movement -= PluginConstants.MAX_SPEED
                        if(humanMove.lastOrNull() is Advance &&
                           action is Advance &&
                           newState.board.doesFieldHaveCurrent(newState.currentShip.position) &&
                           state.board.doesFieldHaveCurrent(state.currentShip.position))
                            newState.currentShip.movement++
                        humanMove.add(action)
                        gameModel.gameState.set(newState)
                    }
                }
            }
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
    
    private fun humanMove(move: Move): Boolean {
        if(gameModel.atLatestTurn.value && gameModel.isHumanTurn.value) {
            fire(HumanMoveAction(move.also { logger.debug("Human Move: {}", it) }))
            return true
        }
        return false
    }
    
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
                val bounds = gameState?.board?.bounds
                leftAnchor = (coordinates.x / 2.0 - (bounds?.first?.first ?: -2)) * size
                topAnchor = (coordinates.r - (bounds?.second?.first ?: -2)) * size * 0.862
            }
        }
        return node
    }
}

private fun Node.setClass(className: String, add: Boolean = true) =
        if(add) addClass(className) else removeClass(className)
