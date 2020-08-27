package sc.gui.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import sc.gui.view.BoardView
import sc.plugin2021.*
import sc.plugin2021.util.Constants
import tornadofx.ItemViewModel

class BoardModel: ItemViewModel<BoardView>() {
    // NOTE that this is not how the ItemViewModel should be used, see https://edvin.gitbooks.io/tornadofx-guide/content/part1/11_Editing_Models_and_Validation.html

    val fields: ObservableList<Field> = FXCollections.observableArrayList()

    init {
        // create all elements, so that we can use the index in the updateFields method
        for (x in 0 until Constants.BOARD_SIZE) {
            for (y in 0 until Constants.BOARD_SIZE) {
                fields.add(Field(Coordinates(x, y), FieldContent.EMPTY))
            }
        }
        updateFields(Board())
    }

    fun updateFields(board: Board) {
        for (x in 0 until Constants.BOARD_SIZE) {
            for (y in 0 until Constants.BOARD_SIZE) {
                setField(x, y, board.getField(x, y))
            }
        }
    }

    private fun indexOf(x: Int, y: Int): Int {
        return y*Constants.BOARD_SIZE+x
    }

    fun setField(x: Int, y: Int, field: Field) {
        fields.set(indexOf(x, y), field)
    }

    fun getField(x: Int, y: Int): Field{
        return fields.get(indexOf(x, y))
    }
}
