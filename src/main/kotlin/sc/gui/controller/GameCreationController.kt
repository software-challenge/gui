package sc.gui.controller

import sc.gui.model.PlayerType
import sc.gui.model.TeamSettings
import sc.gui.model.TeamSettingsModel
import tornadofx.Controller
import tornadofx.booleanBinding

class GameCreationController: Controller() {
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
        fire(StartGameRequest(playerOneSettingsModel.item, playerTwoSettingsModel.item))
    }
    
    val hasHumanPlayer =
            booleanBinding(playerOneSettings.type, playerTwoSettings.type)
            { playerOneSettings.isHuman || playerTwoSettings.isHuman }
}
