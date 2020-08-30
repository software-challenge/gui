package sc.gui.controller

import sc.gui.model.AppModel
import sc.gui.model.ViewTypes
import sc.gui.view.AppView
import sc.gui.view.GameCreationView
import sc.gui.view.GameView
import sc.gui.view.MasterView
import tornadofx.*
import kotlin.reflect.KClass

class AppController : Controller() {
    val model = AppModel()
    private val view: AppView by inject()

    fun <T : UIComponent> changeViewTo(nodeType: KClass<T>) {
        with(view) {
            this.root.center(nodeType)
        }
        model.currentViewProperty().set(when (nodeType) {
            GameView::class -> {
                ViewTypes.GAME
            }
            GameCreationView::class -> {
                ViewTypes.GAME_CREATION
            }
            MasterView::class -> {
                ViewTypes.START
            }
            else ->
                throw Exception("Unknown instance of View")
        })
    }
}