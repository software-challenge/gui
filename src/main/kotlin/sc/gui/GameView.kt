package sc.gui

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Button
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.paint.Color
import sc.gui.view.BoardView
import sc.gui.view.PiecesFragment
import sc.gui.view.RedUndeployedPiecesView
import tornadofx.*

class GameView: View() {
    val input = SimpleStringProperty()
    private val boardView: BoardView by inject()
    private val redPiecesView: RedUndeployedPiecesView by inject()
    override val root = vbox {
        rectangle {
            width = 100.0
            height = 100.0
            fill = Color.BLUE
            setOnDragDetected {
                val db = this.startDragAndDrop(TransferMode.MOVE)
                val content = ClipboardContent()
                content.putString("content")
                db.setContent(content)
                println("Dragging started!")
                it.consume()
            }
            setOnDragDone {
                println("Dragging ended!")
                it.consume()
            }
        }
        children.filterIsInstance<Button>().addClass(AppStyle.tackyButton)
        vbox {
            addClass(AppStyle.area2)
            add(redPiecesView)
            add(boardView)
        }
    }
}