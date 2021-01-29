package sc.gui.view

import javafx.beans.binding.StringBinding
import javafx.geometry.Pos
import javafx.scene.control.Label
import sc.gui.AppStyle
import sc.gui.controller.GameController
import sc.plugin2021.Color
import sc.plugin2021.Team
import sc.shared.GameResult
import sc.shared.ScoreCause
import tornadofx.*

class StatusBinding(private val game: GameController) : StringBinding() {
    init {
        bind(game.started)
        bind(game.currentTeam)
        bind(game.currentColor)
        bind(game.playerNames)
        bind(game.gameResult)
    }

    fun winner(gameResult: GameResult): String = gameResult.winners?.firstOrNull()?.let { player ->
        player.displayName +
        " (" + (player.color as Team).colors.joinToString("+", transform = Color::german) + ") hat gewonnen!"
    } ?: "Unentschieden!"

    fun irregularities(gameResult: GameResult): String {
        loop@ for (score in gameResult.scores) {
            when(score.cause) {
                ScoreCause.REGULAR -> continue@loop
                ScoreCause.LEFT -> return "Grund: Vorzeitiges Verlassen des Spiels"
                ScoreCause.RULE_VIOLATION -> return "Grund: Regelverletzung"
                ScoreCause.SOFT_TIMEOUT -> return "Grund: Überschreitung des Zeitlimits"
                ScoreCause.HARD_TIMEOUT -> return "Grund: Keine Antwort auf Zuganfrage"
                ScoreCause.UNKNOWN -> return "Grund: Kommunikationsfehler"
            }
        }
        return ""
    }

    override fun computeValue(): String {
        if(!game.started.value)
            return "Drücke auf Start"
        return game.gameResult.get()?.let { gameResult -> """
                Spiel ist beendet
                ${winner(gameResult)}
                ${irregularities(gameResult)}
            """.trimIndent()
        } ?: "${game.playerNames.get()?.get(game.currentTeam.get().index) ?: game.currentTeam.get()}, ${game.currentColor.get()} ist dran"
    }
}

class ScoreBinding(private val game: GameController) : StringBinding() {
    init {
        bind(game.currentRound)
        bind(game.teamOneScore)
        bind(game.teamTwoScore)
    }

    override fun computeValue(): String {
        return "Runde ${game.currentRound.get()} - ${game.teamOneScore.get()} : ${game.teamTwoScore.get()}"
    }
}

class StatusView : View() {
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