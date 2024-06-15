package sc.gui.view

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.effect.ColorAdjust
import javafx.scene.input.KeyEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import sc.gui.AppStyle
import sc.plugin2025.*
import sc.plugin2025.util.HuIConstants
import tornadofx.*

private const val BOARDSIZE = 9

class HuIBoard: GameBoard<GameState>() {
    val grid = GridPane()
    
    override val root = HBox().apply {
        this.alignment = Pos.CENTER
        this.children.add(grid)
    }
    
    private val graphicSize = squareSize.doubleBinding { it?.toDouble()?.div(BOARDSIZE) ?: 10.0 }
    
    private val emptyRegion = Region()
    private val fields: Array<Node> = Array(HuIConstants.NUM_FIELDS) { emptyRegion }
    
    override fun onNewState(oldState: GameState?, state: GameState?) {
        grid.clear()
        if(state == null)
            return
        state.board.fields.withIndex().forEach { (index, field) ->
            fields[index] = putOnPosition(field.name, index)
        }
        state.players.forEach {
            putOnPosition(it.team.color, it.position)
        }
    }
    
    private fun putOnPosition(node: Node, position: Int) =
        grid.add(node, position % BOARDSIZE, position / BOARDSIZE)
    
    private fun putOnPosition(graphic: String, position: Int): Node =
        ResizableImageView(graphicSize).also { view ->
            view.image = resources.image("/hui/${graphic.lowercase()}.png")
            putOnPosition(view, position)
        }
    
    override fun renderHumanControls(state: GameState) {
        if(state.mustEatSalad()) {
            fields[state.currentPlayer.position].onClickMove(EatSalad)
            return
        }
        
        state.possibleExchangeCarrotMoves().forEach { car ->
            putOnPosition(
                Button("${if(car.amount > 0) "+" else ""}${car.amount}").apply {
                    translateYProperty().bind(graphicSize.doubleBinding {
                        -car.amount * (it?.toDouble()?.div(20) ?: 3.0)
                    })
                    onLeftClick { sendHumanMove(car) }
                },
                state.currentPlayer.position
            )
        }
        
        val pos = state.currentPlayer.position
        val maxAdvance = GameRuleLogic.calculateMoveableFields(state.currentPlayer.carrots)
        val fallBack = state.nextFallBack()
        
        fields.forEachIndexed { fieldPos, node ->
            val distance = fieldPos - pos
            when {
                distance <= 0 -> {
                    if(fallBack != fieldPos)
                        return@forEachIndexed
                    node.onClickMove(FallBack)
                }
                
                else -> {
                    if(pos + maxAdvance < fieldPos || state.checkAdvance(distance) != null)
                        return@forEachIndexed
                    state.possibleCardMoves(distance)?.forEachIndexed { index, advance ->
                        putOnPosition(
                            Button(advance.getCards().joinToString("\nthen ")).apply {
                                addClass("small")
                                translateYProperty().bind(
                                    graphicSize.doubleBinding {
                                        AppStyle.fontSizeRegular.value * index * advance.getCards().size * 2 -
                                        (it?.toDouble() ?: 10.0) / 3
                                    }
                                )
                                //translateYProperty().bind(graphicSize.doubleBinding { -car.value * (it?.toDouble()?.div(10) ?: 3.0) })
                                onLeftClick { sendHumanMove(advance) }
                            },
                            fieldPos
                        )
                    } ?: node.onClickMove(Advance(distance))
                }
            }
        }
    }
    
    fun Node.onClickMove(move: Move) {
        effect = ColorAdjust(0.0, 0.0, -0.5, 0.0)
        onLeftClick { sendHumanMove(move) }
    }
    
    override fun handleKeyPress(state: GameState, keyEvent: KeyEvent): Boolean {
        return false
    }
    
}