package sc.gui.view

import javafx.scene.image.Image
import javafx.scene.paint.Color
import sc.gui.AppStyle
import sc.plugin2021.Piece
import sc.gui.model.UndeployedPiecesModel
import sc.plugin2021.PieceShape
import sc.plugin2021.pieceShapes
import tornadofx.*

class PiecesFragment: Fragment() {
    val model: UndeployedPiecesModel by inject()
    override val root = datagrid(model.undeployedPieces) {
        singleSelect = true
        cellCache {
            val shapeId = it.kind
            imageview("file:resources/graphics/blokus/blue/Tiles_Blue_%02d.png".format(shapeId)) {
                isSmooth = false
            }
        }
        onUserSelect {
            println("selected item! "+it.toString())
        }
    }

    fun selectedItem(): Piece? {
        return root.selectedItem
    }
}
