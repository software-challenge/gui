package sc.gui.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Point2D
import sc.gui.view.BoardView
import tornadofx.ItemViewModel

enum class FieldContent {
    EMPTY, RED, GREEN, BLUE, YELLOW
}

const val boardSize: Int = 20
class Field(val coordinates: Point2D, val content: FieldContent)

class BoardModel: ItemViewModel<BoardView>() {
    val fields: ObservableList<Field> = FXCollections.observableArrayList()

    val fieldSize: Double = 20.0

    init {
        for (y in 0 until boardSize) {
            for (x in 0 until boardSize) {
                fields.add(
                        Field(
                                Point2D(
                                        x.toDouble(),
                                        y.toDouble()
                                ),
                                FieldContent.values().random()
                        ))
            }
        }
    }

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
