package sc.gui.controller

import sc.gui.view.GameView
import sc.gui.model.BoardModel
import sc.gui.model.GameModel
import sc.gui.view.BoardView
import sc.plugin2021.*
import tornadofx.Controller

class BoardController: Controller() {

    val board: BoardModel by inject()
    val view: BoardView by inject()
    val gameView: GameView by inject()
    val game: GameController by inject()

    fun handleClick(x: Int, y: Int) {
        val color = game.currentColorProperty().get()
        val shape = game.currentPieceShapeProperty().get()
        val rotation = game.currentRotationProperty().get() ?: Rotation.NONE
        val flip = game.currentFlipProperty().get() ?: false

        if (shape != null && color != null) {
            for (c in Piece(color, shape, rotation, flip, Coordinates(x, y)).coordinates) {
                board.setField(c.x, c.y, Field(c, +color))
            }
        } else {
            println("Click, but no item selected")
        }

    }

}
