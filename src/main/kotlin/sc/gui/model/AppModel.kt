package sc.gui.model

import sc.gui.view.AppView
import tornadofx.*

enum class ViewTypes {
    GAME_CREATION,
    GAME,
    START
}

class AppModel : ItemViewModel<AppView>() {
    val isDarkMode = booleanProperty(true)
    val currentView = objectProperty(ViewTypes.START)
    val previousView = objectProperty(ViewTypes.START)
}