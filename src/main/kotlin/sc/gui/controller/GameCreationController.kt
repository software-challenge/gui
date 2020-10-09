package sc.gui.controller

import javafx.beans.binding.Bindings
import sc.gui.model.PlayerType
import sc.gui.model.TeamSettings
import sc.gui.model.TeamSettingsModel
import sc.gui.model.ViewTypes
import sc.gui.view.GameView
import tornadofx.*
import java.util.concurrent.Callable

class GameCreationController : Controller() {
    private val appController: AppController by inject()
    private val gameController: GameController by inject()

    private val playerOneSettings = TeamSettings()
    val playerOneSettingsModel = TeamSettingsModel(playerOneSettings)
    private val playerTwoSettings = TeamSettings()
    val playerTwoSettingsModel = TeamSettingsModel(playerTwoSettings)

    init {
        playerOneSettings.name.set("Spieler 1")
        playerOneSettings.type.set(PlayerType.INTERNAL)
        playerTwoSettings.name.set("Spieler 2")
        playerTwoSettings.type.set(PlayerType.HUMAN)
    }

    fun createGame() {
        // as we currently just support a single game at a time
        if (appController.model.previousView.get() == ViewTypes.GAME) {
            gameController.clearGame()
        }

        playerOneSettingsModel.commit()
        playerTwoSettingsModel.commit()
        appController.changeViewTo(GameView::class)

        fire(StartGameRequest(playerOneSettingsModel.item, playerTwoSettingsModel.item))
    }
    
    val hasHumanPlayer = Bindings.createBooleanBinding(Callable { playerOneSettings.isHuman || playerTwoSettings.isHuman }, playerOneSettings.type, playerTwoSettings.type)
}
