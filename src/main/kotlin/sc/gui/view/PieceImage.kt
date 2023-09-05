package sc.gui.view

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.beans.value.ObservableDoubleValue
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.util.Duration
import mu.KotlinLogging
import sc.gui.model.AppModel
import sc.util.listenImmediately
import tornadofx.*
import java.lang.ref.WeakReference
import kotlin.random.Random

private val logger = KotlinLogging.logger { }

val animationDuration = Duration.seconds(0.1)
val transitionDuration = animationDuration.multiply(8.0)
val animationInterval = timeline {
    cycleCount = Animation.INDEFINITE
    this += KeyFrame(animationDuration, {
        animationFunctions.removeIf {
            // Remove invalid WeakReferences
            it.get()?.let {
                it()
                false
            } ?: true
        }
    })
}
private val animationFunctions = ArrayList<WeakReference<() -> Unit>>()

// this custom class is required to be able to shrink upsized images back to smaller sizes
// see: https://stackoverflow.com/a/35202191/9127322
class ResizableImageView(sizeProperty: ObservableValue<Number>): ImageView() {
    init {
        fitWidthProperty().bind(sizeProperty)
        isPreserveRatio = true
    }
    
    override fun minHeight(width: Double): Double = 16.0
    override fun minWidth(height: Double): Double = 16.0
    override fun isResizable(): Boolean = true
    
    override fun toString(): String =
            "${styleClass.joinToString(".")}@${Integer.toHexString(hashCode())}${pseudoClassStates.joinToString("") { ":$it" }}"
}

/** Holds a potentially animated piece on a position on the board.
 * Can stack multiple images and will resize automatically. */
class PieceImage(private val sizeProperty: ObservableDoubleValue, private val content: String): StackPane() {
    private val animateFn = ::animate
    
    init {
        alignment = Pos.BOTTOM_CENTER
        addChild(content)
        animationFunctions.add(WeakReference(animateFn))
        viewOrder = 1.0
    }
    
    val frameCount = 20
    var frame = Random.nextInt(1, frameCount)
    fun animate() {
        if((AppModel.animate.value || frame > 0) && !hasClass("inactive"))
            frame = nextFrame()
    }
    
    fun nextFrame(prefix: String = "idle", oldFrame: Int = frame, randomize: Boolean = true, remove: Boolean = false): Int {
        val img = children.lastOrNull()
        img?.removePseudoClass("$prefix$oldFrame")
        return if(!remove)
            (oldFrame.inc() + if(randomize) Random.nextInt(1, 5).div(5) else 0)
                    .mod(frameCount).also { newFrame ->
                        img?.addPseudoClass("$prefix$newFrame")
                    }
        else -1
    }
    
    fun addChild(graphic: String, index: Int? = null) {
        logger.trace { "$this: Adding $graphic" }
        children.add(index ?: children.size, ResizableImageView(sizeProperty).apply {
            addClass(graphic)
            if(graphic == "penguin")
                sizeProperty.listenImmediately {
                    this.translateY = -it.toDouble() / 5
                }
        })
    }
    
    override fun toString(): String =
            "$content@${Integer.toHexString(hashCode())}" +
            pseudoClassStates.joinToString("") { ":$it" } +
            children
}

