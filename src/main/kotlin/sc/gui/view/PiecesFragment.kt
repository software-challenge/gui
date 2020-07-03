package sc.gui.view

import sc.plugin2021.Piece
import sc.gui.model.UndeployedPiecesModel
import tornadofx.Fragment
import tornadofx.listview
import tornadofx.selectedItem

class PiecesFragment: Fragment() {
    val model: UndeployedPiecesModel by inject()
    override val root = listview(model.undeployedPieces) {

    }

    fun selectedItem(): Piece? {
        return root.selectedItem
    }
}
