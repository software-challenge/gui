package sc.gui.view.game

import javafx.animation.Animation
import javafx.animation.SequentialTransition
import javafx.animation.Transition
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.effect.ColorAdjust
import javafx.scene.effect.DropShadow
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import sc.api.plugins.CubeCoordinates
import sc.api.plugins.CubeDirection
import sc.api.plugins.Team
import sc.gui.AppStyle
import sc.gui.util.listenImmediately
import sc.gui.view.GameBoard
import sc.gui.view.PieceImage
import sc.plugin2024.*
import sc.plugin2024.Field
import sc.plugin2024.actions.Accelerate
import sc.plugin2024.actions.Advance
import sc.plugin2024.actions.Push
import sc.plugin2024.actions.Turn
import sc.plugin2024.util.MQConstants
import tornadofx.*
import kotlin.math.absoluteValue
import kotlin.math.pow

class MississippiBoard: GameBoard<GameState>() {
    
    private fun GameState.visibleBoard(): List<Segment> =
        let { state ->
            state.board.segments.slice(
                state.ships.map { state.board.segmentIndex(it.position) }.sorted()
                    .let { IntRange((it.first() - 1).coerceAtLeast(0), state.board.segments.lastIndex) }
            )
        }
    
    private val gridSize: Double
        get() = gameState?.visibleBoard()?.rectangleSize?.let {
            minOf(
                root.scene?.width?.div(it.x + 2) ?: 64.0,
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
    private fun fontSizeFromBlockSize(factor: Double = .3) =
        calculatedBlockSize.stringBinding { "-fx-font-size: ${it?.toDouble()?.times(factor)}" }
    
    private var originalState: GameState? = null
    private val humanMove = ArrayList<Action>()
    
    private var transition: Transition? = null
    
    private fun Ship.canAdvance() =
        coal + movement + freeAcc > 0 &&
        // negative movement points are turned into acceleration
        speed - movement < MQConstants.MAX_SPEED
    
    private fun nameSpeed(speed: Int): String? =
        if(speed > 3) {
            "full"
        } else if(speed > 1) {
            "half"
        } else {
            null
        }
    
    init {
        Platform.runLater {
            calculatedBlockSize.bind(
                gameModel.gameState.doubleBinding(
                    gameModel.gameResult,
                    gameModel.atLatestTurn,
                    grid.parentProperty(),
                    root.widthProperty(),
                    root.heightProperty(),
                    grid.widthProperty(),
                    grid.heightProperty()
                ) { gridSize })
            grid.apply {
                clipProperty().bind(
                    Bindings.createObjectBinding(
                        { Rectangle(0.0, -gridSize, root.width, viewHeight) },
                        gameModel.gameState,
                        widthProperty(),
                        parentProperty(),
                        root.widthProperty(),
                        root.heightProperty()
                    )
                )
            }
        }
    }
    
    override fun onNewState(oldState: GameState?, state: GameState?) {
        if(state == null) {
            return
        }
        grid.children.clear()
        var wasHumanMove = false
        if(state.turn != oldState?.turn) {
            humanMove.clear()
            if(originalState != oldState) {
                wasHumanMove = true
            }
            originalState = state
        }
        logger.trace { "New state for board: ${state.longString()}" }
        
        state.currentShip.movement += state.currentShip.maxAcc
        val pushes = state.getPossiblePushs()
        state.currentShip.movement -= state.currentShip.maxAcc
        logger.trace { "Available Pushes: $pushes"}
        
        val neighbors = hashMapOf<CubeCoordinates, ArrayList<CubeDirection>>()
        state.board.forEachField { cubeCoordinates, field ->
            CubeDirection.values().forEach { dir ->
                val coord = cubeCoordinates + dir.vector
                if(state.board[coord] == null)
                    neighbors.getOrPut(coord) { ArrayList() }.add(dir)
            }
            createPiece(
                (if(field == Field.GOAL) Field.WATER else field).let {
                    it.toString().lowercase() + when(it) {
                        Field.ISLAND -> cubeCoordinates.hashCode().mod(3) + 1
                        else -> ""
                    }
                }
            ).also { piece ->
                piece.nextFrame()
                if(state.board.segmentIndex(cubeCoordinates).mod(2) == 1)
                    piece.effect = ColorAdjust(0.0, 0.0, -contrastFactor / 3, 0.0)
                if(field.isEmpty) {
                    piece.viewOrder++
                    val push = pushes.firstOrNull {
                        state.currentShip.position + it.direction.vector == cubeCoordinates
                    }
                    if(push != null) {
                        logger.debug { "Registering Push '$push' for $piece" }
                        piece.glow(.4)
                        piece.onHover { hover ->
                            // TODO hover not recognized when stream is on top
                            piece.glow(if(hover) 1 else .4)
                        }
                        piece.tooltip("Gegenspieler in Richtung ${push.direction} abdrängen (Taste ${push.direction.ordinal})")
                        piece.onLeftClick { addHumanAction(push) }
                    }
                }
                state.board.getFieldCurrentDirection(cubeCoordinates)?.let { dir ->
                    addPiece(
                        createPiece("stream").also { piece ->
                            piece.rotate = dir.angle.toDouble()
                            piece.nextFrame()
                        },
                        cubeCoordinates
                    )
                }
                addPiece(piece, cubeCoordinates)
            }
            if(field == Field.GOAL) {
                addPiece(createPiece(field.toString().lowercase()), cubeCoordinates)
            }
        }
        
        val tip = state.board.segments.last().center + state.board.nextDirection.vector * 2
        val fog = ArrayList<CubeCoordinates>()
        if(state.board[tip] != Field.GOAL) {
            Segment.empty(
                state.board.segments.last().center + state.board.nextDirection.vector * 4,
                state.board.nextDirection
            ).forEachField { cubeCoordinates, _ -> fog.add(cubeCoordinates) }
            fog.forEach { cubeCoordinates ->
                CubeDirection.values().forEach { dir ->
                    val coord = cubeCoordinates + dir.vector
                    if(!fog.contains(coord) && state.board[coord] == null)
                        neighbors.getOrPut(coord) { ArrayList() }.add(dir)
                }
                addPiece(createPiece("fog"), cubeCoordinates)
            }
            
            val excludeTip = state.board.segments.last().center + state.board.nextDirection.vector * 6
            val excluded =
                CubeDirection.values()
                    .flatMap { listOf(excludeTip + it.vector, excludeTip + it.vector + (it + 1).vector) }
            excluded.forEach { exclude ->
                CubeDirection.values().forEach { dir ->
                    val coord = exclude + dir.vector
                    if(state.board[coord] == null) {
                        neighbors[coord]?.add(dir)
                    }
                }
                neighbors.remove(exclude)
            }
        }
        
        neighbors.forEach { (coords, dirs) ->
            addPiece(
                createPiece(
                    when {
                        state.board.neighboringFields(coords).all { it == null } -> "fog_"
                        fog.contains(coords) -> "fog_water_"
                        else -> ""
                    } +
                    when(dirs.size) {
                        1 -> "border_inner"
                        2 -> "border"
                        3 -> "border_outer"
                        else -> {
                            logger.warn { "Piece at $coords has wrong border directions: $dirs" }
                            ""
                        }
                    }
                ).apply {
                    if(fog.contains(coords))
                        addChild("water", 0)
                    this.rotate =
                        (dirs.single { dir -> dirs.all { dir.turnCountTo(it) >= 0 } } - (if(dirs.size == 1) 5 else 4)).angle.toDouble()
                },
                coords
            )
        }
        
        // TODO issues when double move through overtaking
        val animState = oldState?.clone()?.takeIf {
            !wasHumanMove && state.turn - 1 == it.turn && state.lastMove != null
        }
        
        fun PieceImage.shipBowWaveSpeed(speed: Int) {
            this.children.removeIf { it.styleClass.any { it.startsWith("waves") } }
            nameSpeed(speed)?.let { this.addChild("waves_${it}_speed", 0) }
        }
        
        fun PieceImage.addPassenger(shipName: String, passenger: Int) =
            addChild("${shipName}_passenger_${(96 + passenger).toChar()}")
        
        val pieces = Team.values().map { team ->
            val ship = (animState ?: state).getShip(team)
            val shipName = "ship_${team.name.lowercase()}"
            val shipPiece = createPiece(shipName)
            
            nameSpeed(state.getShip(ship.team).speed)
                ?.let { speed -> shipPiece.addChild("smoke_${speed}_speed") }
            
            shipPiece.addChild("coal${ship.coal}")
            (1..ship.passengers).forEach { passenger ->
                shipPiece.addPassenger(shipName, passenger)
            }
            
            shipPiece.rotate = ship.direction.angle.toDouble()
            if((animState ?: state).currentTeam == ship.team && !state.isOver)
                shipPiece.glow() // lower in history: if(gameModel.atLatestTurn.value == false) .5 else 1.0)
            if(ship.crashed)
                shipPiece.effect = ColorAdjust().apply { saturation = -0.8 } // SepiaTone(1.0)
            else
                shipPiece.shipBowWaveSpeed(ship.speed)
            addPiece(shipPiece, ship.position)
        }
        
        fun addLabels() {
            state.ships.forEach { ship ->
                addPiece(
                    Label("⚙${if(state.currentTeam == ship.team && humanMove.isNotEmpty()) "${ship.movement}/" else ""}${ship.speed}")
                        .apply {
                            styleProperty().bind(fontSizeFromBlockSize())
                            this.effect = DropShadow(AppStyle.spacing, Color.BLACK) // TODO not working somehow
                            translateY = gridSize / 10
                        }, ship.position
                )
            }
            checkHumanControls()
        }
        
        if(animState != null) {
            val ship = animState.getShip(animState.currentTeam)
            val piece = pieces[animState.currentTeam.index]
            
            val factors = coordinateFactors()
            val move = state.lastMove!!
            
            transition?.pause()
            transition = SequentialTransition(
                *move.actions.mapNotNull { action ->
                    when(action) {
                        is Turn -> {
                            piece.rotate(
                                Duration.seconds(animFactor * ship.direction.turnCountTo(action.direction).absoluteValue),
                                Double.NaN,
                                play = false
                            ).apply {
                                byAngle = ship.direction.angleTo(action.direction).toDouble()
                                this.statusProperty().addListener { _ ->
                                    if(this.status == Animation.Status.RUNNING) {
                                        piece.shipBowWaveSpeed(0)
                                    }
                                }
                            }
                        }
                        
                        is Advance -> {
                            // TODO collate sequential advances
                            val dist = action.distance
                            val diff = ship.direction.vector * dist
                            piece.move(
                                Duration.seconds(animFactor * dist.toDouble().pow(0.7)),
                                Point2D(Double.NaN, Double.NaN),
                                play = false
                            ) {
                                byX = diff.x / 2.0 * factors.x
                                byY = diff.r * factors.y
                                this.statusProperty().addListener { _ ->
                                    if(this.status == Animation.Status.RUNNING) {
                                        piece.shipBowWaveSpeed(6)
                                    } else {
                                        piece.shipBowWaveSpeed(ship.speed)
                                    }
                                }
                            }
                        }
                        
                        is Push -> {
                            val diff = action.direction.vector
                            val otherPiece = pieces[animState.otherTeam.index]
                            otherPiece.move(
                                Duration.seconds(animFactor / 2),
                                Point2D(Double.NaN, Double.NaN),
                                play = false
                            ) {
                                byX = diff.x / 2.0 * factors.x
                                byY = diff.r * factors.y
                                otherPiece.shipBowWaveSpeed(0)
                            }
                        }
                        
                        else -> null
                    }.also { action.perform(animState) }
                }.toTypedArray()
            ).apply {
                setOnFinished {
                    logger.debug { "Finished transition $it with elements ${children.joinToString()} for $move" }
                    addLabels()
                    pieces[animState.currentTeam.index].effect = null
                    pieces[state.currentTeam.index].glow()
                    Team.values().forEach { team ->
                        val newPassengers = state.getShip(team).passengers
                        if(animState.getShip(team).passengers < newPassengers) {
                            pieces[team.index].addPassenger(pieces[team.index].content, newPassengers)
                        }
                    }
                }
                play()
            }
        } else {
            addLabels()
        }
    }
    
    override fun handleKeyPress(state: GameState, keyEvent: KeyEvent): Boolean {
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
        
        logger.debug { "Adding Human Action from keypress $action" }
        if(action != null) {
            addHumanAction(action)
            return true
        }
        return false
    }
    
    
    override fun renderHumanControls(state: GameState) {
        val ship = state.currentShip
        addPiece(VBox().apply {
            translateX = -gridSize / 2
            translateY = gridSize / 10
                styleProperty().bind(fontSizeFromBlockSize(.16))
            if(humanMove.all { it is Accelerate }) {
                val acc = (humanMove.firstOrNull() as? Accelerate)?.acc ?: 0
                hbox {
                    if(ship.speed < 6 && acc > -1)
                        button("+") {
                            tooltip("Beschleunigen (Accelerate +1)")
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
        if(awaitingHumanMove.value && isHumanMoveIncomplete()) {
            alert(Alert.AlertType.ERROR, "Unvollständiger Zug!")
        } else {
            val state = gameState ?: return
            if(state.currentShip.movement != 0) {
                humanMove.add(0, Accelerate(-state.currentShip.movement))
            }
            if(!sendHumanMove(Move(ArrayList(humanMove))))
                gameModel.gameState.set(originalState)
            humanMove.clear()
        }
    }
    
    private fun createPiece(type: String): PieceImage =
        PieceImage(calculatedBlockSize, type)
    
    private fun <T: Node> addPiece(node: T, coordinates: CubeCoordinates): T {
        if(grid.children.contains(node))
            logger.warn { "Attempting to add duplicate grid child at $coordinates: $node" }
        else
            grid.add(node)
        calculatedBlockSize.listenImmediately {
            val size = coordinateFactors(it.toDouble())
            node.anchorpaneConstraints {
                val state = gameState ?: return@anchorpaneConstraints
                val bounds = state.visibleBoard().bounds
                leftAnchor = (coordinates.x / 2.0 - bounds.first.first + .5) * size.x
                topAnchor = (coordinates.r - bounds.second.first - .5) * size.y
                logger.trace { "$coordinates: $node at ${leftAnchor?.toInt()},${topAnchor?.toInt()} within $bounds" }
            }
        }
        return node
    }
    
    fun coordinateFactors(size: Double = calculatedBlockSize.value) = Point2D(size * .774, size * .668)
}
