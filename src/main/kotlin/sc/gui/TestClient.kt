package sc.gui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sc.api.plugins.exceptions.GameLogicException
import sc.framework.plugins.Player
import sc.plugin2021.*
import sc.plugin2021.util.GameRuleLogic
import sc.shared.GameResult

// connects our game handler (ClientListener) to the server
class TestClient(playerType: PlayerType, host: String, port: Int): AbstractClient(host, port) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(HumanClient::class.java)
    }

    init {
        val logic = TestGameHandler(playerType, this)
        handler = logic
    }
}

class TestGameHandler(private val playerType: PlayerType, private val client: AbstractClient): IGameHandler {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(TestGameHandler::class.java)
    }

    var currentState: GameState? = null

    override fun gameEnded(data: GameResult, team: Team?, errorMessage: String?) {
    }

    override fun onRequestAction() {
        logger.debug(this.playerType.toString(), "got new action request!")
        if (currentState != null) {
            val possibleMoves = GameRuleLogic.getPossibleMoves(currentState!!)
            if (possibleMoves.isEmpty())
                throw GameLogicException("No possible Moves found!?")

            sendAction(possibleMoves.random())
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

