package sc.gui.model

import sc.gui.view.GameCreationView
import tornadofx.*

class GameCreation : ItemViewModel<GameCreationView>() {
    val name: String by property<String>()

    override fun toString() = name
}

class GameCreationModel : ItemViewModel<GameCreation>() {
    val name = bind(GameCreation::name)
}
