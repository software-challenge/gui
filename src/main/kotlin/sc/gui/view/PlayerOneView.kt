package sc.gui.view

import javafx.beans.binding.DoubleBinding
import javafx.geometry.Insets
import javafx.geometry.Pos
import sc.api.plugins.Team
import sc.gui.controller.BlokusController
import sc.gui.model.GameModel
import sc.gui.view.game.SkipMoveButton
import sc.plugin2027.Color
import tornadofx.*

class PlayerOneView(gameController: BlokusController, gridSize: DoubleBinding): View() {
    private val game: GameModel by inject()
    
    // This is a vbox since all elements are below each other.
    override val root = vbox {
        useMaxWidth = true
        alignment = Pos.TOP_CENTER
        add(playerLabel(game, Team.ONE))
        add(UndeployedPiecesFragment(
            Color.BLUE,
            gameController.undeployedPieces.getValue(Color.BLUE),
            gameController.validPieces.getValue(Color.BLUE),
            gridSize
        ))
        add(UndeployedPiecesFragment(
            Color.RED,
            gameController.undeployedPieces.getValue(Color.RED),
            gameController.validPieces.getValue(Color.RED),
            gridSize
        ))
        add(SkipMoveButton(gameController, gridSize, Team.ONE))
        // Run this later since scene is not ready yet
        runLater {
            this.padding = Insets(0.0, 0.0, 0.0, 0.0)
        }
    }
}