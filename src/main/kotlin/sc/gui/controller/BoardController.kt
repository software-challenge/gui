package sc.gui.controller

import javafx.scene.image.ImageView
import sc.gui.view.GameView
import sc.gui.model.BoardModel
import sc.gui.view.BoardView
import sc.plugin2021.*
import sc.plugin2021.util.GameRuleLogic
import tornadofx.*

class BoardController : Controller() {

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
            val colorName = when (game.currentColorProperty().get()) {
                Color.RED -> "red"
                Color.GREEN -> "green"
                Color.YELLOW -> "yellow"
                Color.BLUE -> "blue"
                else -> throw Exception("Unkown color")
            }
            val image = ImageView("file:resources/graphics/blokus/$colorName/${game.currentPieceShapeProperty().get().name.toLowerCase()}.png")
            // Rotating Image
            image.rotate = when (game.currentRotationProperty().get()) {
                Rotation.LEFT -> -90.0
                Rotation.MIRROR -> 180.0
                Rotation.NONE -> 0.0
                Rotation.RIGHT -> 90.0
                else -> throw Exception("Impossible Rotation...")
            }

            // currently all images are 4 x 4 blocks big (aka 64px x 64px)
            // scale image to fit board
            image.scaleX = view.root.width / (16.0 * 20)
            image.scaleY = view.root.height / (16.0 * 20)
            view.root.add(image, x, y, 4, 4)
        } else {
            println("Click, but no item selected")
        }

    }

}
