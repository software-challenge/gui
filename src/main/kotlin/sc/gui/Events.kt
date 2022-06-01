package sc.gui.events

import tornadofx.*

sealed class GameControlEvent: FXEvent()
data class PauseGame(val pause: Boolean): GameControlEvent()
data class StepGame(val steps: Int): GameControlEvent()
/** Signals that the current game should be terminated.
 * @param close whether to return to start screen */
data class TerminateGame(val close: Boolean = false): GameControlEvent()
