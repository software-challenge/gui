package sc.gui.view

import javafx.geometry.Orientation
import sc.gui.controller.GameController
import sc.gui.model.UndeployedPiecesModel
import tornadofx.*

class PiecesListFragment(model: UndeployedPiecesModel) : Fragment() {
    val controller: GameController by inject()

    override val root = flowpane {
        hgap = 4.0
        vgap = 4.0

        // fill column by column and not row by row
        orientation = Orientation.VERTICAL

        children.bind(model.undeployedPieces) {
            hbox {
                add(PiecesFragment(it))
            }
        }
    }
}
