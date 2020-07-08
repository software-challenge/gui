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
            val color: FieldContent

            when (selected.color) {
                PlayerColor.BLUE -> color = FieldContent.BLUE
                PlayerColor.RED -> color = FieldContent.RED
                PlayerColor.YELLOW -> color = FieldContent.YELLOW
                PlayerColor.GREEN -> color = FieldContent.GREEN
            }
            val shape = PieceShape.shapes[selected.kind].second
            for (c in shape.coordinates) {
                val cx = x + c.x
                val cy = y + c.y
                model.setField(cx, cy, Field(Coordinates(cx, cy), color))
            }
        } else {
            println("Click, but no item selected")
        }

    }

}
