package sc.gui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sc.api.plugins.exceptions.GameLogicException
import sc.framework.plugins.Player
import sc.plugin2021.*
import sc.plugin2021.util.GameRuleLogic
import sc.shared.GameResult

// connects our game handler (ClientListener) to the server
class TestClient(playerType: PlayerType, host: String, port: Int): AbstractGuiClient(host, port) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(HumanClient::class.java)
    }

    init {
        handler = TestGameHandler(playerType, this)
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
        logger.debug("$playerType got new action request!")
        currentState?.let { state ->
            val possibleMoves = GameRuleLogic.getPossibleMoves(state)
            if(possibleMoves.isEmpty())
                throw GameLogicException("No possible Moves found!?")
    
            sendAction(possibleMoves.random())
        }?:logger.error("got move request before gamestate")
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

