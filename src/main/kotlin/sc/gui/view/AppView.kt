package sc.gui.view

import sc.gui.controller.AppController
import tornadofx.*
import java.awt.Desktop
import java.net.URI

class AppView : View() {
    val controller: AppController by inject()

    override val root = borderpane {
        top = menubar {
            menu("File") {
                // TODO: will be removed
                menu("Connect") {
                    item("Facebook").action { println("Connecting Facebook!") }
                    item("Twitter").action { println("Connecting Twitter!") }
                }
                item("Quit", "Shortcut+Q").action {
                    println("Quitting!")
                }
            }
            menu("Game") {
                item("New", "Shortcut+N").action {
                    println("New!")
                    center(GameCreationView::class)
                }
                separator()
                item("Replay laden", "Shortcut+R").action {
                    // TODO
                    println("Replay wird geladen")
                }
                item("Logs öffnen", "Shortcut+L").action {
                    // TODO
                    println("Logs werden geöffnet")
                }
            }
            menu("Hilfe") {
                item("Spielregeln", "Shortcut+S").action {
                    // TODO: github.io Link der Doku einfügen
                    Desktop.getDesktop().browse(URI("https://www.software-challenge.de"))
                }
                item("Hilfe", "Shortcut+H").action {
                    // TODO: github.io Link der Doku einfügen
                    Desktop.getDesktop().browse(URI("https://www.software-challenge.de"))
                }
                item("Webseite", "Shortcut+I").action {
                    Desktop.getDesktop().browse(URI("https://www.software-challenge.de"))
                }
                item("Wettbewerb", "Shortcut+W").action {
                    Desktop.getDesktop().browse(URI("https://contest.software-challenge.de/saison/28"))
                }
            }
        }
    }

    init {
        this.root.center(MasterView::class)
    }
}