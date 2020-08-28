package sc.gui.view

import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.input.TransferMode
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.BoardController
import sc.gui.controller.NewGameState
import sc.gui.model.BoardModel
import sc.plugin2021.Color
import sc.plugin2021.Coordinates
import sc.plugin2021.Field
import sc.plugin2021.FieldContent
import sc.plugin2021.util.Constants
import tornadofx.*


class BlockImage(url: String) : ImageView(url) {
    init {
        preserveRatioProperty().set(false)
        smoothProperty().set(false)
    }

    override fun minHeight(width: Double): Double {
        return 16.0
    }

    override fun prefHeight(width: Double): Double {
        image ?: return minHeight(width)
        return image.height
    }

    override fun minWidth(height: Double): Double {
        return 16.0
    }

    override fun prefWidth(height: Double): Double {
        image ?: return minWidth(height)
        return image.width
    }

    override fun isResizable(): Boolean {
        return true
    }
}

class BoardView : View() {
    val controller: BoardController by inject()
    private val model: BoardModel by inject()
    private val grid: GridPane = GridPane()
    override val root = StackPane()

    init {
        subscribe<NewGameState> { event ->
            model.updateFields(event.gameState.board)
        }


        controller.game.selectedShapeProperty().addListener { _, _, _ ->
            cleanupHover()
            if (controller.currentHover != null) {
                paneHoverEnter(controller.currentHover!!.x, controller.currentHover!!.y)
            }
        }

        grid.isGridLinesVisible = true


        model.fields.forEach { field ->
            grid.add(paneFromField(field), field.coordinates.x, field.coordinates.y)
        }

        grid.alignment = Pos.CENTER
        grid.hgrow = Priority.ALWAYS
        grid.vgrow = Priority.ALWAYS
        grid.minHeight = 16.0
        grid.minWidth = 16.0
        for (i in 0 until Constants.BOARD_SIZE) {
            grid.constraintsForRow(i).percentHeight = 100.0 / Constants.BOARD_SIZE
            grid.constraintsForColumn(i).percentWidth = 100.0 / Constants.BOARD_SIZE
        }

        // TODO seems that the listener binding is quite slow, update here directly
        // is that the right way to do that?
        model.fields.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasAdded()) {
                    change.addedSubList.forEach { addedField ->
                        val x = addedField.coordinates.x
                        val y = addedField.coordinates.y
                        grid.add(paneFromField(addedField), x, y)
                    }
                }
            }
        })

        root.widthProperty().addListener { _, old, new ->
            logger.debug("Width changed from $old -> $new")
            resize()
        }
        root.heightProperty().addListener { _, old, new ->
            logger.debug("Height changed from $old -> $new")
            resize()
        }
        grid.style {
            backgroundColor += javafx.scene.paint.Color.DARKGRAY
        }
        root += grid
        root.style {
            backgroundColor += javafx.scene.paint.Color.LIGHTGRAY
        }
    }

    private fun resize() {

        val bounds = grid.layoutBoundsProperty().get()
        val size = minOf(root.widthProperty().get(), root.heightProperty().get())
        logger.debug("Root width: ${root.widthProperty().get()}, height: ${root.heightProperty().get()} and board bounds width: ${bounds.width}, height: ${bounds.height} -> size: $size (${size / bounds.width}, ${size / bounds.height})")

        grid.scaleXProperty().set(size / bounds.width)
        grid.scaleYProperty().set(size / bounds.height)
    }


    private fun getPane(x: Int, y: Int): Node {
        for (node in grid.children) {
            if (GridPane.getColumnIndex(node) == x && GridPane.getRowIndex(node) == y) {
                return node
            }
        }
        throw Exception("Pane of ($x, $y) is not part of the BoardView")
    }

    private fun cleanupHover() {
        for (place in grid.children) {
            if (place.hasClass(AppStyle.colorRED)) {
                place.removeClass(AppStyle.colorRED)
            }
            if (place.hasClass(AppStyle.colorBLUE)) {
                place.removeClass(AppStyle.colorBLUE)
            }
            if (place.hasClass(AppStyle.colorGREEN)) {
                place.removeClass(AppStyle.colorGREEN)
            }
            if (place.hasClass(AppStyle.colorYELLOW)) {
                place.removeClass(AppStyle.colorYELLOW)
            }
            if (place.hasClass(AppStyle.fieldUnplaceable)) {
                place.removeClass(AppStyle.fieldUnplaceable)
            }
        }
    }

    private fun paneHoverEnter(x: Int, y: Int) {
        controller.currentHover = Coordinates(x, y)
        val placeable: Boolean = isPlaceable(x, y, controller.game.selectedShapeProperty().get())
        for (place in controller.game.selectedShapeProperty().get()) {
            if (hoverInBound(x + place.x, y + place.y)) {
                if (placeable) {
                    getPane(x + place.x, y + place.y).addClass(when (controller.game.currentColorProperty().get()) {
                        Color.RED -> AppStyle.colorRED
                        Color.BLUE -> AppStyle.colorBLUE
                        Color.GREEN -> AppStyle.colorGREEN
                        Color.YELLOW -> AppStyle.colorYELLOW
                        else -> throw Exception("Unknown player color for hover effect")
                    })
                }
                getPane(x + place.x, y + place.y).addClass(AppStyle.fieldUnplaceable)
            }
        }
    }

    private fun paneHoverExit() {
        controller.currentHover = null
        cleanupHover()
    }

    private fun hoverInBound(x: Int, y: Int): Boolean {
        return x >= 0 && y >= 0 && x < Constants.BOARD_SIZE && y < Constants.BOARD_SIZE
    }

    private fun isPlaceable(x: Int, y: Int, shape: Set<Coordinates>): Boolean {
        val field: FieldContent = when (controller.game.currentColorProperty().get()) {
            Color.RED -> FieldContent.RED
            Color.YELLOW -> FieldContent.YELLOW
            Color.GREEN -> FieldContent.GREEN
            Color.BLUE -> FieldContent.BLUE
            else -> FieldContent.EMPTY
        }

        for (place in shape) {
            if (!hoverInBound(x + place.x, y + place.y) || model.getField(x + place.x, y + place.y).content != FieldContent.EMPTY ||
                    hoverInBound(x + place.x + 1, y + place.y) && model.getField(x + place.x + 1, y + place.y).content == field ||
                    hoverInBound(x + place.x - 1, y + place.y) && model.getField(x + place.x - 1, y + place.y).content == field ||
                    hoverInBound(x + place.x, y + place.y + 1) && model.getField(x + place.x, y + place.y + 1).content == field ||
                    hoverInBound(x + place.x, y + place.y - 1) && model.getField(x + place.x, y + place.y - 1).content == field
            ) {
                return false
            }
        }

        return true
    }

    fun paneFromField(field: Field): HBox {
        val x = field.coordinates.x
        val y = field.coordinates.y

        val pane = HBox()
        with(pane) {

            setOnDragEntered {
                logger.debug("Dragging entered on pane $x,$y")
                paneHoverEnter(x, y)
                it.consume()
            }
            setOnDragExited {
                logger.debug("Dragging exited on pane $x,$y")
                paneHoverExit()
                it.consume()
            }
            setOnDragOver {
                it.acceptTransferModes(TransferMode.MOVE)
                it.consume()
            }
            setOnDragDropped {
                logger.debug("Drag ended on pane $x,$y")
                cleanupHover()
                controller.handleClick(x, y)
                it.isDropCompleted = true
                it.consume()
            }
            setOnMouseEntered {
                paneHoverEnter(x, y)
                it.consume()
            }
            setOnMouseExited {
                paneHoverExit()
                it.consume()
            }
            setOnMouseClicked {
                if (it.button == MouseButton.PRIMARY) {
                    println("Clicked on pane $x, $y")
                    cleanupHover()
                    controller.handleClick(x, y)
                } else if (it.button == MouseButton.SECONDARY) {
                    logger.debug("Right-click, flipping piece")
                    controller.game.selectFlip(!controller.game.currentFlipProperty().get())
                }
                it.consume()
            }

            val image: BlockImage? = when (field.content) {
                FieldContent.RED -> BlockImage("file:resources/graphics/blokus/single/red.png")
                FieldContent.BLUE -> BlockImage("file:resources/graphics/blokus/single/blue.png")
                FieldContent.GREEN -> BlockImage("file:resources/graphics/blokus/single/green.png")
                FieldContent.YELLOW -> BlockImage("file:resources/graphics/blokus/single/yellow.png")
                FieldContent.EMPTY -> null
                else -> throw Exception("Unknown Color-value for placed piece")
            }

            if (image != null) {
                image.fitWidthProperty().bind(root.widthProperty() / Constants.BOARD_SIZE)
                image.fitHeightProperty().bind(root.heightProperty() / Constants.BOARD_SIZE)
                this += image
            }
        }
        return pane
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BoardView::class.java)
    }
}