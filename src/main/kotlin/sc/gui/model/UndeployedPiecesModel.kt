package sc.gui.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import sc.data.Piece
import sc.data.PlayerColor
import sc.data.pieceShapes
import sc.gui.view.PiecesFragment
import tornadofx.ItemViewModel

class UndeployedPiecesModel: ItemViewModel<PiecesFragment>() {
    val undeployedPieces: ObservableList<Piece> = FXCollections.observableArrayList()

    init {
        for (shape in pieceShapes) {
            undeployedPieces.add(Piece(shape.id, PlayerColor.values().random()))
        }
    }
}