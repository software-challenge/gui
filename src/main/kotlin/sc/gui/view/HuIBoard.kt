package sc.gui.view

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.geometry.HPos
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.effect.ColorAdjust
import javafx.scene.image.Image
import javafx.scene.input.KeyEvent
import javafx.scene.layout.*
import javafx.util.Duration
import sc.api.plugins.Team
import sc.gui.AppStyle
import sc.plugin2025.*
import sc.plugin2025.util.HuIConstants
import tornadofx.*

private const val BOARDSIZE = 9

class HuIBoard: GameBoard<GameState>() {
    val grid = GridPane().apply {
        hgap = AppStyle.formSpacing
        vgap = AppStyle.formSpacing
        
    }
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
            runLater {
                card.prefWidthProperty().bind(graphicSize.multiply(2))
            }
        }
        this.children.add(1, grid)
    }
    
    private val emptyRegion = Region()
    private val fields: Array<Node> = Array(HuIConstants.NUM_FIELDS) { emptyRegion }
    private var timeline: Timeline? = null
    private val playerEffects = Team.values().map { ColorAdjust() }
    private val players = Team.values().map {
        createImage(it.color, 0.8).apply {
            effect = playerEffects[it.index]
        }
    }
    private val toClear = ArrayList<Node>()
    
    override fun onNewState(oldState: GameState?, state: GameState?) {
        if(oldState?.board != state?.board) {
            grid.clear()
            (state ?: return).board.fields.withIndex().forEach { (index, field) ->
                fields[index] = putOnPosition(createImage(field.name), index, false)
                if(index != 0)
                    putOnPosition(Label(index.toString()), index, false)
            }
        } else {
            toClear.forEach {
                grid.children.remove(it)
            }
            toClear.clear()
            fields.forEach {
                it.onMouseClicked = null
                it.effect = null
            }
        }
        
        if(state == null || oldState == state)
            return
        
        val animState = oldState?.takeIf {
            state.turn in arrayOf(it.turn + 1, it.turn + 2) && state.lastMove is Advance
        }
        Team.values().forEach { team ->
            putOnPosition(players[team.index], (animState ?: state).getHare(team).position)
        }
        
        fun highlightPiece(team: Team) {
            timeline {
                keyframe(Duration.ZERO) {
                    playerEffects.forEach {
                        keyvalue(it.brightnessProperty(), it.brightness)
                    }
                }
                keyframe(Duration.seconds(animFactor / 2)) {
                    keyvalue(
                        playerEffects[team.opponent().index].brightnessProperty(),
                        0.0
                    )
                    keyvalue(
                        playerEffects[team.index].brightnessProperty(),
                        contrastFactor / 2
                    )
                }
            }
        }
        
        val activeTeam = (animState ?: state).currentTeam
        val piece = players[activeTeam.index]
        highlightPiece(activeTeam)
        fun movePiece() {
            val finalPos = state.getHare(activeTeam).position
            val coords = Point2D(piece.layoutX, piece.layoutY)
            piece.isVisible = false
            putOnPosition(piece, finalPos)
            root.layout()
            piece.move(Duration.seconds(animFactor), Point2D.ZERO) {
                fromX = coords.x - piece.layoutX
                fromY = coords.y - piece.layoutY
                setOnFinished {
                    if(state == gameState)
                        highlightPiece(state.currentTeam)
                }
            }
            piece.isVisible = true
        }
        animState?.let { st ->
            val pos = st.currentPlayer.position
            runLater {
                timeline?.stop()
                timeline = timeline(play = true) {
                    val dist = (state.lastMove as Advance).distance
                    var frame = 0
                    keyFrames.add(
                        KeyFrame(Duration.seconds(animFactor),
                            {
                                logger.trace { "Animating $piece to $frame" }
                                if(frame < dist)
                                    putOnPosition(piece, pos + ++frame)
                            })
                    )
                    rate = dist.toDouble()
                    cycleCount = (dist * 1.3).toInt() + 1
                    setOnFinished { movePiece() }
                }
            }
        } ?: movePiece()
        
        cards[activeTeam.index].apply {
            clear()
            children.addAll(state.getHare(activeTeam).getCards().map { createImage(it.graphicName(), 1.6) })
        }
    }
    
    private fun <T: Node> putOnPosition(node: T, position: Int, clear: Boolean = true): T {
        if(clear) {
            grid.children.remove(node)
            toClear.add(node)
        }
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
                    var cardCount = 0
                    state.possibleCardMoves(distance)?.forEach { advance ->
                        val cards = advance.getCards()
                        putOnPosition(
                            Button(
                                cards.joinToString(" dann\n") { it.label },
                                HBox().apply {
                                    cards.map {
                                        imageview(
                                            Image(
                                                resources.stream(huiGraphic(it.graphicName())),
                                                AppStyle.fontSizeRegular.value,
                                                AppStyle.fontSizeRegular.value,
                                                true,
                                                true
                                            )
                                        )
                                    }
                                }
                            ).apply {
                                addClass("small")
                                val myCount = cardCount
                                translateYProperty().bind(
                                    graphicSize.doubleBinding {
                                        logger.trace { "Placing $this at $myCount" }
                                        AppStyle.fontSizeRegular.value * myCount -
                                        (it?.toDouble() ?: 10.0) / 3
                                    }
                                )
                                cardCount += cards.size + 1
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
        effect = ColorAdjust().apply { brightness = -contrastFactor / 2 }
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