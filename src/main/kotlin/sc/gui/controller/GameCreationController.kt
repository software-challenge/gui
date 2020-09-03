package sc.gui.controller

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

    fun createGame() {
        // as we currently just support a single game at a time
        if (appController.model.previousViewProperty().get() == ViewTypes.GAME) {
            gameController.clearGame()
            //TODO("Kill previous game")
        }

        println("Creating new game")
        playerOneSettingsModel.commit()
        playerTwoSettingsModel.commit()
        println("Player 1: ${playerOneSettingsModel.item.nameProperty()}, ${playerOneSettingsModel.item.typeProperty()}")
        println("Player 2: ${playerTwoSettingsModel.item.nameProperty()}, ${playerTwoSettingsModel.item.typeProperty()}")
        appController.changeViewTo(GameView::class)

        fire(StartGameRequest(playerOneSettingsModel.item, playerTwoSettingsModel.item))
    }
}