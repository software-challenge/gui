package sc.gui.view

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.image.ImageView
import sc.gui.controller.AppController
import sc.gui.controller.ClientController
import sc.gui.controller.ServerController
import sc.gui.model.ViewTypes
import tornadofx.*
import java.awt.Desktop
import java.net.URI

class AppView : View() {
    val controller: AppController by inject()
    private val clientController: ClientController by inject()
    private val serverController: ServerController by inject()
    val sochaIcon = ImageView("https://raw.githubusercontent.com/CAU-Kiel-Tech-Inf/socha-gui/master/assets/build-resources/icon.png")

    override val root = borderpane {
        top = menubar {
            menu(graphic = sochaIcon) {
                item("Beenden", "Shortcut+Q").action {
                    println("Quitting!")
                    Platform.exit()
                }
            }
            menu("Spiel") {
                item("Neues Spiel", "Shortcut+N").action {
                    println("New Game!")
                    if (controller.model.currentView == ViewTypes.GAME) {
                        alert(
                                type = Alert.AlertType.CONFIRMATION,
                                header = "Neues Spiel anfangen",
                                content = "Willst du wirklich dein aktuelles Spiel verwerfen und ein neues anfangen?",
                                actionFn = { btnType ->
                                    if (btnType.buttonData == ButtonBar.ButtonData.OK_DONE) {
                                        serverController.endGame()
                                        controller.changeViewTo(GameCreationView::class)
                                    }
                                }
                        )
                    } else if (controller.model.currentView != ViewTypes.GAME_CREATION) {
                        controller.changeViewTo(GameCreationView::class)
                    }
                }
                item("Start", "Shortcut+R").action {
                    // TODO: remove
                    controller.changeViewTo(GameView::class)
                    clientController.startGame()
                }
                separator()
                item("Replay laden").action {
                    // TODO
                    println("Replay wird geladen")
                }
                item("Logs öffnen", "Shortcut+L").action {
                    // TODO
                    println("Logs werden geöffnet")
                }
            }
            menu("Hilfe") {
                item("Spielregeln", "Shortcut+S").action {
                    // TODO: github.io Link der Doku einfügen
                    Desktop.getDesktop().browse(URI("https://www.software-challenge.de"))
                }
                item("Hilfe", "Shortcut+H").action {
                    // TODO: github.io Link der Doku einfügen
                    Desktop.getDesktop().browse(URI("https://www.software-challenge.de"))
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
            prefWidth = 1300.0
            prefHeight = 800.0
            center(MasterView::class)
        }
    }
}