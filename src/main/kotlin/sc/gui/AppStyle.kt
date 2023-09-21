package sc.gui

import javafx.scene.effect.DropShadow
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import org.slf4j.LoggerFactory
import sc.api.plugins.Team
import tornadofx.*

class AppStyle: Stylesheet() {
    
    companion object {
        private val logger = LoggerFactory.getLogger(LobbyManager::class.java)
        
        private val resources = ResourceLookup(this)
        
        private val colorBackground = c("#36d2ff")
        
        const val pieceOpacity = 1.0
        
        val fontSizeRegular = Font.getDefault().also { logger.debug("System Font: $it") }.size.pt
        val fontSizeBig = fontSizeRegular * 1.2
        val fontSizeHeader = fontSizeBig * 1.5
        
        val spacing = fontSizeRegular.value
        val formSpacing = spacing / 2
        
        // CLASSES
        val background by cssclass()
        
        val fullWidth by cssclass()
        val lightColorSchema by cssclass()
        val darkColorSchema by cssclass()
        
        val heading by cssclass()
        val statusLabel by cssclass()
        val plainLabel by cssclass()
        
        val gridHover by csspseudoclass()
        val gridLock by csspseudoclass()
        
        fun background() =
                StackPane(
                        Region().apply {
                            hgrow = Priority.ALWAYS
                            vgrow = Priority.ALWAYS
                            addClass(AppStyle.background)
                        }
                )
        
        init {
            arrayOf("Regular", "Bold", "Italic", "BoldItalic").forEach {
                Font.loadFont(resources["/fonts/Raleway-$it.ttf"], 0.0)
            }
        }
    }
    
    private fun themed(block: CssSelectionBlock.(theme: Theme) -> Unit) {
        lightColorSchema {
            block(Theme(false, c("#CCC"), c("#DDD")))
        }
        darkColorSchema {
            block(Theme(true, c("#444"), c("#222")))
        }
    }
    
    data class Theme(val isDark: Boolean, val base: Color, val background: Color)
    
    private fun Selectable.theme(block: CssSelectionBlock.(theme: Theme) -> Unit) {
        val inner = this
        themed { theme ->
            inner {
                block(theme)
            }
        }
    }
    
    init {
        themed {
            baseColor = it.base
            backgroundColor += it.background
            faintFocusColor = it.base
        }
        menuBar.theme {
            backgroundColor += it.background
        }
        contextMenu.theme {
            backgroundColor += it.base
        }
        themed {
            textFill = it.background.invert()
            textField {
                textFill = it.background.invert()
                backgroundColor += it.background
            }
        }
        
        root {
            fontFamily = "Raleway"
            fontSize = fontSizeRegular
            accentColor = Color.MEDIUMPURPLE
        }
        background {
            opacity = 0.8
            backgroundColor += colorBackground
        }
        
        // Generic Components
        button {
            backgroundRadius = multi((box(fontSizeRegular)))
            borderRadius = backgroundRadius
        }
        label.theme { theme ->
            effect = DropShadow(AppStyle.spacing, theme.base)
        }
        label {
            and(plainLabel) {
                "-fx-effect".force("null")
            }
        }
        heading {
            fontSize = fontSizeHeader
            wrapText = true
            textAlignment = TextAlignment.CENTER
            fontWeight = FontWeight.BOLD
        }
        
        // Special Components
        legend {
            // label of GameCreationForm
            fontSize = fontSizeBig
            fontStyle = FontPosture.ITALIC
        }
        statusLabel {
            fontSize = fontSizeBig
        }
        fullWidth {
            prefWidth = 100.percent
        }
        
        Team.values().forEach { team ->
            ".${team.color}" {
                val color = team.color
                backgroundColor += c(color, 0.6).desaturate()
                and(gridHover) {
                    backgroundColor += c(color, 0.6)
                }
                and(gridLock) {
                    backgroundColor += c(color, 0.8)
                }
            }
        }
        
        CssRule.c("grid").theme {
            borderStyle += BorderStrokeStyle.DOTTED
            borderColor += box(if(it.isDark) colorBackground.brighter() else colorBackground.darker())
        }
        
        mqStyles()
    }
    
    fun mqStyles() {
        background {
            backgroundImage += resources.url("/background.jpg").toURI()
            backgroundRepeat += BackgroundRepeat.REPEAT to BackgroundRepeat.REPEAT
        }
        
        
        (0 until 12).forEach {
            ".passenger${it % 6}${it / 6}" {
                image = resources.url("/mq/fields/islands/passenger_island_${(97 + (it % 6)).toChar()}_${it / 6}.png").toURI()
            }
        }
        ".island" {
            image = resources.url("/mq/fields/islands/empty_island_A.png").toURI()
        }
        ".goal" {
            image = resources.url("/mq/fields/goal.png").toURI()
        }
        ".water" {
            image = resources.url("/mq/fields/water_textures/water_A.png").toURI()
        }
        ".stream" {
            image = resources.url("/mq/fields/stream_A.png").toURI()
        }
        arrayOf("ship_one", "ship_two").forEach {
            select(CssRule.c(it)) { image = resources.url("/mq/boats/$it.png").toURI() }
            "ab".forEach { ch ->
                select(CssRule.c(it + "_passenger_$ch")) {
                    image = resources.url("/mq/boats/passengers/passenger_${ch}_$it.png").toURI()
                }
            }
        }
        (1..6).forEach {
            select(CssRule.c("coal$it")) { image = resources.url("/mq/boats/coal/coal_$it.png").toURI() }
        }
    }
}
