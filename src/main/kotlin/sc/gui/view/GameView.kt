package sc.gui.view

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.effect.Glow
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Region
import javafx.util.Duration
import mu.KotlinLogging
import sc.api.plugins.IGameState
import sc.api.plugins.IMove
import sc.gui.AppStyle
import sc.gui.controller.HumanMoveAction
import sc.gui.model.GameModel
import tornadofx.*
import java.util.ServiceLoader

class GameView: View() {
    override val root = borderpane {
        paddingAll = AppStyle.spacing
        top(StatusView::class)
        // TODO allow selection? Move boards into plugins?
        center = ServiceLoader.load(GameBoard::class.java).findFirst().get().root
        bottom(ControlView::class)
    }
}

@Suppress("UNCHECKED_CAST")
abstract class GameBoard<GameState: IGameState>: View(), ChangeListener<IGameState?> {
    protected val logger = KotlinLogging.logger { }
    
    protected val gameModel: GameModel by inject()
    protected val gameState: GameState?
        get() = gameModel.gameState.value as? GameState
    
    protected val awaitingHumanMove =
        gameModel.isHumanTurn.booleanBinding(gameModel.atLatestTurn) {
            (gameModel.atLatestTurn.value && gameModel.isHumanTurn.value).also {
                logger.trace { "Awaiting Human Turn: $it"}
            }
        }
    
    override fun changed(observable: ObservableValue<out IGameState>?, oldValue: IGameState?, newValue: IGameState?) {
        onNewState(oldValue as? GameState, newValue as? GameState)
    }
    
    abstract fun onNewState(oldState: GameState?, state: GameState?)
    
    abstract override val root: Region
    protected val viewHeight: Double
        get() = (root.parent as? Region ?: root).height
            .coerceAtMost(root.scene?.height?.minus(AppStyle.fontSizeBig.value * 12) ?: Double.MAX_VALUE)
    protected val squareSize = doubleProperty(16.0)
    
    /** Shorter animations when game speed is higher.
     * Animations should be finished within 2 times this value. */
    protected val animFactor
        get() = 3 / gameModel.stepSpeed.value
    
    protected val contrastFactor = 0.5
    protected fun Node.glow(factor: Number = 1) {
        val glow: Glow = effect.let {
            it as? Glow ?: Glow().also { effect = it }
        }
        timeline {
            keyframe(Duration.ZERO) {
                keyvalue(
                    glow.levelProperty(),
                    glow.level
                )
            }
            keyframe(Duration.seconds(animFactor / 2)) {
                keyvalue(
                    glow.levelProperty(),
                    contrastFactor * factor.toDouble()
                )
            }
        }
    }
    
    init {
        Platform.runLater {
            squareSize.bind(Bindings.min(root.widthProperty(), root.heightProperty().doubleBinding { viewHeight }))
            gameModel.gameState.addListener(this)
            this.changed(null, null, gameModel.gameState.value)
        }
        
        Platform.runLater {
            awaitingHumanMove.addListener { _ ->
                Platform.runLater {
                    checkHumanControls()
                }
            }
            
            root.scene.setOnKeyPressed { keyEvent ->
                val state = gameState ?: return@setOnKeyPressed
                if(handleKeyPress(state, keyEvent)) {
                    keyEvent.consume()
                }
            }
        }
    }
    
    protected abstract fun handleKeyPress(state: GameState, keyEvent: KeyEvent): Boolean
    
    protected fun checkHumanControls() {
        if(awaitingHumanMove.value)
            renderHumanControls(gameState ?: return)
    }
    
    protected abstract fun renderHumanControls(state: GameState)
    
    protected fun sendHumanMove(move: IMove): Boolean {
        if(awaitingHumanMove.value) {
            fire(HumanMoveAction(move.also { logger.debug("Human Move: {}", it) }))
            return true
        }
        return false
    }
    
}
