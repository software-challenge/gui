package sc.gui.model

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*
import java.util.prefs.Preferences

enum class ViewType {
    START,
    GAME_CREATION,
    GAME_LOADING,
    GAME,
}

// TODO this shouldn't be global, only for GuiApp
object AppModel: Component() {
    val currentView = objectProperty(ViewType.START)
    
    val darkMode = configurableBooleanProperty("dark", true)
    val animate = configurableBooleanProperty("animate", true)
    
    fun save() {
        preferences {
            save(darkMode)
            save(animate)
        }
    }
}

fun Component.configurableBooleanProperty(key: String, default: Boolean): BooleanProperty {
    var value = default
    preferences { value = getBoolean(key, default) }
    return SimpleBooleanProperty(this, key, value)
}

fun Preferences.save(prop: ReadOnlyBooleanProperty) = putBoolean(prop.name, prop.value)