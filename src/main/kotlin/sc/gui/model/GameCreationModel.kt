package sc.gui.model

import sc.gui.view.GameCreationView
import tornadofx.*

enum class PlayerType {
    PLAYER {
        override fun toString(): String {
            return "Player"
        }
    },
    MANUELL {
        override fun toString(): String {
            return "Manuell"
        }
    },
    COMPUTER {
        override fun toString(): String {
            return "Computer"
        }
    };
}

class GameCreation : ItemViewModel<GameCreationView>() {
    val name: String by property<String>()
    val selectedPlayerType1: PlayerType by property<PlayerType>()
    val selectedPlayerType2: PlayerType by property<PlayerType>()
    override fun toString() = name
}

class GameCreationModel : ItemViewModel<GameCreation>() {
    val name = bind(GameCreation::name)
    var selectedPlayerType1 = bind(GameCreation::selectedPlayerType1)
    val selectedPlayerType2 = bind(GameCreation::selectedPlayerType2)
}
