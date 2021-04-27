package sc.gui.controller

import sc.api.plugins.IGameState
import sc.shared.GameResult

interface IGameListener {
    fun onGameStarted(error: Throwable? = null)
    fun onNewState(state: IGameState)
    fun onGameOver(gameResult: GameResult)
}