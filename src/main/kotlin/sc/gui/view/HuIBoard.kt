package sc.gui.view

import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.effect.ColorAdjust
import javafx.scene.effect.Glow
import javafx.scene.image.Image
import javafx.scene.image.ImageView
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
                children.addAll(player.getCards().map { createImage(it.graphicName()) })
            }
        }
    }
    
    private fun <T: Node> putOnPosition(node: T, position: Int): T {
        grid.add(node, position % BOARDSIZE, position / BOARDSIZE)
        return node
    }
    
    private fun createImage(graphic: String, scale: Double? = null) =
        ResizableImageView(scale?.let { graphicSize.multiply(scale) } ?: graphicSize)
            .also { it.image = resources.image(huiGraphic(graphic)) }
    
    override fun renderHumanControls(state: GameState) {
        if(state.mustEatSalad()) {
            fields[state.currentPlayer.position].onClickMove(EatSalad)
            return
        }
        
        state.possibleExchangeCarrotMoves().forEach { car ->
            putOnPosition(
                Button(carrotCostString(car.amount)).apply {
                    translateYProperty().bind(graphicSize.doubleBinding {
                        -car.amount * (it?.toDouble()?.div(20) ?: 3.0)
                    })
                    onLeftClick { sendHumanMove(car) }
                },
                state.currentPlayer.position
            )
        }
        
        val currentPos = state.currentPlayer.position
        val maxAdvance = GameRuleLogic.calculateMoveableFields(state.currentPlayer.carrots)
        val fallBack = state.nextFallBack()
        
        fields.forEachIndexed { targetPos, node ->
            val distance = targetPos - currentPos
            when {
                distance <= 0 -> {
                    if(fallBack != targetPos)
                        return@forEachIndexed
                    node.onClickMove(FallBack)
                    carrotCost(distance * -10, targetPos)
                }
                
                else -> {
                    if(currentPos + maxAdvance < targetPos || state.checkAdvance(distance) != null)
                        return@forEachIndexed
                    state.possibleCardMoves(distance)?.forEachIndexed { index, advance ->
                        putOnPosition(
                            Button(
                                advance.getCards().joinToString(" dann\n") { it.label }, HBox().apply {
                                    advance.getCards().map {
                                        add(
                                            ImageView(
                                                Image(
                                                    resources.stream(huiGraphic(it.graphicName())),
                                                    AppStyle.fontSizeRegular.value,
                                                    AppStyle.fontSizeRegular.value,
                                                    true,
                                                    true
                                                )
                                            )
                                        )
                                    }
                                }
                            ).apply {
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
                            targetPos
                        )
                    } ?: node.onClickMove(Advance(distance))
                    carrotCost(-GameRuleLogic.calculateCarrots(distance), targetPos)
                }
            }
        }
    }
    
    private fun Node.onClickMove(move: Move) {
        effect = ColorAdjust(0.0, 0.0, -0.5, 0.0)
        onLeftClick { sendHumanMove(move) }
    }
    
    private fun carrotCost(value: Int, position: Int) =
        putOnPosition(Label(carrotCostString(value)).apply {
            gridpaneConstraints {
                this.hAlignment = HPos.CENTER
            }
        }, position)
    
    override fun handleKeyPress(state: GameState, keyEvent: KeyEvent): Boolean {
        return false
    }
    
}

private fun huiGraphic(graphic: String) =
    "/hui/${graphic.lowercase()}.png"

private fun carrotCostString(value: Int) =
    "▾ ${if(value > 0) "+" else ""}${value}"

private fun Card.graphicName() =
    "hasenjoker_" + when(this) {
        Card.FALL_BACK -> "backward"
        Card.HURRY_AHEAD -> "forward"
        Card.EAT_SALAD -> "salad"
        Card.SWAP_CARROTS -> "tausch"
    }