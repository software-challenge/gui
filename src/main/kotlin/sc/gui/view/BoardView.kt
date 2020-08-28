package sc.gui.view

import javafx.collections.ListChangeListener
import javafx.scene.Node
import javafx.scene.image.ImageView
import javafx.scene.input.TransferMode
import javafx.scene.layout.*
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
import java.lang.Exception

class BoardView : View() {
    val controller: BoardController by inject()
    private val model: BoardModel by inject()
    override val root = GridPane()

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

        root.isGridLinesVisible = true

        root.addClass(AppStyle.area)
        root.prefHeightProperty().bind(root.widthProperty())

        model.fields.forEach { field ->
            root.add(paneFromField(field), field.coordinates.x, field.coordinates.y)
        }
        for (i in 0 until Constants.BOARD_SIZE) {
            root.constraintsForRow(i).percentHeight = 100.0 / Constants.BOARD_SIZE
            root.constraintsForColumn(i).percentWidth = 100.0 / Constants.BOARD_SIZE
            root.constraintsForColumn(i).hgrow = Priority.ALWAYS
        }

        // TODO seems that the listener binding is quite slow, update here directly
        // is that the right way to do that?
        model.fields.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasAdded()) {
                    change.addedSubList.forEach { addedField ->
                        val x = addedField.coordinates.x
                        val y = addedField.coordinates.y
                        root.add(paneFromField(addedField), x, y)
                    }
                }
            }
        })
    }

    private fun getPane(x: Int, y: Int): Node {
        for (node in root.children) {
            if (GridPane.getColumnIndex(node) == x && GridPane.getRowIndex(node) == y) {
                return node
            }
        }
        throw Exception("Pane of ($x, $y) is not part of the BoardView")
    }

    private fun cleanupHover() {
        for (place in root.children) {
            if (place.hasClass(AppStyle.colorRED)) {
                place.removeClass(AppStyle.colorRED).addClass(AppStyle.colorGRAY)
            }
            if (place.hasClass(AppStyle.colorBLUE)) {
                place.removeClass(AppStyle.colorBLUE).addClass(AppStyle.colorGRAY)
            }
            if (place.hasClass(AppStyle.colorGREEN)) {
                place.removeClass(AppStyle.colorGREEN).addClass(AppStyle.colorGRAY)
            }
            if (place.hasClass(AppStyle.colorYELLOW)) {
                place.removeClass(AppStyle.colorYELLOW).addClass(AppStyle.colorGRAY)
            }
        }
    }

    private fun paneHoverEnter(x: Int, y: Int) {
        controller.currentHover = Coordinates(x, y)
        for (place in controller.game.selectedShapeProperty().get()) {
            if (hoverInBound(x + place.x, y + place.y)) {
                getPane(x + place.x, y + place.y).removeClass(AppStyle.colorGRAY).addClass(when(controller.game.currentColorProperty().get()) {
                    Color.RED -> AppStyle.colorRED
                    Color.BLUE -> AppStyle.colorBLUE
                    Color.GREEN -> AppStyle.colorGREEN
                    Color.YELLOW -> AppStyle.colorYELLOW
                    else -> throw Exception("Unknown player color for hover effect")
                })
            }
        }
    }

    private fun paneHoverExit() {
        controller.currentHover = null
        cleanupHover()
    }

    private fun hoverInBound(x: Int, y: Int): Boolean {
        return x >= 0 && y >= 0 && x < Constants.BOARD_SIZE && y < Constants.BOARD_SIZE && model.getField(x, y).content == FieldContent.EMPTY
    }

    fun paneFromField(field: Field): HBox {
        val x = field.coordinates.x
        val y = field.coordinates.y

        val pane = HBox()
        with(pane) {
            label("$x,$y").style {
                fontSize = 13.px
            }
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
                logger.debug("Hover entered on pane $x,$y")
                paneHoverEnter(x, y)
                it.consume()
            }
            setOnMouseExited {
                logger.debug("Hover exited on pane $x,$y")
                paneHoverExit()
                it.consume()
            }
            setOnMouseClicked {
                println("Click event")
                cleanupHover()
                controller.handleClick(x, y)
                it.consume()
            }

            val image: ImageView? = when (field.content) {
                FieldContent.RED -> ImageView("file:resources/graphics/blokus/single/red.png")
                FieldContent.BLUE -> ImageView("file:resources/graphics/blokus/single/blue.png")
                FieldContent.GREEN -> ImageView("file:resources/graphics/blokus/single/green.png")
                FieldContent.YELLOW -> ImageView("file:resources/graphics/blokus/single/yellow.png")
                else -> null
            }

            if (image != null) {
                image.scaleX = root.width / (16.0 * 20)
                image.scaleY = root.height / (16.0 * 20)
                add(image)
            } else {
                addClass(AppStyle.colorGRAY)
            }
        }
        return pane
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BoardView::class.java)
    }
}