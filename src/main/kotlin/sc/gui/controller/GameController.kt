package sc.gui.controller

import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import org.slf4j.LoggerFactory
import sc.gui.model.PiecesModel
import sc.gui.view.PiecesFragment
import sc.plugin2021.*
import sc.plugin2021.util.GameRuleLogic
import sc.shared.GameResult
import tornadofx.Controller
import tornadofx.nonNullObjectBinding
import tornadofx.objectProperty
import java.util.*
import kotlin.math.max

// The following *Binding-classes are necessary to automatically unbind and rebind to a new piece (when switched)
// in order to prevent this hassle in every other class, that uses the current selected PieceModel
class ColorBinding(piece: Property<PiecesModel>) : ObjectBinding<Color>() {
    val model = piece

    init {
        bind(model)
        bind(model.value.colorProperty())
        model.addListener { _, oldValue, newValue ->
            unbind(oldValue.colorProperty())
            bind(newValue.colorProperty())
            model.value = newValue
        }
    }

    fun set(data: Color) {
        model.value.colorProperty().set(data)
    }

    override fun computeValue(): Color {
        return model.value.colorProperty().get()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}

class ShapeBinding(piece: Property<PiecesModel>) : ObjectBinding<PieceShape>() {
    val model = piece

    init {
        bind(model)
        bind(model.value.shapeProperty())
        model.addListener { _, oldValue, newValue ->
            unbind(oldValue.shapeProperty())
            bind(newValue.shapeProperty())
            model.value = newValue
        }
    }

    fun set(data: PieceShape) {
        model.value.shapeProperty().set(data)
    }

    override fun computeValue(): PieceShape {
        return model.value.shapeProperty().get()
    }
}

class RotationBinding(piece: Property<PiecesModel>) : ObjectBinding<Rotation>() {
    val model = piece

    init {
        bind(model)
        bind(model.value.rotationProperty())
        model.addListener { _, oldValue, newValue ->
            unbind(oldValue.rotationProperty())
            bind(newValue.rotationProperty())
            model.value = newValue
        }
    }

    fun set(data: Rotation) {
        model.value.rotationProperty().set(data)
    }

    override fun computeValue(): Rotation {
        return model.value.rotationProperty().get()
    }
}

class FlipBinding(piece: Property<PiecesModel>) : BooleanBinding() {
    val model = piece

    init {
        bind(model)
        bind(model.value.flipProperty())
        model.addListener { _, oldValue, newValue ->
            unbind(oldValue.flipProperty())
            bind(newValue.flipProperty())
            model.value = newValue
        }
    }

    fun set(data: Boolean) {
        model.value.flipProperty().set(data)
    }

    override fun computeValue(): Boolean {
        return model.value.flipProperty().get()
    }
}

class CalculatedShapeBinding(piece: Property<PiecesModel>) : ObjectBinding<Set<Coordinates>>() {
    val model = piece

    init {
        bind(model)
        bind(model.value.shapeProperty())
        model.addListener { _, oldValue, newValue ->
            unbind(oldValue.calculatedShapeProperty())
            bind(newValue.calculatedShapeProperty())
            model.value = newValue
        }
    }

    fun set(data: Set<Coordinates>) {
        model.value.calculatedShapeProperty().set(data)
    }

    override fun computeValue(): Set<Coordinates> {
        return model.value.calculatedShapeProperty().get()
    }
}


class GameController : Controller() {
    val boardController: BoardController by inject()
    
    val gameState = objectProperty(GameState())

    val availableTurns = objectProperty(0)
    val currentTurn = objectProperty(0)
    val currentRound = objectProperty(0)
    val currentColor = objectProperty(Color.RED)
    val currentTeam = objectProperty(Team.ONE)
    val isHumanTurn = objectProperty(false)
    val canSkip = objectProperty(false)
    val previousColor = objectProperty(Color.RED)
    val teamOneScore = objectProperty(0)
    val teamTwoScore = objectProperty(0)
    
