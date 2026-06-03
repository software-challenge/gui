package sc.gui.view.game

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.effect.ColorAdjust
import javafx.scene.effect.Glow
import javafx.scene.input.KeyEvent
import javafx.scene.layout.GridPane
import sc.api.plugins.Coordinates
import sc.gui.util.listenImmediately
import sc.gui.view.GameBoard
import sc.gui.view.PieceImage
import sc.gui.view.transitionDuration
import sc.plugin2098.FieldState
import sc.plugin2098.GameState
import sc.plugin2098.util.GameRuleLogic
import sc.plugin2098.util.Connect4Constants
import tornadofx.*

class Connect4Board: GameBoard<GameState>() {
    
    private val gridSize
        get() = squareSize.div(Connect4Constants.BOARD_WIDTH) // "Length of the smaller side of the window."
    
    val grid: GridPane = GridPane().addClass("grid").apply {
        squareSize.listenImmediately { size ->
            padding = Insets(
                size.toDouble() / 80,
                size.toDouble() / 80,
                size.toDouble() / 300,
                size.toDouble() / 200,
            )
        }
    }
    
    override val root = hbox {
        this.alignment = Pos.CENTER
        vbox {
            this.alignment = Pos.CENTER
            add(grid)
        }
    }
    
    var selected: Node? = null
    val hovers = ArrayList<Node>()
    
    fun clearHovers() {
        logger.trace { "Clearing hovers: $hovers" }
        grid.children.removeAll(hovers)
        hovers.clear()
    }
    
    fun addToGrid(child: Node, coordinates: Coordinates) {
        grid.add(child, coordinates.x, Connect4Constants.BOARD_WIDTH - 1 - coordinates.y)
    }
    
    override fun onNewState(oldState: GameState?, state: GameState?) {
        selected = grid
        logger.debug { "New State: $state" }
        grid.children.clear()
        hovers.clear()
        selected = null

        // this ensures proper sizing of the board
        (0 until Connect4Constants.BOARD_WIDTH).forEach { y ->
            //grid.add(PieceImage(gridSize, "cell").apply { opacity = 0.5 }, y, 0)
            grid.add(PieceImage(gridSize, "cell").apply { opacity = 0.0 }, y, 0)
        }
        
        (0 until Connect4Constants.BOARD_HEIGHT).forEach { y ->
            //grid.add(PieceImage(gridSize, "cell").apply { opacity = 0.5 }, 0, y)
            grid.add(PieceImage(gridSize, "cell").apply { opacity = 0.0 }, 0, y)
        }
        //grid.add(PieceImage(gridSize, "cell").apply { opacity = 0.5 }, Connect4Constants.BOARD_WIDTH - 1, Connect4Constants.BOARD_HEIGHT - 1)

        
        state?.let { state ->
//            val move = state.lastMove?.let { move ->
//                if(oldState?.turn?.minus(state.turn) == -1) {
//                    move.from to GameRuleLogic.targetCoordinates(oldState.board, move)
//                } else {
//                    null
//                }
//            }
            state.board.forEach { (pos: Coordinates, field: FieldState) ->
//                val piece = PieceImage(
//                    gridSize,
//                    field.team?.let { team -> "${team}_${field.size}" } ?: field.name.lowercase())
                
                println(field.team ?: field.name)
                
                if(field.team == null) {
                    return@forEach
                }
                
                println("${field.team!!.name}_chip".lowercase())
                
                val piece = PieceImage(gridSize, field.team.let { team -> "${team}-chip".lowercase() })
                addToGrid(piece, pos)
            }
        }
    }
    
    override fun handleKeyPress(state: GameState, keyEvent: KeyEvent): Boolean {
        return false
    }
    
    override fun renderHumanControls(state: GameState) {
        state.getSensibleMoves().forEach { move ->
            
            val piece = PieceImage(gridSize, "${state.currentTeam}-chip".lowercase())
            
            piece.opacity = 0.7
            
            piece.onLeftClick {
                sendHumanMove(move)
            }
            
            addToGrid(piece, move.position)
        }
    }
    
}