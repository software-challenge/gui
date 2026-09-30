package sc.gui.view

import sc.gui.model.PiecesModel
import sc.plugin2027.Color
import sc.plugin2027.PieceShape

// FIXME I removed all the rotation and flip state from the sidepanes
/**
 * This is a data class to give access to the PieceModel when using the
 * sidepanes.
 */
class PiecesFragment(color: Color, shape: PieceShape, val pieceImage: PieceImage) {
    val model: PiecesModel = PiecesModel(color, shape)
}