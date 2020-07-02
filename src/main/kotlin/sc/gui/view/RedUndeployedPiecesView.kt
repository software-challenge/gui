package sc.gui.view

import sc.data.Piece
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
