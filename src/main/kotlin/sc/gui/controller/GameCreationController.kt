package sc.gui.controller

import sc.gui.model.GameCreationModel
import sc.gui.view.GameView
import tornadofx.*

class GameCreationController : Controller() {
    var model = GameCreationModel()
    private val clientController: ClientController by inject()
    private val appController: AppController by inject()

    fun createGame() {
        println("Creating new game")
        println("TODO: handling maunell und computer") //TODO
        println("Player 1: ${model.playerName1.value}, ${model.selectedPlayerType1.value}")
        println("Selected executable: ${model.playerExecutable1.value}")
        println("Player 2: ${model.playerName2.value}, ${model.selectedPlayerType2.value}")
        println("Selected executable: ${model.playerExecutable2.value}")
        appController.changeViewTo(GameView::class)

        fire(StartGameRequest(model))
    }
}