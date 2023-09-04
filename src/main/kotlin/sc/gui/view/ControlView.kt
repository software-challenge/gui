package sc.gui.view

import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import mu.KotlinLogging
import sc.gui.AppStyle
import sc.gui.GamePausedEvent
import sc.gui.GameReadyEvent
import sc.gui.events.*
import sc.gui.model.AppModel
import sc.gui.model.GameModel
import sc.gui.view.ControlView.GameControlState.*
import sc.util.binding
import sc.util.booleanBinding
import sc.util.listen
import sc.util.listenImmediately
import tornadofx.*

class ControlView: View() {
    private val logger = KotlinLogging.logger {}
    
    private val gameModel: GameModel by inject()
    private val gameControlState: Property<GameControlState> = objectProperty(START)
    
    private val MouseEvent.modifierMultiplicator
        get() = when {
            isControlDown -> 100
            isShiftDown -> 10
            else -> 1
        }
    
    override val root =
            hbox {
                alignment = Pos.CENTER
                spacing = AppStyle.formSpacing
                button {
                    prefWidth = AppStyle.fontSizeRegular.value * 15
                    gameControlState.listenImmediately { controlState ->
                        logger.debug { "GameControlState $controlState" }
                        isDisable = controlState == null
                        controlState?.let { text = it.text }
                    }
                    action {
                        isDisable = true
                        fire(gameControlState.value.action)
                    }
                }
                val prev = button {
                    disableWhen(gameModel.currentTurn.isEqualTo(0))
                    text = "◀" //"⏮"
                    setOnMouseClicked {
                        if(gameModel.atLatestTurn.value)
                            fire(PauseGame(true))
                        fire(StepGame(-1 * it.modifierMultiplicator))
                        it.consume()
                    }
                    setOnAction {
                        fire(StepGame(1))
                    }
                }
                label {
                    alignment = Pos.CENTER
                    prefWidth = AppStyle.fontSizeRegular.value * 7
                    textProperty().bind(
                            arrayOf<ObservableValue<Number>>(gameModel.currentTurn, gameModel.availableTurns).binding
                            { (cur, all) -> "Zug " + if(cur != all || gameModel.gameOver.value) "$cur/$all" else cur }
                    )
                }
                button {
                    disableProperty().bind(
                            arrayOf<ObservableValue<Boolean>>(gameModel.atLatestTurn, gameModel.isHumanTurn, gameModel.gameOver).booleanBinding
                            { (latest, human, end) ->
                                logger.trace { "latest: $latest, human: $human, end: $end" }
                                (latest && (human || end)).also {
                                    if(it && isFocused)
                                        prev.requestFocus()
                                }
                            }
                    )
                    text = "▶" //"⏭"
                    setOnMouseClicked {
                        fire(StepGame(it.modifierMultiplicator))
                        if(gameControlState.value == START) gameControlState.value = PAUSED
                        //it.consume()
                    }
                    setOnAction {
                        fire(StepGame(1))
                    }
                }
                group {
                    svgpath("M75.694 480a48.02 48.02 0 0 1-42.448-25.571C12.023 414.3 0 368.556 0 320 0 160.942 128.942 32 288 32s288 128.942 288 288c0 48.556-12.023 94.3-33.246 134.429A48.018 48.018 0 0 1 500.306 480H75.694zM512 288c-17.673 0-32 14.327-32 32 0 17.673 14.327 32 32 32s32-14.327 32-32c0-17.673-14.327-32-32-32zM288 128c17.673 0 32-14.327 32-32 0-17.673-14.327-32-32-32s-32 14.327-32 32c0 17.673 14.327 32 32 32zM64 288c-17.673 0-32 14.327-32 32 0 17.673 14.327 32 32 32s32-14.327 32-32c0-17.673-14.327-32-32-32zm65.608-158.392c-17.673 0-32 14.327-32 32 0 17.673 14.327 32 32 32s32-14.327 32-32c0-17.673-14.327-32-32-32zm316.784 0c-17.673 0-32 14.327-32 32 0 17.673 14.327 32 32 32s32-14.327 32-32c0-17.673-14.327-32-32-32zm-87.078 31.534c-12.627-4.04-26.133 2.92-30.173 15.544l-45.923 143.511C250.108 322.645 224 350.264 224 384c0 35.346 28.654 64 64 64 35.346 0 64-28.654 64-64 0-19.773-8.971-37.447-23.061-49.187l45.919-143.498c4.039-12.625-2.92-26.133-15.544-30.173z") {
                        // from tachometer.svg, see https://edencoding.com/svg-javafx/#svg-as-image and https://www.tutorialspoint.com/javafx/2dshapes_svgpath.htm
                        this.scaleY = 0.1
                        this.scaleX = 0.1
                    }
                    this.hboxConstraints { this.hGrow = Priority.NEVER }
                }
                spinner(
                        min = 0.0,
                        max = 99.0,
                        initialValue = gameModel.stepSpeed.value,
                        amountToStepBy = 2.0,
                        editable = true,
                        property = gameModel.stepSpeed,
                        enableScroll = true,
                ) {
                    // TODO unfocus on normal character typed
                    prefWidth = AppStyle.fontSizeRegular.value * 6
                }
                checkbox("Animationen", AppModel.animate)
            }
    
    init {
        subscribe<GameReadyEvent> {
            gameControlState.value = START
        }
        subscribe<GamePausedEvent> { event ->
            gameControlState.value = when {
                event.paused -> PAUSED
                gameModel.isHumanTurn.value -> null
                else -> PLAYING
            }
        }
        gameModel.isHumanTurn.onChange {
            if(it && gameControlState.value != PAUSED) {
                gameControlState.value = PLAYING
                gameControlState.value = null
            }
        }
        arrayOf<ObservableValue<Boolean>>(gameModel.atLatestTurn, gameModel.gameOver).listen { (latestTurn, end) ->
            if(latestTurn && end) gameControlState.value = FINISHED
        }
    }
    
    /** Encapsulates the different actions of the GameControlButton.
     * @param action the event to fire when this state is invoked */
    enum class GameControlState(val text: String, val action: FXEvent) {
        START("Start", PauseGame(false)),
        PLAYING("Anhalten", PauseGame(true)),
        PAUSED("Weiter", PauseGame(false)),
        FINISHED("Spiel beenden", TerminateGame(true));
    }
}
