package sc.gui.controller

import org.slf4j.LoggerFactory
import sc.api.plugins.Team
import sc.gui.GameOverEvent
import sc.gui.GameReadyEvent
import sc.gui.NewGameState
import sc.plugin2022.GameState
import sc.shared.GameResult
import tornadofx.*
import kotlin.math.max

class GameController: Controller() {
    val gameState = objectProperty<GameState?>(null)
    val gameResult = objectProperty<GameResult>()
    val isHumanTurn = objectProperty(false)
    
    val currentTurn = nonNullObjectBinding(gameState) { value?.turn ?: 0 }
    val currentRound = nonNullObjectBinding(gameState) { value?.round ?: 0 }
    val currentTeam = nonNullObjectBinding(gameState) { value?.currentTeam ?: Team.ONE }
    val teamScores = gameState.objectBinding { state ->
        Team.values().map { state?.getPointsForTeam(it) }
    }
    
    val availableTurns = objectProperty(0).also { avTurns ->
        currentTurn.addListener { _, _, turn ->
            avTurns.set(max(turn, avTurns.value))
        }
    }
    val atLatestTurn = booleanBinding(currentTurn, availableTurns)
    { currentTurn.value == availableTurns.value }
    
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
        subscribe<GameReadyEvent> {
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