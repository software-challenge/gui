package sc.gui.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import sc.gui.controller.GameController
import sc.shared.GameResult
import sc.shared.PlayerScore
import tornadofx.*

class GameEndedView : View() {
    private val gameController: GameController by inject()
    private var result: GameResult? = null

    private val gameResult = label {
        padding = Insets(0.0, 0.0, 10.0, 0.0)
        style {
            fontSize = 24.px
            textFill = Color.GOLDENROD
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
            add(player1Name, 1, 1)
            add(player1Score, 2, 1)
        }
        row {
            add(player2Name, 1, 2)
            add(player2Score, 2, 2)
        }
        constraintsForColumn(1).percentWidth = 60.0
        constraintsForColumn(2).percentWidth = 40.0
    }
    private val leftPane = hbox {
        alignment = Pos.CENTER
        this += gameResult
        this += points
    }
    private val rightPane = hbox {
        this += find(BoardView::class)
    }
    override val root = vbox {
        paddingAll = 10.0
        alignment = Pos.CENTER

        this += leftPane
        this += rightPane
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
}