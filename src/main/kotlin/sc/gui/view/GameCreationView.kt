package sc.gui.view

import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableValue
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Region
import javafx.scene.text.TextAlignment
import javafx.stage.FileChooser
import sc.api.plugins.Team
import sc.gui.AppStyle
import sc.gui.controller.StartGame
import sc.gui.controller.selectReplay
import sc.gui.guideMq
import sc.gui.model.PlayerType
import sc.gui.model.TeamSettings
import sc.gui.model.TeamSettingsModel
import tornadofx.*
import java.io.File

class GameCreationView: View() {
    private val playerSettingsModels =
            arrayOf(TeamSettings("Spieler 1", PlayerType.COMPUTER_EXAMPLE),
                    TeamSettings("Spieler 2", PlayerType.allowedValues().first()))
                    .map { TeamSettingsModel(it) }
    
    override val root = borderpane {
        padding = Insets(AppStyle.spacing)
        center = form {
            alignment = Pos.CENTER
            label("Willkommen bei der Software-Challenge!") {
                addClass(AppStyle.heading)
                isWrapText = true
                textAlignment = TextAlignment.CENTER
            }
            label(guideMq)
            gridpane {
                hgap = AppStyle.spacing
                Team.values().forEach { team ->
                    val settings = playerSettingsModels[team.index]
                    fieldset(if(team == Team.ONE) "Erster Spieler" else "Zweiter Spieler") {
                        alignment = Pos.TOP_CENTER
                        spacing = AppStyle.formSpacing
                        textfield(settings.name) {
                            promptText = "Name des Spielers ${team.index + 1}"
                            required()
                        }
                        add(PlayerFileSelectFragment(team, settings))
                        gridpaneConstraints {
                            rowIndex = 0
                            columnIndex = team.index
                        }
                    }
                    constraintsForColumn(team.index).percentWidth = 50.0
                }
            }
        }
        top =  hbox(AppStyle.spacing, Pos.CENTER_RIGHT) {
            button("Replay laden").action { selectReplay() }
        }
        bottom = hbox(AppStyle.spacing, Pos.CENTER_RIGHT) {
            button("Erstellen") {
                action {
                    playerSettingsModels.all { it.commit() }
                    fire(StartGame(playerSettingsModels.map { it.item }))
                }
                enableWhen(Bindings.and(playerSettingsModels[0].valid, playerSettingsModels[1].valid))
            }
        }
    }
}

class PlayerFileSelectFragment(private val team: Team, private val settings: TeamSettingsModel): Fragment() {
    override val root = borderpane {
        prefHeight = AppStyle.fontSizeRegular.value * 9
        top = combobox(settings.type, PlayerType.allowedValues().toList()) {
            maxWidth = Double.MAX_VALUE
        }
    }
    
    private fun updatePlayerType() {
        // TODO: work with proper binding of property
        when(settings.type.value as PlayerType) {
            PlayerType.COMPUTER -> {
                root.center = hbox(AppStyle.spacing) {
                    button("Client wählen") {
                        action {
                            val selectedFile = chooseFile(
                                    "Client wählen",
                                    arrayOf(
                                            FileChooser.ExtensionFilter("Alle Dateien", "*.*"),
                                            FileChooser.ExtensionFilter("jar", "*.jar")
                                    )
                            )
                            if(selectedFile.isNotEmpty()) {
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
                root.center = label("Spieler muss nach Erstellung des Spiels separat gestartet werden")
                root.bottom = label("")
            }
            PlayerType.COMPUTER_EXAMPLE -> {
                root.center = label("Ein einfacher, interner Computerspieler")
                root.bottom = label()
            }
            PlayerType.HUMAN -> {
                root.center = label("Ein von Hand gesteuerter Spieler")
                root.bottom = label()
            }
        }
        (root.center as Region).paddingTop = AppStyle.formSpacing
    }
    
    init {
        settings.type.onChange {
            updatePlayerType()
            settings.validate()
        }
        settings.executable.onChange {
            root.bottom = textflow {
                val parentWidth = widthProperty()
                label("Ausgewählte Datei: ")
                label(settings.executable.value.absolutePath) {
                    isWrapText = true
                    prefWidthProperty().bind(parentWidth)
                }
            }
        }
        updatePlayerType()
        
        val obs: ObservableValue<File?> = settings.executable
        settings.validationContext.addValidator(root.bottom, obs, ValidationTrigger.OnChange()) {
            if(settings.type.value == PlayerType.COMPUTER && settings.executable.value == null) error("Bitte wähle eine ausführbare Datei aus") else null
        }
        settings.validate()
    }
}
