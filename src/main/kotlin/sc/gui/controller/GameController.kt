package sc.gui.controller

import javafx.beans.value.ObservableValue
import org.slf4j.LoggerFactory
import sc.api.plugins.Team
import sc.gui.GameOverEvent
import sc.gui.NewGameState
import sc.gui.view.TerminateGame
import sc.plugin2022.GameState
import sc.shared.GameResult
import sc.util.binding
import tornadofx.*
import kotlin.math.max

class GameController: Controller() {
    val gameState = objectProperty<GameState?>(null)
    val gameResult = objectProperty<GameResult>()
    val isHumanTurn = booleanProperty(false)
    
    val currentTurn = integerBinding(gameState) { value?.turn ?: 0 }
    val currentRound = integerBinding(gameState) { value?.round ?: 0 }
    val currentTeam = nonNullObjectBinding(gameState) { value?.currentTeam ?: Team.ONE }
    val teamScores = gameState.objectBinding { state ->
        Team.values().map { state?.getPointsForTeam(it) }
    }
    
    val availableTurns = intProperty(0).apply {
        currentTurn.addListener { _, _, turn ->
            set(max(turn.toInt(), get()))
        }
    }
    val atLatestTurn =
            arrayOf<ObservableValue<Number>>(currentTurn, availableTurns)
                    .binding { (cur, av) -> cur == av }
    
    val gameStarted =
            booleanBinding(currentTurn, isHumanTurn)
            { value > 0 || isHumanTurn.value }
    val gameEnded = gameResult.booleanBinding { it != null }
    
    init {
        subscribe<NewGameState> { event ->
            val state = event.gameState
            if (state !is GameState) {
                logger.warn("Received unknown state: $state")
                return@subscribe
            }
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
        subscribe<TerminateGame> {
            clearGame()
        }
    }
    
    private fun clearGame() {
        logger.debug("Resetting GameController")
        gameState.set(null)
        gameResult.set(null)
        availableTurns.set(0)
        isHumanTurn.set(false)
    }
    
    companion object {
        private val logger = LoggerFactory.getLogger(GameController::class.java)
    }
}