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
import javafx.util.Duration
import sc.api.plugins.Coordinates
import sc.gui.util.listenImmediately
import sc.gui.view.GameBoard
import sc.gui.view.PieceImage
import sc.plugin2099.FieldState
import sc.plugin2099.GameState
import sc.plugin2099.Move
import sc.plugin2099.util.GameRuleLogic
import sc.plugin2099.util.TicTacToeConstants
import tornadofx.*

class TicTacToeBoard: GameBoard<GameState>() {
    
    private val gridSize
        get() = squareSize.div(TicTacToeConstants.BOARD_LENGTH)
    
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
        grid.add(child, coordinates.x, TicTacToeConstants.BOARD_LENGTH - 1 - coordinates.y)
    }
    
    override fun onNewState(oldState: GameState?, state: GameState?) {
        logger.debug { "New State: $state" }
        grid.children.clear()
        hovers.clear()
        
        // this ensures proper sizing of the board
        (0 until TicTacToeConstants.BOARD_LENGTH).forEach { y ->
            grid.add(PieceImage(gridSize, "blue").apply { opacity = 0.0 }, 0, y)
            grid.add(PieceImage(gridSize, "blue").apply { opacity = 0.0 }, y, 0)
        }
        
        state?.let { state ->
            val move = state.lastMove?.let { move ->
                if(oldState?.turn?.minus(state.turn) == -1) {
                    move.field
                } else {
                    null
                }
            }
            state.board.forEach { (pos: Coordinates, field: FieldState) ->
                val piece = PieceImage(
                    gridSize,
                    field.team?.color ?: field.name.lowercase())

                addToGrid(piece, pos)
                if(pos == move) {
                    logger.debug { "Animating piece $piece" }
                    piece.effect = Glow(0.2)
                    piece.scaleX = 2.0
                    piece.scaleY = 2.0
                    piece.scale(Duration(0.4), Point2D(1.0, 1.0))
                }
                
               
                if(field.team != null || state.isOver)
                    return@forEach
                piece.hoverProperty().addListener { _, _, hover ->
                    if(selected == null) {
                        if(hover) {
                            Platform.runLater {
                                addHovers(state, pos)
                            }
                        } else {
                            if(field != FieldState.EMPTY || !awaitingHumanMove.value)
                                clearHovers()
                        }
                    }
                }
                piece.onLeftClick {
                    if(field == FieldState.EMPTY && awaitingHumanMove.value) {
                        logger.debug { "Clicked empty field on $pos" }
                        selected?.effect = null
                        if(selected == piece) {
                            clearHovers()
                            selected = null
                            return@onLeftClick
                        }
                        selected = piece
                        piece.effect = Glow(0.6)
                        addHovers(state, pos)
                    }
                }
            }
        }
    }
    
    fun addHovers(state: GameState, pos: Coordinates) {
        logger.trace { "Clearing hovers and adding for $pos in turn ${state.turn}" }
        clearHovers()
        
        val board = state.board
        if (GameRuleLogic.checkMove(board, Move(pos)) == null && awaitingHumanMove.value) {
            val hover = PieceImage(gridSize, state.currentTeam.color)
            
            hover.effect = ColorAdjust().apply {
                saturation = if(awaitingHumanMove.value) -0.4 else -0.9
            }
            
            hover.onLeftClick { sendHumanMove(Move(pos)) }
            
            hovers.add(hover)
            addToGrid(hover, pos)
        }
    }
    
    override fun handleKeyPress(state: GameState, keyEvent: KeyEvent): Boolean {
        return false
    }
    
    override fun renderHumanControls(state: GameState) {
        // not needed for TicTacToe, handled above
    }
    
}