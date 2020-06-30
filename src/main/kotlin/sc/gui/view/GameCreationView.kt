package sc.gui.view

import sc.gui.MasterView
import sc.gui.controller.GameCreationController
import sc.gui.model.GameCreationModel
import tornadofx.*

class GameCreationView : View("Neues Spiel") {
    val controller: GameCreationController by inject()
    val model: GameCreationModel by inject()

    override val root = form {
        fieldset("Neues Spiel erstellen") {
            field("Name") {
                textfield(model.name).required(message = "Gib eine Bezeichnung für das Spiel ein")
            }
            // TODO
        }
        hbox {
            button("Erstellen") {
                action {
                    // TODO
                }

                // TODO
                enableWhen(model.valid)
            }

            button("Zurück") {
                action {
                    replaceWith(MasterView::class)
                }
            }
        }
    }
}