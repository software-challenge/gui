package sc.gui

import javafx.collections.FXCollections
import javafx.scene.Parent
import tornadofx.*

class MyController: Controller() {
    val values = FXCollections.observableArrayList("Alpha","Beta","Gamma","Delta")
    fun writeToDb(inputValue: String) {
        println("Writing $inputValue to database!")
    }
}
