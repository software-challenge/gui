package sc.gui.controller

import javafx.beans.binding.Bindings
import sc.gui.model.PlayerType
import sc.gui.model.TeamSettings
import sc.gui.model.TeamSettingsModel
import sc.gui.model.ViewType
import tornadofx.Controller

class GameCreationController : Controller() {
    private val appController: AppController by inject()

    private val playerOneSettings = TeamSettings()
    val playerOneSettingsModel = TeamSettingsModel(playerOneSettings)
    private val playerTwoSettings = TeamSettings()
    val playerTwoSettingsModel = TeamSettingsModel(playerTwoSettings)

    init {
        playerOneSettings.name.set("Spieler 1")
        playerOneSettings.type.set(PlayerType.COMPUTER_EXAMPLE)
        playerTwoSettings.name.set("Spieler 2")
        playerTwoSettings.type.set(PlayerType.HUMAN)
    }

    fun createGame() {
        playerOneSettingsModel.commit()
        playerTwoSettingsModel.commit()
        appController.changeViewTo(ViewType.GAME_LOADING)

        fire(StartGameRequest(playerOneSettingsModel.item, playerTwoSettingsModel.item))
    }
    
    val hasHumanPlayer = Bindings.createBooleanBinding({ playerOneSettings.isHuman || playerTwoSettings.isHuman }, playerOneSettings.type, playerTwoSettings.type)
}
