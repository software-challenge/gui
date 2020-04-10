package sc.gui

import javafx.beans.property.SimpleStringProperty
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
            addClass(MyStyle.tackyButton)
            action {
                find<MyFragment>().openModal(stageStyle = StageStyle.UTILITY)
            }
        }
        button("Other view") {
            action {
                replaceWith(MyView::class, ViewTransition.Slide(0.3.seconds, ViewTransition.Direction.RIGHT))
            }
        }
        children.filter { it is Button }.addClass(MyStyle.tackyButton)
    }
}

class BottomView: View() {
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
