package sc.gui.view

import javafx.beans.binding.StringBinding
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import sc.api.plugins.ITeam
import sc.api.plugins.Team
import sc.gui.AppStyle
import sc.gui.model.AppModel
import sc.gui.model.GameModel
import sc.gui.strings
import tornadofx.*

fun decodeXmlEntities(toDecode: String): String {
    //    if ('&' !in toDecode) return toDecode
    // Replace potentially bad characters
    return toDecode.replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&#38;", "&")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
}

fun playerLabel(game: GameModel, team: Team) =
    Label().apply {
        // Add string to display for team including teamStats.
        textProperty().bind(
            game.gameState.stringBinding { state ->
                state?.teamStats(team)?.takeUnless { it.isEmpty() }?.let { stats ->
                    stats.joinToString(
                        "\n",
                        "${decodeXmlEntities(game.playerNames[team.index])} (${strings["color.${team.color}"]})\n"
                    ) { stat ->
                        "${stat.label} ${stat.icon?.let { if(stat.value > 0) it.repeat(stat.value) else "-" } ?: stat.value}"
                    }
                }
            })
        // Handle theming
        textFillProperty().bind(AppModel.darkMode.objectBinding {
            if(it == true)
                Color.hsb(Color.valueOf(team.color).hue, .4, 1.0)
            else
                Color.hsb(Color.valueOf(team.color).hue, .8, .6)
        })
        font = Font(AppStyle.fontSizeBig.value)
        
    }

class StatusBinding(private val game: GameModel): StringBinding() {
    init {
        bind(game.gameStarted, game.currentTeam, game.gameResult, game.playerNames, game.atLatestTurn)
    }
    
    override fun computeValue(): String =
        if(game.gameStarted.value && game.atLatestTurn.value || game.gameResult.value != null)
            game.gameResult.takeIf { game.atLatestTurn.value }?.get()?.let { gameResult ->
                """
                    ${gameResult.win?.winner?.let { "${decodeXmlEntities(it.displayName)} hat gewonnen!" } ?: "Unentschieden"}
                    ${decodeXmlEntities(gameResult.win?.reason?.message?.replace(" brig", " übrig").orEmpty())}
                    """.trimIndent().trim('\n')
            } ?: "${decodeXmlEntities(game.currentTeam.value.displayName)} am Zug"
        else game.playerNames.map { decodeXmlEntities(it) }.joinToString(" vs ")
    
    val ITeam.displayName
        get() = index.let { game.playerNames.getOrNull(it) ?: "Spieler ${it + 1}" }
}

class ScoreBinding(private val game: GameModel): StringBinding() {
    init {
        bind(game.gameStarted, game.gameState)
    }
    
    /**
     * A ScoreBinding should have the following computed String value:
     * Runde X - Punkte Team 1 : Punkte Team 2
     * This point order is inverted if the startTeam is Team.TWO.
     * This is only used for the finals.
     */
    override fun computeValue(): String {
        if(game.gameStarted.value) {
            return "Runde ${(game.currentTurn.get() + 1) / 2} - " +
                    game.gameState.value?.getPointsForTeam(Team.ONE)?.first().toString() +
                    " : " +
                    game.gameState.value?.getPointsForTeam(Team.TWO)?.first().toString()
        } else {
            return "Drücke auf Start".takeUnless { game.gameOver.value && game.atLatestTurn.value }.orEmpty()
        }
    }
}

class StatusView: View() {
    private val game: GameModel by inject()
    
    override val root = hbox {
        useMaxWidth = true
        alignment = Pos.CENTER
        // Moved to PlayerOne/TwoView
//        add(playerLabel(Team.ONE))
        vbox(alignment = Pos.CENTER) {
            this.spacing = AppStyle.fontSizeUnscaled.value
            runLater {
                prefWidthProperty().bind(scene.widthProperty().divide(2))
                hgrow = Priority.ALWAYS
                maxWidth = AppStyle.fontSizeRegular.value * 90
                prefHeightProperty().bind(scene.heightProperty().divide(6))
            }
            addClass(AppStyle.statusLabel)
            label(StatusBinding(game)) {
                textAlignment = TextAlignment.CENTER
                isWrapText = true
            }
            label(ScoreBinding(game))
        }
        // Moved to PlayerOne/TwoView
//        add(playerLabel(Team.TWO))
        
        //runLater {
        //    scene.root.apply {
        //        add(Label().apply {
        //            textProperty().bind(playerLabel(Team.ONE))
        //            stackpaneConstraints {
        //                alignment = Pos.TOP_LEFT
        //            }
        //        })
        //        add(Label().apply {
        //            textProperty().bind(playerLabel(Team.TWO))
        //            stackpaneConstraints {
        //                alignment = Pos.TOP_RIGHT
        //            }
        //        })
        //    }
        //}
    }
    
    fun playerLabel(team: Team) =
        Label().apply {
            textProperty().bind(
                game.gameState.stringBinding { state ->
                    state?.teamStats(team)?.takeUnless { it.isEmpty() }?.let { stats ->
                        stats.joinToString(
                            "\n",
                            "${game.playerNames[team.index]} (${strings["color.${team.color}"]})\n" // FIXME change based on color
                        ) { stat ->
                            "${stat.label} ${stat.icon?.let { if(stat.value > 0) it.repeat(stat.value) else "-" } ?: stat.value}"
                        }
                    }
                })
            textFillProperty().bind(AppModel.darkMode.objectBinding {
                if(it == true)
                    Color.hsb(Color.valueOf(team.color).hue, .4, 1.0)
                else
                    Color.hsb(Color.valueOf(team.color).hue, .8, .6)
            })
        }
}