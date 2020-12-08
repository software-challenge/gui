package sc.gui.controller.client

import sc.gui.model.PlayerType

interface ClientInterface {
    val type: PlayerType
    fun joinAnyGame()
    fun joinPreparedGame(reservation: String)
}
