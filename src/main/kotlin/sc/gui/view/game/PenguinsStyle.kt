package sc.gui.view.game

import javafx.geometry.Side
import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundSize
import sc.gui.AppStyle.Companion.background
import tornadofx.*

class PenguinsStyle: Stylesheet() {
    
    private val resources = ResourceLookup(this)
    
    private val colorBackground = c("#0ec9ff")
    
    init {
        background {
            backgroundImage += resources.url("/penguins/background.jpg").toURI()
            backgroundSize += BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
            backgroundPosition += BackgroundPosition(Side.LEFT, .0, true, Side.TOP, -10.0, false)
        }
        
        // Game
        arrayOf("fish", "ice").forEach {
            select(CssRule.c(it)) { image = resources.url("/penguins/$it.png").toURI() }
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
