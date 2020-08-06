package sc.gui.controller

import javafx.beans.property.SimpleStringProperty
import sc.plugin2021.Color
import sc.plugin2021.PieceShape
import tornadofx.Controller
import tornadofx.getProperty
import tornadofx.property

class GameController: Controller() {
    private var currentColor:Color by property<Color>(Color.RED)
    private var currentPieceShape:PieceShape by property<PieceShape>(PieceShape.MONO)
    fun currentColorProperty() = getProperty(GameController::currentColor)
    fun currentPieceShapeProperty() = getProperty(GameController::currentPieceShape)

    fun selectColor(color: Color) {
        currentColorProperty().set(color)
    }

    fun selectPieceShape(shape: PieceShape) {
        currentPieceShapeProperty().set(shape)
    }
}