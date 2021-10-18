package sc.gui

import javafx.geometry.Side
import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.paint.Color
import javafx.scene.text.Font
import sc.plugin2022.PieceType
import tornadofx.*

class AppStyle: Stylesheet() {
    
    companion object {
        private val red = c("#AA0100")
        private val placeableRed = c("#FB0A12")
        private val blue = c("#005784")
        private val placeableBlue = c("#31A2F2")
        
        private val gotuRegular = Font.loadFont(ResourceLookup(this)["/fonts/NotoSans-Regular.ttf"], 16.0)
        
        const val spacing = 20.0
        val formSpacing = spacing / 2
        
        val fontSizeRegular = 20.pt
        val fontSizeBig = 24.pt
        val fontSizeHeader = 32.pt
        
        val background by cssclass()
        
        val fullWidth by cssclass()
        val lightColorSchema by cssclass()
        val darkColorSchema by cssclass()
        
        val statusLabel by cssclass()
        
        val hoverColor by cssclass()
        val softHoverColor by cssclass()
    }
    
    init {
        val resources = ResourceLookup(this)
        
        root {
            font = gotuRegular
            fontSize = fontSizeRegular
        }
        background {
            opacity = 0.8
            backgroundColor += c("#f2df8e")
            backgroundImage += ResourceLookup(this).url("/graphics/sea_beach.png").toURI()
            backgroundPosition += BackgroundPosition(Side.LEFT, .0, true, Side.TOP, -10.0, false)
            backgroundRepeat += BackgroundRepeat.REPEAT to BackgroundRepeat.NO_REPEAT
        }
        statusLabel {
            fontSize = fontSizeBig
            prefHeight = fontSizeBig.times(6)
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
                backgroundColor += this@lightColorSchema.baseColor
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
                backgroundColor += this@darkColorSchema.baseColor
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
    
        fullWidth {
            prefWidth = 100.percent
        }
    
        hoverColor {
            backgroundColor += c("#2225")
        }
        softHoverColor {
            backgroundColor += c("#2222")
        }
    
        arrayOf("amber", "blank").forEach {
            ".$it" { image = resources.url("/graphics/$it.png").toURI() }
        }
        PieceType.values().forEach { type ->
            val keyframe = when(type) {
                PieceType.Herzmuschel -> "cockle/keyframes/__olive_cockle_idle"
                PieceType.Moewe -> "seagull/keyframes/__seagull_idle"
                PieceType.Robbe -> "seal/keyframes/__cream_seal_idle_on_land_upright"
                PieceType.Seestern -> "starfish/keyframes/__tan_starfish_side_view_happy_idle"
            }
            (0..19).forEach {
                ".${type.name.lowercase()}:frame$it" {
                    javaClass.getResource("/graphics/${keyframe}_${it.toString().padStart(3, '0')}.png")?.toURI()?.let {
                        image = it
                    }
                    if(type != PieceType.Moewe) {
                        val scale = if(type == PieceType.Robbe) 1.4 else 1.2
                        scaleX = scale
                        scaleY = if(type == PieceType.Herzmuschel) 1.3 else scale
                    }
                }
            }
        }
    }
}