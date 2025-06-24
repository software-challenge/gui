package sc.gui.view.game

import javafx.scene.input.KeyEvent
import javafx.scene.layout.Region
import sc.gui.view.GameBoard
import sc.plugin2026.GameState

class PiranhasBoard: GameBoard<GameState>() {
    override fun onNewState(oldState: GameState?, state: GameState?) {
        TODO("Not yet implemented")
    }
    
    override val root: Region
        get() = TODO("Not yet implemented")
    
    override fun handleKeyPress(state: GameState, keyEvent: KeyEvent): Boolean {
        TODO("Not yet implemented")
    }
    
    override fun renderHumanControls(state: GameState) {
        TODO("Not yet implemented")
    }
}