package sc.gui.view

import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import sc.gui.GuiApp
import sc.gui.controller.*
import sc.gui.model.PiecesModel
import sc.plugin2021.Color
import sc.plugin2021.PieceShape
import sc.plugin2021.Rotation
import tornadofx.*
import java.io.File

class PiecesFragment(color: Color, shape: PieceShape) : Fragment() {
    private val boardController: BoardController by inject()
    val model: PiecesModel = PiecesModel(color, shape)
    private val path: String = "/graphics/blokus/${model.colorProperty().get().name.toLowerCase()}/${model.shapeProperty().get().name.toLowerCase()}.png"
    private val imageUrl: String = PiecesFragment::class.java.getResource(path).toExternalForm()
    private val image: ImageView = ImageView(imageUrl)

    constructor(selectedColor: ColorBinding, selectedShape: ShapeBinding, selectedRotation: RotationBinding, selectedFlip: FlipBinding) : this(selectedColor.value, selectedShape.value) {
        selectedColor.addListener { _, _, new -> model.colorProperty().set(new) }
        selectedShape.addListener { _, _, new -> model.shapeProperty().set(new) }
        selectedRotation.addListener { _, _, new -> model.rotationProperty().set(new) }
        selectedFlip.addListener { _, _, new -> model.flipProperty().set(new) }
    }

    override val root = hbox {
        this += image
        tooltip(model.shapeProperty().get().name)
    }

    init {
        model.colorProperty().addListener { _, _, _ -> updateImage() }
        model.shapeProperty().addListener { _, _, _ ->
            updateImage()
            root.tooltip(model.shapeProperty().get().name)
        }
        model.rotationProperty().addListener { _, _, _ -> updateImage() }
        model.flipProperty().addListener { _, _, _ -> updateImage() }
        updateImage()
    }

    fun updateImage() {
        val imagePath = "/graphics/blokus/${model.colorProperty().get().name.toLowerCase()}/${model.shapeProperty().get().name.toLowerCase()}.png"
        val size = boardController.board.calculatedBlockSizeProperty().get() * 2
        image.image = Image(PiecesFragment::class.java.getResource(imagePath).toExternalForm(), size, size, true, false)

        if (model.flipProperty().get()) {
            val canvas = Canvas(image.image.width, image.image.height)
            val gc = canvas.graphicsContext2D
            gc.save()
            // TODO: set transparent background
            gc.fill = javafx.scene.paint.Color.TRANSPARENT
            gc.rect(0.0, 0.0, image.image.width, image.image.height)

            // due to rotation we have to be careful how to flip the image
            val x: Double = when (model.rotationProperty().get()) {
                Rotation.RIGHT -> 0.0
                Rotation.LEFT -> 0.0
                else -> image.image.width
            }
            val y: Double = when (model.rotationProperty().get()) {
                Rotation.RIGHT -> image.image.height
                Rotation.LEFT -> image.image.height
                else -> 0.0
            }

            // here the image is actually being flipped
            gc.translate(x * 2, y * 2)
            when (model.rotationProperty().get()) {
                Rotation.RIGHT -> gc.scale(1.0, -1.0)
                Rotation.LEFT -> gc.scale(1.0, -1.0)
                Rotation.MIRROR -> gc.scale(-1.0, 1.0)
                else -> gc.scale(-1.0, 1.0)
            }
            gc.drawImage(image.image, x, y)
            image.image = canvas.snapshot(SnapshotParameters(), null)
        }

        // apply rotation to imageview
        image.rotate = when (model.rotationProperty().get()) {
            Rotation.RIGHT -> 90.0
            Rotation.LEFT -> -90.0
            Rotation.MIRROR -> 180.0
            else -> 0.0
        }
    }
}