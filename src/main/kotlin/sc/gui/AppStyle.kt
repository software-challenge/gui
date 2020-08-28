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
        private val rounding = multi(box(10.percent))

        val fullWidth by cssclass()

        val undeployedPiece by cssclass()
        val fieldUnplaceable by cssclass()
        val hoverColor by cssclass()

        val colorRED by cssclass()
        val borderRED by cssclass()
        val colorBLUE by cssclass()
        val borderBLUE by cssclass()
        val colorGREEN by cssclass()
        val borderGREEN by cssclass()
        val colorYELLOW by cssclass()
        val borderYELLOW by cssclass()



    }

    init {
        root {
            font = gotuRegular
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

        undeployedPiece {
            borderRadius = rounding
            backgroundRadius = rounding
            borderWidth = multi(box(2.px))
        }
        hoverColor {
            backgroundColor += Color.LIGHTGRAY
        }
        fieldUnplaceable {
            backgroundColor += Color.BLACK
        }

        colorRED {
            backgroundColor += Color.RED
        }
        borderRED {
            borderColor += box(Color.RED)
        }

        colorBLUE {
            backgroundColor += Color.BLUE
        }
        borderBLUE {
            borderColor += box(Color.BLUE)
        }

        colorGREEN {
            backgroundColor += Color.GREEN
        }
        borderGREEN {
            borderColor += box(Color.GREEN)
        }

        colorYELLOW {
            backgroundColor += Color.GOLD
        }
        borderYELLOW {
            borderColor += box(Color.GOLD)
        }
    }
}