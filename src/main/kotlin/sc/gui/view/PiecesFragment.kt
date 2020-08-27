package sc.gui.view

import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.Dragboard
import javafx.scene.input.MouseButton
import javafx.scene.input.TransferMode
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.GameController
import sc.plugin2021.Color
import sc.plugin2021.PieceShape
import sc.plugin2021.Rotation
import tornadofx.*

class PiecesFragment(color: Color, pieceShape: PieceShape) : Fragment() {
    val controller: GameController by inject()
    val colorName = when (color) {
        Color.RED -> "red"
        Color.GREEN -> "green"
        Color.YELLOW -> "yellow"
        Color.BLUE -> "blue"
    }
    private val imagePath: String = "file:resources/graphics/blokus/$colorName/${pieceShape.name.toLowerCase()}.png"

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

            val db: Dragboard = startDragAndDrop(TransferMode.MOVE)
            val content: ClipboardContent = ClipboardContent()

            // Rotating Image
            val image: ImageView = ImageView(imagePath)
            image.rotate = when(controller.currentRotationProperty().get()) {
                Rotation.LEFT -> -90.0
                Rotation.MIRROR -> 180.0
                Rotation.NONE -> 0.0
                Rotation.RIGHT -> 90.0
                else -> throw Exception("Impossible Rotation...")
            }
            val rotated = image.snapshot(SnapshotParameters(), null)

            content.putImage(rotated)
            db.setContent(content)

            it.consume()
        }

        imageview(imagePath) {
            isSmooth = false
        }

        tooltip(pieceShape.name)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}