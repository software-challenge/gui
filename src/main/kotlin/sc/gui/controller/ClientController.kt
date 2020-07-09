package sc.gui.controller

import sc.framework.plugins.Player
import sc.plugin2020.AbstractClient
import sc.plugin2020.PlayerType
import sc.protocol.responses.PrepareGameProtocolMessage
import sc.protocol.responses.ProtocolErrorMessage
import sc.shared.GameResult
import tornadofx.Controller

// XXX: this is the client class for the game hive (NOT blokus), just for testing until the blokus client is implemented
class UIClient : AbstractClient("localhost", 13050, PlayerType.PLAYER_ONE) {

    fun join() {
        joinAnyGame()
    }

    override fun onError(p0: String?, p1: ProtocolErrorMessage?) {
        TODO("Not yet implemented")
    }

    override fun onGameJoined(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onGameLeft(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onGameObserved(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onGameOver(p0: String?, p1: GameResult?) {
        TODO("Not yet implemented")
    }

    override fun onGamePaused(p0: String?, p1: Player?) {
        TODO("Not yet implemented")
    }

    override fun onGamePrepared(p0: PrepareGameProtocolMessage?) {
        TODO("Not yet implemented")
    }

    override fun onNewState(p0: String?, p1: Any?) {
        TODO("Not yet implemented")
    }

    override fun onRoomMessage(p0: String?, p1: Any?) {
        TODO("Not yet implemented")
    }

}

class ClientController : Controller() {
    val client = UIClient()
    fun startClient() {
        client.join()
    }
}
