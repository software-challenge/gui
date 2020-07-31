package sc.gui.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import sc.gui.view.PiecesFragment
import sc.plugin2021.*
import tornadofx.ItemViewModel

class UndeployedPiecesModel(color: Color): ItemViewModel<PiecesFragment>() {
    val undeployedPieces: ObservableList<Piece> = FXCollections.observableArrayList()

    init {
        for (shape in PieceShape.shapes.keys) {
            undeployedPieces.add(Piece(color, shape, Rotation.NONE))
        }
    }
}