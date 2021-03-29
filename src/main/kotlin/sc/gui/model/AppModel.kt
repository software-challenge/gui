package sc.gui.model

import sc.gui.view.AppView
import tornadofx.ItemViewModel
import tornadofx.booleanProperty
import tornadofx.objectProperty

enum class ViewType {
    START,
    GAME_CREATION,
    GAME_LOADING,
    GAME,
}

class AppModel : ItemViewModel<AppView>() {
    val isDarkMode = booleanProperty(true)
    val currentView = objectProperty(ViewType.START)
    val previousView = objectProperty(ViewType.START)
}
