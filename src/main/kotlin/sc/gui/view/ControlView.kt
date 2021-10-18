package sc.gui.view

import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
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
                button {
                    disableWhen(gameModel.currentTurn.isEqualTo(0))
                    text = "⏮"
                    setOnMouseClicked {
                        if (gameModel.atLatestTurn.value)
                            fire(PauseGame(true))
                        fire(StepGame(-1))
                    }
                }
                label {
                    alignment = Pos.CENTER
                    prefWidth = AppStyle.fontSizeRegular.value * 7
                    textProperty().bind(
                            arrayOf<ObservableValue<Number>>(gameModel.currentTurn, gameModel.availableTurns).binding
                            { (cur, all) -> "Zug " + if (cur != all || gameModel.gameEnded.value) "$cur/$all" else cur }
                    )
                }
                button {
                    disableProperty().bind(
                            arrayOf<ObservableValue<Boolean>>(gameModel.atLatestTurn, gameModel.isHumanTurn, gameModel.gameEnded).booleanBinding
                            { (latest, human, end) -> logger.trace("latest: $latest, human: $human, end: $end"); latest && (human || end) }
                    )
                    text = "⏭"
                    setOnMouseClicked {
                        fire(StepGame(1))
                        if (gameControlState.value == START)
                            gameControlState.value = PAUSED
                    }
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
            if (it && gameControlState.value != PAUSED) {
                gameControlState.value = PLAYING
                gameControlState.value = null
            }
        }
        arrayOf<ObservableValue<Boolean>>(gameModel.atLatestTurn, gameModel.gameEnded).listen { (latestTurn, end) ->
            if (latestTurn && end) gameControlState.value = FINISHED
        }
    }
    
    /** Encapsulates the different actions of the GameControlButton.
     * @param action the event to fire when this state is invoked */
    enum class GameControlState(val text: String, val action: FXEvent) {
        START("Start", PauseGame(false)),
        PLAYING("Anhalten", PauseGame(true)),
        PAUSED("Weiter", PauseGame(false)),
        FINISHED("Spiel beenden", TerminateGame());
    }
}
