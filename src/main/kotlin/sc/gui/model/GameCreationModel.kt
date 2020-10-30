package sc.gui.model

import tornadofx.*
import java.io.File

enum class PlayerType {
    HUMAN {
        override fun toString(): String {
            return "Mensch"
        }
    },
    COMPUTER_EXAMPLE {
        override fun toString(): String {
            return "Beispiel-Computerspieler"
        }
    },
    COMPUTER {
        override fun toString(): String {
            return "eigener Computerspieler, von GUI gestartet"
        }
    },
    MANUAL {
        override fun toString(): String {
            return "eigener Computerspieler, manuell gestaret"
        }
    };
}

class TeamSettings {
    val name = objectProperty("Team")
    val type = objectProperty(PlayerType.HUMAN)
    val executable = objectProperty<File>()
	
    val isHuman
        get() = type.value == PlayerType.HUMAN
}

class TeamSettingsModel(settings: TeamSettings) : ItemViewModel<TeamSettings>(settings) {
    val name = bind(TeamSettings::name)
    val type = bind(TeamSettings::type)
    val executable = bind(TeamSettings::executable)
    
    val isHuman
        get() = type.value == PlayerType.HUMAN
}
