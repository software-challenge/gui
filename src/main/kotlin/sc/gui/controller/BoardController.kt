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
        val shape = game.currentPieceShapeProperty().get()
        val color = game.currentColorProperty().get()
        if (shape != null && color != null) {
            for (c in shape.coordinates) {
                val cx = x + c.x
                val cy = y + c.y
                board.setField(cx, cy, Field(Coordinates(cx, cy), +color))
            }
        } else {
            println("Click, but no item selected")
        }

    }

}
