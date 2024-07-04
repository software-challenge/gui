package sc.gui

import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import org.slf4j.LoggerFactory
import sc.api.plugins.Team
import sc.gui.model.AppModel
import tornadofx.*

class AppStyle: Stylesheet() {
    
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        
        private val resources = ResourceLookup(this)
        
        const val pieceOpacity = 1.0
        
        val fontSizeRegular = Font.getDefault().also { logger.debug("System Font: $it") }.size.pt * AppModel.scaling.value
        val fontSizeSmall = fontSizeRegular * 0.7
        val fontSizeBig = fontSizeRegular * 1.2
        val fontSizeHeader = fontSizeBig * 1.5
        
        val spacing = fontSizeRegular.value
        val formSpacing = spacing / 2
        val miniSpacing = formSpacing / 4
        
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
            block(Theme.LIGHT)
        }
        darkColorSchema {
            block(Theme.DARK)
        }
    }
    
    data class Theme(val isDark: Boolean, val base: Color, val background: Color) {
        val textColor: Color
            get() = background.invert()
        companion object {
            val LIGHT = Theme(false, c("#CCC"), c("#DDD"))
            val DARK = Theme(true, c("#222"), c("#111"))
        }
    }
    
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
            textFill = it.textColor
            textField {
                textFill = it.textColor
                backgroundColor += it.background
            }
        }
        
        root {
            fontFamily = "Raleway"
            fontSize = fontSizeRegular
            accentColor = Color.MEDIUMPURPLE
        }
        
        // Generic Components
        button {
            backgroundRadius = multi((box(fontSizeRegular)))
            borderRadius = backgroundRadius
        }
        ".small" {
            fontSize = fontSizeSmall
        }
        label.theme { theme ->
            effect = DropShadow(formSpacing, theme.base).apply {
                spread = 0.9
                blurType = BlurType.TWO_PASS_BOX
            }
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
        
        huiStyles()
    }
    
    fun huiStyles() {
        background {
            opacity = 0.5
            backgroundImage += resources.url("/hui/background_very_simple.png").toURI()
            backgroundRepeat += BackgroundRepeat.REPEAT to BackgroundRepeat.REPEAT
        }
    }
    
    fun ostseeschachStyles() {
        val colorBackground = c("#36d2ff")
        
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
        
        background {
            opacity = 0.7
            backgroundColor += colorBackground
        }
    }
    
    fun mqStyles() {
        background {
            opacity = 0.7
            backgroundColor += c("#2a9b46")
            backgroundImage += resources.url("/mq/fields/background/background.png").toURI()
            backgroundRepeat += BackgroundRepeat.REPEAT to BackgroundRepeat.REPEAT
        }
        
        (0 until 12).forEach {
            ".passenger${it % 6}${it / 6}" {
                image = resources.url("/mq/fields/islands/passenger_island_${(97 + (it % 6)).toChar()}_${it / 6}.png")
                    .toURI()
            }
        }
        ".goal" { image = resources.url("/mq/fields/goal.png").toURI() }
        
        ".island1" { image = resources.url("/mq/fields/islands/empty_island_A.png").toURI() }
        ".island2" { image = resources.url("/mq/fields/islands/empty_island_B.png").toURI() }
        ".island3" { image = resources.url("/mq/fields/islands/empty_island_D.png").toURI() }
        
        select(CssRule.c("water")) {
            image = resources.url("/mq/fields/water_textures/water_A.png").toURI()
            (0..19).forEach { frame ->
                and(CssRule.pc("idle$frame")) {
                    image = resources.url("/mq/fields/water_textures/water_${(frame.div(5) + 65).toChar()}.png").toURI()
                }
            }
        }
        select(CssRule.c("stream")) {
            (0..19).forEach { frame ->
                and(CssRule.pc("idle$frame")) {
                    image = resources.url("/mq/fields/stream_${(frame.div(4).mod(2) + 65).toChar()}.png").toURI()
                }
            }
        }
        
        ".border" { image = resources.url("/mq/fields/background/border_vertical.png").toURI() }
        ".border_inner" { image = resources.url("/mq/fields/background/border_inner_corner.png").toURI() }
        ".border_outer" { image = resources.url("/mq/fields/background/border_outer_corner.png").toURI() }
        ".fog" { image = resources.url("/mq/fields/background/fog_tile.png").toURI() }
        ".fog_border" { image = resources.url("/mq/fields/background/fog_border_beach_vertical.png").toURI() }
        ".fog_border_inner" { image = resources.url("/mq/fields/background/fog_border_beach_inner_corner.png").toURI() }
        ".fog_border_outer" { image = resources.url("/mq/fields/background/fog_border_beach_outer_corner.png").toURI() }
        ".fog_water_border" { image = resources.url("/mq/fields/background/fog_border_vertical.png").toURI() }
        ".fog_water_border_inner" { image = resources.url("/mq/fields/background/fog_border_inner_corner.png").toURI() }
        ".fog_water_border_outer" { image = resources.url("/mq/fields/background/fog_border_outer_corner.png").toURI() }
        
        arrayOf("ship_one", "ship_two").forEach {
            select(CssRule.c(it)) { image = resources.url("/mq/boats/$it.png").toURI() }
            "ab".forEach { ch ->
                select(CssRule.c(it + "_passenger_$ch")) {
                    image = resources.url("/mq/boats/passengers/passenger_${ch}_$it.png").toURI()
                }
            }
        }
        arrayOf("half_speed", "full_speed").forEach { speed ->
            arrayOf("smoke", "waves").forEach { asset ->
                val full = asset + "_" + speed
                select(CssRule.c(full)) {
                    image = resources.url("/mq/boats/$full.png").toURI()
                }
            }
        }
        (1..6).forEach {
            select(CssRule.c("coal$it")) { image = resources.url("/mq/boats/coal/coal_$it.png").toURI() }
        }
    }
}
