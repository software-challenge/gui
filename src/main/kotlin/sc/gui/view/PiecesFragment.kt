package sc.gui.view

import javafx.scene.control.SelectionMode
import sc.gui.model.UndeployedPiecesModel
import sc.plugin2021.Piece
import sc.plugin2021.PlayerColor
import tornadofx.*

class PiecesFragment(model: UndeployedPiecesModel): Fragment() {
    override val root = listview(model.undeployedPieces) {
        selectionModel.selectionMode = SelectionMode.SINGLE
        cellFormat {
            val number = "%02d".format(it.kind)
            val color = when (it.color) {
                PlayerColor.RED -> "red"
                PlayerColor.GREEN -> "green"
                PlayerColor.YELLOW -> "pink"
                PlayerColor.BLUE -> "blue"
            }
            val filename = "tiles_${color}_$number.png"
            graphic = hbox {
                imageview("file:resources/graphics/blokus/$color/$filename") {
                    isSmooth = false
                }
                label(number)
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
