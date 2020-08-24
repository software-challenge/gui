package sc.gui.view

import javafx.scene.control.ListCell
import javafx.scene.control.SelectionMode
import sc.gui.controller.GameController
import sc.gui.model.UndeployedPiecesModel
import sc.plugin2021.Color
import sc.plugin2021.Piece
import tornadofx.*

class PiecesFragment(model: UndeployedPiecesModel): Fragment() {
    val controller: GameController by inject()
    override val root = listview(model.undeployedPieces) {
        selectionModel.selectionMode = SelectionMode.SINGLE
        cellFormat {
            val number = "%02d".format(it.kind.ordinal)
            val color = when (it.color) {
                Color.RED -> "red"
                Color.GREEN -> "green"
                Color.YELLOW -> "yellow"
                Color.BLUE -> "blue"
            }
            val filename = "${it.kind.name.toLowerCase()}.png"
            graphic = hbox {
                imageview("file:resources/graphics/blokus/$color/$filename") {
                    isSmooth = false
                }
                label(number)
            }
        }
        setOnMouseClicked {
            println("mouse clicked! "+it.toString())
            if (it.target is ListCell<*>) {
                val t = it.target as ListCell<Piece>
                controller.selectColor(t.item.color)
                controller.selectPieceShape(t.item.kind)
            }
        }
    }

    fun selectedItem(): Piece? {
        return root.selectedItem
    }
}
