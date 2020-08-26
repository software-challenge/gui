package sc.gui.view

import javafx.collections.ListChangeListener
import javafx.scene.canvas.Canvas
import javafx.scene.input.TransferMode
import javafx.scene.layout.*
import javafx.scene.paint.Color
import sc.gui.AppStyle
import sc.gui.controller.BoardController
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
        model.fields.addListener(ListChangeListener { change ->
            while (change.next()) {
                println("change detected $change")
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
        pane.setOnDragEntered {
            pane.addClass(AppStyle.dragTarget)
            println("Dragging entered!")
            it.consume()
        }
        pane.setOnDragExited {
            pane.removeClass(AppStyle.dragTarget)
            println("Dragging exited!")
            it.consume()
        }
        pane.setOnDragOver {
            it.acceptTransferModes(TransferMode.MOVE)
            it.consume()
        }
        pane.setOnDragDropped {
            it.isDropCompleted = true
            it.consume()
        }
        pane.setOnMouseClicked {
            println("Click event")
            controller.handleClick(x, y)
            it.consume()
        }
        val content = field.content
        pane.label("$x,$y").style {
            fontSize = 13.px
        }
        var cssClass: CssRule
        when (content) {
            FieldContent.EMPTY -> cssClass = AppStyle.colorGRAY
            FieldContent.RED -> cssClass = AppStyle.colorRED
            FieldContent.BLUE -> cssClass = AppStyle.colorBLUE
            FieldContent.GREEN -> cssClass = AppStyle.colorGREEN
            FieldContent.YELLOW -> cssClass = AppStyle.colorYELLOW
        }
        pane.addClass(cssClass)
        return pane
    }
}

// TODO: experiment by Leo to have an responsive canvas (currently invisible if drawn? why??)
class BoardCanvas : Canvas() {
    init {
        widthProperty().addListener { _, old, new ->
            println("Width changed from $old -> $new")
            draw()
        }
        heightProperty().addListener { _, old, new ->
            println("Height changed from $old -> $new")
            draw()
        }
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