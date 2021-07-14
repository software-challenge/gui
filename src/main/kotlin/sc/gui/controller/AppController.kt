package sc.gui.controller

import mu.KLogging
import sc.gui.GameReadyEvent
import sc.gui.model.AppModel
import sc.gui.model.GameModel
import sc.gui.model.ViewType
import sc.gui.model.ViewType.*
import sc.gui.view.*
import sc.util.toggle
import tornadofx.*

object NavigateBackEvent: FXEvent()
object CreateGame: FXEvent()

class AppController: Controller() {
    val model = AppModel()
    private val gameModel: GameModel by inject()
    private val clientController: ClientController by inject()
    
    init {
        subscribe<NavigateBackEvent> {
            changeViewTo(model.previousView.get())
        }
        subscribe<StartGameRequest> { event ->
            changeViewTo(GAME_LOADING)
            gameModel.playerNames.setAll(event.settings.map { it.name.value })
            task(daemon = true) {
                clientController.startGame(event.settings)
            }
        }
        subscribe<GameReadyEvent> {
            changeViewTo(GAME)
        }
        subscribe<CreateGame> {
            if (model.currentView.get() != GAME_CREATION)
                changeViewTo(GAME_CREATION)
        }
        subscribe<TerminateGame> {
            changeViewTo(GAME_CREATION)
        }
    }
    
    fun changeViewTo(newView: ViewType) {
        val current = model.currentView.get()
        logger.debug("Requested View change from ${current.name} -> $newView")
        if (current == newView) {
            logger.warn("Noop view change request!")
            return
        }
        find(current.view).replaceWith(newView.view)
        model.previousView.set(current)
        model.currentView.set(newView)
    }
    
    fun toggleDarkmode() {
        model.isDarkMode.toggle()
    }
    
    companion object: KLogging()
}

val ViewType.view
    get() = when (this) {
        START -> StartView::class
        GAME_CREATION -> GameCreationView::class
        GAME_LOADING -> GameLoadingView::class
        GAME -> GameView::class
    }
