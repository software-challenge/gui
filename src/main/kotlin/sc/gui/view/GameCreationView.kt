package sc.gui.view

import javafx.beans.property.Property
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.stage.FileChooser
import sc.gui.controller.GameCreationController
import sc.gui.model.GameCreationModel
import sc.gui.model.PlayerType
import tornadofx.*
import java.io.File

class GameCreationView : View("Neues Spiel") {
    val controller: GameCreationController by inject()
    val model: GameCreationModel by inject()

    override val root = borderpane {
        style {
            padding = box(20.px)
        }
        center = form {
            hbox {
                vbox(20) {
                    add(PlayerFragment(model, 1))
                }
                vbox(20) {
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

    override var root = vbox(20) {
        fieldset("Spieler Nr. $player") {
            textfield(getPlayerName()).required()
            add(PlayerFileSelectFragment(model, player))
        }
    }


    private fun getPlayerName(): Property<String> {
        if (player == 1) {
            return model.playerName1
        }
        return model.playerName2
    }
}

class PlayerFileSelectFragment(model: GameCreationModel, player: Int) : Fragment() {
    private val player: Int = player
    private val model: GameCreationModel = model
    private val playerTypes: ObservableList<PlayerType> = FXCollections.observableArrayList(PlayerType.HUMAN, PlayerType.MANUELL, PlayerType.COMPUTER)

    override var root = borderpane {
        top = hbox {
            combobox(getPlayerType(), playerTypes) {
                getPlayerType().value
            }
        }
    }

    fun updatePlayerType() {
        if (getPlayerType().value == PlayerType.COMPUTER) {
            root.center = hbox(20) {
                button("Client wählen") {
                    action {
                        val fileChooser = FileChooser()
                        fileChooser.title = "Client suchen"
                        fileChooser.extensionFilters.addAll(
                                FileChooser.ExtensionFilter("Alle Dateien", "*.*"),
                                FileChooser.ExtensionFilter("jar", "*.jar")
                        )
                        val selectedFile = fileChooser.showOpenDialog(find(AppView::class).currentWindow)
                        if (selectedFile != null) {
                            println("Selected file $selectedFile")
                            getPlayerJarFile().value = selectedFile
                        }
                    }
                }
                text("Wähle eine ausführbare Datei aus")
            }
            root.bottom = textflow {
                text("Ausgewählte Datei: ")
                text("")
            }
        } else if (getPlayerType().value == PlayerType.MANUELL) {
            root.center = text("Das Programm muss nach Erstellung des Spiels manuell gestartet werden.")
            root.bottom = text()
        } else {
            root.center = text("Ein Mensch wird das Spiel hier spielen")
            root.bottom = text()
        }
    }

    private fun getPlayerType(): Property<PlayerType> {
        if (player == 1) {
            return model.selectedPlayerType1
        }
        return model.selectedPlayerType2
    }

    private fun getPlayerJarFile(): Property<File> {
        if (player == 1) {
            return model.playerJarFile1
        }
        return model.playerJarFile2
    }


    init {
        if (player == 1) {
            model.selectedPlayerType1.onChange {
                updatePlayerType()
            }
            model.playerJarFile1.onChange {
                root.bottom = textflow {
                    text("Ausgewählte Datei: ")
                    text(model.playerJarFile1.value.absolutePath)
                }
            }
        } else {
            model.selectedPlayerType2.onChange {
                updatePlayerType()
            }
            model.playerJarFile2.onChange {
                root.bottom = textflow {
                    text("Ausgewählte Datei: ")
                    text(model.playerJarFile2.value.absolutePath)
                }
            }
        }
        updatePlayerType()
    }
}