package sc.gui.controller

import javafx.beans.value.WritableValue
import org.slf4j.LoggerFactory
import sc.gui.model.AppModel
import sc.gui.model.ViewTypes
import sc.gui.view.*
import tornadofx.*
import kotlin.reflect.KClass

class AppController : Controller() {
    val model = AppModel()

    fun <T: UIComponent> changeViewTo(nodeType: KClass<T>) {
        logger.debug("Requested View change from ${model.currentView.get().name} -> $nodeType")
        find(when (model.currentView.get()) {
            ViewTypes.GAME_CREATION -> GameCreationView::class
            ViewTypes.GAME -> GameView::class
            ViewTypes.START -> StartView::class
            null -> throw NoWhenBranchMatchedException("Current view can't be null!")
        }).replaceWith(nodeType)
        model.previousView.set(model.currentView.get())
        model.currentView.set(when (nodeType) {
            GameCreationView::class -> ViewTypes.GAME_CREATION
            GameView::class -> ViewTypes.GAME
            StartView::class -> ViewTypes.START
            else -> throw NoWhenBranchMatchedException("Unknown new view: $nodeType")
        })
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