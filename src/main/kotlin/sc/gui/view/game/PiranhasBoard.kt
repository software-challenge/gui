package sc.gui.view.game

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.effect.ColorAdjust
import javafx.scene.input.KeyEvent
import javafx.scene.layout.GridPane
import sc.api.plugins.Coordinates
import sc.gui.view.GameBoard
import sc.gui.view.PieceImage
import sc.plugin2026.FieldState
import sc.plugin2026.GameState
import sc.plugin2026.util.GameRuleLogic
import sc.plugin2026.util.PiranhaConstants
import tornadofx.*

class PiranhasBoard: GameBoard<GameState>() {
    
    private val gridSize
        get() = squareSize.div(PiranhaConstants.BOARD_LENGTH)
    
    val grid: GridPane = GridPane().addClass("grid")
    
    override val root = hbox {
        this.alignment = Pos.CENTER
        vbox {
            this.alignment = Pos.CENTER
            add(grid)
        }
    }
    
    var selected: Coordinates? = null
    val hovers = ArrayList<Node>()
    
    override fun onNewState(oldState: GameState?, state: GameState?) {
        grid.children.clear()
        
        (0 until PiranhaConstants.BOARD_LENGTH).forEach { y ->
            grid.add(PieceImage(gridSize, "squid").apply { opacity = 0.0 }, 0, y)
            grid.add(PieceImage(gridSize, "squid").apply { opacity = 0.0 }, y, 0)
        }
        
        state?.let { state ->
            state.board.forEach { (pos: Coordinates, field: FieldState) ->
                val piece = PieceImage(gridSize,
                    field.team?.let { team -> "${team}_${field.size}" } ?: field.name.lowercase())
                grid.add(piece, pos.x, pos.y)
                if(field.team == null)
                    return@forEach
                piece.onHover {
                    if(selected == null) {
                        grid.children.removeAll(hovers)
                        hovers.clear()
                        addHovers(state, pos, field)
                    }
                }
                piece.onLeftClick {
                    grid.children.removeAll(hovers)
                    hovers.clear()
                    if(selected == pos) {
                        selected = null
                        return@onLeftClick
                    }
                    selected = pos
                    addHovers(state, pos, field)
                }
            }
        }
    }
    
    fun addHovers(state: GameState, pos: Coordinates, field: FieldState) {
        val board = state.board
        GameRuleLogic.possibleMovesFor(board, pos).forEach { move ->
            val target = GameRuleLogic.targetField(board, move)
            val hover = PieceImage(gridSize, "${field.team}_${field.size}")
            
            val current = field.team == state.currentTeam
            hover.effect = ColorAdjust().apply {
                saturation = if(current && awaitingHumanMove.value) -0.4 else -0.9
            }
            if(current)
                hover.onLeftClick { sendHumanMove(move) }
            
            hovers.add(hover)
            grid.add(hover, target.x, target.y)
        }
    }
    
    override fun handleKeyPress(state: GameState, keyEvent: KeyEvent): Boolean {
        return false
    }
    
    override fun renderHumanControls(state: GameState) {
        // TODO "Not yet implemented"
    }
    
}