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

class PlayerTwoView(gameController: BlokusController, gridSize: DoubleBinding): View() {
    private val game: GameModel by inject()
    
    // This is a vbox since all elements are below each other.
    override val root = vbox {
        useMaxWidth = true
        alignment = Pos.TOP_CENTER
        add(playerLabel(game, Team.TWO))
        add(UndeployedPiecesFragment(
            Color.YELLOW,
            gameController.undeployedPieces.getValue(Color.YELLOW),
            gameController.validPieces.getValue(Color.YELLOW),
            gridSize
        ))
        add(UndeployedPiecesFragment(
            Color.GREEN,
            gameController.undeployedPieces.getValue(Color.GREEN),
            gameController.validPieces.getValue(Color.GREEN),
            gridSize
        ))
        add(SkipMoveButton(gameController, gridSize, Team.TWO))
        // Run this later since scene is not ready yet
        runLater {
            this.padding = Insets(0.0, 0.0, 0.0, 0.0)
        }
    }
}