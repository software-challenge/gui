package sc.gui.view

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.input.TransferMode
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import sc.gui.AppStyle
import sc.gui.controller.BoardController
import sc.gui.model.BoardModel
import tornadofx.*
import java.awt.Color

class BoardView: View() {
    val controller: BoardController by inject()
    val model: BoardModel by inject()
    override val root = gridpane {
        isGridLinesVisible = true

        addClass(AppStyle.area)
        prefHeightProperty().bind(widthProperty())


        (0..19).forEach{ y ->
            row {
                (0..19).forEach { x ->
                    pane {
                        setOnDragEntered {
                            addClass(AppStyle.dragTarget)
                            println("Dragging entered!")
                            it.consume()
                        }
                        setOnDragExited {
                            removeClass(AppStyle.dragTarget)
                            println("Dragging exited!")
                            it.consume()
                        }
                        setOnDragOver {
                            it.acceptTransferModes(TransferMode.MOVE)
                            it.consume()
                        }
                        setOnDragDropped {
                            it.isDropCompleted = true
                            it.consume()
                        }
                        label("$x,$y")
                    }
                }
                constraintsForRow(y).percentHeight = 5.0
                hgrow = Priority.ALWAYS
            }
            constraintsForColumn(y).percentWidth = 5.0
            constraintsForColumn(y).hgrow = Priority.ALWAYS
        }
    }
}

