package sc.gui.controller

import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.Property
import org.slf4j.LoggerFactory
import sc.gui.model.PiecesModel
import sc.gui.view.PiecesFragment
import sc.plugin2021.*
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

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
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

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
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

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
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

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}


class GameController : Controller() {
    private var currentPiece: PiecesModel by property(PiecesModel(Color.RED, PieceShape.MONO))
    private val boardController: BoardController by inject()
    private var availableTurns: Int by property(0)
    private var currentTurn: Int by property(0)
    private var turnColor: Color by property(Color.RED)
    private var isHumanTurn: Boolean by property(false)

    fun turnColorProperty() = getProperty(GameController::turnColor)
    fun availableTurnsProperty() = getProperty(GameController::availableTurns)
    fun currentTurnProperty() = getProperty(GameController::currentTurn)
    fun isHumanTurnProperty() = getProperty(GameController::isHumanTurn)

    // use selected* to access the property of currentPiece in order to always correctly be automatically rebind
    private fun currentPieceProperty() = getProperty(GameController::currentPiece)

    var selectedColor: ColorBinding = ColorBinding(currentPieceProperty())
    var selectedShape: ShapeBinding = ShapeBinding(currentPieceProperty())
    var selectedRotation: RotationBinding = RotationBinding(currentPieceProperty())
    var selectedFlip: FlipBinding = FlipBinding(currentPieceProperty())
    var selectedCalculatedShape: CalculatedShapeBinding = CalculatedShapeBinding(currentPieceProperty())

    init {
        subscribe<NewGameState> { event ->
            availableTurnsProperty().set(max(availableTurns, event.gameState.turn))
            currentTurnProperty().set(event.gameState.turn)
            turnColorProperty().set(event.gameState.currentColor)
        }
        subscribe<HumanMoveRequest> {
            isHumanTurnProperty().set(true)
            boardController.calculateIsPlaceableBoard()
        }
    }

    fun selectPiece(piece: PiecesModel) {
        currentPieceProperty().set(piece)
    }

    fun flipPiece() {
        currentPieceProperty().get().flipPiece()
    }

    fun rotatePiece(rotate: Rotation) {
        selectedRotation.set(selectedRotation.get().rotate(rotate))
    }

    fun scroll(deltaY: Double) {
        currentPieceProperty().get().scroll(deltaY)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}
