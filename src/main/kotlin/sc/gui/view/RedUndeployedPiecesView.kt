package sc.gui.view

import sc.gui.model.Piece
import sc.gui.model.PlayerColor
import tornadofx.View
import tornadofx.pane

class RedUndeployedPiecesView: View() {
    val pieces = PiecesFragment()
    override val root = pane {
        add(pieces)
    }

    fun selectedItem(): Piece? {
        return pieces.selectedItem()
    }
}
