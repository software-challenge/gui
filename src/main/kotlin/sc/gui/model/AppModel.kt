package sc.gui.model

import sc.gui.view.AppView
import tornadofx.*

enum class ViewTypes {
    GAME_CREATION,
    GAME,
    START
}

class AppModel : ItemViewModel<AppView>() {
    private var isDarkMode: Boolean by property(true)
	
    private var currentView: ViewTypes by property(ViewTypes.START)
    private var previousView: ViewTypes by property(ViewTypes.START)
    
    fun isDarkModeProperty() = getProperty(AppModel::isDarkMode)

    fun currentViewProperty() = getProperty(AppModel::currentView)
    fun previousViewProperty() = getProperty(AppModel::previousView)
}