package sc.gui.view

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser
import mu.KotlinLogging
import sc.gui.AppStyle
import sc.gui.controller.AppController
import sc.gui.controller.CreateGame
import sc.gui.controller.GameFlowController
import sc.gui.events.TerminateGame
import sc.gui.model.ViewType
import sc.util.browse
import sc.util.browseUrl
import sc.util.listenImmediately
import tornadofx.*
import java.io.File

private val logger = KotlinLogging.logger {}

class AppView: View("Software-Challenge Germany") {
    val controller: AppController by inject()
    private val gameFlowController: GameFlowController by inject()
    private val sochaIcon = resources.imageview("/icon.png")
    
    override val root = borderpane {
        addClass(AppStyle.lightColorSchema)
        top = menubar {
            menu(graphic = sochaIcon) {
                item("Beenden", "Shortcut+Q").action {
                    logger.debug("Quitting!")
                    Platform.exit()
                }
                item("Neues Spiel", "Shortcut+N") {
                    enableWhen(controller.model.currentView.isNotEqualTo(ViewType.GAME_CREATION))
                    action {
                        logger.debug("New Game!")
                        if (controller.model.currentView.get() == ViewType.GAME) {
                            confirm(
                                    header = "Neues Spiel anfangen",
                                    content = "Willst du wirklich dein aktuelles Spiel verwerfen und ein neues anfangen?",
                            ) { fire(TerminateGame()) }
                        } else {
                            fire(CreateGame)
                        }
                    }
                }
                item("Dunkles Design umschalten", "Shortcut+U").action {
                    controller.toggleDarkmode()
                }
                separator()
                item("Replay laden", "Shortcut+R").action {
                    chooseFile("Replay laden", arrayOf(FileChooser.ExtensionFilter("XML", "*.xml", "*.xml.gz")), File("replays")).forEach {
                        if (controller.model.currentView.get() == ViewType.GAME)
                            fire(TerminateGame())
                        gameFlowController.loadReplay(it)
                    }
                }
                item("Logs öffnen", "Shortcut+L").action {
                    File("log").absoluteFile.browse()
                }
            }
            menu("Hilfe") {
                item("Bedienungsanleitung", "Shortcut+H").action {
                    alert(Alert.AlertType.INFORMATION, "Bedienungsanleitung", """
                        - Fahre über eine Figur, um ihre möglichen Züge zu sehen
                        - Klicke eine Figur und dann das Zielfeld an, um sie zu bewegen
                        - Durch ein erneutes Klicken auf die Figur kannst du sie wieder abwählen
                    """.trimIndent(), title = "Hilfe")
                }
                item("↗ Spielregeln", "Shortcut+S").action {
                    "https://docs.software-challenge.de/spiele/aktuell/regeln.html".browseUrl()
                }
                item("↗ Dokumentation", "Shortcut+D").action {
                    "https://docs.software-challenge.de".browseUrl()
                }
                item("↗ Webseite", "Shortcut+I").action {
                    "https://www.software-challenge.de".browseUrl()
                }
                item("↗ Wettbewerb", "Shortcut+W").action {
                    "https://contest.software-challenge.de/saison/current".browseUrl()
                }
            }
        }
        
    }
    
    init {
        sochaIcon.fitHeight = 32.0
        sochaIcon.fitWidth = 32.0
        with(root) {
            prefWidth = 1100.0
            prefHeight = 700.0
            center = StackPane(
                    Region().apply {
                        hgrow = Priority.ALWAYS
                        vgrow = Priority.ALWAYS
                        addClass(AppStyle.background)
                    }
            ).apply { add(StartView::class) }
        }
        
        val gameTitle = "Blokus"
        val version = resources.text("/version.txt")
        val sochaTitle = "Software-Challenge GUI $version"
        titleProperty.bind(controller.model.currentView.stringBinding {
            when (it) {
                ViewType.START -> sochaTitle
                ViewType.GAME_CREATION -> "Neues Spiel - $sochaTitle"
                ViewType.GAME_LOADING -> "Starte Spiel $gameTitle - $sochaTitle"
                ViewType.GAME -> "Spiele $gameTitle - $sochaTitle"
                null -> throw NoWhenBranchMatchedException("Current view can't be null!")
            }
        })
        
        controller.model.darkMode.listenImmediately { value ->
            if (value) {
                root.removeClass(AppStyle.lightColorSchema)
                root.addClass(AppStyle.darkColorSchema)
            } else {
                root.removeClass(AppStyle.darkColorSchema)
                root.addClass(AppStyle.lightColorSchema)
            }
        }
    }
}
