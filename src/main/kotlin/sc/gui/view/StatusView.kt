package sc.gui.view

import javafx.beans.binding.StringBinding
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import sc.api.plugins.ITeam
import sc.api.plugins.Team
import sc.gui.AppStyle
import sc.gui.model.GameModel
import sc.gui.strings
import tornadofx.*

class StatusBinding(private val game: GameModel): StringBinding() {
    init {
        bind(game.gameStarted, game.currentTeam, game.gameResult, game.playerNames, game.atLatestTurn)
    }
    
    override fun computeValue(): String =
            if(game.gameStarted.value && game.atLatestTurn.value || game.gameResult.value != null)
                game.gameResult.takeIf { game.atLatestTurn.value }?.get()?.let { gameResult ->
                    """
                    ${gameResult.win?.winner?.let { "${it.displayName} hat gewonnen!" } ?: "Unentschieden"}
                    ${gameResult.win?.reason?.message?.replace(" brig", " übrig").orEmpty()}
                    """.trimIndent().trim('\n')
                } ?: "${game.currentTeam.value.displayName} am Zug"
            else game.playerNames.joinToString(" vs ")
    
    val ITeam.displayName
        get() = index.let { game.playerNames.getOrNull(it) ?: "Spieler ${it + 1}" }
}

class ScoreBinding(private val game: GameModel): StringBinding() {
    init {
        bind(game.gameStarted, game.gameState)
    }
    
    override fun computeValue(): String =
            if(game.gameStarted.value)
                "Runde ${game.currentRound.get()} - " +
                        game.gameState.value?.run {
                            Team.values().joinToString(" : ") {
                                getPointsForTeam(it).first().toString()
                            }
                        }
            else "Drücke auf Start".takeUnless { game.gameOver.value && game.atLatestTurn.value }.orEmpty()
}

class StatusView: View() {
    private val game: GameModel by inject()
    
    override val root = hbox {
        useMaxWidth = true
        alignment = Pos.CENTER
        label(playerLabel(Team.ONE))
        vbox(alignment = Pos.CENTER) {
            this.spacing = AppStyle.spacing
            runLater {
                prefWidthProperty().bind(scene.widthProperty().divide(2))
                hgrow = Priority.ALWAYS
                maxWidth = AppStyle.fontSizeRegular.value * 60
            }
            addClass(AppStyle.statusLabel)
            label(StatusBinding(game)) {
                textAlignment = TextAlignment.CENTER
                isWrapText = true
            }
            label(ScoreBinding(game))
        }
        label(playerLabel(Team.TWO))
    }
    
    fun playerLabel(team: Team) =
            game.gameState.stringBinding { state ->
                state?.teamStats(team)?.takeUnless { it.isEmpty() }?.let { stats ->
                    stats.joinToString("\n", "${game.playerNames[team.index]} (${strings["color.${team.color}"]})\n") { stat ->
                        "${stat.label}: ${stat.icon?.let { if(stat.value > 0) it.repeat(stat.value) else "-" } ?: stat.value}"
                    }
                }
            }
}