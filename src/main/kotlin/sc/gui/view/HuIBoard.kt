package sc.gui.view

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.geometry.Orientation
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.effect.ColorAdjust
import javafx.scene.input.KeyEvent
import javafx.scene.layout.*
import javafx.util.Duration
import sc.api.plugins.Team
import sc.gui.AppStyle
import sc.gui.util.listenImmediately
import sc.plugin2025.*
import sc.plugin2025.Field
import sc.plugin2025.util.HuIConstants
import tornadofx.*
import kotlin.math.*

class HuIBoard: GameBoard<GameState>() {
    val grid = AnchorPane()
    val cards = Array(2) { VBox() }
    
    private val graphicSize = squareSize.doubleBinding {
        minOf(
            root.width.div(12 + 4 /* cards on the sides */),
            viewHeight / 12
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
        createImage("player_" + it.color, 0.8).apply {
            effect = playerEffects[it.index]
            isMouseTransparent = true
        }
    }
    private val toClear = ArrayList<Node>()
    
    override fun onNewState(oldState: GameState?, state: GameState?) {
        if(oldState?.board != state?.board) {
            grid.clear()
            (state ?: return).board.fields.withIndex().forEach { (index, field) ->
                logger.trace { "Adding Field $field" }
                fields[index] = putOnPosition(
                    createImage(("field_") + field.name).apply {
                        isPickOnBounds = true
                    },
                    index,
                    false
                )
                if(index != 0)
                    runLater { // So the labels are in front of the fields
                        putOnPosition(
                            Label(index.toString()).apply { isMouseTransparent = true },
                            index,
                            false
                        )
                    }
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
            state.succeedsState(it) && state.lastMove is Advance
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
            piece.translateX = coords.x - piece.layoutX
            piece.translateY = coords.y - piece.layoutY
            piece.isVisible = true
            parallelTransition {
                this.children.add(piece.move(
                    Duration.seconds(animFactor),
                    destination = Point2D.ZERO,
                    play = false,
                ) {
                    setOnFinished {
                        if(state == gameState)
                            highlightPiece(state.currentTeam)
                    }
                })
                oldState?.takeIf { state.succeedsState(it) }?.let { old ->
                    state.players.forEach { player ->
                        var carrotDiff = player.carrots - old.getHare(player.team).carrots
                        val m = state.lastMove
                        if(m is Advance && player.team == old.currentTeam)
                            carrotDiff += m.cost
                        if(carrotDiff != 0) {
                            val label = carrotCost(carrotDiff, player.position)
                            this.children.addAll(
                                label.move(
                                    Duration.seconds(animFactor),
                                    Point2D(0.0, graphicSize.value * -1.0),
                                    play = false,
                                ),
                                label.fade(
                                    Duration.seconds(animFactor * 2),
                                    0.0,
                                    play = false,
                                )
                            )
                        }
                    }
                }
            }
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
            children.addAll(state.getHare(activeTeam).getCards().map { createImage(it.graphicName(), 1.5) })
        }
    }
    
    private var spiralFactor = 0.0
    /** Translated from https://stackoverflow.com/questions/78472829/equidistant-points-along-an-archimedean-spiral-with-fixed-gap-between-points-and*/
    private fun spiral(radius: Double, numCycles: Double, nPoints: Int): DoubleArray {
        val dr = radius / numCycles
        val thetaMax = 2 * PI * numCycles
        val a = radius / thetaMax
        spiralFactor = radius / (2 * PI * numCycles.toInt())
        val sMax = (a / 2) * (thetaMax * sqrt(1 + thetaMax.pow(2)) + ln(thetaMax + sqrt(1 + thetaMax.pow(2))))
        val s = DoubleArray(nPoints) { dr / 2 + it * (sMax - dr / 2) / nPoints }
        
        val theta = DoubleArray(nPoints)
        for(i in s.indices) {
            var t = 0.0
            var told = t + 1
            while(abs(t - told) > 1.0e-10) {
                told = t
                t = sqrt((-1 + sqrt(1 + 4 * (2 * s[i] / a - ln(t + sqrt(1 + t.pow(2)))).pow(2))) / 2)
            }
            theta[i] = t
        }
        return theta
    }
    
    val spiralRadius = 5.0
    val archimedeanSpiral = spiral(spiralRadius, 3.65, HuIConstants.NUM_FIELDS).reversed()
    
    private fun <T: Node> putOnPosition(node: T, position: Int, clear: Boolean = true): T {
        if(clear) {
            grid.children.remove(node)
            toClear.add(node)
        }
        grid.add(node)
        val pos = archimedeanSpiral[position]
        graphicSize.listenImmediately { value ->
            val adjustedValue = value.toDouble()
            node.anchorpaneConstraints {
                leftAnchor = adjustedValue * (spiralFactor * pos * cos(pos) + spiralRadius)
                bottomAnchor = adjustedValue * (spiralFactor * pos * sin(pos) + spiralRadius)
            }
        }
        return node
    }
    
    private fun createImage(graphic: String, scale: Double? = null) =
        ResizableImageView(scale?.let { graphicSize.multiply(scale) } ?: graphicSize)
            .also { it.image = resources.image(huiGraphic(graphic)) }
    
    override fun renderHumanControls(state: GameState) {
        if(state.mustEatSalad()) {
            val pos = state.currentPlayer.position
            fields[pos].onClickMove(EatSalad)
            putOnPosition(
                Button("Salat fressen").apply {
                    translateYProperty().bind(graphicSize.divide(-2))
                    addClass("small")
                    onLeftClick { sendHumanMove(EatSalad) }
                },
                pos
            )
            return
        }
        
        state.possibleExchangeCarrotMoves().forEach { car ->
            putOnPosition(
                Button(carrotCostString(car.amount)).apply {
                    translateX = AppStyle.spacing
                    translateYProperty().bind(graphicSize.multiply(-car.amount / 20.0 - .2))
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
                    val flow = FlowPane(Orientation.HORIZONTAL).apply {
                        maxWidthProperty().bind(graphicSize)
                        hgap = AppStyle.miniSpacing
                        vgap = AppStyle.miniSpacing
                    }
                    var totalCards = 0
                    state.possibleCardMoves(distance)?.also {
                        node.darken()
                        putOnPosition(
                            Group(Group(flow).apply { isManaged = false }).apply {
                                translateX = AppStyle.spacing
                                translateY = -AppStyle.formSpacing
                            },
                            targetPos
                        )
                        totalCards = it.sumOf { it.getCards().size }
                    }?.forEach { advance ->
                        val cards = advance.getCards()
                        var suffix = ""
                        if(state.board.getField(targetPos) == Field.MARKET ||
                           cards.getOrNull(cards.lastIndex - 1)?.let {
                               val clone = state.clonePlayer()
                               it.play(clone)
                               clone.currentField == Field.MARKET
                           } == true
                        ) {
                            suffix = " kaufen"
                        }
                        flow.add(Button().apply {
                            paddingAll = AppStyle.miniSpacing
                            hbox {
                                cards.map {
                                    add(ResizableImageView(graphicSize.multiply(.6 / totalCards + if(suffix.isEmpty()) .15 else .1)).apply {
                                        image = resources.image(huiGraphic(it.graphicName()))
                                    })
                                }
                                if(suffix.isNotEmpty())
                                    children.add(children.size - 1, Label("+").hboxConstraints {
                                        alignment = Pos.CENTER
                                    })
                            }
                            tooltip = Tooltip(cards.joinToString(" dann ") { it.label } + suffix)
                            onLeftClick { sendHumanMove(advance) }
                        })
                    } ?: node.onClickMove(Advance(distance))
                    carrotCost(-GameRuleLogic.calculateCarrots(distance), targetPos)
                }
            }
        }
    }
    
    private fun Node.onClickMove(move: Move) {
        darken()
        onLeftClick { sendHumanMove(move) }
    }
    
    private fun Node.darken() {
        effect = ColorAdjust().apply { brightness = contrastFactor * -0.6 }
    }
    
    private fun carrotCost(value: Int, position: Int) =
        putOnPosition(Label(carrotCostString(value)).apply {
            translateY = -.8 * graphicSize.value
            isMouseTransparent = true
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
    "card_" + this.name.replace("_", "")