    val started = nonNullObjectBinding(currentTurn, isHumanTurn) {
        value > 0 || isHumanTurn.value
    }
    val playerNames = objectProperty<Array<String>>()
    val gameResult = objectProperty<GameResult>()

    val undeployedPieces: Map<Color, ObjectProperty<Collection<PieceShape>>> = EnumMap(
        Color.values().associateWith { objectProperty(PieceShape.shapes.values) })
	
    val validPieces: Map<Color, ObjectProperty<Collection<PieceShape>>> = EnumMap(
        Color.values().associateWith { objectProperty(emptyList()) })

    // use selected* to access the property of currentPiece in order to always correctly be automatically rebind
    // TODO maybe this should be nullable rather than having a random default -
    //  then it could also be unset to prevent spurious errors like https://github.com/CAU-Kiel-Tech-Inf/gui/issues/43
    val currentPiece = objectProperty(PiecesModel(Color.RED, PieceShape.MONO))
    val selectedColor: ColorBinding = ColorBinding(currentPiece)
    val selectedShape: ShapeBinding = ShapeBinding(currentPiece)
    val selectedRotation: RotationBinding = RotationBinding(currentPiece)
    val selectedFlip: FlipBinding = FlipBinding(currentPiece)
    val selectedCalculatedShape: CalculatedShapeBinding = CalculatedShapeBinding(currentPiece)

    fun isValidColor(color: Color): Boolean = gameState.get().isValid(color)

    init {
        subscribe<NewGameState> { event ->
            logger.debug("New game state")

            val state = event.gameState
            gameState.set(state)
            canSkip.set(false)

            previousColor.set(currentColor.get())
            currentColor.set(state.currentColor)
            currentTeam.set(state.currentTeam)
            boardController.board.boardProperty().set(state.board)
            undeployedPieces.forEach { (color, pieces) ->
                pieces.set(state.undeployedPieceShapes(color))
            }
            validPieces.forEach { (_, pieces) ->
                pieces.set(emptyList())
            }
            
            availableTurns.set(max(availableTurns.get(), state.turn))
            playerNames.set(state.playerNames)
            currentTurn.set(state.turn)
            currentRound.set(state.round)
            teamOneScore.set(state.getPointsForPlayer(Team.ONE))
            teamTwoScore.set(state.getPointsForPlayer(Team.TWO))
        }
        subscribe<HumanMoveRequest> { event ->
            val state = event.gameState
            val moves = state.undeployedPieceShapes().map {
                it to GameRuleLogic.getPossibleMovesForShape(state, it)
            }.toMap()
            logger.debug("Human move request for {} - {} possible moves",
                state.currentColor,
                moves.values.sumBy { it.size })
    
            isHumanTurn.set(true)
            canSkip.set(!gameEnded() && isHumanTurn.get() && !GameRuleLogic.isFirstMove(state))
            boardController.calculateIsPlaceableBoard(state.board, state.currentColor)
    
            validPieces.getValue(state.currentColor)
                .set(moves.filterValues { it.isNotEmpty() }.keys)
        }
        subscribe<GameOverEvent> { event ->
            gameResult.set(event.result)
        }
    }

    fun gameEnded(): Boolean = gameResult.isNotNull.get()

    fun clearGame() {
        gameResult.set(null)
        boardController.board.boardProperty().set(Board())
        availableTurns.set(0)
        currentTurn.set(0)
        currentRound.set(0)
        undeployedPieces.forEach { (_, pieces) -> pieces.set(PieceShape.values().toList()) }
    }

    fun selectPiece(piece: PiecesModel) {
        currentPiece.set(piece)
    }

    fun flipPiece() {
        currentPiece.get().flipPiece()
    }

    fun rotatePiece(rotate: Rotation) {
        selectedRotation.set(selectedRotation.get().rotate(rotate))
    }

    fun scroll(deltaY: Double) {
        currentPiece.get().scroll(deltaY)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GameController::class.java)
    }
}