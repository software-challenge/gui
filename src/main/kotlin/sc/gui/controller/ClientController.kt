package sc.gui.controller

import org.slf4j.LoggerFactory
import sc.framework.plugins.Player
import sc.gui.ControllingClient
import sc.gui.model.BoardModel
import sc.networking.clients.ILobbyClientListener
import sc.networking.clients.LobbyClient
import sc.plugin2021.*
import sc.plugin2021.util.GameRuleLogic
import sc.protocol.responses.PrepareGameProtocolMessage
import sc.protocol.responses.ProtocolErrorMessage
import sc.server.Configuration
import sc.shared.GameResult
import tornadofx.Controller

// connects our game handler (ClientListener) to the server
class TestClient(playerType: PlayerType, host: String, port: Int): AbstractClient(host, port) {
    companion object {
        val logger = LoggerFactory.getLogger(TestClient::class.java)
    }

    init {
        val logic = ClientListener(playerType, this)
        handler = logic
    }
}

class ClientListener(private val playerType: PlayerType, private val client: AbstractClient): IGameHandler {

    var currentState: GameState? = null;

    override fun gameEnded(data: GameResult, team: Team?, errorMessage: String) {
    }

    override fun onRequestAction() {
        println(this.playerType.toString() + " got new action request!")
        if (currentState != null) {
            val possibleMoves = GameRuleLogic.getPossibleMoves(currentState!!)
            sendAction(
                    if (possibleMoves.isEmpty()) PassMove(currentState!!.currentColor)
                    else possibleMoves.random())
            /*
            val color = currentState!!.currentColor
            val pieces = currentState!!.undeployedPieceShapes[color]
            val pieceShape = currentState!!.startPiece
            val move = SetMove(Piece(
                    color,
                    pieceShape,
                    Rotation.NONE,
                    false,
                    Coordinates(0, 0)
            ))
            sendAction(move)
             */
        } else {
            println("ERROR: got move request before gamestate")
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

class UIGameListener : ILobbyClientListener {
    override fun onNewState(roomId: String?, state: Any?) {
        println("listener: onNewState")
        val gameState = state as GameState
        println("This is what I got: " + gameState.toString())
    }

    override fun onError(roomId: String?, error: ProtocolErrorMessage?) {
        println("listener: onError")
    }

    override fun onRoomMessage(roomId: String?, data: Any?) {
        println("listener: onRoomMessage")
    }

    override fun onGamePrepared(response: PrepareGameProtocolMessage?) {
        println("listener: onGamePrepared")
    }

    override fun onGameLeft(roomId: String?) {
        println("listener: onGameLeft")
    }

    override fun onGameJoined(roomId: String?) {
        println("listener: onGameJoined")
    }

    override fun onGameOver(roomId: String?, data: GameResult?) {
        println("listener: onGameOver")
    }

    override fun onGamePaused(roomId: String?, nextPlayer: Player?) {
        println("listener: onGamePaused")
    }

    override fun onGameObserved(roomId: String?) {
        println("listener: onGameObserved")
    }

}

class ClientController : Controller() {

    val boardModel: BoardModel by inject()
    var controllingClient: ControllingClient? = null
    val listener: UIGameListener = UIGameListener()

    fun startGame() {
        val host = "localhost"
        val port = 13050
        println("creating and observing")
        // NOTE that testClientOne and testClientTwo are currently *internal* clients to wire the logic of the GameHandlers to the server. When external clients should join the game, these are not needed.
        val testClientOne = TestClient(PlayerType.PLAYER_ONE, host, port)
        val testClientTwo = TestClient(PlayerType.PLAYER_TWO, host, port)
        controllingClient = ControllingClient(host, port, testClientOne, testClientTwo, listener)
    }
}
