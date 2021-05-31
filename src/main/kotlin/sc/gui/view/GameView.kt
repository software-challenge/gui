package sc.gui.view

import javafx.geometry.Insets
import sc.plugin2022.util.Constants
import tornadofx.*

class GameView: View() {
    val game = borderpane {
        top(StatusView::class)
        center {
            this += find(BoardView::class)
        }
        bottom(ControlView::class)
    }
    override val root = hbox {
        paddingProperty().set(Insets(6.0))
        this += game
    }
    
    override fun onDock() {
        super.onDock()
        resize()
    }
    
    fun resize() {
        val width = root.widthProperty().get()
        var height = root.heightProperty().get()
        val app = find(AppView::class).root
        // 50px buffer for menubar etc.
        if (app.height - 50 < root.height) {
            height = app.height - 50
        }
        
        val size = minOf(width * 0.5, height - find(StatusView::class).root.height - find(ControlView::class).root.height)
        
        val board = find(BoardView::class)
        board.grid.setMaxSize(size, size)
        board.grid.setMinSize(size, size)
        board.calculatedBlockSize.set(size / Constants.BOARD_SIZE)
    }
    
    init {
        val resizer = ChangeListener<Number> { _, _, _ -> resize() }
        // responsive scaling
        root.widthProperty().addListener(resizer)
        root.heightProperty().addListener(resizer)
    }
}
