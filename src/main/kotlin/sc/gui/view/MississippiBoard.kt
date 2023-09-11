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
import javafx.scene.layout.VBox
import mu.KotlinLogging
import sc.api.plugins.Coordinates
import sc.api.plugins.CubeCoordinates
import sc.api.plugins.CubeDirection
import sc.api.plugins.IGameState
import sc.gui.AppStyle
import sc.gui.controller.HumanMoveAction
import sc.gui.model.GameModel
import sc.plugin2024.*
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
            ) * 1.3
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
    private var currentShip: PieceImage? = null
    
    private fun Ship.canAdvance() =
            coal + movement + freeAcc > 0 &&
            // negative movement points are turned into acceleration
            speed - movement < PluginConstants.MAX_SPEED
    
    init {
        val stateListener = ChangeListener<GameState?> { _, oldState, state ->
            if(state == null) {
                return@ChangeListener
            }
            grid.children.clear()
            if(state.turn != oldState?.turn) {
                humanMove.clear()
                originalState = state
            }
            logger.trace("New state for board: ${state.longString()}")
            
            state.currentShip.movement += state.currentShip.maxAcc
            val pushes = state.getPossiblePushs()
            state.currentShip.movement -= state.currentShip.maxAcc
            logger.trace("Available Pushes: {}", pushes)
            
            state.board.forEachField { cubeCoordinates, field ->
                // TODO overlay current and goal flag
                (state.board.getFieldCurrentDirection(cubeCoordinates)?.let { dir ->
                    createPiece("stream").also { it.rotate = dir.angle.toDouble() }
                } ?: createPiece(field.toString().lowercase())).also { piece ->
                    if(field.isEmpty) {
                        piece.viewOrder++
                        val push = pushes.firstOrNull { state.currentShip.position + it.direction.vector == cubeCoordinates }
                        if(push != null) {
                            logger.debug("Registering '{}' for {}", push, piece)
                            piece.addClass(AppStyle.gridHover) // TODO not yet working
                            piece.onLeftClick { addHumanAction(push) }
                        }
                    }
                    addPiece(piece, cubeCoordinates)
                }
            }
            state.ships.forEach { ship ->
                val shipName = "ship_${ship.team.name.lowercase()}"
                val shipPiece = createPiece(shipName)
                shipPiece.addChild("coal${ship.coal}")
                (1..ship.passengers).forEach {
                    shipPiece.addChild("${shipName}_passenger_${(96 + it).toChar()}")
                }
                shipPiece.rotate = ship.direction.angle.toDouble()
                addPiece(Label("S${ship.speed}" +
                               "\nM${ship.movement}"
                                       .takeIf { state.currentTeam == ship.team && humanMove.isNotEmpty() }
                                       .orEmpty()).apply {
                    styleProperty().bind(fontSizeBinding)
                }, ship.position)
                addPiece(shipPiece, ship.position)
                if(ship.team == state.currentTeam) {
                    currentShip = shipPiece
                    renderHumanControls()
                }
            }
        }
        Platform.runLater {
            @Suppress("UNCHECKED_CAST")
            gameModel.gameState.addListener(stateListener as ChangeListener<in IGameState?>)
            stateListener.changed(null, null, gameModel.gameState.value)
            gameModel.isHumanTurn.addListener { _ -> renderHumanControls() }
            
            root.scene.setOnKeyPressed { keyEvent ->
                val state = gameState ?: return@setOnKeyPressed
                val action: Action? = when(keyEvent.code) {
                    KeyCode.UP, KeyCode.W ->
                        Advance(1)
                    
                    KeyCode.LEFT, KeyCode.A ->
                        Turn(state.currentShip.direction - 1)
                    
                    KeyCode.RIGHT, KeyCode.D ->
                        Turn(state.currentShip.direction + 1)
                    
                    KeyCode.BACK_SPACE, KeyCode.C, KeyCode.X -> {
                        keyEvent.consume()
                        cancelHumanMove()
                        null
                    }
                    
                    KeyCode.ACCEPT, KeyCode.ENTER, KeyCode.S, KeyCode.SPACE -> {
                        keyEvent.consume()
                        confirmHumanMove()
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
                if(action == null) return@setOnKeyPressed
                
                keyEvent.consume()
                addHumanAction(action)
            }
        }
    }
    
    private fun renderHumanControls() {
        if(awaitingHumanMove()) {
            val ship = gameState?.currentShip ?: return
            addPiece(VBox().apply {
                translateX = -(AppStyle.spacing * 5)
                if(ship.canTurn())
                    button("↺ A") { action { addHumanAction(Turn(ship.direction - 1)) } }
                if(ship.canAdvance())
                    button("→ W") { action { addHumanAction(Advance(1)) } }
                if(ship.canTurn())
                    button("↻ D") { action { addHumanAction(Turn(ship.direction + 1)) } }
                if(!isHumanMoveIncomplete())
                    button("✓ S") { action { confirmHumanMove() } }
                if(humanMove.isNotEmpty())
                    button("╳ C") { action { cancelHumanMove() } }
            }, ship.position)
        }
    }
    
    private fun addHumanAction(action: Action) {
        val state = gameState ?: return
        if(state.mustPush && action !is Push) {
            alert(Alert.AlertType.ERROR, "Abdrängaktion mit Richtung 0 (Rechts) - 5 (Oben Rechts) festlegen!")
            return
        }
        
        val newState = state.clone()
        val ship = newState.currentShip
        val extraMovement = ship.maxAcc
        val currentAdvance = humanMove.lastOrNull() is Advance && action is Advance && state.isCurrentShipOnCurrent()
        if(currentAdvance)
            ship.movement++
        ship.movement += extraMovement
        action.perform(newState)?.let {
            alert(Alert.AlertType.ERROR, it.message)
        } ?: run {
            if(currentAdvance && !newState.isCurrentShipOnCurrent())
                ship.movement--
            ship.movement -= extraMovement
            humanMove.add(action)
            gameModel.gameState.set(newState)
        }
    }
    
    private fun cancelHumanMove() {
        humanMove.clear()
        gameModel.gameState.set(originalState)
    }
    
    private fun isHumanMoveIncomplete() =
        humanMove.isEmpty() || gameState?.let { state -> state.currentShip.movement > state.currentShip.freeAcc + state.currentShip.coal || state.mustPush } ?: true
    
    private fun confirmHumanMove() {
        if(awaitingHumanMove() && isHumanMoveIncomplete()) {
            alert(Alert.AlertType.ERROR, "Unvollständiger Zug!")
        } else {
            val state = gameState ?: return
            if(state.currentShip.movement != 0) {
                humanMove.add(0, Accelerate(-state.currentShip.movement))
            }
            if(!humanMove(Move(ArrayList(humanMove))))
                gameModel.gameState.set(originalState)
            humanMove.clear()
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
    
    private fun awaitingHumanMove() =
            gameModel.atLatestTurn.value && gameModel.isHumanTurn.value
    
    private fun humanMove(move: Move): Boolean {
        if(awaitingHumanMove()) {
            fire(HumanMoveAction(move.also { logger.debug("Human Move: {}", it) }))
            return true
        }
        return false
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
                val bounds = gameState?.board?.segments?.takeLast(4)?.bounds
                leftAnchor = (coordinates.x / 2.0 - (bounds?.first?.first ?: -2)) * size * .774
                topAnchor = (coordinates.r - (bounds?.second?.first ?: -2)) * size * .668
            }
        }
        return node
    }
}

private fun Node.setClass(className: String, add: Boolean = true) =
        if(add) addClass(className) else removeClass(className)
