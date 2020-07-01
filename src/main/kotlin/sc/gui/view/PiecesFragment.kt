package sc.gui.view

import javafx.scene.Parent
import sc.gui.model.Piece
import sc.gui.model.UndeployedPiecesModel
import tornadofx.*

class PiecesFragment: Fragment() {
    val model: UndeployedPiecesModel by inject()
    override val root = listview(model.undeployedPieces) {

    }

    fun selectedItem(): Piece? {
        return root.selectedItem
    }
}
