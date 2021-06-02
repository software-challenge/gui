package sc.gui

import javafx.geometry.Side
import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

class AppStyle: Stylesheet() {
    
    companion object {
        val background by cssclass()
        val tackyButton by cssclass()
        
        private val red = c("#AA0100")
        private val placeableRed = c("#FB0A12")
        private val blue = c("#005784")
        private val placeableBlue = c("#31A2F2")
        
        private val gotuRegular = Font.loadFont(ResourceLookup(this)["/fonts/NotoSans-Regular.ttf"], 16.0)
        private val rounding = multi(box(8.percent))
        
        val fullWidth by cssclass()
        val lightColorSchema by cssclass()
        val darkColorSchema by cssclass()
        
        val statusLabel by cssclass()
        
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
    }
    
    init {
        root {
            font = gotuRegular
        }
        background {
            opacity = 0.6
            backgroundImage.addAll(arrayOf("beach", "sea_beach")
                    .map { ResourceLookup(this).url("/graphics/$it.png").toURI() })
            backgroundPosition = multi(
                    BackgroundPosition(Side.LEFT, .5, true, Side.BOTTOM, .0, true),
                    BackgroundPosition(Side.LEFT, .5, true, Side.TOP, .0, true),
            )
            backgroundRepeat = multi(
                    BackgroundRepeat.NO_REPEAT to BackgroundRepeat.REPEAT,
                    BackgroundRepeat.NO_REPEAT to BackgroundRepeat.NO_REPEAT,
            )
            val contain = BackgroundSize(1.0, BackgroundSize.AUTO, true, true, true, true)
            backgroundSize = multi(contain, contain)
        }
        statusLabel {
            fontSize = 24.pt
        }
    
        lightColorSchema {
            baseColor = c("#E0E0E0")
            backgroundColor += c("#EEE")
            accentColor = Color.MEDIUMPURPLE
            faintFocusColor = baseColor
            
            menuBar {
                backgroundColor = this@lightColorSchema.backgroundColor
            }
            contextMenu {
                backgroundColor += baseColor
            }
        }
        darkColorSchema {
            baseColor = c("#444")
            backgroundColor += c("#222")
            accentColor = Color.MEDIUMPURPLE
            faintFocusColor = baseColor
            textFill = c("#EEE")
    
            menuBar {
                backgroundColor = this@darkColorSchema.backgroundColor
            }
            contextMenu {
                backgroundColor += c("#555")
            }
            textField {
                baseColor = Color.WHITE
                textFill = c("#222")
            }
        }
        
        button {
            backgroundRadius = multi((box(1.percent)))
            borderRadius = multi((box(1.percent)))
        }
        
        tackyButton {
            rotate = 5.deg
            borderColor += box(red, blue)
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
    }
}