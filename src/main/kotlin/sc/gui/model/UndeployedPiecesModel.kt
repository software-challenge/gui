package sc.gui.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import sc.gui.view.PiecesFragment
import sc.plugin2021.*
import tornadofx.ItemViewModel

class UndeployedPiecesModel(color: PlayerColor): ItemViewModel<PiecesFragment>() {
    val undeployedPieces: ObservableList<Piece> = FXCollections.observableArrayList()

    init {
        for (shape in PieceShape.shapes) {
            undeployedPieces.add(Piece(shape.first, Rotation.NONE, color))
        }
    }
}