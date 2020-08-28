package sc.gui

import org.slf4j.LoggerFactory
import sc.api.plugins.exceptions.GameLogicException
import sc.framework.plugins.Player
import sc.gui.controller.HumanMoveRequest
import sc.plugin2021.*
import sc.plugin2021.util.GameRuleLogic
import sc.shared.GameResult

// connects our game handler (ClientListener) to the server
class HumanClient(playerType: PlayerType, host: String, port: Int, moveRequestHandler: (gs: GameState) -> Unit): AbstractClient(host, port) {
    companion object {
        val logger = LoggerFactory.getLogger(HumanClient::class.java)
    }

    init {
        val logic = HumanGameHandler(playerType, this, moveRequestHandler)
        handler = logic
    }
}

// handles communication with the server for a human player using the GUI
class HumanGameHandler(private val playerType: PlayerType, private val client: AbstractClient, private val moveRequestHandler: (gs: GameState) -> Unit): IGameHandler {

    companion object {
        val logger = LoggerFactory.getLogger(HumanGameHandler::class.java)
    }

    var currentState: GameState? = null;

    override fun gameEnded(data: GameResult, team: Team?, errorMessage: String?) {
    }

    override fun onRequestAction() {
        logger.debug("human ${this.playerType} got new action request!")
        if (currentState != null) {
            moveRequestHandler(currentState!!)
        } else {
            logger.error("got move request before gamestate")
        }
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

