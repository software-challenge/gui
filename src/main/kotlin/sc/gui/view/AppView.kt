package sc.gui.view

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.stage.FileChooser
import mu.KotlinLogging
import sc.gui.AppStyle
import sc.gui.controller.AppController
import sc.gui.controller.CreateGame
import sc.gui.controller.GameFlowController
import sc.gui.events.*
import sc.gui.guideMq
import sc.gui.model.ViewType
import sc.networking.clients.GameLoaderClient
import sc.util.browse
import sc.util.browseUrl
import sc.util.listenImmediately
import tornadofx.*
import java.io.File

private val logger = KotlinLogging.logger {}

class AppView: View("Software-Challenge Germany") {
    private val controller: AppController by inject()
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
                        if(controller.model.currentView.get() == ViewType.GAME) {
                            confirm(
                                    header = "Neues Spiel anfangen",
                                    content = "Willst du wirklich dein aktuelles Spiel verwerfen und ein neues anfangen?",
                            ) { fire(TerminateGame(true)) }
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
                    chooseFile("Replay laden",
                        arrayOf(FileChooser.ExtensionFilter("XML", "*.xml", "*.xml.gz")),
                        File("replays").takeIf { it.exists() }
                    ).forEach {
                        if(controller.model.currentView.get() == ViewType.GAME)
                            fire(TerminateGame())
                        try {
                            gameFlowController.loadReplay(GameLoaderClient(it))
                        } catch(e: Exception) {
                            warning("Replay laden fehlgeschlagen", "Das Replay $it konnte nicht geladen werden:\n" + e.stackTraceToString())
                        }
                    }
                }
                item("Logs öffnen", "Shortcut+L").action {
                    browse(File("log").absoluteFile)
                }
            }
            menu("Hilfe") {
                viewOrder = -9.0
                item("Bedienungsanleitung", "Shortcut+H").action {
                    alert(Alert.AlertType.INFORMATION, "Bedienungsanleitung", guideMq, title = "Hilfe")
                }
                item("↗ Spielregeln", "Shortcut+S").action {
                    browseUrl("https://docs.software-challenge.de/spiele/aktuell/regeln")
                }
                item("↗ Dokumentation", "Shortcut+D").action {
                    browseUrl("https://docs.software-challenge.de")
                }
                item("↗ Webseite", "Shortcut+I").action {
                    browseUrl("https://www.software-challenge.de")
                }
                item("↗ Wettbewerb", "Shortcut+W").action {
                    browseUrl("https://contest.software-challenge.de")
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
            center = AppStyle.background().apply { add(GameCreationView::class) }
            fire(CreateGame)
            // DEBUG Platform.runLater { scene.addEventHandler(EventType.ROOT) { logger.trace("EVENT: {}", it) } }
        }

        val gameTitle = "Ostseeschach"
        val version = resources.text("/version.txt")
        val sochaTitle = "Software-Challenge GUI $version"
        titleProperty.bind(controller.model.currentView.stringBinding {
            when(it) {
                ViewType.GAME_CREATION -> sochaTitle
                ViewType.GAME_LOADING -> "Starte Spiel $gameTitle - $sochaTitle"
                ViewType.GAME -> "Spiele $gameTitle - $sochaTitle"
                null -> throw NoWhenBranchMatchedException("Current view can't be null!")
            }
        })

        controller.model.darkMode.listenImmediately { value ->
            if(value) {
                root.removeClass(AppStyle.lightColorSchema)
                root.addClass(AppStyle.darkColorSchema)
            } else {
                root.removeClass(AppStyle.darkColorSchema)
                root.addClass(AppStyle.lightColorSchema)
            }
        }
    }
}
