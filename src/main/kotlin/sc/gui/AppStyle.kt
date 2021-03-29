package sc.gui

import javafx.scene.paint.Color
import javafx.scene.text.Font
import sc.gui.view.AppView
import tornadofx.*

class AppStyle : Stylesheet() {

    companion object {
        val tackyButton by cssclass()

        private val red = c("#AA0100")
        private val placeableRed = c("#FB0A12")
        private val blue = c("#005784")
        private val placeableBlue = c("#31A2F2")
        private val green = c("#2B7200")
        private val placeableGreen = c("#55E601")
        private val yellow = c("#9E8900")
        private val placeableYellow = c("#FEDE06")

        private val gotuRegular = Font.loadFont(AppView::class.java.getResource("/fonts/NotoSans-Regular.ttf").toExternalForm(), 16.0)
        private val rounding = multi(box(8.percent))

        val fullWidth by cssclass()
        val lightColorSchema by cssclass()
        val darkColorSchema by cssclass()
        val lightBoard by cssclass()
        val darkBoard by cssclass()

        val statusLable by cssclass()

        val undeployedPiece by cssclass()
        val fieldUnplaceable by cssclass()
        val pieceUnselectable by cssclass()
        val hoverColor by cssclass()

        val colorRED by cssclass()
        val placeableRED by cssclass()
        val borderRED by cssclass()
        val colorBLUE by cssclass()
        val placeableBLUE by cssclass()
        val borderBLUE by cssclass()
        val colorGREEN by cssclass()
        val placeableGREEN by cssclass()
        val borderGREEN by cssclass()
        val colorYELLOW by cssclass()
        val placeableYELLOW by cssclass()
        val borderYELLOW by cssclass()
    }

    init {
        root {
            font = gotuRegular
        }

        lightColorSchema {
            baseColor = c("#E0E0E0")
            backgroundColor += c("#EEEEEE")
            accentColor = Color.MEDIUMPURPLE
            faintFocusColor = baseColor
            menuBar {
                backgroundColor += c("#DEDEDE")
            }
            contextMenu {
                backgroundColor += c("#E0E0E0")
            }
            statusLable {
                textFill = c("#262626")
                fontSize = 24.pt
            }
        }
        darkColorSchema {
            baseColor = c("#424242")
            backgroundColor += c("#212121")
            accentColor = Color.MEDIUMPURPLE
            faintFocusColor = baseColor

            menuBar {
                backgroundColor += c("#2c2c2c")
            }
            contextMenu {
                backgroundColor += c("#515151")
            }
            textField {
                baseColor = Color.WHITE
                textFill = c("#212121")
            }
            label {
                textFill = c("#BDBDBD")
            }
            statusLable {
                textFill = c("#E3E3E3")
                fontSize = 24.pt
            }
        }
        lightBoard {
            backgroundColor += c("#E0E0E0")
        }
        darkBoard {
            backgroundColor += c("#424242")
        }

        button {
            backgroundRadius = multi((box(1.percent)))
            borderRadius = multi((box(1.percent)))
        }

        tackyButton {
            rotate = 5.deg
            borderColor += box(red, blue, green, yellow)
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
            and(hover) {
                Color.DARKGRAY
            }
        }
        fieldUnplaceable {
            backgroundColor += Color.BLACK
        }
        pieceUnselectable {
            opacity = 0.6
        }

        colorRED {
            backgroundColor += red
        }
        placeableRED {
            backgroundColor += placeableRed
        }
        borderRED {
            borderColor += box(red)
        }

        colorBLUE {
            backgroundColor += blue
        }
        placeableBLUE {
            backgroundColor += placeableBlue
        }
        borderBLUE {
            borderColor += box(blue)
        }

        colorGREEN {
            backgroundColor += green
        }
        placeableGREEN {
            backgroundColor += placeableGreen
        }
        borderGREEN {
            borderColor += box(green)
        }

        colorYELLOW {
            backgroundColor += yellow
        }
        placeableYELLOW {
            backgroundColor += placeableYellow
        }
        borderYELLOW {
            borderColor += box(yellow)
        }
    }
}