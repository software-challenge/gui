package sc.gui.view

import sc.gui.MasterView
import tornadofx.*

class AppView : View() {

    override val root = borderpane {
        top = menubar {
            menu("File") {
                menu("Connect") {
                    item("Facebook").action { println("Connecting Facebook!") }
                    item("Twitter").action { println("Connecting Twitter!") }
                }
                item("Save", "Shortcut+S").action {
                    println("Saving!")
                }
                item("Quit", "Shortcut+Q").action {
                    println("Quitting!")
                }
            }
            menu("Game") {
                item("New", "Shortcut+N").action {
                    println("New!")
                    center(GameCreationView::class)
                    //, tornadofx.ViewTransition.Slide(0.3.seconds, tornadofx.ViewTransition.Direction.RIGHT))
                }
                separator()
                item("Copy", "Shortcut+C").action {
                    println("Copying!")
                }
                item("Paste", "Shortcut+V").action {
                    println("Pasting!")
                }
            }
        }
        bottom = hbox {
            hyperlink("Software-Challenge Germany 2020/21").action {

            }
        }
    }

    init {
        this.root.center(MasterView::class)
    }
}