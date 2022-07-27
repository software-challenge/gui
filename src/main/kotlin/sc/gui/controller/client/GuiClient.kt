package sc.gui.controller.client

import sc.api.plugins.IGameState
import sc.api.plugins.IMove
import sc.gui.model.PlayerType
import sc.networking.clients.LobbyClient
import sc.player.IGameHandler
import sc.plugin2023.GameState
import sc.plugin2023.Move
import sc.shared.GameResult
import java.util.concurrent.CompletableFuture

/** A client directly managed by the Gui. */
class GuiClient(
        host: String,
        port: Int,
        override val type: PlayerType,
        moveRequestHandler: (state: GameState) -> CompletableFuture<Move>,
): ClientInterface {
    val player = LobbyClient(host, port).asPlayer(InternalGameHandler(moveRequestHandler))
    
    override fun joinGameRoom(roomId: String) = player.joinGameRoom(roomId)
    override fun joinGameWithReservation(reservation: String) = player.joinGameWithReservation(reservation)
    
    private val location = "$host:$port"
    override fun toString() = super.toString() + " type $type on $location"
}

/** Handles communication with the server for a player whose moves are supplied by [moveRequestHandler]. */
class InternalGameHandler(
        private val moveRequestHandler: (state: GameState) -> CompletableFuture<Move>,
): IGameHandler {
    private var currentState: GameState? = null
    
    override fun calculateMove(): IMove =
            currentState?.let(moveRequestHandler)?.get()
            ?: throw IllegalStateException("Received move request before GameState!")
    
    override fun onUpdate(gameState: IGameState) {
        currentState = gameState as GameState
    }
    
    override fun onGameOver(data: GameResult) {
    }
    
    override fun onError(error: String) {
    }
    
}
