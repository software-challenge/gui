package sc.gui.model

import javafx.beans.property.Property
import tornadofx.*
import java.io.File

enum class PlayerType(val description: String) {
    HUMAN("Mensch"),
    COMPUTER_SIMPLE("Zufalls-Computerspieler"),
    COMPUTER_ADVANCED("Fortgeschrittener Computerspieler"),
    COMPUTER("Eigener Computerspieler, von GUI gestartet"),
    EXTERNAL("Eigener Computerspieler, manuell gestartet");
    override fun toString() = description
    companion object {
        /** Helper to disable human player until ready. */
        fun allowedValues() = entries//.takeLast(4) // TODO automatically determine via ServiceLoader prop
    }
}

class TeamSettings(name: String? = "Team", type: PlayerType = PlayerType.HUMAN) {
    val name = objectProperty(name)
    val type = objectProperty(type)
    val executable = objectProperty<File>()
}

class TeamSettingsModel(settings: TeamSettings): ItemViewModel<TeamSettings>(settings) {
    // explicit declarations needed - see https://github.com/edvin/tornadofx2/issues/12
    val name: Property<String> = bind(TeamSettings::name)
    val type: Property<PlayerType> = bind(TeamSettings::type)
    val executable: Property<File> = bind(TeamSettings::executable)
}
