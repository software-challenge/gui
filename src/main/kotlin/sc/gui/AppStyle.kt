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
        
        private val colorBackground = c("#36d2ff") //c("#0ec9ff")
        
        const val pieceOpacity = 1.0
        
        val fontSizeRegular = Font.getDefault().also { logger.debug("System Font: $it") }.size.pt * 2.5
        val fontSizeBig = fontSizeRegular * 1.2
        val fontSizeHeader = fontSizeRegular * 2
        
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
            backgroundImage += resources.url("/graphics/background.png").toURI()
            backgroundSize += BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
            //backgroundPosition += BackgroundPosition(Side.LEFT, .0, true, Side.TOP, -10.0, false)
            backgroundRepeat += BackgroundRepeat.REPEAT to BackgroundRepeat.NO_REPEAT
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
        
        // Game
        gridHover {
            backgroundColor += c("#222", 0.3)
            and(hover) {
                backgroundColor += c("#222", 0.5)
            }
        }
        Team.values().forEach { team ->
            ".${team.color}" {
                val color = when(team.color) {
                    "Rot" -> "red"
                    "Blau" -> "blue"
                    else -> throw NoWhenBranchMatchedException("Illegal color ${team.color}")
                }
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
        
        arrayOf("fish", "ice").forEach {
            select(CssRule.c(it)) { image = resources.url("/graphics/$it.png").toURI() }
        }
        
        select(CssRule.c("penguin")) {
            focusColor = colorBackground
            val frames = PieceFrames("penguin", "penuin_3") { "jump_${it.padded}" }
            (0..19).forEach { frame ->
                and(CssRule.pc("idle$frame")) {
                    javaClass.getResource(frames.getIdle(frame))?.toURI()?.let { image = it }
                }
                and(CssRule.pc("move$frame")) {
                    javaClass.getResource(frames.getMove(frame))?.toURI()?.let {
                        unsafe("-fx-image", raw(PropertyHolder.toCss(it) + " !important"))
                    }
                }
                and(CssRule.pc("consume$frame")) {
                    javaClass.getResource(frames.getConsume(frame))?.toURI()?.let {
                        unsafe("-fx-image", raw(PropertyHolder.toCss(it) + " !important"))
                    }
                }
            }
        }
    }
    
    data class PieceFrames(
        val type: String,
        val prefix: String = type,
        val idlePrefix: String = "idle",
        private val move: ((Int) -> String) = { "walk_${it.padded}" },
        private val consume: ((Int) -> String) = move,
    ) {
        fun getIdle(frame: Int) = getFrame("${idlePrefix}_${frame.padded}")
        fun getMove(frame: Int) = getFrame(move(frame))
        fun getConsume(frame: Int) = getFrame(consume(frame))
        private fun getFrame(suffix: String) = "/graphics/$type/__${prefix}_$suffix.png"
    }
    
    private fun chain(vararg frames: Pair<String, Int>): ((Int) -> String) = { frame ->
        var count = frame
        var index = -1
        var frameCount: Int
        do {
            index++
            frameCount = frames[index].second
            val frameSub = frameCount.coerceAtLeast(1)
            if(count < frameSub || index == frames.lastIndex)
                break
            count -= frameSub
        } while(true)
        frames[index].first + if(frameCount < 1) "" else ("_" + count.coerceAtMost(frameCount - 1).padded)
    }
}

private val Int.padded
    get() = toString().padStart(3, '0')
