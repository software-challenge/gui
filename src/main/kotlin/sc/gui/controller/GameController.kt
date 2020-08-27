package sc.gui.controller

import sc.plugin2021.Color
import sc.plugin2021.PieceShape
import sc.plugin2021.Rotation
import tornadofx.Controller
import tornadofx.getProperty
import tornadofx.property
import kotlin.math.max

class GameController: Controller() {
    private var currentColor:Color by property<Color>(Color.RED)
    private var currentPieceShape:PieceShape by property<PieceShape>(PieceShape.MONO)
    private var currentRotation:Rotation by property<Rotation>(Rotation.NONE)
    private var currentFlip:Boolean by property<Boolean>(false)
    private var availableTurns by property<Int>(0)
    private var currentTurn by property<Int>(0)

    fun currentColorProperty() = getProperty(GameController::currentColor)
    fun currentPieceShapeProperty() = getProperty(GameController::currentPieceShape)
    fun currentRotationProperty() = getProperty(GameController::currentRotation)
    fun currentFlipProperty() = getProperty(GameController::currentFlip)
    fun availableTurnsProperty() = getProperty(GameController::availableTurns)
    fun currentTurnProperty() = getProperty(GameController::currentTurn)

    init {
        subscribe<NewGameState> { event ->
            availableTurnsProperty().set(max(availableTurns, event.gameState.turn))
            currentTurnProperty().set(event.gameState.turn)
        }
    }

    fun selectColor(color: Color) {
        currentColorProperty().set(color)
    }

    fun selectPieceShape(shape: PieceShape) {
        currentPieceShapeProperty().set(shape)
    }

    fun selectRotation(rotation: Rotation) {
        currentRotationProperty().set(rotation)
    }

    fun selectFlip(flip: Boolean) {
        currentFlipProperty().set(flip)
    }
}