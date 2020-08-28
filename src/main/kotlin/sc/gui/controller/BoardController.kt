package sc.gui.controller

import sc.gui.view.GameView
import sc.gui.model.BoardModel
import sc.gui.view.BoardView
import sc.plugin2021.*
import sc.plugin2021.util.GameRuleLogic
import tornadofx.*

class BoardController : Controller() {
    var currentHover: Coordinates? = null
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
            val piece = Piece(color, shape, rotation, flip, Coordinates(x, y))
            GameRuleLogic.validateSetMove(board.board, SetMove(piece))

            for (c in piece.coordinates) {
                board.setField(c.x, c.y, +color)
                board.board[c.x, c.y] = +color
            }
        } else {
            println("Click, but no item selected")
        }

    }

}
