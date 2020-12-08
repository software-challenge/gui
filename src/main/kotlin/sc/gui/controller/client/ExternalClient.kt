package sc.gui.controller.client

import sc.gui.model.PlayerType

class ExternalClient(private val host: String, private val port: Int) : ClientInterface {
    override val type = PlayerType.EXTERNAL
    
    override fun joinAnyGame() {
        println("Please start the manual client on $host:$port")
    }
    
    override fun joinPreparedGame(reservation: String) {
        throw Exception("External/Manual client can't join a prepared game (reservation: $reservation)")
    }
}
