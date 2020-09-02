package sc.gui.view

import javafx.beans.binding.StringBinding
import javafx.geometry.Pos
import javafx.scene.control.Label
import sc.gui.controller.GameController
import tornadofx.*

class StatusBinding(private val game: GameController) : StringBinding() {
    init {
        bind(game.currentTurnProperty())
        bind(game.isHumanTurnProperty())
        bind(game.turnColorProperty())
        bind(game.gameEndedProperty())
    }

    override fun computeValue(): String {
        if (game.gameStartedProperty().get()) {
            if (game.gameEndedProperty().get()) {
                return "Spiel ist beendet"
            } else {
                val type = when (game.isHumanTurnProperty().get()) {
                    true -> "Spieler"
                    false -> "Computer"
                }
                return "$type " + game.turnColorProperty().get().name + " ist dran (Zug ${game.currentTurnProperty().get()})"
            }
        }
        return "Drücke auf Start"
    }
}

class StatusView() : View() {
    private val game: GameController by inject()
    private val statusLabel = Label()

    override val root = hbox {
        alignment = Pos.CENTER
        statusLabel.style = "-fx-text-fill: white; -fx-font-size: 24pt;"
        this += statusLabel
    }

    init {
        statusLabel.bind(StatusBinding(game))
    }
}