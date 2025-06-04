package sc.gui.controller

import io.github.oshai.kotlinlogging.KLogging
import sc.gui.GameReadyEvent
import sc.gui.events.*
import sc.gui.model.AppModel
import sc.gui.model.GameModel
import sc.gui.model.ViewType
import sc.gui.model.ViewType.*
import sc.gui.util.toggle
import sc.gui.view.GameCreationView
import sc.gui.view.GameLoadingView
import sc.gui.view.GameView
import tornadofx.*

object NavigateBackEvent: FXEvent()
object CreateGame: FXEvent()

class AppController: Controller() {
    val model = AppModel
    private val gameModel: GameModel by inject()
    private val clientController: ClientController by inject()
    
    init {
        subscribe<CreateGame> { changeViewTo(GAME_CREATION) }
        subscribe<StartGame> { event ->
            changeViewTo(GAME_LOADING)
            gameModel.playerNames.setAll(event.settings.map { it.name.value })
            task(daemon = true) {
                clientController.startGame(event.settings)
            }
        }
        subscribe<GameReadyEvent> { changeViewTo(GAME) }
        subscribe<TerminateGame> { if(it.close) changeViewTo(GAME_CREATION) }
    }
    
    private fun changeViewTo(newView: ViewType) {
        val current = model.currentView.get()
        logger.debug("Requested View change from ${current.name} -> $newView")
        if (current == newView) {
            logger.warn("Noop view change request!")
            return
        }
        find(current.view).replaceWith(newView.view)
        model.currentView.set(newView)
    }
    
    fun toggleDarkmode() {
        model.darkMode.toggle()
    }
    
    companion object: KLogging()
}

val ViewType.view
    get() = when (this) {
        GAME_CREATION -> GameCreationView::class
        GAME_LOADING -> GameLoadingView::class
        GAME -> GameView::class
    }
