package sc.gui.controller

import javafx.beans.value.WritableValue
import mu.KLogging
import sc.gui.model.AppModel
import sc.gui.model.ViewType
import sc.gui.view.*
import tornadofx.Controller
import tornadofx.FXEvent

class NavigateBackEvent: FXEvent()

class AppController: Controller() {
	val model = AppModel()
	
	init {
		subscribe<NavigateBackEvent> {
			changeViewTo(model.previousView.get())
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
		ViewType.START -> StartView::class
		ViewType.GAME_CREATION -> GameCreationView::class
        ViewType.GAME_LOADING -> GameLoadingView::class
		ViewType.GAME -> GameView::class
	}
