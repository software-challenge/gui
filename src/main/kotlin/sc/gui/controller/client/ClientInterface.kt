package sc.gui.controller.client

import sc.gui.model.PlayerType

interface ClientInterface {
    val type: PlayerType
    fun joinGameRoom(roomId: String)
    fun joinPreparedGame(reservation: String)
}
