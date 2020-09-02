package sc.gui.view

import javafx.beans.property.Property
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.stage.FileChooser
import sc.gui.controller.AppController
import sc.gui.controller.GameCreationController
import sc.gui.model.PlayerType
import sc.gui.model.ViewTypes
import tornadofx.*
import java.io.File

class GameCreationView : View() {
    private val appController: AppController by inject()
    val controller: GameCreationController by inject()

    override val root = borderpane {
        style {
            padding = box(20.px)
        }
        center = form {
            hbox {
                vbox(20) {
                    add(PlayerFragment(1))
                }
                vbox(20) {
                    add(PlayerFragment(2))
                }
            }
        }
        bottom = hbox {
            style {
                alignment = Pos.TOP_RIGHT
            }
            button("Erstellen") {
                action {
                    controller.createGame()
                }

                // TODO
                enableWhen(controller.model.valid)
            }

            button("Zurück") {
                action {
                    appController.changeViewTo(when (appController.model.previousViewProperty().get()) {
                        ViewTypes.GAME_CREATION -> GameCreationView::class
                        ViewTypes.GAME_ENDED -> GameEndedView::class
                        ViewTypes.GAME -> GameView::class
                        ViewTypes.START -> StartView::class
                        else -> throw Exception("Unknown type of view")
                    })
                }
            }
        }
    }
}

class PlayerFragment(private val player: Int) : Fragment() {
    val controller: GameCreationController by inject()

    override var root = vbox(20) {
        fieldset("Spieler Nr. $player") {
            textfield(getPlayerName()).required()
            add(PlayerFileSelectFragment(player))
        }
    }


    private fun getPlayerName(): Property<String> {
        if (player == 1) {
            return controller.model.playerName1
        }
        return controller.model.playerName2
    }
}

class PlayerFileSelectFragment(private val player: Int) : Fragment() {
    val controller: GameCreationController by inject()
    private val playerTypes: ObservableList<PlayerType> = FXCollections.observableArrayList(PlayerType.HUMAN, PlayerType.MANUELL, PlayerType.COMPUTER)

    override var root = borderpane {
        top = hbox {
            combobox(getPlayerType(), playerTypes) {
                getPlayerType().value
            }
        }
    }

    private fun updatePlayerType() {
        // TODO: work with proper binding of property
        when (getPlayerType().value) {
            PlayerType.COMPUTER -> {
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
                                getPlayerExecutable().value = selectedFile
                            }
                        }
                    }
                    label("Wähle eine ausführbare Datei aus")
                }
                root.bottom = textflow {
                    label("Ausgewählte Datei: ")
                    label("")
                }
            }
            PlayerType.MANUELL -> {
                root.center = label("Das Programm muss nach Erstellung des Spiels manuell gestartet werden.")
                root.bottom = label()
            }
            else -> {
                root.center = label("Ein Mensch wird das Spiel hier spielen")
                root.bottom = label()
            }
        }
    }

    private fun getPlayerType(): Property<PlayerType> {
        if (player == 1) {
            return controller.model.selectedPlayerType1
        }
        return controller.model.selectedPlayerType2
    }

    private fun getPlayerExecutable(): Property<File> {
        if (player == 1) {
            return controller.model.playerExecutable1
        }
        return controller.model.playerExecutable2
    }


    init {
        if (player == 1) {
            controller.model.selectedPlayerType1.onChange {
                updatePlayerType()
            }
            controller.model.playerExecutable1.onChange {
                root.bottom = textflow {
                    label("Ausgewählte Datei: ")
                    label(controller.model.playerExecutable1.value.absolutePath)
                }
            }
        } else {
            controller.model.selectedPlayerType2.onChange {
                updatePlayerType()
            }
            controller.model.playerExecutable2.onChange {
                root.bottom = textflow {
                    label("Ausgewählte Datei: ")
                    label(controller.model.playerExecutable2.value.absolutePath)
                }
            }
        }
        updatePlayerType()
    }
}