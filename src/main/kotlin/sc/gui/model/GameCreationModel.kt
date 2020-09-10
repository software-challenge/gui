package sc.gui.model

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.io.File

enum class PlayerType {
    HUMAN {
        override fun toString(): String {
            return "Mensch"
        }
    },
    INTERNAL {
        override fun toString(): String {
            return "Beispiel-Computerspieler"
        }
    },
    COMPUTER {
        override fun toString(): String {
            return "eigener Computerspieler, von GUI gestartet"
        }
    },
    MANUALLY {
        override fun toString(): String {
            return "eigener Computerspieler, manuell gestaret"
        }
    };
}

class TeamSettings {
    private var name: String by property<String>("Team")
    fun nameProperty() = getProperty(TeamSettings::name)
    private var type: PlayerType by property<PlayerType>(PlayerType.HUMAN)
    fun typeProperty() = getProperty(TeamSettings::type)
    val executableProperty = SimpleObjectProperty<File>()
    private var executable: File? by executableProperty
}

class TeamSettingsModel(settings: TeamSettings) : ItemViewModel<TeamSettings>(settings) {
    val name = bind { settings.nameProperty() }
    val type = bind { settings.typeProperty() }
    val executable = bind(TeamSettings::executableProperty)
}
