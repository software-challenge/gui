package sc.gui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sc.framework.plugins.Player
import sc.plugin2021.*
import sc.shared.GameResult
import java.util.concurrent.CompletableFuture

/** Connects our game handler (ClientListener) to the server. */
class InternalClient(host: String, port: Int, moveRequestHandler: (state: GameState) -> CompletableFuture<Move>) : AbstractGuiClient(host, port) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(InternalClient::class.java)
    }

    init {
        handler = InternalGameHandler(this, moveRequestHandler)
    }
}

/** Handles communication with the server for a player whose moves are supplied by [moveRequestHandler]. */
class InternalGameHandler(private val client: AbstractClient, private val moveRequestHandler: (state: GameState) -> CompletableFuture<Move>) : IGameHandler {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(InternalGameHandler::class.java)
    }

    private var currentState: GameState? = null

    override fun gameEnded(data: GameResult, team: Team?, errorMessage: String?) {
    }

    override fun onRequestAction() {
        logger.debug("Player ${client.team} got new action request!")
        currentState?.let(moveRequestHandler)?.thenAccept { sendAction(it) } ?: throw IllegalStateException("Received move request before GameState!")
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

