package sc.gui.controller

import javafx.beans.value.WritableValue
import org.slf4j.LoggerFactory
import sc.gui.model.AppModel
import sc.gui.model.ViewType
import sc.gui.view.*
import tornadofx.*

class AppController: Controller() {
	val model = AppModel()
	
	fun changeViewTo(newView: ViewType) {
		val current = model.currentView.get()
		logger.debug("Requested View change from ${current.name} -> $newView")
		find(current.view).replaceWith(newView.view)
		model.previousView.set(current)
		model.currentView.set(newView)
	}
	
	fun toggleDarkmode() {
		model.isDarkMode.toggle()
	}
	
	companion object {
		private val logger = LoggerFactory.getLogger(AppController::class.java)
	}
}

fun WritableValue<Boolean>.toggle() {
	value = !value
}

val ViewType.view
	get() = when(this) {
		ViewType.GAME_CREATION -> GameCreationView::class
		ViewType.GAME -> GameView::class
		ViewType.START -> StartView::class
	}