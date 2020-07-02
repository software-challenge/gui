package sc.gui.controller

import javafx.geometry.Point2D
import sc.data.PlayerColor
import sc.data.pieceShapes
import sc.gui.model.BoardModel
import sc.gui.model.Field
import sc.gui.model.FieldContent
import sc.gui.view.BoardView
import sc.gui.view.RedUndeployedPiecesView
import tornadofx.Controller

class BoardController: Controller() {

    val model: BoardModel by inject()
    val view: BoardView by inject()
    val redPieces: RedUndeployedPiecesView by inject()

    fun handleClick(x: Int, y: Int) {

        val selected = redPieces.selectedItem()
        if (selected != null) {
            val color: FieldContent

            when (selected.color) {
                PlayerColor.BLUE -> color = FieldContent.BLUE
                PlayerColor.RED -> color = FieldContent.RED
                PlayerColor.YELLOW -> color = FieldContent.YELLOW
                PlayerColor.GREEN -> color = FieldContent.GREEN
            }
            val shape = pieceShapes[selected.kind]
            for (c in shape.coordinates) {
                val cx = x + c.x
                val cy = y + c.y
                model.setField(cx, cy, Field(Point2D(cx.toDouble(), cy.toDouble()), color))
            }
        }
    }

}
