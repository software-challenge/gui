package sc.gui.view

import javafx.beans.property.Property
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.stage.FileChooser
import sc.gui.MasterView
import sc.gui.controller.GameCreationController
import sc.gui.model.GameCreationModel
import sc.gui.model.PlayerType
import tornadofx.*

class GameCreationView : View("Neues Spiel") {
    val controller: GameCreationController by inject()
    val model: GameCreationModel by inject()

    private val playerTypes: ObservableList<PlayerType> = FXCollections.observableArrayList(PlayerType.PLAYER, PlayerType.MANUELL, PlayerType.COMPUTER)

    override val root = borderpane {
        style {
            padding = box(20.px)
        }
        center = form {
            fieldset("Neues Spiel erstellen") {
                field("Name") {
                    textfield(model.name).required()
                }
                hbox {
                    add(PlayerFragment(model, 1))
                    add(PlayerFragment(model, 2))
                }
            }
        }
        bottom = hbox {
            style {
                alignment = Pos.TOP_RIGHT
            }
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

class PlayerFragment(model: GameCreationModel, player: Int) : Fragment() {
    private val player: Int = player
    private val model: GameCreationModel = model
    private val playerTypes: ObservableList<PlayerType> = FXCollections.observableArrayList(PlayerType.PLAYER, PlayerType.MANUELL, PlayerType.COMPUTER)

    override val root = borderpane {
        style {
            minWidth = 120.px
            padding = CssBox(0.px, 20.px, 0.px, 0.px)
        }
        top = vbox {
            label("Player $player")
            combobox(getPlayerType(), playerTypes) {
                selectionModel.selectFirst()
            }
        }
    }

    private fun getPlayerType(): Property<PlayerType> {
        if (player == 1) {
            return model.selectedPlayerType1
        }
        return model.selectedPlayerType2
    }

    fun updatePlayerType() {
        println("Updated Playertype for player $player ->" + getPlayerType())
        if (getPlayerType().value == PlayerType.COMPUTER) {
            root.center = button("Client wählen") {
                action {
                    val fileChooser = FileChooser()
                    fileChooser.title = "Wähle den Client"
                    val selectedFile = fileChooser.showOpenDialog(find(AppView::class).currentWindow)
                    if (selectedFile != null) {
                        println("Selected file $selectedFile")
                    }
                }
            }
        } else if (getPlayerType().value == PlayerType.MANUELL) {
            root.center = label("Das Programm muss nach Erstellung des Spiels manuell gestartet werden.")
        }
    }


    init {
        if (player == 1) {
            model.selectedPlayerType1.onChange {
                updatePlayerType()
            }
        } else {
            model.selectedPlayerType2.onChange {
                updatePlayerType()
            }
        }
    }
}