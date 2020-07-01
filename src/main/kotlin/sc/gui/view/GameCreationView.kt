package sc.gui.view

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.stage.FileChooser
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
            hbox {
                PlayerFragment(1)
                PlayerFragment(2)
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

class PlayerFragment(number: Int) : Fragment() {
    private val selected = SimpleStringProperty()
    private val playerTypes: ObservableList<String> = FXCollections.observableArrayList("Player", "Manuell", "Computer")

    override val root = field("Player $number") {
        combobox(selected, playerTypes)
        if (selected == SimpleStringProperty("Computer")) {
            button("Client wählen") {
                action {
                    val fileChooser = FileChooser()
                    fileChooser.title = "Wähle den Client"
                    val selectedFile = fileChooser.showOpenDialog(find(AppView::class).currentWindow)
                    if (selectedFile != null) {
                        println("Selected file $selectedFile")
                    }
                }
            }
        } else if (selected == SimpleStringProperty("Manuell")) {
            label("Das Programm muss nach Erstellung des Spiels manuell gestartet werden.")
        }
    }

    init {
        selected.onChange {
            println("Player $number changed to: $it")
        }
        println("player $number has been initilized")
    }
}