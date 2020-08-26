package sc.gui.model

enum class ViewTypes {
    GAME_CREATION,
    GAME,
    START
}

class AppModel {
    var currentView: ViewTypes = ViewTypes.START

}