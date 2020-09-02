package sc.gui.controller

import org.slf4j.LoggerFactory
import sc.gui.model.BoardModel
import sc.gui.view.BoardView
import sc.gui.view.PiecesFragment
import sc.plugin2021.*
import sc.plugin2021.util.Constants
import sc.plugin2021.util.GameRuleLogic
import tornadofx.*

class BoardController : Controller() {
    var currentHover: Coordinates? = null
    var currentPlaceable: Boolean = true
    val board: BoardModel by inject()
    val view: BoardView by inject()
    val game: GameController by inject()
    private var isPlaceableBoard: Array<Array<Boolean>> = Array(Constants.BOARD_SIZE) { Array(Constants.BOARD_SIZE) { true } }

    fun handleClick(x: Int, y: Int) {
        if (isPlaceable(x, y, game.selectedCalculatedShape.get())) {
            val color = game.selectedColor.get()

            val move = SetMove(Piece(color, game.selectedShape.get(), game.selectedRotation.get(), game.selectedFlip.get(), Coordinates(x, y)))
            GameRuleLogic.validateSetMove(board.boardProperty().get(), move)
            fire(HumanMoveAction(move))
        } else {
            println("Click, but no item selected")
        }

    }

    fun hoverInBound(x: Int, y: Int): Boolean {
        return x >= 0 && y >= 0 && x < Constants.BOARD_SIZE && y < Constants.BOARD_SIZE
    }

    fun calculateIsPlaceableBoard() {
        logger.debug("Calculating isPlaceableBoard...")
        val board = board.boardProperty().get()
        for (x in 0 until Constants.BOARD_SIZE) {
            for (y in 0 until Constants.BOARD_SIZE) {
                val color = game.selectedColor.get()
                if (color == null || board[x, y].content != FieldContent.EMPTY) {
                    isPlaceableBoard[x][y] = false
                } else {
                    val field: FieldContent = when (color) {
                        Color.RED -> FieldContent.RED
                        Color.YELLOW -> FieldContent.YELLOW
                        Color.GREEN -> FieldContent.GREEN
                        Color.BLUE -> FieldContent.BLUE
                    }
                    isPlaceableBoard[x][y] = (!hoverInBound(x + 1, y) || hoverInBound(x + 1, y) && board[x + 1, y].content != field) &&
                            (!hoverInBound(x - 1, y) || hoverInBound(x - 1, y) && board[x - 1, y].content != field) &&
                            (!hoverInBound(x, y + 1) || hoverInBound(x, y + 1) && board[x, y + 1].content != field) &&
                            (!hoverInBound(x, y - 1) || hoverInBound(x, y - 1) && board[x, y - 1].content != field)
                }
            }
        }
    }

    fun isPlaceable(x: Int, y: Int, shape: Set<Coordinates>): Boolean {
        if (!game.isHumanTurnProperty().get()) {
            return false
        }

        for (place in shape) {
            // check every adjacent field if it is the same color
            if (!hoverInBound(x + place.x, y + place.y)) {
                return false
            } else if (!isPlaceableBoard[x + place.x][y + place.y]) {
                return false
            }
        }

        return true
    }


    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}
