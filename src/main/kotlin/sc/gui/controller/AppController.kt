package sc.gui.controller

import javafx.beans.value.WritableValue
import mu.KLogging
import sc.gui.GameReadyEvent
import sc.gui.model.AppModel
import sc.gui.model.ViewType
import sc.gui.model.ViewType.*
import sc.gui.view.*
import tornadofx.Controller
import tornadofx.FXEvent
import tornadofx.task

object NavigateBackEvent: FXEvent()
object CreateGame: FXEvent()

class AppController: Controller() {
	val model = AppModel()
	private val clientController: ClientController by inject()
	
	init {
		subscribe<NavigateBackEvent> {
			changeViewTo(model.previousView.get())
		}
		subscribe<StartGameRequest> { event ->
			changeViewTo(GAME_LOADING)
            task(daemon = true) {
				clientController.startGame(arrayOf(event.playerOneSettings, event.playerTwoSettings))
			}
		}
		subscribe<GameReadyEvent> {
			changeViewTo(GAME)
		}
		subscribe<CreateGame> {
			if(model.currentView.get() != GAME_CREATION)
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

fun WritableValue<Boolean>.toggle() {
	value = !value
}

val ViewType.view
	get() = when(this) {
		START -> StartView::class
		GAME_CREATION -> GameCreationView::class
        GAME_LOADING -> GameLoadingView::class
		GAME -> GameView::class
	}
