package sc.gui.controller

import sc.gui.GameView
import sc.gui.model.BoardModel
import sc.gui.view.BoardView
import sc.plugin2021.*
import tornadofx.Controller

class BoardController: Controller() {

    val model: BoardModel by inject()
    val view: BoardView by inject()
    val gameView: GameView by inject()

    fun handleClick(x: Int, y: Int) {
        val selected = gameView.redUndeployedPieces.selectedItem()
        if (selected != null) {
            val shape = PieceShape.shapes[selected.kind]
            if (shape != null) {
                for (c in shape.coordinates) {
                    val cx = x + c.x
                    val cy = y + c.y
                    model.setField(cx, cy, Field(Coordinates(cx, cy), +selected.color))
                }
            }
        } else {
            println("Click, but no item selected")
        }

    }

}
