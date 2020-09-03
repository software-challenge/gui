package sc.gui.model

import sc.gui.view.AppView
import sc.gui.view.GameCreationView
import sc.gui.view.GameView
import sc.gui.view.StartView
import sc.plugin2020.Game
import tornadofx.*
import kotlin.reflect.KClass

enum class ViewTypes {
    GAME_CREATION,
    GAME,
    START
}

class AppModel : ItemViewModel<AppView>() {
    private var currentView: ViewTypes by property(ViewTypes.START)
    private var previousView: ViewTypes by property(ViewTypes.START)
    private var isDarkMode: Boolean by property(false)

    fun currentViewProperty() = getProperty(AppModel::currentView)
    fun previousViewProperty() = getProperty(AppModel::previousView)

    fun isDarkModeProperty() = getProperty(AppModel::isDarkMode)
}