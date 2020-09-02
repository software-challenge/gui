package sc.gui.controller

import sc.gui.model.GameCreationModel
import sc.gui.model.ViewTypes
import sc.gui.view.GameView
import tornadofx.*

class GameCreationController : Controller() {
    var model = GameCreationModel()
    private val appController: AppController by inject()
    private val gameController: GameController by inject()


    fun createGame() {
        // as we currently just support a single game at a time
        if (appController.model.previousViewProperty().get() == ViewTypes.GAME ||
                appController.model.previousViewProperty().get() == ViewTypes.GAME_ENDED) {
            gameController.clearGame()
            //TODO("Kill previous game")
        }

        println("Creating new game")
        println("Player 1: ${model.playerName1.value}, ${model.selectedPlayerType1.value}")
        println("Selected executable: ${model.playerExecutable1.value}")
        println("Player 2: ${model.playerName2.value}, ${model.selectedPlayerType2.value}")
        println("Selected executable: ${model.playerExecutable2.value}")
        appController.changeViewTo(GameView::class)

        fire(StartGameRequest(model))
    }
}