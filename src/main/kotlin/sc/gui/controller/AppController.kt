package sc.gui.controller

import org.slf4j.LoggerFactory
import sc.gui.model.AppModel
import sc.gui.model.ViewTypes
import sc.gui.view.*
import tornadofx.*
import kotlin.reflect.KClass

class AppController : Controller() {
    val model = AppModel()

    fun <T: UIComponent> changeViewTo(nodeType: KClass<T>) {
        logger.debug("Requested View change from ${model.currentViewProperty().get().name} -> $nodeType")
        find(when (model.currentViewProperty().get()) {
            ViewTypes.GAME_CREATION -> GameCreationView::class
            ViewTypes.GAME -> GameView::class
            ViewTypes.START -> StartView::class
            null -> throw NoWhenBranchMatchedException("Current view can't be null!")
        }).replaceWith(nodeType)
        model.previousViewProperty().set(model.currentViewProperty().get())
        model.currentViewProperty().set(when (nodeType) {
            GameCreationView::class -> ViewTypes.GAME_CREATION
            GameView::class -> ViewTypes.GAME
            StartView::class -> ViewTypes.START
            else -> throw NoWhenBranchMatchedException("Unknown new view: $nodeType")
        })
    }

    fun toggleDarkmode() {
        model.isDarkModeProperty().set(!model.isDarkModeProperty().get())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AppController::class.java)
    }
}