package sc.gui

import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

class MyStyle: Stylesheet() {

    companion object {
        val tackyButton by cssclass()

        private val topColor = Color.RED
        private val rightColor = Color.DARKGREEN
        private val leftColor = c("#FFA500")
        private val bottomColor = Color.PURPLE

        private val gotuRegular = Font.loadFont("file:resources/fonts/NotoSans-Regular.ttf", 16.0)


    }

    init {
        root {
            font = gotuRegular
            backgroundColor += Color.WHITESMOKE
        }
        tackyButton {
            rotate = 5.deg
            borderColor += box(topColor, rightColor, bottomColor, leftColor)
            fontSize = 20.px
        }
        label {
            fontSize = 20.px
        }
    }
}