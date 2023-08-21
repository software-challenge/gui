package sc.gui.controller

import sc.api.plugins.IGameState
import sc.api.plugins.IMove
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
                PlayerType.COMPUTER_EXAMPLE -> GuiClient(host, port, type, ::getSimpleMove)
                PlayerType.COMPUTER -> ExecClient(host, port, teamSettings.executable.get())
                PlayerType.EXTERNAL -> ExternalClient(host, port)
                else -> throw IllegalArgumentException("Cannot create game: Invalid playerType $type")
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
    
    fun getSimpleMove(state: IGameState): CompletableFuture<IMove> {
        val possibleMoves = state.getAllMoves()
        if (!possibleMoves.hasNext())
            throw GameLogicException("No possible Moves found!")
        val best = ArrayList<IMove>()
        var bestValue = Integer.MIN_VALUE
        var count = 0
        while(possibleMoves.hasNext() && count < 100) {
            val next = possibleMoves.next()
            val points = @Suppress("UNCHECKED_CAST") (state as TwoPlayerGameState<IMove>).performMove(next).getPointsForTeam(state.currentTeam).first()
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
