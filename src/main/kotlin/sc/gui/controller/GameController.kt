package sc.gui.controller

import javafx.scene.input.Dragboard
import sc.plugin2021.Color
import sc.plugin2021.Coordinates
import sc.plugin2021.PieceShape
import sc.plugin2021.Rotation
import tornadofx.Controller
import tornadofx.getProperty
import tornadofx.property
import kotlin.math.max

class GameController : Controller() {
    private var currentColor: Color by property<Color>(Color.RED)
    private var currentPieceShape: PieceShape by property<PieceShape>(PieceShape.MONO)
    private var currentRotation: Rotation by property<Rotation>(Rotation.NONE)
    private var currentFlip: Boolean by property<Boolean>(false)
    private var availableTurns by property<Int>(0)
    private var currentTurn by property<Int>(0)
    private var selectedShape: Set<Coordinates> by property(currentPieceShape.transform(currentRotation, currentFlip))
    var selectedDragBoard by property<Dragboard>()

    fun currentColorProperty() = getProperty(GameController::currentColor)
    fun currentPieceShapeProperty() = getProperty(GameController::currentPieceShape)
    fun currentRotationProperty() = getProperty(GameController::currentRotation)
    fun currentFlipProperty() = getProperty(GameController::currentFlip)
    fun selectedShapeProperty() = getProperty(GameController::selectedShape)
    fun availableTurnsProperty() = getProperty(GameController::availableTurns)
    fun currentTurnProperty() = getProperty(GameController::currentTurn)

    init {
        subscribe<NewGameState> { event ->
            availableTurnsProperty().set(max(availableTurns, event.gameState.turn))
            currentTurnProperty().set(event.gameState.turn)
        }
        currentPieceShapeProperty().addListener { _, _, newValue ->
            selectedShape = newValue.transform(currentRotationProperty().get(), currentFlipProperty().get())
        }
        currentRotationProperty().addListener { _, _, newValue ->
            selectedShape = currentPieceShapeProperty().get().transform(newValue, currentFlipProperty().get())
        }
        currentFlipProperty().addListener { _, _, newValue ->
            selectedShape = currentPieceShapeProperty().get().transform(currentRotationProperty().get(), newValue)
        }
    }

    fun selectColor(color: Color) {
        currentColorProperty().set(color)
    }

    fun selectPieceShape(shape: PieceShape) {
        currentPieceShapeProperty().set(shape)
        selectedShape = shape.transform(currentRotationProperty().get(), currentFlipProperty().get())
    }

    fun selectRotation(rotation: Rotation) {
        currentRotationProperty().set(rotation)
        selectedShape = currentPieceShapeProperty().get().transform(rotation, currentFlipProperty().get())
    }

    fun selectFlip(flip: Boolean) {
        currentFlipProperty().set(flip)
        selectedShape = currentPieceShapeProperty().get().transform(currentRotationProperty().get(), flip)
    }


}