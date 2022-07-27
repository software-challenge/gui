package sc.gui.model

import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import sc.api.plugins.Team
import sc.gui.GameOverEvent
import sc.gui.NewGameState
import sc.gui.controller.CreateGame
import sc.gui.controller.HumanMoveAction
import sc.gui.controller.HumanMoveRequest
import sc.gui.events.*
import sc.plugin2023.GameState
import sc.shared.GameResult
import sc.util.booleanBinding
import tornadofx.*
import kotlin.math.max

class GameModel: ViewModel() {
    val playerNames: ObservableList<String> = FXCollections.observableArrayList()
    val gameState = objectProperty<GameState?>(null)
    val gameResult = objectProperty<GameResult>()
    
    val stepSpeed = objectProperty(5.0)
    
    val currentTurn = integerBinding(gameState) { value?.turn ?: 0 }
    val currentRound = integerBinding(gameState) { value?.round ?: 0 }
    val currentTeam = nonNullObjectBinding(gameState) { value?.currentTeam ?: Team.ONE }
    val teamScores = gameState.objectBinding { state ->
        Team.values().map { state?.getPointsForTeam(it) }
    }
    
    val isHumanTurn = booleanProperty(false)
    
    val availableTurns = intProperty(0).apply {
        currentTurn.addListener { _, _, turn ->
            set(max(turn.toInt(), get()))
        }
    }
    val atLatestTurn =
            arrayOf<ObservableValue<Number>>(currentTurn, availableTurns)
                    .booleanBinding { (cur, av) -> cur == av }
    
    val gameStarted =
            booleanBinding(availableTurns, isHumanTurn)
            { value > 0 || isHumanTurn.value }
    val gameOver = gameResult.booleanBinding { it != null }
    
    init {
        subscribe<NewGameState> { event ->
            val state = event.gameState
            if (state !is GameState) {
                logger.warn("Received unknown state: $state")
                return@subscribe
            }
            gameResult.set(null)
            logger.debug("New state: $state")
            if (logger.isTraceEnabled)
                logger.trace(state.longString())
            gameState.set(state)
        }
        subscribe<HumanMoveRequest> {
            isHumanTurn.set(true)
        }
        subscribe<HumanMoveAction> {
            isHumanTurn.set(false)
        }
        subscribe<GameOverEvent> { event ->
            gameResult.set(event.result)
        }
        subscribe<TerminateGame> { clearGame() }
        subscribe<CreateGame> { clearGame() }
    }
    
    private fun clearGame() {
        logger.debug("Resetting GameModel")
        gameState.set(null)
        gameResult.set(null)
        availableTurns.set(0)
        isHumanTurn.set(false)
    }
    
    companion object {
        private val logger = LoggerFactory.getLogger(GameModel::class.java)
    }
}