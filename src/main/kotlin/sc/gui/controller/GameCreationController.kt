package sc.gui.controller

import sc.gui.model.PlayerType
import sc.gui.model.TeamSettings
import sc.gui.model.TeamSettingsModel
import sc.gui.model.ViewTypes
import sc.gui.view.GameView
import tornadofx.*

class GameCreationController : Controller() {
    private val appController: AppController by inject()
    private val gameController: GameController by inject()

    private val playerOneSettings = TeamSettings()
    val playerOneSettingsModel = TeamSettingsModel(playerOneSettings)
    private val playerTwoSettings = TeamSettings()
    val playerTwoSettingsModel = TeamSettingsModel(playerTwoSettings)

    init {
        playerOneSettings.nameProperty().set("Spieler 1")
        playerOneSettings.typeProperty().set(PlayerType.INTERNAL)
        playerTwoSettings.nameProperty().set("Spieler 2")
        playerTwoSettings.typeProperty().set(PlayerType.HUMAN)
    }

    fun createGame() {
        // as we currently just support a single game at a time
        if (appController.model.previousViewProperty().get() == ViewTypes.GAME) {
            gameController.clearGame()
        }

        playerOneSettingsModel.commit()
        playerTwoSettingsModel.commit()
        appController.changeViewTo(GameView::class)

        fire(StartGameRequest(playerOneSettingsModel.item, playerTwoSettingsModel.item))
    }
}
