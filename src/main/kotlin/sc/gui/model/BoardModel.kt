package sc.gui.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Point2D
import sc.gui.view.BoardView
import tornadofx.ItemViewModel

enum class FieldContent {
    EMPTY, RED, GREEN, BLUE, YELLOW
}
class Field(val coordinates: Point2D, val content: FieldContent)

class BoardModel: ItemViewModel<BoardView>() {
    val root = bind(BoardView::root)
    val fields: ObservableList<ObservableList<Field>> = FXCollections.observableArrayList<ObservableList<Field>>()

    val fieldSize: Double = 20.0

    val boardSize: Int = 20

    init {
        for (x in 0 until boardSize) {
            fields.add(x, FXCollections.observableArrayList<Field>())
            for (y in 0 until boardSize) {
                fields[x].add(
                        y,
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
}
