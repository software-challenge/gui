package sc.gui.view

import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.stage.FileChooser
import sc.api.plugins.Team
import sc.gui.controller.NavigateBackEvent
import sc.gui.controller.StartGameRequest
import sc.gui.model.PlayerType
import sc.gui.model.TeamSettings
import sc.gui.model.TeamSettingsModel
import tornadofx.*
import java.io.File

class GameCreationView: View() {
    private val playerSettingsModels =
            arrayOf(TeamSettings("Spieler 1", PlayerType.COMPUTER_EXAMPLE),
                    TeamSettings("Spieler 2", PlayerType.HUMAN))
                    .map { TeamSettingsModel(it) }
    
    override val root = borderpane {
        style {
            padding = box(20.px)
        }
        center = form {
            hbox {
                Team.values().forEach { team ->
                    vbox(20) {
                        add(PlayerFragment(team, playerSettingsModels[team.index]))
                    }
                }
            }
        }
        bottom = hbox {
            style {
                alignment = Pos.TOP_RIGHT
            }
            button("Erstellen") {
                action {
                    playerSettingsModels.all { it.commit() }
                    fire(StartGameRequest(playerSettingsModels.map { it.item }))
                }
                enableWhen(Bindings.and(playerSettingsModels[0].valid, playerSettingsModels[1].valid))
            }
            
            button("Zurück") {
                action {
                    fire(NavigateBackEvent)
                }
            }
        }
    }
}

class PlayerFragment(private val team: Team, private val settings: TeamSettingsModel): Fragment() {
    override val root = vbox(20) {
        fieldset(if (team == Team.ONE) "Erster Spieler" else "Zweiter Spieler") {
            textfield(settings.name).required()
            add(PlayerFileSelectFragment(team, settings))
        }
    }
}

class PlayerFileSelectFragment(private val team: Team, private val settings: TeamSettingsModel): Fragment() {
    override val root = borderpane {
        top = hbox {
            combobox(settings.type, PlayerType.values().toList())
        }
    }
    
    private fun updatePlayerType() {
        // TODO: work with proper binding of property
        when (settings.type.value) {
            PlayerType.COMPUTER -> {
                root.center = hbox(20) {
                    button("Client wählen") {
                        action {
                            val selectedFile = chooseFile(
                                    "Client wählen",
                                    arrayOf(
                                            FileChooser.ExtensionFilter("Alle Dateien", "*.*"),
                                            FileChooser.ExtensionFilter("jar", "*.jar")
                                    )
                            )
                            if (selectedFile.isNotEmpty()) {
                                println("Selected file $selectedFile")
                                settings.executable.value = selectedFile.first()
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
            PlayerType.EXTERNAL -> {
                root.center = label("Spieler muss nach Erstellung des Spiels manuell gestartet werden")
                root.bottom = label("")
            }
            PlayerType.COMPUTER_EXAMPLE -> {
                root.center = label("Ein interner Computerspieler")
                root.bottom = label()
            }
            PlayerType.HUMAN -> {
                root.center = label("Ein von Hand gesteuerter Spieler")
                root.bottom = label()
            }
            else -> throw Exception("Unknown Player-Type")
        }
    }
    
    init {
        settings.type.onChange {
            updatePlayerType()
            settings.validate()
        }
        settings.executable.onChange {
            root.bottom = textflow {
                label("Ausgewählte Datei: ")
                label(settings.executable.value.absolutePath)
            }
        }
        updatePlayerType()
        
        val obs: ObservableValue<File?> = settings.executable
        settings.validationContext.addValidator(root.bottom, obs, ValidationTrigger.OnChange()) {
            if (settings.type.value == PlayerType.COMPUTER && settings.executable.value == null) error("Bitte wähle eine ausführbare Datei aus") else null
        }
        settings.validate()
    }
}
