package sc.gui.view

import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.*
import sc.gui.model.PiecesModel
import sc.plugin2021.Color
import sc.plugin2021.PieceShape
import sc.plugin2021.Rotation
import tornadofx.*

class PiecesFragment(color: Color, shape: PieceShape) : Fragment() {
    private var isBound: Boolean = false;
    private val gameController: GameController by inject()
    private val board: BoardView by inject()
    private var dragging: Boolean = false
    private var dragStartX: Double = 0.0
    private var dragStartY: Double = 0.0
    val model: PiecesModel = PiecesModel(color, shape)
    private val image: ImageView = ImageView("file:resources/graphics/blokus/${model.colorProperty().get()}/${model.shapeProperty().get().name.toLowerCase()}.png")


    constructor(color: ColorBinding, shape: ShapeBinding, rotation: RotationBinding, flip: FlipBinding) : this(color.get(), shape.get()) {
        model.colorProperty().bind(color)
        model.shapeProperty().bind(shape)
        model.rotationProperty().bind(rotation)
        model.flipProperty().bind(flip)
        isBound = true

        color.addListener { _, _, newValue ->
            with(root) {
                if (hasClass(AppStyle.borderRED)) {
                    removeClass(AppStyle.borderRED)
                }
                if (hasClass(AppStyle.borderBLUE)) {
                    removeClass(AppStyle.borderBLUE)
                }
                if (hasClass(AppStyle.borderGREEN)) {
                    removeClass(AppStyle.borderGREEN)
                }
                if (hasClass(AppStyle.borderYELLOW)) {
                    removeClass(AppStyle.borderYELLOW)
                }
                if (newValue != null) {
                    addClass(when (newValue) {
                        Color.RED -> AppStyle.borderRED
                        Color.BLUE -> AppStyle.borderBLUE
                        Color.GREEN -> AppStyle.borderGREEN
                        Color.YELLOW -> AppStyle.borderYELLOW
                    })
                }
            }
        }
    }

    override val root = hbox {
        addClass(AppStyle.undeployedPiece, when (color) {
            Color.RED -> AppStyle.borderRED
            Color.BLUE -> AppStyle.borderBLUE
            Color.GREEN -> AppStyle.borderGREEN
            Color.YELLOW -> AppStyle.borderYELLOW
        })

        if (!isBound) {
            setOnScroll {
                logger.debug("Scrolling detected: rotating selected piece, bound: $isBound")
                model.scroll(it)
                it.consume()
            }

            setOnMouseClicked {
                if (it.button == MouseButton.SECONDARY) {
                    logger.debug("Right-click, flipping piece")
                    model.flipPiece()
                }
                it.consume()
            }
            setOnMousePressed {
                if (it.button == MouseButton.PRIMARY) {
                    if (dragging) {
                        logger.debug("Dragging to ${it.sceneX}, ${it.sceneY} from initial $dragStartX, $dragStartY")
                        translateXProperty().set(it.sceneX - dragStartX)
                        translateYProperty().set(it.sceneY - dragStartY)
                    } else {
                        logger.debug("Clicked on $color $shape")
                        gameController.selectPiece(model)
                        mouseTransparentProperty().set(true)
                        dragging = true
                        dragStartX = it.sceneX
                        dragStartY = it.sceneY
                    }
                }
                it.consume()
            }
            setOnMouseMoved {
                if (it.button == MouseButton.PRIMARY) {
                    if (dragging) {
                        logger.debug("Dragging to ${it.sceneX}, ${it.sceneY} from initial $dragStartX, $dragStartY")
                        translateXProperty().set(it.sceneX - dragStartX)
                        translateYProperty().set(it.sceneY - dragStartY)
                    }
                }
                it.consume()
            }
            setOnMouseReleased {
                if (it.button == MouseButton.PRIMARY) {
                    dragging = false
                    mouseTransparentProperty().set(false)
                }
                it.consume()
            }

            setOnDragDetected {
                logger.debug("Drag detected of $color, $shape")
                gameController.selectPiece(model)
                startFullDrag()

                it.consume()
            }
        }

        setOnMouseEntered {
            addClass(AppStyle.hoverColor)
            it.consume()
        }

        setOnMouseExited {
            removeClass(AppStyle.hoverColor)
            it.consume()
        }

        this += image

        tooltip(model.shapeProperty().get().name)
    }

    init {
        model.colorProperty().addListener { _, _, _ -> updateImage() }
        model.shapeProperty().addListener { _, _, _ -> updateImage() }
        model.rotationProperty().addListener { _, _, _ -> updateImage() }
        model.flipProperty().addListener { _, _, _ -> updateImage() }
        updateImage()
    }

    private fun updateImage() {
        val imagePath = "file:resources/graphics/blokus/${model.colorProperty().get()}/${model.shapeProperty().get().name.toLowerCase()}.png"
        image.image = Image(imagePath)

        if (model.flipProperty().get()) {
            val canvas = Canvas(image.image.width, image.image.height)
            val gc = canvas.graphicsContext2D
            gc.save()
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

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesFragment::class.java)
    }
}