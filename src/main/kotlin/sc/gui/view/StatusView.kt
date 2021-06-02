package sc.gui.view

import javafx.beans.binding.StringBinding
import javafx.geometry.Pos
import javafx.scene.control.Label
import sc.api.plugins.Team
import sc.gui.AppStyle
import sc.gui.controller.GameController
import sc.shared.GameResult
import sc.shared.ScoreCause
import tornadofx.*

class StatusBinding(private val game: GameController): StringBinding() {
    init {
        bind(game.gameStarted, game.currentTeam, game.gameResult)
    }
    
    fun winner(gameResult: GameResult): String = gameResult.winners?.firstOrNull()?.let { player ->
        "$player hat gewonnen!"
    } ?: "Unentschieden!"
    
    fun irregularities(gameResult: GameResult): String {
        loop@ for (score in gameResult.scores) {
            when (score.cause) {
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
        if (!game.gameStarted.value)
            return Team.values().joinToString(" vs ")
        return game.gameResult.get()?.let { gameResult ->
            """
                Spiel ist beendet
                ${winner(gameResult)}
                ${irregularities(gameResult)}
            """.trimIndent()
        } ?: "${game.currentTeam.value} ist dran"
    }
}

class ScoreBinding(private val game: GameController): StringBinding() {
    init {
        bind(game.currentRound)
        bind(game.teamScores)
    }
    
    override fun computeValue(): String {
        if (game.currentRound.get() == 0)
            return "Drücke auf Start"
        return "Runde ${game.currentRound.get()} - " +
               game.teamScores.value?.joinToString(" : ")
    }
}

class StatusView: View() {
    private val game: GameController by inject()
    private val scoreLabel = Label()
    private val statusLabel = Label()
    
    override val root = hbox {
        alignment = Pos.CENTER
        scoreLabel.addClass(AppStyle.statusLabel)
        statusLabel.addClass(AppStyle.statusLabel)
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