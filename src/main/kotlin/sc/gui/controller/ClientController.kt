package sc.gui.controller

import sc.api.plugins.IGameState
import sc.api.plugins.IMove
import sc.api.plugins.SENSIBLE_MOVES_COUNT
import sc.api.plugins.TwoPlayerGameState
import sc.api.plugins.exceptions.GameLogicException
import sc.gui.LobbyManager
import sc.gui.controller.client.ClientInterface
import sc.gui.controller.client.ExecClient
import sc.gui.controller.client.ExternalClient
import sc.gui.controller.client.GuiClient
import sc.gui.model.PlayerType
import sc.gui.model.TeamSettings
import sc.gui.serverAddress
import sc.gui.serverPort
import tornadofx.*
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

data class StartGame(val settings: List<TeamSettings>): FXEvent()
class HumanMoveRequest: FXEvent()
/** Human making a move.
 * @param move the move */
data class HumanMoveAction(val move: IMove): FXEvent()

data class Player(val name: String, val client: ClientInterface)

class ClientController: Controller() {
    private val host = serverAddress
    private val port = serverPort
    private val lobbyManager = LobbyManager(host, port)
    
    // Do NOT call this directly in the UI thread, use fire(StartGameRequest(gameCreationModel))
    // This way, the game starting is done in the background - otherwise the UI will be blocked
    // TODO put everything triggered by events in a different class and call these from the controller using events
    fun startGame(playerSettings: List<TeamSettings>) {
        val players = playerSettings.map { teamSettings ->
            Player(teamSettings.name.get(), when (val type = teamSettings.type.value) {
                PlayerType.HUMAN -> GuiClient(host, port, type, ::humanMoveRequest)
                PlayerType.COMPUTER_SIMPLE -> GuiClient(host, port, type, ::getSimpleMove)
                PlayerType.COMPUTER_ADVANCED -> GuiClient(host, port, type, ::getAdvancedMove)
                PlayerType.COMPUTER -> ExecClient(host, port, teamSettings.executable.get())
                PlayerType.EXTERNAL -> ExternalClient(host, port)
            })
        }
        // TODO handle client start failures
        
        lobbyManager.startNewGame(players, players.none { it.client.type == PlayerType.HUMAN })
    }
    
    fun humanMoveRequest(@Suppress("UNUSED_PARAMETER") state: IGameState): CompletableFuture<IMove> {
        val future = CompletableFuture<IMove>()
        subscribe<HumanMoveAction>(1) {
            future.complete(it.move)
        }
        fire(HumanMoveRequest())
        return future
    }
    
    val random = Random
    
    /** Reservoir sampling.
     * https://math.stackexchange.com/questions/1058500/can-you-select-random-entry-from-unknown-number-of-entries/1058547#1058547 */
    fun getSimpleMove(state: IGameState): CompletableFuture<IMove> {
        val possibleMoves = state.moveIterator()
        var selection = possibleMoves.next()
        var count = 1
        while(possibleMoves.hasNext()) {
            count++
            val next = possibleMoves.next()
            if(random.nextInt(count) == 0)
                selection = next
        }
        return CompletableFuture.completedFuture(selection)
    }
    
    /** Evaluation of following state. */
    fun getAdvancedMove(state: IGameState): CompletableFuture<IMove> {
        val possibleMoves = state.moveIterator()
        if (!possibleMoves.hasNext())
            throw GameLogicException("No possible Moves found!")
        val best = ArrayList<IMove>()
        var bestValue = Integer.MIN_VALUE
        var count = 0
        while(possibleMoves.hasNext() && count < SENSIBLE_MOVES_COUNT) {
            val next = possibleMoves.next()
            @Suppress("UNCHECKED_CAST")
            val newState = (state as TwoPlayerGameState<IMove>).performMove(next)
            val points = newState.getPointsForTeamExtended(state.currentTeam).sum() -
                         newState.getPointsForTeamExtended(state.otherTeam).sum()
            if(points >= bestValue) {
                if(points > bestValue)
                    best.clear()
                best.add(next)
                bestValue = points
            }
            count++
        }
        return CompletableFuture.completedFuture(best.random())
    }
}
