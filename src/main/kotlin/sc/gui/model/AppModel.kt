package sc.gui.model

import sc.gui.view.AppView
import tornadofx.*

enum class ViewType {
    GAME_CREATION,
    GAME,
    START
}

class AppModel : ItemViewModel<AppView>() {
    val isDarkMode = booleanProperty(true)
    val currentView = objectProperty(ViewType.START)
    val previousView = objectProperty(ViewType.START)
}