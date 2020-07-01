package sc.gui.controller

import javafx.geometry.Point2D
import sc.gui.model.BoardModel
import sc.gui.model.Field
import sc.gui.model.FieldContent
import sc.gui.model.PlayerColor
import sc.gui.view.BoardView
import sc.gui.view.RedUndeployedPiecesView
import tornadofx.Controller

class BoardController: Controller() {

    val model: BoardModel by inject()
    val view: BoardView by inject()
    val redPieces: RedUndeployedPiecesView by inject()

    fun handleClick(x: Int, y: Int) {

        val color: FieldContent
        when (redPieces.selectedItem()?.color) {
            null -> color = FieldContent.EMPTY
            PlayerColor.BLUE -> color = FieldContent.BLUE
            PlayerColor.RED -> color = FieldContent.RED
            PlayerColor.YELLOW -> color = FieldContent.YELLOW
            PlayerColor.GREEN -> color = FieldContent.GREEN
        }
        model.setField(x, y, Field(Point2D(x.toDouble(), y.toDouble()), color))
    }

}
