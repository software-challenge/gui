package sc.gui.view

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.ChangeListener
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.effect.ColorAdjust
import javafx.scene.effect.DropShadow
import javafx.scene.effect.Glow
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import mu.KotlinLogging
import sc.api.plugins.CubeCoordinates
import sc.api.plugins.CubeDirection
import sc.api.plugins.IGameState
import sc.gui.AppStyle
import sc.gui.controller.HumanMoveAction
import sc.gui.model.GameModel
import sc.plugin2024.*
import sc.plugin2024.Field
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
    
    private fun GameState.visibleBoard(): List<Segment> =
            let { state ->
                state.board.segments.slice(
                        state.ships.map { state.board.segmentIndex(it.position) }.sorted()
                                .let { IntRange((it.first() - 1).coerceAtLeast(0), state.board.segments.lastIndex) }
                )
            }
    
    private val viewHeight: Double
        get() = (root.parent as? Region ?: root).height.coerceAtMost(
                root.scene?.height?.minus(AppStyle.fontSizeBig.value * 12) ?: Double.MAX_VALUE)
    private val gridSize: Double
        get() = gameState?.visibleBoard()?.rectangleSize?.let {
            minOf(
                    root.scene?.width?.div(it.x + 1) ?: 64.0,
                    viewHeight / (it.y + 2) * 1.1
            ) * 1.3
        } ?: 100.0
    
    private val grid: Pane = AnchorPane().apply {
        paddingAll = 0.0
    }
    
    override val root = hbox {
        viewOrder = 3.0
        alignment = Pos.CENTER
        vbox {
            alignment = Pos.CENTER
            paddingAll = 0.0
            group(listOf(grid)) {
                paddingAll = 0.0
            }
        }
    }
    
    private val calculatedBlockSize = SimpleDoubleProperty(10.0)
    private val fontSizeBinding = calculatedBlockSize.stringBinding { "-fx-font-size: ${it?.toDouble()?.times(0.3)}" }
    
    private var originalState: GameState? = null
    private val humanMove = ArrayList<Action>()
    private var currentShip: PieceImage? = null
    
    private fun Ship.canAdvance() =
            coal + movement + freeAcc > 0 &&
            // negative movement points are turned into acceleration
            speed - movement < PluginConstants.MAX_SPEED
    
    init {
        Platform.runLater {
            calculatedBlockSize.bind(
                    gameModel.gameState.doubleBinding(gameModel.gameResult, gameModel.atLatestTurn, grid.parentProperty(), root.widthProperty(), root.heightProperty(), grid.widthProperty(), grid.heightProperty()) { gridSize })
            grid.apply {
                clipProperty().bind(
                        Bindings.createObjectBinding({
                            Rectangle(width - root.width, -gridSize, root.width, viewHeight)
                        }, gameModel.gameState, widthProperty(), parentProperty(), root.widthProperty(), root.heightProperty())
                )
            }
        }
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
                (state.board.getFieldCurrentDirection(cubeCoordinates)?.let { dir ->
                    createPiece("stream").also { it.rotate = dir.angle.toDouble() }
                } ?: createPiece((if(field == Field.GOAL) Field.WATER else field)
                        .toString().lowercase())).also { piece ->
                    if(field.isEmpty) {
                        piece.viewOrder++
                        val push = pushes.firstOrNull { state.currentShip.position + it.direction.vector == cubeCoordinates }
                        if(push != null) {
                            logger.debug("Registering Push '{}' for {}", push, piece)
                            piece.effect = Glow(0.2)
                            piece.onHover { hover ->
                                piece.effect = Glow(if(hover) 0.5 else 0.2)
                            }
                            piece.tooltip("Gegenspieler in Richtung ${push.direction} abdrängen (Taste ${push.direction.ordinal})")
                            piece.onLeftClick { addHumanAction(push) }
                        }
                    }
                    addPiece(piece, cubeCoordinates)
                }
                if(field == Field.GOAL) {
                    addPiece(createPiece(field.toString().lowercase()), cubeCoordinates)
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
                if(state.currentTeam == ship.team && !state.isOver)
                    shipPiece.effect = Glow(0.4)
                if(ship.stuck)
                    shipPiece.effect = ColorAdjust().apply { saturation = -0.8 } //SepiaTone(1.0)
                addPiece(shipPiece, ship.position)
                addPiece(
                        Label("⚙${if(state.currentTeam == ship.team && humanMove.isNotEmpty()) "${ship.movement}/" else ""}${ship.speed}")
                                .apply {
                                    styleProperty().bind(fontSizeBinding)
                                    this.effect = DropShadow(AppStyle.spacing, Color.BLACK) // TODO not working somehow
                                    translateY = gridSize / 10
                                }, ship.position)
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
            gameModel.isHumanTurn.addListener { _ -> Platform.runLater { renderHumanControls() } }
            
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
                translateY = gridSize / 10
                if(humanMove.all { it is Accelerate }) {
                    val acc = (humanMove.firstOrNull() as? Accelerate)?.acc ?: 0
                    hbox {
                        if(ship.speed < 6 && acc > -1)
                            button("+") {
                                tooltip("Beschleunigen (Accelerate 1)")
                                action { addHumanAction(Accelerate(1)) }
                            }
                        if(ship.speed > 1 && acc < 1)
                            button("-") {
                                tooltip("Bremsen (Accelerate -1)")
                                action { addHumanAction(Accelerate(-1)) }
                            }
                    }
                }
                if(gameState?.mustPush != true) {
                    if(ship.canTurn())
                        button("↺ A") {
                            tooltip("Gegen den Uhrzeigersinn drehen (Turn -1")
                            action { addHumanAction(Turn(ship.direction - 1)) }
                        }
                    if(ship.canAdvance())
                        button("→ W") {
                            tooltip("Ein Feld vorwärts bewegen (Advance 1)")
                            action { addHumanAction(Advance(1)) }
                            runLater { this.requestFocus() }
                        }
                    if(ship.canTurn())
                        button("↻ D") {
                            tooltip("Im Uhrzeigersinn drehen (Turn 1)")
                            action { addHumanAction(Turn(ship.direction + 1)) }
                        }
                }
                hbox {
                    if(!isHumanMoveIncomplete())
                        button("✓ S") {
                            tooltip("Zug bestätigen")
                            action { confirmHumanMove() }
                        }
                    if(humanMove.isNotEmpty())
                        button("╳ C") {
                            tooltip("Zug zurücksetzen")
                            action { cancelHumanMove() }
                        }
                }
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
        val extraMovement = ship.maxAcc.takeUnless { humanMove.firstOrNull() is Accelerate } ?: 0
        // Continual Advance on current
        val currentAdvance = humanMove.lastOrNull() is Advance && action is Advance &&
                             state.isCurrentShipOnCurrent() && state.board.doesFieldHaveCurrent(state.currentShip.position + state.currentShip.direction.vector * action.distance)
        if(currentAdvance)
            ship.movement++
        ship.movement += extraMovement
        action.perform(newState)?.let {
            alert(Alert.AlertType.ERROR, it.message)
        } ?: run {
            ship.movement -= extraMovement
            if(action is Accelerate && humanMove.isNotEmpty())
                humanMove[0] = humanMove[0] as Accelerate + action
            else
                humanMove.add(action)
            gameModel.gameState.set(newState)
        }
    }
    
    private fun cancelHumanMove() {
        humanMove.clear()
        gameModel.gameState.set(originalState)
    }
    
    private fun isHumanMoveIncomplete() =
            humanMove.none { it is Advance } || gameState?.let { state ->
                state.currentShip.movement > state.currentShip.freeAcc + state.currentShip.coal ||
                humanMove.first() is Accelerate && state.currentShip.movement != 0 ||
                state.mustPush
            } ?: true
    
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
    
    private fun <T: Node> addPiece(node: T, coordinates: CubeCoordinates): T {
        if(grid.children.contains(node))
            logger.warn("Attempting to add duplicate grid child at $coordinates: $node")
        else
            grid.add(node)
        calculatedBlockSize.listenImmediately {
            val size = it.toDouble()
            node.anchorpaneConstraints {
                val state = gameState ?: return@anchorpaneConstraints
                val bounds = state.visibleBoard().bounds
                leftAnchor = (coordinates.x / 2.0 - bounds.first.second) * size * .774
                topAnchor = (coordinates.r - bounds.second.first) * size * .668
                //logger.trace { "$coordinates: $node at $leftAnchor,$topAnchor within $bounds" }
            }
        }
        return node
    }
}

private fun Node.setClass(className: String, add: Boolean = true) =
        if(add) addClass(className) else removeClass(className)
