package sc.gui

import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

class AppStyle : Stylesheet() {

    companion object {
        val tackyButton by cssclass()

        private val topColor = Color.RED
        private val rightColor = Color.DARKGREEN
        private val leftColor = c("#FFA500")
        private val bottomColor = Color.PURPLE

        private val gotuRegular = Font.loadFont("file:resources/fonts/NotoSans-Regular.ttf", 16.0)

        val fullWidth by cssclass()
        val area by cssclass()
        val area2 by cssclass()
        val dragTarget by cssclass()


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
        fullWidth {
            prefWidth = 100.percent
        }
        area {
            backgroundColor += Color.GREENYELLOW
        }
        area2 {
            backgroundColor += Color.DARKGREEN
        }
        dragTarget {
            backgroundColor += Color.DARKGRAY
        }
    }
}