package sc.gui.model

import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import sc.api.plugins.IGameState
import sc.api.plugins.Team
import sc.gui.GameOverEvent
import sc.gui.controller.CreateGame
import sc.gui.controller.HumanMoveAction
import sc.gui.controller.HumanMoveRequest
import sc.gui.events.*
import sc.shared.GameResult
import sc.util.booleanBinding
import tornadofx.*
import kotlin.math.max

class GameModel: ViewModel() {
    val playerNames: ObservableList<String> = FXCollections.observableArrayList()
    val gameState = objectProperty<IGameState?>(null)
    val gameResult = objectProperty<GameResult>()
    
    val stepSpeed = objectProperty(5.0)
    
    val currentTurn = integerBinding(gameState) { value?.turn ?: 0 }
    val currentRound = integerBinding(gameState) { value?.round ?: 0 }
    val currentTeam = nonNullObjectBinding(gameState) { value?.currentTeam ?: Team.ONE }
    val teamScores = gameState.objectBinding { state ->
        Team.values().map { state?.getPointsForTeam(it) }
    }
    
    val isHumanTurn = booleanProperty(false)
    
    val availableTurns =
            intProperty(0).apply {
                currentTurn.addListener { _, _, turn ->
                    updateAvailableTurns(turn.toInt())
                }
            }
    fun updateAvailableTurns(turn: Int) {
        availableTurns.set(max(turn, availableTurns.get()))
    }
    
    val atLatestTurn =
            arrayOf<ObservableValue<Number>>(currentTurn, availableTurns)
                    .booleanBinding { (cur, av) -> cur == av }
    
    val gameOver = gameResult.booleanBinding { it != null }
    val gameStarted =
            booleanBinding(availableTurns, isHumanTurn, gameOver)
            { value > 0 || isHumanTurn.value || gameOver.value }
    
    init {
        subscribe<HumanMoveRequest> {
            isHumanTurn.set(true)
            fire(PauseGame(false))
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