package sc.gui.view.game

import javafx.geometry.Pos
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
    
    val grid: GridPane = GridPane()
    
    override val root = hbox {
        this.alignment = Pos.CENTER
        add(grid)
    }
    
    override fun onNewState(oldState: GameState?, state: GameState?) {
        grid.children.clear()
        state?.let { state ->
            state.board.forEach { (pos: Coordinates, field: FieldState) ->
                val piece = PieceImage(gridSize, field.team?.let { team -> "${team}_${field.size}" } ?: field.name.lowercase())
                grid.add(piece, pos.x, pos.y)
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