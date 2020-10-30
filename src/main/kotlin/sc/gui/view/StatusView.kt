package sc.gui.view

import javafx.beans.binding.StringBinding
import javafx.geometry.Pos
import javafx.scene.control.Label
import sc.gui.AppStyle
import sc.gui.controller.GameController
import sc.plugin2021.Color
import sc.plugin2021.Team
import tornadofx.*

class StatusBinding(private val game: GameController) : StringBinding() {
    init {
        bind(game.currentTurn)
        bind(game.currentTeam)
        bind(game.isHumanTurn)
        bind(game.currentColor)
        bind(game.gameEnded)
    }

    fun translateColor(color: Color): String {
        return when(color) {
            Color.RED -> "Rot"
            Color.GREEN -> "Grün"
            Color.YELLOW -> "Gelb"
            Color.BLUE -> "Blau"
        }

    }
    
    override fun computeValue(): String {
        if(game.currentTurn.get() > 0) {
            if(game.gameEnded.get()) {
                return "Spiel ist beendet"
            } else {
                val team = when(game.currentTeam.get()) {
                    Team.ONE -> "Erstes Team"
                    Team.TWO -> "Zweites Team"
                }
                return "$team, Farbe " + translateColor(game.currentColor.get()) + " ist dran (Zug ${game.currentTurn.get()})"
            }
        }
        return "Drücke auf Start"
    }
}

class ScoreBinding(private val game: GameController) : StringBinding() {
    init {
        bind(game.teamOneScore)
        bind(game.teamTwoScore)
    }

    override fun computeValue(): String {
        return "${game.teamOneScore.get()} : ${game.teamTwoScore.get()}"
    }
}

class StatusView() : View() {
    private val game: GameController by inject()
    private val scoreLabel = Label()
    private val statusLabel = Label()

    override val root = hbox {
        alignment = Pos.CENTER
        scoreLabel.addClass(AppStyle.statusLable)
        statusLabel.addClass(AppStyle.statusLable)
        this += vbox {
            alignment = Pos.CENTER
            this += scoreLabel
            this += statusLabel
        }
    }

    init {
        statusLabel.bind(StatusBinding(game))
        scoreLabel.bind(ScoreBinding(game))
    }
}