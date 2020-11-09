package sc.gui.controller

import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import org.slf4j.LoggerFactory
import sc.gui.model.PiecesModel
import sc.gui.view.*
import sc.plugin2021.*
import sc.plugin2021.util.GameRuleLogic
import sc.shared.GameResult
import sc.shared.InvalidMoveException
import tornadofx.*
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
        logger.debug("Color: ${model.value.colorProperty().get()}")
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
    val currentColor = objectProperty(Color.RED)
    val currentTeam = objectProperty(Team.ONE)
    val isHumanTurn = objectProperty(false)
    val canSkip = objectProperty(false)
    val previousColor = objectProperty(Color.RED)
    val teamOneScore = objectProperty(0)
    val teamTwoScore = objectProperty(0)
    val gameResult: ObjectProperty<GameResult?> = objectProperty(null)

    // we need to have them split separately otherwise we cannot listen to a specific color alone
    val undeployedRedPieces = objectProperty(PieceShape.shapes.values)
    val undeployedBluePieces = objectProperty(PieceShape.shapes.values)
    val undeployedGreenPieces = objectProperty(PieceShape.shapes.values)
    val undeployedYellowPieces = objectProperty(PieceShape.shapes.values)
	
    val validRedPieces = objectProperty(ArrayList<PieceShape>())
    val validBluePieces = objectProperty(ArrayList<PieceShape>())
    val validGreenPieces = objectProperty(ArrayList<PieceShape>())
    val validYellowPieces = objectProperty(ArrayList<PieceShape>())

    // use selected* to access the property of currentPiece in order to always correctly be automatically rebind
    val currentPiece = objectProperty(PiecesModel(Color.RED, PieceShape.MONO))
    val selectedColor: ColorBinding = ColorBinding(currentPiece)
    val selectedShape: ShapeBinding = ShapeBinding(currentPiece)
    val selectedRotation: RotationBinding = RotationBinding(currentPiece)
    val selectedFlip: FlipBinding = FlipBinding(currentPiece)
    val selectedCalculatedShape: CalculatedShapeBinding = CalculatedShapeBinding(currentPiece)

    fun isValidColor(color: Color): Boolean = gameState.get().orderedColors.contains(color)

    init {
        subscribe<NewGameState> { event ->
            logger.debug("New game state")
            gameState.set(event.gameState)
            canSkip.set(false)

            // I don't know why orderedColors becomes an empty array and results in CurrentColor being inaccessible (throwing error) when the game ended,
            // but this is how we can avoid it for now TODO("fix this in the plugin")
            if (event.gameState.orderedColors.isNotEmpty()) {
                previousColor.set(currentColor.get())
                currentColor.set(event.gameState.currentColor)
                currentTeam.set(event.gameState.currentTeam)
            }
            undeployedRedPieces.set(event.gameState.undeployedPieceShapes(Color.RED))
            undeployedBluePieces.set(event.gameState.undeployedPieceShapes(Color.BLUE))
            undeployedGreenPieces.set(event.gameState.undeployedPieceShapes(Color.GREEN))
            undeployedYellowPieces.set(event.gameState.undeployedPieceShapes(Color.YELLOW))
            boardController.board.boardProperty().set(event.gameState.board)
            validRedPieces.set(ArrayList())
            validBluePieces.set(ArrayList())
            validGreenPieces.set(ArrayList())
            validYellowPieces.set(ArrayList())
            availableTurns.set(max(availableTurns.get(), event.gameState.turn))
            currentTurn.set(event.gameState.turn)
            teamOneScore.set(event.gameState.getPointsForPlayer(Team.ONE))
            teamTwoScore.set(event.gameState.getPointsForPlayer(Team.TWO))
        }
        subscribe<HumanMoveRequest> { event ->
            logger.debug("Human move request")

            val moves = GameRuleLogic.getPossibleMoves(event.gameState)

            isHumanTurn.set(true)
            canSkip.set(!gameEnded() && isHumanTurn.get() && !GameRuleLogic.isFirstMove(event.gameState))
            boardController.calculateIsPlaceableBoard(event.gameState.board, event.gameState.currentColor)

            when (event.gameState.currentColor) {
                Color.RED -> validRedPieces
                Color.BLUE -> validBluePieces
                Color.GREEN -> validGreenPieces
                Color.YELLOW -> validYellowPieces
            }.set(event.gameState.undeployedPieceShapes(event.gameState.currentColor).filter { shape ->
                moves.any { it.piece.kind == shape }
            } as ArrayList<PieceShape>?)
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
        undeployedRedPieces.set(PieceShape.values().toList())
        undeployedBluePieces.set(PieceShape.values().toList())
        undeployedGreenPieces.set(PieceShape.values().toList())
        undeployedYellowPieces.set(PieceShape.values().toList())
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