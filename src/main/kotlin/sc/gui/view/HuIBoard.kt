package sc.gui.view

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.effect.ColorAdjust
import javafx.scene.effect.Glow
import javafx.scene.input.KeyEvent
import javafx.scene.layout.*
import sc.gui.AppStyle
import sc.plugin2025.*
import sc.plugin2025.util.HuIConstants
import tornadofx.*

private const val BOARDSIZE = 9

class HuIBoard: GameBoard<GameState>() {
    val grid = GridPane()
    val cards = Array(2) { VBox() }
    
    private val graphicSize = squareSize.doubleBinding {
        minOf(
            root.width.div(BOARDSIZE + 4 /* cards on the sides */),
            viewHeight / BOARDSIZE
        )
    }
    
    override val root: Region = HBox().apply {
        this.alignment = Pos.CENTER
        this.children.addAll(cards)
        cards.forEachIndexed { index, card ->
            card.alignment = if(index == 0) Pos.CENTER_LEFT else Pos.CENTER_RIGHT
            card.hgrow = Priority.SOMETIMES
            card.prefWidthProperty().bind(graphicSize.multiply(3))
        }
        this.children.add(1, grid)
    }
    
    private val emptyRegion = Region()
    private val fields: Array<Node> = Array(HuIConstants.NUM_FIELDS) { emptyRegion }
    
    override fun onNewState(oldState: GameState?, state: GameState?) {
        grid.clear()
        if(state == null)
            return
        state.board.fields.withIndex().forEach { (index, field) ->
            fields[index] = putOnPosition(createImage(field.name), index)
            if(index != 0)
                putOnPosition(Label(index.toString()), index)
        }
        state.players.forEach { player ->
            putOnPosition(createImage(player.team.color, 0.8), player.position).apply {
                if(player.team == state.currentTeam)
                    effect = Glow(.3)
            }
            cards[player.team.index].apply {
                clear()
                children.addAll(player.getCards().map { createImage("hasenjoker_" + when(it) {
                    Card.FALL_BACK -> "backward"
                    Card.HURRY_AHEAD -> "forward"
                    Card.EAT_SALAD -> "salad"
                    Card.SWAP_CARROTS -> "tausch"
                }) })
            }
        }
    }
    
    private fun <T: Node> putOnPosition(node: T, position: Int): T {
        grid.add(node, position % BOARDSIZE, position / BOARDSIZE)
        return node
    }
    
    private fun createImage(graphic: String, scale: Double? = null) =
        ResizableImageView(scale?.let { graphicSize.multiply(scale) } ?: graphicSize)
            .also { it.image = resources.image("/hui/${graphic.lowercase()}.png") }
    
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