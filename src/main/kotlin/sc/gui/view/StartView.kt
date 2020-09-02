package sc.gui.view

import sc.gui.controller.AppController
import tornadofx.*

class StartView: View() {
    private val controller: AppController by inject()
    override val root = borderpane {
        center = vbox {
            label("Willkommen bei der Software-Challenge!")
            button {
                text = "Neues Spiel"
                setOnMouseClicked {
                    controller.changeViewTo(GameCreationView::class)
                }
            }
        }

    }
}

