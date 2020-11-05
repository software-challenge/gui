package sc.gui.controller.client

import sc.gui.model.PlayerType

interface ClientInterface {
    val type: PlayerType
    fun joinPreparedGame(reservation: String)
}
