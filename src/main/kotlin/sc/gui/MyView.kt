package sc.gui

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Point2D
import javafx.scene.control.Button
import javafx.stage.StageStyle
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
        children.filter { it is Button }.addClass(AppStyle.tackyButton)
        stackpane {
            group {
                bindChildren(model.segments) { segment ->
                    line(segment.first.x, segment.first.y, segment.second.x, segment.second.y)
                }
            }
        }
    }
}

class TopViewModel : ItemViewModel<TopView>() {
    val input = bind(TopView::input)
    val root = bind(TopView::root)
    val segments: ObservableList<Segment> = FXCollections.observableArrayList<Segment>()

    val fieldSize: Double = 20.0
    val boardSize: Int = 20

    init {
        for (x in 0..boardSize) {
            for (y in 0..boardSize) {
                // vertical
                segments.add(Segment(Point2D(x * fieldSize, 0.0), Point2D(x * fieldSize, fieldSize * boardSize)))
                // horizontal
                segments.add(Segment(Point2D(0.0, y * fieldSize), Point2D(fieldSize * boardSize, y * fieldSize)))
            }
        }
    }
}

class Segment(val first: Point2D, val second: Point2D)


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
