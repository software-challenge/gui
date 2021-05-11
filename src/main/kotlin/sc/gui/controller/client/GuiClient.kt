package sc.gui.controller.client

import sc.framework.plugins.Player
import sc.gui.model.PlayerType
import sc.plugin2021.*
import sc.shared.GameResult
import java.util.concurrent.CompletionStage

/** A client directly managed by the Gui. */
class GuiClient(
        host: String,
        port: Int,
        override val type: PlayerType,
        moveRequestHandler: (state: GameState) -> CompletionStage<Move>,
): ClientInterface, AbstractClient(host, port) {
    init {
        handler = InternalGameHandler(this, moveRequestHandler)
    }
    
    private val location = "$host:$port"
    override fun toString() = super.toString() + " type $type on $location"
}

/** Handles communication with the server for a player whose moves are supplied by [moveRequestHandler]. */
class InternalGameHandler(
        private val client: AbstractClient,
        private val moveRequestHandler: (state: GameState) -> CompletionStage<Move>,
): IGameHandler {
    private var currentState: GameState? = null
    
    override fun gameEnded(data: GameResult, team: Team?, errorMessage: String?) {
    }
    
    override fun onRequestAction() {
        currentState?.let(moveRequestHandler)?.thenAccept { sendAction(it) }
        ?: throw IllegalStateException("Received move request before GameState!")
    }
    
    override fun onUpdate(player: Player, otherPlayer: Player) {
    }
    
    override fun onUpdate(gamestate: GameState) {
        currentState = gamestate
    }
    
    override fun sendAction(move: Move) {
        client.sendMove(move)
    }
    
}

