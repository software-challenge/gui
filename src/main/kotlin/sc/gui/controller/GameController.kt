package sc.gui.controller

import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.Property
import javafx.scene.input.ScrollEvent
import org.slf4j.LoggerFactory
import sc.gui.model.PiecesModel
import sc.gui.view.PiecesFragment
import sc.plugin2021.Color
import sc.plugin2021.Coordinates
import sc.plugin2021.PieceShape
import sc.plugin2021.Rotation
import tornadofx.*
import kotlin.math.max

// The following *Binding-classes are necessary to automatically unbind and rebind to a new piece (when switched)
// in order to prevent this hassle in every other class, that uses the current selected PieceModel
class ColorBinding(private val piece: Property<PiecesModel>) : ObjectBinding<Color>() {
    val model = piece

    init {
        bind(model)
        bind(model.value.colorProperty())
        model.addListener { _, oldValue, newValue ->
            logger.debug("Piece changed, updating color " + oldValue.colorProperty().get() + " -> " + newValue.colorProperty().get())
            unbind(oldValue.colorProperty())
            bind(newValue.colorProperty())
            model.value = newValue
            logger.debug("Now returning ${computeValue()}")
        }
    }

    override fun computeValue(): Color {
        logger.debug("Color: ${model.value.colorProperty().get()}")
        return model.value.colorProperty().get()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}

class ShapeBinding(private val piece: Property<PiecesModel>) : ObjectBinding<PieceShape>() {
    val model = piece

    init {
        bind(model)
        bind(model.value.shapeProperty())
        model.addListener { _, oldValue, newValue ->
            logger.debug("Piece changed, updating shape " + oldValue.shapeProperty().get() + " -> " + newValue.shapeProperty().get())
            unbind(oldValue.shapeProperty())
            bind(newValue.shapeProperty())
            model.value = newValue
            logger.debug("Now returning ${computeValue()}")
        }
    }

    override fun computeValue(): PieceShape {
        return model.value.shapeProperty().get()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}

class RotationBinding(private val piece: Property<PiecesModel>) : ObjectBinding<Rotation>() {
    val model = piece

    init {
        bind(model)
        bind(model.value.rotationProperty())
        model.addListener { _, oldValue, newValue ->
            logger.debug("Piece changed, updating rotation " + oldValue.rotationProperty().get() + " -> " + newValue.rotationProperty().get())
            unbind(oldValue.rotationProperty())
            bind(newValue.rotationProperty())
            model.value = newValue
            logger.debug("Now returning ${computeValue()}")
        }
    }

    override fun computeValue(): Rotation {
        return model.value.rotationProperty().get()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}

class FlipBinding(private val piece: Property<PiecesModel>) : BooleanBinding() {
    val model = piece

    init {
        bind(model)
        bind(model.value.flipProperty())
        model.addListener { _, oldValue, newValue ->
            logger.debug("Piece changed, updating flip " + oldValue.flipProperty().get() + " -> " + newValue.flipProperty().get())
            unbind(oldValue.flipProperty())
            bind(newValue.flipProperty())
            model.value = newValue
            logger.debug("Now returning ${computeValue()}")
        }
    }

    override fun computeValue(): Boolean {
        return model.value.flipProperty().get()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}

class CalculatedShapeBinding(private val piece: Property<PiecesModel>) : ObjectBinding<Set<Coordinates>>() {
    val model = piece

    init {
        bind(model)
        bind(model.value.shapeProperty())
        model.addListener { _, oldValue, newValue ->
            logger.debug("Piece changed, updating calculatedShape " + oldValue.calculatedShapeProperty().get() + " -> " + newValue.calculatedShapeProperty().get())
            unbind(oldValue.calculatedShapeProperty())
            bind(newValue.calculatedShapeProperty())
            model.value = newValue
            logger.debug("Now returning ${computeValue()}")
        }
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
    private var availableTurns: Int by property(0)
    private var currentTurn: Int by property(0)
    private var turnColor: Color by property(Color.RED)
    private var isHumanTurn: Boolean by property(true)

    fun turnColorProperty() = getProperty(GameController::turnColor)
    fun availableTurnsProperty() = getProperty(GameController::availableTurns)
    fun currentTurnProperty() = getProperty(GameController::currentTurn)
    fun isHumanTurnProperty() = getProperty(GameController::isHumanTurn)

    // use selected* functions to access the property of currentPiece in order to always correctly be automatically rebind
    private fun currentPieceProperty() = getProperty(GameController::currentPiece)


    var selectedColor: ColorBinding = ColorBinding(currentPieceProperty())
    var selectedShape: ShapeBinding = ShapeBinding(currentPieceProperty())
    var selectedRotation: RotationBinding = RotationBinding(currentPieceProperty())
    var selectedFlip: FlipBinding = FlipBinding(currentPieceProperty())
    var selectedCalulatedShape: CalculatedShapeBinding = CalculatedShapeBinding(currentPieceProperty())

    init {
        subscribe<NewGameState> { event ->
            availableTurnsProperty().set(max(availableTurns, event.gameState.turn))
            currentTurnProperty().set(event.gameState.turn)
        }
        subscribe<HumanMoveRequest> { event ->
            isHumanTurnProperty().set(true)
        }
    }

    fun selectPiece(piece: PiecesModel) {
        logger.debug("Recieved new piece, updating")
        currentPieceProperty().set(piece)
    }

    fun flipPiece() {
        currentPieceProperty().get().flipPiece()
    }

    fun scroll(event: ScrollEvent) {
        currentPieceProperty().get().scroll(event)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}
