package sc.gui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sc.framework.plugins.Player
import sc.plugin2021.*
import sc.shared.GameResult

// connects our game handler (ClientListener) to the server
class HumanClient(playerType: PlayerType, host: String, port: Int, moveRequestHandler: (gs: GameState) -> Unit) : AbstractGuiClient(host, port) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(HumanClient::class.java)
    }

    init {
        handler = HumanGameHandler(playerType, this, moveRequestHandler)
    }
}

// handles communication with the server for a human player using the GUI
class HumanGameHandler(private val playerType: PlayerType, private val client: AbstractClient, private val moveRequestHandler: (gs: GameState) -> Unit) : IGameHandler {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(HumanGameHandler::class.java)
    }

    private var currentState: GameState? = null

    override fun gameEnded(data: GameResult, team: Team?, errorMessage: String?) {
    }

    override fun onRequestAction() {
        logger.debug("$playerType got new action request!")
        currentState?.let(moveRequestHandler) ?: logger.error("got move request before gamestate")
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

