package sc.gui.controller

import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import org.slf4j.LoggerFactory
import sc.gui.model.PiecesModel
import sc.gui.view.PiecesFragment
import sc.plugin2021.*
import sc.plugin2021.util.GameRuleLogic
import sc.shared.GameResult
import tornadofx.*
import java.util.EnumMap
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
    val gameState = objectProperty<GameState?>(null)
    val gameResult = objectProperty<GameResult>()
    val isHumanTurn = objectProperty(false)

    val currentTurn = nonNullObjectBinding(gameState) { value?.turn ?: 0 }
    val currentRound = nonNullObjectBinding(gameState) { value?.round ?: 0 }
    val currentColor = nonNullObjectBinding(gameState) { value?.currentColor ?: Color.RED }
    val currentTeam = nonNullObjectBinding(gameState) { value?.currentTeam ?: Team.ONE }
    val teamScores = gameState.objectBinding { state ->
        Team.values().map { state?.getPointsForPlayer(it) }
    }
    
    val availableTurns = objectProperty(0).also { avTurns ->
        currentTurn.addListener { _, _, turn ->
            avTurns.set(turn?.let { max(it, avTurns.value) }) }
    }
    
    val started = nonNullObjectBinding(currentTurn, isHumanTurn) {
        value > 0 || isHumanTurn.value
    }
    val playerNames = gameState.objectBinding { it?.playerNames }
    val gameEnded = gameResult.booleanBinding { it != null }
    
    val canSkip = isHumanTurn.booleanBinding(gameEnded) { humanTurn ->
        (humanTurn == true &&
         !gameEnded.value &&
         gameState.value?.let { GameRuleLogic.isFirstMove(it) } == false
        ).also { logger.debug("Human turn $humanTurn - canSkip $it") }
    }
    
    val undeployedPieces: Map<Color, ObservableValue<Collection<PieceShape>>> = EnumMap(
        Color.values().associateWith { color ->
            nonNullObjectBinding(gameState, gameState) { value?.undeployedPieceShapes(color) ?: PieceShape.values().toList() }
        })
	
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

    fun isValidColor(color: Color): Boolean =
            gameState.get()?.isValid(color) != false

    init {
        subscribe<NewGameState> { event ->
            val state = event.gameState
            logger.debug("New GameState $state")
            gameState.set(state)
        }
        subscribe<HumanMoveRequest> { event ->
            val state = event.gameState
            val moves = EnumMap(
                state.undeployedPieceShapes().associateWith {
                    GameRuleLogic.getPossibleMovesForShape(state, it)
                })
            logger.debug("Human move request for {} - {} possible moves",
                state.currentColor,
                moves.values.sumBy { it.size })
            
            gameState.set(event.gameState)
            isHumanTurn.set(true)
    
            validPieces.getValue(state.currentColor)
                .set(moves.filterValues { it.isNotEmpty() }.keys)
        }
        subscribe<HumanMoveAction> {
            isHumanTurn.set(false)
        }
        subscribe<GameOverEvent> { event ->
            gameResult.set(event.result)
        }
    }

    fun clearGame() {
        gameState.set(null)
        gameResult.set(null)
        availableTurns.set(0)
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