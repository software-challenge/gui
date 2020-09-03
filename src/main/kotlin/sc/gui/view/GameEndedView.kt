package sc.gui.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import sc.gui.controller.AppController
import sc.gui.controller.GameController
import sc.plugin2021.util.Constants
import sc.shared.GameResult
import sc.shared.PlayerScore
import tornadofx.*

class GameEndedView : View() {
    private val gameController: GameController by inject()
    private val appController: AppController by inject()
    private var result: GameResult? = null

    private val gameResult = label {
        padding = Insets(0.0, 0.0, 10.0, 0.0)
        style {
            fontSize = 32.px
            textFill = Color.GOLD
        }
    }
    private val player1Name = label()
    private val player1Score = label()
    private val player2Name = label()
    private val player2Score = label()
    private val points = gridpane {
        vgrow = Priority.ALWAYS
        hgrow = Priority.NEVER

        row {
            this += player1Name
            this += player1Score
        }
        row {
            this += player2Name
            this += player2Score
        }
        constraintsForColumn(1).percentWidth = 60.0
        constraintsForColumn(2).percentWidth = 40.0
    }
    private val leftPane = vbox {
        paddingAll = 10.0
        style {
            backgroundColor += Color.GRAY
        }

        vbox {
            alignment = Pos.TOP_CENTER
            style {
                backgroundColor += Color.CYAN
            }

            this += gameResult
            this += points
        }
        vbox {
            alignment = Pos.BOTTOM_CENTER
            padding = Insets(0.0, 0.0, 10.0, 0.0)
            prefHeight = Double.MAX_VALUE
            useMaxHeight = true
            vgrow = Priority.ALWAYS
            style {
                backgroundColor += Color.FLORALWHITE
            }

            button {
                text = "Spiel beenden"

                action {
                    appController.changeViewTo(StartView::class)
                    gameController.clearGame()
                }
            }
        }
    }
    val game = borderpane {
    }
    override val root = hbox {
        fitToParentWidth()
        alignment = Pos.TOP_CENTER

        this += leftPane
        this += game
    }

    fun gameEnded(result: GameResult) {
        this.result = result
        if (result.winners.size > 1) {
            gameResult.text = "Unentschieden"
        } else {
            gameResult.text = result.winners.first().displayName + " hat gewonnen"
        }

        val player1: PlayerScore
        val player2: PlayerScore
        if (result.scores.first().cause.ordinal > result.scores.last().cause.ordinal) {
            player1 = result.scores.first()
            player2 = result.scores.last()
        } else {
            player1 = result.scores.last()
            player2 = result.scores.first()
        }

        player1Name.text = player1.reason
        player1Score.text = player1.cause.name
        player2Name.text = player2.reason
        player2Score.text = player2.cause.name
    }

    fun resize() {
        val width = root.widthProperty().get()
        var height = root.heightProperty().get()
        val app = find(AppView::class).root
        // 50px buffer for menubar etc.
        if (app.height - 50 < root.height) {
            height = app.height - 50
        }
        leftPane.prefWidthProperty().set(width * 0.3)
        game.prefWidthProperty().set(width * 0.7)

        val size = minOf(width * 0.7, height)

        val board = find(BoardView::class)
        board.grid.setMaxSize(size, size)
        board.grid.setMinSize(size, size)
        board.model.calculatedBlockSizeProperty().set(size / Constants.BOARD_SIZE)
    }

    override fun onDock() {
        super.onDock()
        resize()
    }

    init {
        val resizer = ChangeListener<Number> { _, _, _ -> resize() }
        // responsive scaling
        root.widthProperty().addListener(resizer)
        root.heightProperty().addListener(resizer)
    }
}