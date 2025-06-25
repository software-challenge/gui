package sc.gui.view.game

import javafx.scene.input.KeyEvent
import javafx.scene.layout.GridPane
import sc.api.plugins.Coordinates
import sc.gui.view.GameBoard
import sc.gui.view.PieceImage
import sc.plugin2026.FieldState
import sc.plugin2026.GameState
import sc.plugin2026.util.PiranhaConstants
import tornadofx.*

class PiranhasBoard: GameBoard<GameState>() {
    
    private val gridSize
        get() = squareSize.div(PiranhaConstants.BOARD_LENGTH)
    
    override val root: GridPane = GridPane()
    
    override fun onNewState(oldState: GameState?, state: GameState?) {
        root.children.clear()
        state?.let { state ->
            state.board.forEach { (pos: Coordinates, field: FieldState) ->
                val piece = PieceImage(gridSize, field.team?.let { team -> "${team}_${field.size}" } ?: field.name.lowercase())
                root.add(piece, pos.x, pos.y)
            }
        }
    }
    
    override fun handleKeyPress(state: GameState, keyEvent: KeyEvent): Boolean {
        return false
    }
    
    override fun renderHumanControls(state: GameState) {
        // TODO "Not yet implemented"
    }
    
}