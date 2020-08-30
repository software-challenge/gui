package sc.gui.controller

import sc.gui.model.BoardModel
import sc.gui.view.BoardView
import sc.plugin2021.*
import sc.plugin2021.util.Constants
import sc.plugin2021.util.GameRuleLogic
import tornadofx.*

class BoardController : Controller() {
    var currentHover: Coordinates? = null
    val board: BoardModel by inject()
    val view: BoardView by inject()
    val game: GameController by inject()

    fun handleClick(x: Int, y: Int) {
        if (isPlaceable(x, y, game.selectedCalculatedShape.get())) {
            val color = game.selectedColor.get()

            val piece = Piece(color, game.selectedShape.get(), game.selectedRotation.get(), game.selectedFlip.get(), Coordinates(x, y))
            GameRuleLogic.validateSetMove(board.board, SetMove(piece))

            for (c in piece.coordinates) {
                board.setField(c.x, c.y, +color)
                board.board[c.x, c.y] = +color
            }
        } else {
            println("Click, but no item selected")
        }

    }

    fun hoverInBound(x: Int, y: Int): Boolean {
        return x >= 0 && y >= 0 && x < Constants.BOARD_SIZE && y < Constants.BOARD_SIZE
    }

    fun isPlaceable(x: Int, y: Int, shape: Set<Coordinates>): Boolean {
        if (!game.isHumanTurnProperty().get()) {
            return false
        }

        val field: FieldContent = when (game.selectedColor.get()) {
            Color.RED -> FieldContent.RED
            Color.YELLOW -> FieldContent.YELLOW
            Color.GREEN -> FieldContent.GREEN
            Color.BLUE -> FieldContent.BLUE
            else -> FieldContent.EMPTY
        }

        for (place in shape) {
            // check every adjacent field if it is the same color
            if (!hoverInBound(x + place.x, y + place.y) || board.getField(x + place.x, y + place.y).content != FieldContent.EMPTY ||
                    hoverInBound(x + place.x + 1, y + place.y) && board.getField(x + place.x + 1, y + place.y).content == field ||
                    hoverInBound(x + place.x - 1, y + place.y) && board.getField(x + place.x - 1, y + place.y).content == field ||
                    hoverInBound(x + place.x, y + place.y + 1) && board.getField(x + place.x, y + place.y + 1).content == field ||
                    hoverInBound(x + place.x, y + place.y - 1) && board.getField(x + place.x, y + place.y - 1).content == field
            ) {
                return false
            }
        }

        return true
    }

}
