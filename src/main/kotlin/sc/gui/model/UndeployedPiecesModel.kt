package sc.gui.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import sc.gui.view.PiecesFragment
import tornadofx.ItemViewModel

enum class PlayerColor {
    RED, GREEN, BLUE, YELLOW
}

class Piece(val kind: Int, val color: PlayerColor) {
    override fun toString(): String {
        return "$color Piece $kind"
    }
}

class UndeployedPiecesModel: ItemViewModel<PiecesFragment>() {
    val undeployedPieces: ObservableList<Piece> = FXCollections.observableArrayList()

    init {
        for (i in 0..4) {
            undeployedPieces.add(Piece(i, PlayerColor.values().random()))
        }
    }
}