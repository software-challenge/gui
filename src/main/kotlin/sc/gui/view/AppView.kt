package sc.gui.view

import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.Platform
import javafx.scene.control.Alert
import sc.api.plugins.IGamePlugin
import sc.gui.AppStyle
import sc.gui.controller.AppController
import sc.gui.controller.CreateGame
import sc.gui.controller.selectReplay
import sc.gui.events.*
import sc.gui.guide
import sc.gui.model.ViewType
import sc.gui.replaysEnabled
import sc.gui.util.browse
import sc.gui.util.browseUrl
import tornadofx.*
import java.io.File

private val logger = KotlinLogging.logger {}

class AppView: View("Software-Challenge Germany") {
    private val controller: AppController by inject()
    private val sochaIcon = resources.imageview("/icon.png")
    
    override val root = borderpane {
        addClass(AppStyle.lightColorSchema)
        top = menubar {
            // TODO help menus keep disappearing and is offset
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
                if(replaysEnabled)
                    item("Replay laden", "Shortcut+R").action {
                        selectReplay {
                            if(controller.model.currentView.get() == ViewType.GAME)
                                fire(TerminateGame())
                        }
                    }
                item("Logs öffnen", "Shortcut+L").action {
                    browse(File("log").absoluteFile)
                }
            }
            menu("Hilfe") {
                viewOrder = -9.0
                item("Bedienhilfe", "Shortcut+H").action {
                    alert(Alert.AlertType.INFORMATION,
                        header = "Bedienhilfe",
                        content = guide,
                        title = "Hilfe")
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
        
        val gameTitle = IGamePlugin.loadPlugin().name
        val version = resources.text("/version.txt")
        val sochaTitle = "Software-Challenge GUI $version"
        titleProperty.bind(controller.model.currentView.stringBinding {
            when(it) {
                ViewType.GAME_CREATION -> sochaTitle
                ViewType.GAME_LOADING -> "Spiel Startet - $sochaTitle"
                ViewType.GAME -> "Spiele $gameTitle - $sochaTitle"
                null -> throw NoWhenBranchMatchedException("Current view can't be null!")
            }.also { logger.debug { "New window title: $it" } }
        })
        
        controller.model.applyTheme(root)
    }
}
