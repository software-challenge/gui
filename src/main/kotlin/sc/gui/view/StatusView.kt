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
    }

    override fun computeValue(): String {
        val type = when (game.isHumanTurnProperty().get()) {
            true -> "Spieler"
            false -> "Computer"
        }
        return "$type " + game.turnColorProperty().get().name + " ist dran (Zug ${game.currentTurnProperty().get()})"
    }
}

class StatusView() : View() {
    private val game: GameController by inject()
    private val statusLabel = Label()

    override val root = hbox {
        alignment = Pos.CENTER
        statusLabel.style = "-fx-text-fill: white; -fx-font-size: 32pt;"
        this += statusLabel
    }

    init {
        statusLabel.bind(StatusBinding(game))
    }
}