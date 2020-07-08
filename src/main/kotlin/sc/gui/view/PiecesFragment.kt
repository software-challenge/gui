package sc.gui.view

import javafx.scene.control.SelectionMode
import javafx.scene.image.Image
import javafx.scene.paint.Color
import sc.gui.AppStyle
import sc.plugin2021.Piece
import sc.gui.model.UndeployedPiecesModel
import sc.plugin2021.PieceShape
import sc.plugin2021.PlayerColor
import tornadofx.*
import tornadofx.Stylesheet.Companion.datagridCell

class PiecesFragment(model: UndeployedPiecesModel): Fragment() {
    override val root = listview(model.undeployedPieces) {
        selectionModel.selectionMode = SelectionMode.SINGLE
        cellFormat {
            graphic = cache {
                val number = "%02d".format(it.kind)
                val color = when (it.color) {
                    PlayerColor.RED -> "red"
                    PlayerColor.GREEN -> "green"
                    PlayerColor.YELLOW -> "pink"
                    PlayerColor.BLUE -> "blue"
                }
                val filename = "tiles_${color}_$number.png"
                stackpane {
                    imageview("file:resources/graphics/blokus/$color/$filename") {
                        isSmooth = false
                    }
                    label(filename)
                }
            }
        }
        onUserSelect {
            println("selected item! "+it.toString())
        }
    }

    init {
    }

    fun selectedItem(): Piece? {
        return root.selectedItem
    }

    fun selectOne() {
    }
}
