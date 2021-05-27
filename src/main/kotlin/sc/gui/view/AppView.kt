package sc.gui.view

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import mu.KLogging
import sc.gui.AppStyle
import sc.gui.controller.AppController
import sc.gui.controller.CreateGame
import sc.gui.controller.GameController
import sc.gui.model.ViewType
import sc.plugin2021.Rotation
import tornadofx.*
import java.awt.Desktop
import java.net.URI

class AppView : View("Software-Challenge Germany") {
    val controller: AppController by inject()
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
                item("Toggle Darkmode").action {
                    controller.toggleDarkmode()
                }
                separator()
                item("Replay laden").action {
                    // TODO
                    logger.debug("Replay wird geladen")
                }
                item("Logs öffnen", "Shortcut+L").action {
                    // TODO
                    logger.debug("Logs werden geöffnet")
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
                item("Flippen", "R-Click oder CTRL").action {
                    gameController.flipPiece()
                }
            }
            menu("Hilfe") {
                item("Spielregeln", "Shortcut+S").action {
                    // TODO: github.io Link der Doku einfügen
                    Desktop.getDesktop().browse(URI("https://cau-kiel-tech-inf.github.io/socha-enduser-docs/spiele/blokus/regeln.html"))
                }
                item("Dokumentation", "Shortcut+D").action {
                    Desktop.getDesktop().browse(URI("https://cau-kiel-tech-inf.github.io/socha-enduser-docs/"))
                }
                item("Webseite", "Shortcut+I").action {
                    Desktop.getDesktop().browse(URI("https://www.software-challenge.de"))
                }
                item("Wettbewerb", "Shortcut+W").action {
                    Desktop.getDesktop().browse(URI("https://contest.software-challenge.de/saison/28"))
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
        val sochaTitle = "Software-Challenge GUI $version"
        titleProperty.bind(controller.model.currentView.stringBinding {
            when(it) {
                ViewType.START -> sochaTitle
                ViewType.GAME_CREATION -> "Neues Spiel - $sochaTitle"
                ViewType.GAME_LOADING -> "Starte Spiel $gameTitle - $sochaTitle"
                ViewType.GAME -> "Spiele $gameTitle - $sochaTitle"
                null -> throw NoWhenBranchMatchedException("Current view can't be null!")
            }
        })
    
        controller.model.isDarkMode.listenImmediately { value ->
            if (value) {
                root.removeClass(AppStyle.lightColorSchema)
                root.addClass(AppStyle.darkColorSchema)
            } else {
                root.removeClass(AppStyle.darkColorSchema)
                root.addClass(AppStyle.lightColorSchema)
            }
        }
    }
    
    companion object: KLogging()
}

fun ObservableValue<Boolean>.listenImmediately(listener: (newValue: Boolean) -> Unit) {
    listener(this.value)
    addListener { _, _, new -> listener(new) }
}
