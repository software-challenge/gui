package sc.gui.controller

import sc.framework.plugins.Player
import sc.gui.ControllingClient
import sc.gui.model.BoardModel
import sc.networking.clients.ILobbyClientListener
import sc.plugin2021.AbstractClient
import sc.plugin2021.GameState
import sc.plugin2021.PlayerType
import sc.protocol.responses.PrepareGameProtocolMessage
import sc.protocol.responses.ProtocolErrorMessage
import sc.shared.GameResult
import tornadofx.Controller

class UITestClient(playerType: PlayerType) : AbstractClient("localhost", 13050, playerType) {

    override fun onError(roomId: String, error: ProtocolErrorMessage) {
        println("onError ")
    }

    override fun onGameJoined(roomId: String) {
        println("onGameJoined ")
    }

    override fun onGameLeft(roomId: String) {
        println("onGameLeft ")
    }

    override fun onGameObserved(roomId: String) {
        println("onGameObserved ")
    }

    override fun onGameOver(roomId: String, data: GameResult) {
        println("onGameOver ")
    }

    override fun onGamePaused(roomId: String, nextPlayer: Player) {
        println("onGamePaused ")
    }

    override fun onGamePrepared(response: PrepareGameProtocolMessage) {
        println("onGamePrepared ")
    }

    override fun onNewState(roomId: String, state: Any) {
        println("onNewState ")
    }

    override fun onRoomMessage(roomId: String, data: Any) {
        println("onRoomMessage ")
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
    val playerOne = UITestClient(PlayerType.PLAYER_ONE)
    val playerTwo = UITestClient(PlayerType.PLAYER_TWO)
    var controllingClient: ControllingClient? = null
    val listener: UIGameListener = UIGameListener()

    fun startGame() {
        println("creating and observing")
        controllingClient = ControllingClient("localhost", 13050, playerOne, playerTwo, listener)
        controllingClient?.nextStep()
    }
}
