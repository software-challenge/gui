package sc.gui.controller

import org.slf4j.LoggerFactory
import sc.api.plugins.Coordinates
import sc.plugin2027.*
import sc.plugin2027.util.Constants
import sc.plugin2027.util.GameRuleLogic
import tornadofx.*

class BlokusBoardController : Controller() {
    var currentHover: Coordinates? = null
    var hoverable: Boolean = true
    var currentPlaceable: Boolean = false
    val game: BlokusController by inject()
    
    init {
        subscribe<HumanMoveRequest> { event ->
            event.gameState.let {
                calculateIsPlaceableBoard((it as GameState).board, (it as GameState).currentColor)
            }
        }
    }
    
    private var isHoverableBoard: Array<Array<Boolean>> = Array(Constants.BOARD_LENGTH) { Array(Constants.BOARD_LENGTH) { true } }
    private var isPlaceableBoard: Array<Array<Boolean>> = Array(Constants.BOARD_LENGTH) { Array(Constants.BOARD_LENGTH) { false } }
    
    /**
     * This actually sends a human move.
     */
    fun handleClick(x: Int, y: Int) {
        if(!game.atLatestTurn.value)
            return
        val shape = game.selectedCalculatedShape.get()
        val hoverable = isHoverable(x, y, shape)
        val placeable = isPlaceable(x, y, shape)
        if (hoverable && placeable) {
            logger.debug("Set-Move from GUI at [$x,$y] seems valid")
            val color = game.selectedColor.get()
            
            val move = SetMove(Piece(color, game.selectedShape.get(), game.selectedRotation.get(), game.selectedFlip.get(), Coordinates(x, y)))
            val board = game.gameState.get()?.board
            if (board != null) {
                GameRuleLogic.validateSetMove(board, move)
            } else {
                // FIXME what now?
            }
            fire(HumanMoveAction(move))
        } else {
            logger.debug("Set-Move from GUI at [$x,$y] seems invalid")
        }
        
    }
    
    fun hoverInBound(x: Int, y: Int): Boolean =
        x >= 0 && y >= 0 && x < Constants.BOARD_LENGTH && y < Constants.BOARD_LENGTH
    
    fun calculateIsPlaceableBoard(board: Board, color: Color) {
        logger.debug("Calculating where pieces can be hovered and placed on the board...")
        for (x in 0 until Constants.BOARD_LENGTH) {
            for (y in 0 until Constants.BOARD_LENGTH) {
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
        
        // The edges are always placeable in the first round.
        if (game.gameState.get()?.round == 1) {
            for (y in 0 until Constants.BOARD_LENGTH) {
                for(x in 0 until Constants.BOARD_LENGTH) {
                    if(y == 0 || y == Constants.BOARD_LENGTH - 1 || x == 0 || x == Constants.BOARD_LENGTH - 1) {
                        isPlaceableBoard[x][y] = true
                    }
                }
            }
        }
    }
    
    fun isHoverable(x: Int, y: Int, shape: Set<Coordinates>): Boolean {
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
        try {
            
            for (place in shape) {
                // one field is enough as isHoverable prevents otherwise
                val hoverInBound = hoverInBound(x + place.x, y + place.y)
                val isPlaceAbleBoard = isPlaceableBoard[x + place.x][y + place.y]
                if (hoverInBound && isPlaceAbleBoard) {
                    return true
                }
            }
        } catch(e: ArrayIndexOutOfBoundsException) {
            // Fall-through case
        }
        return false
    }
    
    
    companion object {
        private val logger = LoggerFactory.getLogger(BlokusBoardController::class.java)
    }
}