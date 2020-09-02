package sc.gui.view

import javafx.beans.property.Property
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.AppController
import sc.gui.controller.BoardController
import sc.gui.controller.NewGameState
import sc.gui.model.BoardModel
import sc.plugin2021.*
import sc.plugin2021.Field
import sc.plugin2021.util.Constants
import tornadofx.*

// this custom class is requiredto be able to shrink upsized images back to smaller sizes
// see: https://stackoverflow.com/a/35202191/9127322
class BlockImage(private val size: Property<Double>) : ImageView(Image("file:resources/graphics/blokus/single/empty.png", size.value, size.value, true, false)) {
    private var content = FieldContent.EMPTY

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

    private fun contentToString(content: FieldContent): String {
        return when (content) {
            FieldContent.EMPTY -> "empty"
            FieldContent.RED -> "red"
            FieldContent.BLUE -> "blue"
            FieldContent.GREEN -> "green"
            FieldContent.YELLOW -> "yellow"
            else -> throw Exception("Impossible field state: $content")
        }
    }

    fun updateImage(content: FieldContent) {
        this.content = content
        this.image = Image("file:resources/graphics/blokus/single/${contentToString(content)}.png", size.value, size.value, true, false)
    }

    init {
        size.addListener { _, _, _ ->
            updateImage(content)
        }
    }
}

class BoardView : View() {
    val controller: BoardController by inject()
    val model: BoardModel by inject()
    private val appController: AppController by inject()
    val grid = gridpane {
        isGridLinesVisible = true
        for (x in 0 until Constants.BOARD_SIZE) {
            for (y in 0 until Constants.BOARD_SIZE) {
                add(paneFromField(Field(Coordinates(x, y), FieldContent.EMPTY)), x, y)
            }
            constraintsForRow(x).percentHeight = 100.0 / Constants.BOARD_SIZE
            constraintsForColumn(x).percentWidth = 100.0 / Constants.BOARD_SIZE
        }
    }
    override val root = hbox {
        alignment = Pos.CENTER
        this += grid
    }

    init {
        subscribe<NewGameState> { event ->
            model.boardProperty().set(event.gameState.board)
        }


        controller.game.selectedCalculatedShape.addListener { _, _, _ ->
            cleanupHover()
            if (controller.currentHover != null) {
                paneHoverEnter(controller.currentHover!!.x, controller.currentHover!!.y)
            }
        }

        if (appController.model.isDarkModeProperty().get()) {
            grid.addClass(AppStyle.darkBoard)
        } else {
            grid.addClass(AppStyle.lightBoard)
        }
        appController.model.isDarkModeProperty().addListener { _, _, _ ->
            if (appController.model.isDarkModeProperty().get()) {
                if (grid.hasClass(AppStyle.lightBoard)) {
                    grid.removeClass(AppStyle.lightBoard)
                }
                if (!grid.hasClass(AppStyle.darkBoard)) {
                    grid.addClass(AppStyle.darkBoard)
                }
            } else {
                if (grid.hasClass(AppStyle.darkBoard)) {
                    grid.removeClass(AppStyle.darkBoard)
                }
                if (!grid.hasClass(AppStyle.lightBoard)) {
                    grid.addClass(AppStyle.lightBoard)
                }
            }
        }
    }


    private fun getPane(x: Int, y: Int): Node {
        for (node in grid.children) {
            if (GridPane.getColumnIndex(node) == x && GridPane.getRowIndex(node) == y) {
                return node
            }
        }
        throw Exception("Pane of ($x, $y) is not part of the BoardView")
    }

    // remove all applied Stylesheets during the hover-effect
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
        controller.currentPlaceable = controller.isPlaceable(x, y, controller.game.selectedCalculatedShape.get())
        for (place in controller.game.selectedCalculatedShape.get()) {
            if (controller.hoverInBound(x + place.x, y + place.y)) {
                if (!controller.currentPlaceable) {
                    getPane(x + place.x, y + place.y).addClass(AppStyle.fieldUnplaceable)
                } else {
                    getPane(x + place.x, y + place.y).addClass(when (controller.game.selectedColor.get()) {
                        Color.RED -> AppStyle.colorRED
                        Color.BLUE -> AppStyle.colorBLUE
                        Color.GREEN -> AppStyle.colorGREEN
                        Color.YELLOW -> AppStyle.colorYELLOW
                        else -> throw Exception("Unknown player color for hover effect")
                    })
                }
            }
        }
    }

    private fun paneHoverExit() {
        if (controller.currentHover != null) {
            val x = controller.currentHover!!.x
            val y = controller.currentHover!!.y
            cleanupHover()
        }
        controller.currentHover = null
    }

    private fun paneFromField(field: Field): HBox {
        val x = field.coordinates.x
        val y = field.coordinates.y
        val image = BlockImage(controller.board.calculatedBlockSizeProperty())
        image.fitWidthProperty().bind(controller.board.calculatedBlockSizeProperty())
        image.fitHeightProperty().bind(controller.board.calculatedBlockSizeProperty())
        model.boardProperty().addListener { _, oldBoard, newBoard ->
            if (oldBoard == null || oldBoard[x, y].content != newBoard[x, y].content) {
                image.updateImage(newBoard[x, y].content)
            }
        }

        return hbox {
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
                    controller.game.flipPiece()
                }
                it.consume()
            }

            this += image
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BoardView::class.java)
    }
}