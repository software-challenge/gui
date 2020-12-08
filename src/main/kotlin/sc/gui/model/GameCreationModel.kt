package sc.gui.model

import javafx.beans.property.Property
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
    EXTERNAL {
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
    // explicit declarations needed - see https://github.com/edvin/tornadofx2/issues/12
    val name: Property<String> = bind(TeamSettings::name)
    val type: Property<PlayerType> = bind(TeamSettings::type)
    val executable: Property<File> = bind(TeamSettings::executable)
    
    val isHuman
        get() = type.value == PlayerType.HUMAN
}
