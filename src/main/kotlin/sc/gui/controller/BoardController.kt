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
    var hoverable: Boolean = true
    var currentPlaceable: Boolean = false
    val board: BoardModel by inject()
    val view: BoardView by inject()
    val game: GameController by inject()
    private var isHoverableBoard: Array<Array<Boolean>> = Array(Constants.BOARD_SIZE) { Array(Constants.BOARD_SIZE) { true } }
    private var isPlaceableBoard: Array<Array<Boolean>> = Array(Constants.BOARD_SIZE) { Array(Constants.BOARD_SIZE) { false } }

    fun handleClick(x: Int, y: Int) {
        if (isHoverable(x, y, game.selectedCalculatedShape.get()) && isPlaceable(x, y, game.selectedCalculatedShape.get())) {
            logger.debug("Set-Move seems valid from GUI at $x, $y")
            val color = game.selectedColor.get()

            val move = SetMove(Piece(color, game.selectedShape.get(), game.selectedRotation.get(), game.selectedFlip.get(), Coordinates(x, y)))
            GameRuleLogic.validateSetMove(board.boardProperty().get(), move)
            fire(HumanMoveAction(move))
            game.isHumanTurn.set(false)
        } else {
            logger.debug("Set-Move seems invalid from GUI at $x, $y")
        }

    }

    fun hoverInBound(x: Int, y: Int): Boolean {
        return x >= 0 && y >= 0 && x < Constants.BOARD_SIZE && y < Constants.BOARD_SIZE
    }

    fun calculateIsPlaceableBoard(board: Board, color: Color) {
        logger.debug("Calculating isPlaceableBoard...")
        for (x in 0 until Constants.BOARD_SIZE) {
            for (y in 0 until Constants.BOARD_SIZE) {
                if (board[x, y].content != FieldContent.EMPTY) {
                    isHoverableBoard[x][y] = false
                } else {
                    val field: FieldContent = when (color) {
                        Color.RED -> FieldContent.RED
                        Color.YELLOW -> FieldContent.YELLOW
                        Color.GREEN -> FieldContent.GREEN
                        Color.BLUE -> FieldContent.BLUE
                    }
                    isHoverableBoard[x][y] = (!hoverInBound(x + 1, y) || hoverInBound(x + 1, y) && board[x + 1, y].content != field) &&
                            (!hoverInBound(x - 1, y) || hoverInBound(x - 1, y) && board[x - 1, y].content != field) &&
                            (!hoverInBound(x, y + 1) || hoverInBound(x, y + 1) && board[x, y + 1].content != field) &&
                            (!hoverInBound(x, y - 1) || hoverInBound(x, y - 1) && board[x, y - 1].content != field)
                    if (isHoverableBoard[x][y]) {
                        isPlaceableBoard[x][y] = hoverInBound(x + 1, y + 1) && board[x + 1, y + 1].content == field ||
                                hoverInBound(x + 1, y - 1) && board[x + 1, y - 1].content == field ||
                                hoverInBound(x - 1, y + 1) && board[x - 1, y + 1].content == field ||
                                hoverInBound(x - 1, y - 1) && board[x - 1, y - 1].content == field
                    }
                }
            }
        }

        // corner palces are allowed to be placed
        isPlaceableBoard[0][0] = true
        isPlaceableBoard[0][Constants.BOARD_SIZE - 1] = true
        isPlaceableBoard[Constants.BOARD_SIZE - 1][0] = true
        isPlaceableBoard[Constants.BOARD_SIZE - 1][Constants.BOARD_SIZE - 1] = true
    }

    fun isHoverable(x: Int, y: Int, shape: Set<Coordinates>): Boolean {
        if (!game.isHumanTurn.get()) {
            return false
        }

        for (place in shape) {
            // check every adjacent field if it is the same color
            if (!hoverInBound(x + place.x, y + place.y)) {
                return false
            } else if (!isHoverableBoard[x + place.x][y + place.y]) {
                return false
            }
        }

        return true
    }

    fun isPlaceable(x: Int, y: Int, shape: Set<Coordinates>): Boolean {
        if (game.isHumanTurn.get()) {
            for (place in shape) {
                // one field is enough as isHoverable prevents otherwise
                if (hoverInBound(x + place.x, y + place.y) && isPlaceableBoard[x + place.x][y + place.y]) {
                    return true
                }
            }
        }

        return false
    }


    companion object {
        private val logger = LoggerFactory.getLogger(BoardController::class.java)
    }
}
