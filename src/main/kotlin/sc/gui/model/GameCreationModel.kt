package sc.gui.model

import sc.gui.view.GameCreationView
import tornadofx.*
import java.io.File

enum class PlayerType {
    HUMAN {
        override fun toString(): String {
            return "Mensch"
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
    val playerName1: String by property<String>()
    val playerName2: String by property<String>()
    val selectedPlayerType1: PlayerType by property<PlayerType>()
    val selectedPlayerType2: PlayerType by property<PlayerType>()
    val playerJarFile1: File by property<File>()
    val playerJarFile2: File by property<File>()
}

class GameCreationModel : ItemViewModel<GameCreation>() {
    var playerName1 = bind(GameCreation::playerName1)
    var playerName2 = bind(GameCreation::playerName2)
    var selectedPlayerType1 = bind(GameCreation::selectedPlayerType1)
    var selectedPlayerType2 = bind(GameCreation::selectedPlayerType2)
    var playerJarFile1 = bind(GameCreation::playerJarFile1)
    var playerJarFile2 = bind(GameCreation::playerJarFile2)

    init {
        playerName1.value = "Spieler 1"
        playerName2.value = "Spieler 2"
        selectedPlayerType1.value = PlayerType.HUMAN
        selectedPlayerType2.value = PlayerType.HUMAN
    }
}
