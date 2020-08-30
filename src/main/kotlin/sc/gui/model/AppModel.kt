package sc.gui.model

import sc.gui.view.AppView
import tornadofx.*

enum class ViewTypes {
    GAME_CREATION,
    GAME,
    START
}

class AppModel : ItemViewModel<AppView>() {
    private var currentView: ViewTypes by property(ViewTypes.GAME_CREATION)
    private var isGame: Boolean by property(false)

    fun currentViewProperty() = getProperty(AppModel::currentView)
    fun isGameProperty() = getProperty(AppModel::isGame)

    init {
        currentViewProperty().addListener { _, _, newValue ->
            if (newValue == ViewTypes.GAME) {
                isGameProperty().set(true)
            } else {
                isGameProperty().set(false)
            }
        }
    }
}