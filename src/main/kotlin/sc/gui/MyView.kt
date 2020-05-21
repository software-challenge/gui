package sc.gui

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Point2D
import javafx.scene.control.Button
import javafx.scene.input.ClipboardContent
import javafx.scene.input.Dragboard
import javafx.scene.input.TransferMode
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import sc.gui.view.BoardView
import tornadofx.*

class MasterView: View() {
    override val root = borderpane {
        top<TopView>()
        bottom<BottomView>()
    }
}

class TopView: View() {
    val controller: MyController by inject()
    val model: TopViewModel by inject()
    val input = SimpleStringProperty()
    private val boardView: BoardView by inject()
    override val root = vbox {
        form {
            label("Top View")
            fieldset {
                field("Input") {
                    textfield(input)
                }

                button("Commit") {
                    action {
                        controller.writeToDb(input.value)
                        input.value = ""
                    }
                }
            }
        }
        label("My items")
        listview(controller.values)
        button("Press me") {
            addClass(AppStyle.tackyButton)
            action {
                find<MyFragment>().openModal(stageStyle = StageStyle.UTILITY)
            }
        }
        button("Other view") {
            action {
                replaceWith(MyView::class, ViewTransition.Slide(0.3.seconds, ViewTransition.Direction.RIGHT))
            }
        }
        rectangle {
            width = 100.0
            height = 100.0
            fill = Color.BLUE
            setOnDragDetected {
                val db = this.startDragAndDrop(TransferMode.MOVE)
                val content = ClipboardContent()
                content.putString("content")
                db.setContent(content)
                println("Dragging started!")
                it.consume()
            }
            setOnDragDone {
                println("Dragging ended!")
                it.consume()
            }
        }
        children.filterIsInstance<Button>().addClass(AppStyle.tackyButton)
        vbox {
            addClass(AppStyle.area2)
            add(boardView)
            textfield(input)
        }
    }
}

class TopViewModel : ItemViewModel<TopView>() {
    val input = bind(TopView::input)
    val root = bind(TopView::root)
}



class BottomView : View() {
    override val root = label("Bottom View")
}

class MyView : View("My View") {
    override val root = vbox {
        button("Press me") {
            action {
                replaceWith(TopView::class, ViewTransition.Slide(0.3.seconds, ViewTransition.Direction.LEFT))
            }
        }
        label("Waiting")
    }
}

class MyFragment: Fragment() {
    override val root = label("This is a popup")
}
