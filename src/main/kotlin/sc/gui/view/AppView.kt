package sc.gui.view

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.stage.FileChooser
import mu.KotlinLogging
import sc.gui.AppStyle
import sc.gui.controller.AppController
import sc.gui.controller.CreateGame
import sc.gui.controller.GameController
import sc.gui.controller.GameFlowController
import sc.gui.model.ViewType
import sc.plugin2021.Rotation
import tornadofx.*
import java.awt.Desktop
import java.awt.Desktop.Action
import java.io.File
import java.net.URI

private val logger = KotlinLogging.logger {}

class AppView : View("Software-Challenge Germany") {
    val controller: AppController by inject()
    private val gameFlowController: GameFlowController by inject()
    private val gameController: GameController by inject()
    
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
                item("Dunkles Design umschalten").action {
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
            menu("Steuerung") {
                enableWhen(controller.model.currentView.isEqualTo(ViewType.GAME))
                menu("Rotieren") {
                    item("Scrollen", "Mausrad")
                    item("Uhrzeigersinn", "D").action {
                        gameController.rotatePiece(Rotation.RIGHT)
                    }
                    item("Gegen Uhrzeigersinn", "A").action {
                        gameController.rotatePiece(Rotation.LEFT)
                    }
                    item("180", "W oder S").action {
                        gameController.rotatePiece(Rotation.MIRROR)
                    }
                }
                item("Spiegeln", "R-Click oder CTRL").action {
                    gameController.flipPiece()
                }
            }
            menu("Hilfe") {
                item("Spielregeln", "Shortcut+S").action {
                    "https://docs.software-challenge.de/spiele/blokus/regeln.html".browseUrl()
                }
                item("Webseite", "Shortcut+I").action {
                    "https://www.software-challenge.de".browseUrl()
                }
                item("Wettbewerb", "Shortcut+W").action {
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
            center(StartView::class)
        }
        
        // responsive scaling
        val resizer = ChangeListener<Number> { _, _, _ ->
            if (controller.model.currentView.get() == ViewType.GAME) {
                find(GameView::class).resize()
            }
        }
        root.widthProperty().addListener(resizer)
        root.heightProperty().addListener(resizer)
        
        val gameTitle = "Blokus"
        val version = resources.text("/version.txt")
        title = "Spiele $gameTitle - Software-Challenge 2021"
        
        controller.model.isDarkMode.listenImmediately { value ->
            if (value) {
                root.removeClass(AppStyle.lightColorSchema)
                root.addClass(AppStyle.darkColorSchema)
            } else {
                root.removeClass(AppStyle.darkColorSchema)
                root.addClass(AppStyle.lightColorSchema)
            }
        }
        
        fire(CreateGame)
    }
}

fun <T> ObservableValue<T>.listenImmediately(listener: (newValue: T) -> Unit) {
    listener(this.value)
    addListener { _, _, new -> listener(new) }
}

fun String.browseUrl() {
    URI(this).openDesktop(Action.BROWSE, Desktop::browse)
}

fun File.browse() {
    openDesktop(Action.OPEN, Desktop::browseFileDirectory)
}

fun <T> T.openDesktop(action: Action, open: Desktop.(T) -> Unit) {
    val desktop = Desktop.getDesktop()
    logger.debug("Opening {} on {}", this, desktop)
    if(desktop.isSupported(action))
        open(desktop, this)
    else
        Runtime.getRuntime().exec("xdg-open $this")
}
