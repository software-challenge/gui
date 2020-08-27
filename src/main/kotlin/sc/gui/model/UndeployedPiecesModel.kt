package sc.gui.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import sc.gui.view.PiecesListFragment
import sc.plugin2021.*
import tornadofx.ItemViewModel

class UndeployedPiecesModel(val color: Color) : ItemViewModel<PiecesListFragment>() {
    var undeployedPieces: ObservableList<PieceShape> = FXCollections.observableArrayList()

    init {
        for (shape in PieceShape.shapes.values) {
            undeployedPieces.add(shape)
        }
    }

    fun update(shapes: Set<PieceShape> ) {
        val remove = HashSet(undeployedPieces)
        remove.removeAll(shapes)
        val add = HashSet<PieceShape>(shapes)
        add.removeAll(undeployedPieces)
        undeployedPieces.removeAll(remove)
        undeployedPieces.addAll(add)
    }
}