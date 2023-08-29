package sc.gui.view

import javafx.beans.binding.StringBinding
import javafx.geometry.Pos
import javafx.scene.text.TextAlignment
import sc.api.plugins.ITeam
import sc.gui.AppStyle
import sc.gui.model.GameModel
import sc.shared.GameResult
import sc.shared.ScoreCause
import tornadofx.*

class StatusBinding(private val game: GameModel): StringBinding() {
    init {
        bind(game.gameStarted, game.currentTeam, game.gameResult, game.playerNames, game.atLatestTurn)
    }
    
    fun winner(gameResult: GameResult): String =
            gameResult.winner?.let { "${it.displayName} hat gewonnen!" }
            ?: "Unentschieden!"
    
    fun irregularities(gameResult: GameResult): String? =
            gameResult.scores.values.firstNotNullOfOrNull { score ->
                when(score.cause) {
                    ScoreCause.LEFT -> "Grund: Vorzeitiges Verlassen des Spiels"
                    ScoreCause.RULE_VIOLATION -> "Grund: Regelverletzung"
                    ScoreCause.SOFT_TIMEOUT -> "Grund: Überschreitung des Zeitlimits"
                    ScoreCause.HARD_TIMEOUT -> "Grund: Keine Antwort auf Zuganfrage"
                    ScoreCause.UNKNOWN -> "Grund: Kommunikationsfehler"
                    else -> null
                }
            }
    
    override fun computeValue(): String =
            if(game.gameStarted.value && game.atLatestTurn.value)
                game.gameResult.get()?.let { gameResult ->
                    """
                    ${winner(gameResult)}
                    ${irregularities(gameResult).orEmpty()}
                    ${gameResult.scores.values.joinToString("") { it.reason }}
                    """.trimIndent().trim('\n')
                } ?: "${game.currentTeam.value.displayName} ist dran"
            else game.playerNames.joinToString(" vs ")
    
    val ITeam.displayName
        get() = index.let { game.playerNames.getOrNull(it) ?: "Spieler ${it + 1}" }
}

class ScoreBinding(private val game: GameModel): StringBinding() {
    init {
        bind(game.gameStarted, game.teamScores)
    }
    
    override fun computeValue(): String =
            if(game.gameStarted.value)
                "Runde ${game.currentRound.get()} - " +
                game.teamScores.value?.joinToString(" : ") { it?.first().toString() }
            else "Drücke auf Start"
}

class StatusView: View() {
    private val game: GameModel by inject()
    
    override val root = vbox(alignment = Pos.CENTER) {
        addClass(AppStyle.statusLabel)
        label(StatusBinding(game)) {
            textAlignment = TextAlignment.CENTER
            isWrapText = true
        }
        label(ScoreBinding(game))
    }
}