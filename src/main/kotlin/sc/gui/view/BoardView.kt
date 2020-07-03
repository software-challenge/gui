package sc.gui.view

import javafx.collections.ListChangeListener
import javafx.scene.input.TransferMode
import javafx.scene.layout.*
import sc.gui.AppStyle
import sc.gui.controller.BoardController
import sc.gui.model.BoardModel
import sc.plugin2021.Field
import sc.plugin2021.FieldContent
import tornadofx.*

class BoardView: View() {
    val controller: BoardController by inject()
    private val model: BoardModel by inject()
    override val root = GridPane()

    init {
        root.isGridLinesVisible = true

        //addClass(AppStyle.area)
        root.prefHeightProperty().bind(root.widthProperty())

        model.fields.forEach { field ->
            root.add(paneFromField(field), field.coordinates.x.toInt(), field.coordinates.y.toInt())
        }
        for (i in 0 until sc.gui.model.boardSize) {
            root.constraintsForRow(i).percentHeight = 5.0
            root.constraintsForColumn(i).percentWidth = 5.0
            root.constraintsForColumn(i).hgrow = Priority.ALWAYS
        }
        model.fields.addListener(ListChangeListener { change ->
            while (change.next()) {
                println("change detected " + change.toString())
                if (change.wasAdded()) {
                    change.addedSubList.forEach { addedField ->
                        val x = addedField.coordinates.x.toInt()
                        val y = addedField.coordinates.y.toInt()
                        root.add(paneFromField(addedField), x, y)
                    }
                }
            }
        })
    }

    fun paneFromField(field: Field): Pane {
        val x = field.coordinates.x.toInt()
        val y = field.coordinates.y.toInt()
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
        pane.label("$x,$y")
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

