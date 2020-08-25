package sc.gui.controller

import org.slf4j.LoggerFactory
import sc.framework.plugins.Player
import sc.gui.ControllingClient
import sc.gui.TestClient
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

// This is the listener to update the gamestate in the UI
class UIGameListener(val gameStateListener: (g: GameState) -> Unit) : ILobbyClientListener {
    override fun onNewState(roomId: String?, state: Any?) {
        println("listener: onNewState")
        val gameState = state as GameState
        println("This is what I got: " + gameState.toString())
        this.gameStateListener(gameState)
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
    val listener: UIGameListener = UIGameListener(::newGameState)

    fun startGame() {
        val host = "localhost"
        val port = 13050
        println("creating and observing")
        // NOTE that testClientOne and testClientTwo are currently *internal* clients to wire the logic of the GameHandlers to the server. When external clients should join the game, these are not needed.
        val testClientOne = TestClient(PlayerType.PLAYER_ONE, host, port)
        val testClientTwo = TestClient(PlayerType.PLAYER_TWO, host, port)
        controllingClient = ControllingClient(host, port, testClientOne, testClientTwo, listener)
    }

    fun newGameState(gameState: GameState) {
        println("got new board: "+gameState.board.toString())
        //boardModel.updateFields(gameState.board)
    }
}
