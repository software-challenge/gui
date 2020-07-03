package sc.gui.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import sc.gui.view.BoardView
import sc.plugin2021.Board
import sc.plugin2021.Field
import tornadofx.ItemViewModel

const val boardSize: Int = 20

class BoardModel: ItemViewModel<BoardView>() {
    private val board: Board = Board()
    val fields: ObservableList<Field> = FXCollections.observableArrayList(board.fields)

    val fieldSize: Double = 20.0

    private fun indexOf(x: Int, y: Int): Int {
        return y*boardSize+x
    }

    fun setField(x: Int, y: Int, field: Field) {
        fields.set(indexOf(x, y), field)
    }

    fun getField(x: Int, y: Int): Field {
        return fields.get(indexOf(x, y))
    }
}
