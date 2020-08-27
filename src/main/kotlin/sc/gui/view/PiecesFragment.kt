package sc.gui.view

import javafx.scene.input.MouseButton
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.GameController
import sc.plugin2021.Color
import sc.plugin2021.PieceShape
import tornadofx.*

class PiecesFragment(color: Color, pieceShape: PieceShape) : Fragment() {
    val controller: GameController by inject()

    override val root = hbox {
        // setPrefSize(80.0, 80.0)

        setOnMouseEntered {
            addClass(AppStyle.colorGRAY)
            it.consume()
        }

        setOnMouseExited {
            removeClass(AppStyle.colorGRAY)
            it.consume()
        }

        setOnMouseClicked {
            logger.debug("Clicked on $color $pieceShape")
            controller.selectColor(color)
            controller.selectPieceShape(pieceShape)

            if (it.button == MouseButton.MIDDLE) {
                logger.debug("Flipped the current piece")
                controller.selectFlip(!controller.currentFlipProperty().get())
            }
            it.consume()
        }

        setOnDragDetected {
            logger.debug("Drag detected of $color, $pieceShape")
            controller.selectColor(color)
            controller.selectPieceShape(pieceShape)

            startFullDrag()
            it.consume()
        }


        val colorName = when (color) {
            Color.RED -> "red"
            Color.GREEN -> "green"
            Color.YELLOW -> "yellow"
            Color.BLUE -> "blue"
        }
        val filename = "${pieceShape.name.toLowerCase()}.png"
        imageview("file:resources/graphics/blokus/$colorName/$filename") {
            isSmooth = false
        }

        tooltip(pieceShape.name)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}