package sc.gui.view

import javafx.collections.ListChangeListener
import javafx.scene.canvas.Canvas
import javafx.scene.image.ImageView
import javafx.scene.input.TransferMode
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.BoardController
import sc.gui.controller.NewGameState
import sc.gui.model.BoardModel
import sc.plugin2021.Field
import sc.plugin2021.FieldContent
import sc.plugin2021.util.Constants
import tornadofx.*

class BoardView : View() {
    val controller: BoardController by inject()
    private val model: BoardModel by inject()
    override val root = GridPane()

    init {

        subscribe<NewGameState> { event ->
            model.updateFields(event.gameState.board)
        }

        root.isGridLinesVisible = true

        root.addClass(AppStyle.area)
        root.prefHeightProperty().bind(root.widthProperty())

        model.fields.forEach { field ->
            root.add(paneFromField(field), field.coordinates.x, field.coordinates.y)
        }
        for (i in 0 until Constants.BOARD_SIZE) {
            root.constraintsForRow(i).percentHeight = 5.0
            root.constraintsForColumn(i).percentWidth = 5.0
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


        //root.center = BoardCanvas()
    }

    fun paneFromField(field: Field): Pane {
        val x = field.coordinates.x
        val y = field.coordinates.y

        val pane = Pane()
        with(pane) {
            label("$x,$y").style {
                fontSize = 13.px
            }
            setOnDragEntered {
                pane.addClass(AppStyle.dragTarget)
                logger.debug("Dragging entered on pane $x,$y")
                it.consume()
            }
            setOnDragExited {
                pane.removeClass(AppStyle.dragTarget)
                logger.debug("Dragging exited on pane $x,$y")
                it.consume()
            }
            setOnDragOver {
                it.acceptTransferModes(TransferMode.MOVE)
                it.consume()
            }
            setOnDragDropped {
                logger.debug("Drag ended on pane $x,$y")
                controller.handleClick(x, y)
                it.isDropCompleted = true
                it.consume()
            }
            setOnMouseClicked {
                println("Click event")
                controller.handleClick(x, y)
                it.consume()
            }

            addClass(when (field.content) {
                FieldContent.EMPTY -> AppStyle.colorGRAY
                FieldContent.RED -> AppStyle.colorRED
                FieldContent.BLUE -> AppStyle.colorBLUE
                FieldContent.GREEN -> AppStyle.colorGREEN
                FieldContent.YELLOW -> AppStyle.colorYELLOW
            })
        }
        return pane
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BoardView::class.java)
    }
}

// TODO: experiment by Leo to have an responsive canvas (currently invisible if drawn? why??)
class BoardCanvas : Canvas() {
    init {
        widthProperty().addListener { _, old, new ->
            println("canvas width changed from $old -> $new")
            draw()
        }
        heightProperty().addListener { _, old, new ->
            println("canvas height changed from $old -> $new")
            draw()
        }

        println("canvas current H: $height W: $width")
        draw()
    }

    fun draw() {
        graphicsContext2D.clearRect(0.0, 0.0, width, height)
        graphicsContext2D.stroke = Color.BLACK
        for (i in 0 until Constants.BOARD_SIZE) {
            graphicsContext2D.strokeLine(0.0, i * (height / Constants.BOARD_SIZE), width, i * (height / Constants.BOARD_SIZE))
            graphicsContext2D.strokeLine(i * (width / Constants.BOARD_SIZE), 0.0, i * (width / Constants.BOARD_SIZE), height)
        }
    }

    override fun isResizable(): Boolean {
        return true
    }

    override fun prefWidth(height: Double): Double {
        return width
    }

    override fun prefHeight(width: Double): Double {
        return height
    }
}