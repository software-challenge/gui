package sc.gui.view

import javafx.scene.input.MouseButton
import org.slf4j.LoggerFactory
import sc.gui.controller.GameController
import sc.plugin2021.Color
import sc.plugin2021.Piece
import sc.plugin2021.Rotation
import tornadofx.*

class PiecesFragment(piece: Piece) : Fragment() {
    val controller: GameController by inject()


    override val root = borderpane {
        style {
            backgroundColor += c("#cecece")
        }

        setOnMouseEntered {
            style {
                backgroundColor += c("#444444")
            }

        }

        setOnMouseExited {
            style {
                backgroundColor += c("#cecece")
            }
        }

        setOnMouseClicked {
            if (it.button == MouseButton.MIDDLE) {
                logger.debug("Flipped the current piece")
                controller.selectFlip(!controller.currentFlipProperty().get())
            } else {
                logger.debug("Clicked on ${piece.color} ${piece.kind.name}")
                controller.selectColor(piece.color)
                controller.selectPieceShape(piece.kind)
            }
        }

        setOnScroll {
            val rotation = controller.currentRotationProperty().get().rotate(
                    when {
                        it.deltaY >  0.0 -> Rotation.LEFT
                        it.deltaY == 0.0 -> Rotation.NONE
                        it.deltaY <  0.0 -> Rotation.RIGHT
                        else             -> Rotation.MIRROR
                    }
            )
            logger.debug("Set rotation to $rotation")
            controller.selectRotation(rotation)
        }

        val color = when (piece.color) {
            Color.RED -> "red"
            Color.GREEN -> "green"
            Color.YELLOW -> "yellow"
            Color.BLUE -> "blue"
        }
        val filename = "${piece.kind.name.toLowerCase()}.png"
        center = imageview("file:resources/graphics/blokus/$color/$filename") {
            style {
                backgroundColor += c("#000000")
            }
            isSmooth = false
        }

        tooltip(piece.kind.name)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}