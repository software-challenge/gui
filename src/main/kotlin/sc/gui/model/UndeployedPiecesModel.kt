package sc.gui.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import sc.gui.view.PiecesListFragment
import sc.plugin2021.*
import tornadofx.ItemViewModel

class UndeployedPiecesModel(val color: Color) : ItemViewModel<PiecesListFragment>() {
    var undeployedPieces: ObservableList<Piece> = FXCollections.observableArrayList()

    init {
        for (shape in PieceShape.shapes.keys) {
            undeployedPieces.add(Piece(color, shape, Rotation.NONE))
        }
    }

    fun update(shapes: Set<PieceShape> ) {
        undeployedPieces.clear()
        undeployedPieces.addAll(shapes.map { Piece(color, it, Rotation.NONE) })
    }
}