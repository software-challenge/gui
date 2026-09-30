package sc.gui.view.game

import javafx.beans.binding.Bindings
import javafx.beans.binding.DoubleBinding
import javafx.scene.control.Button
import sc.api.plugins.Team
import sc.gui.controller.BlokusController
import sc.plugin2027.SkipMove
import tornadofx.onLeftClick

class SkipMoveButton(
	private val gameController: BlokusController,
	gridSize: DoubleBinding,
    team: Team
) : Button("Aussetzen") {

	init {
		prefWidthProperty().bind(gridSize.multiply(5))
        // Disable button if the current turn is not from a human player,
        // we cannot skip (i.e. the first round),
        // or if it is not our turn.
		disableProperty().bind(Bindings.createBooleanBinding(
			{ !(gameController.isHumanTurn.value && gameController.canSkip.value && gameController.currentColor.value.team == team) },
			gameController.isHumanTurn,
			gameController.canSkip,
			gameController.currentColor
		))
		onLeftClick {
			gameController.sendHumanMove(SkipMove(gameController.currentColor.value))
		}
	}
}