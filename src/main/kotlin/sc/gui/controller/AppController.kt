package sc.gui.controller

import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.model.AppModel
import sc.gui.model.ViewTypes
import sc.gui.view.*
import tornadofx.*
import kotlin.reflect.KClass

class AppController : Controller() {
    val model = AppModel()
    private val view: AppView by inject()

    fun <T: UIComponent> changeViewTo(nodeType: KClass<T>) {
        logger.debug("Requested View change from ${model.currentViewProperty().get().name} -> $nodeType")
        find(when (model.currentViewProperty().get()) {
            ViewTypes.GAME -> GameView::class
            ViewTypes.GAME_CREATION -> GameCreationView::class
            ViewTypes.START -> StartView::class
            else -> throw Exception("Unknown type of view")
        }).replaceWith(nodeType)
        model.previousViewProperty().set(model.currentViewProperty().get())
        model.currentViewProperty().set(when (nodeType) {
            GameView::class -> {
                view.title = "Spiele Blockus - Software-Challenge Germany"
                ViewTypes.GAME
            }
            GameCreationView::class -> {
                view.title = "Neues Spiel - Software-Challenge Germany"
                ViewTypes.GAME_CREATION
            }
            StartView::class -> {
                view.title = "Software-Challenge Germany"
                ViewTypes.START
            }
            else -> throw Exception("Unknown instance of View")
        })
    }

    fun toggleDarkmode() {
        if (model.isDarkModeProperty().get()) {
            if (view.root.hasClass(AppStyle.darkColorSchema)) {
                view.root.removeClass(AppStyle.darkColorSchema)
            }
            if (!view.root.hasClass(AppStyle.lightColorSchema)) {
                view.root.addClass(AppStyle.lightColorSchema)
            }
        } else {
            if (view.root.hasClass(AppStyle.lightColorSchema)) {
                view.root.removeClass(AppStyle.lightColorSchema)
            }
            if (!view.root.hasClass(AppStyle.darkColorSchema)) {
                view.root.addClass(AppStyle.darkColorSchema)
            }
        }
        model.isDarkModeProperty().set(!model.isDarkModeProperty().get())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BoardView::class.java)
    }
}