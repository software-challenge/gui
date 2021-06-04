package sc.gui.view

import sc.gui.AppStyle
import tornadofx.View
import tornadofx.borderpane
import tornadofx.paddingAll

class GameView: View() {
    override val root = borderpane {
        paddingAll = AppStyle.spacing
        top(StatusView::class)
        center(BoardView::class)
        bottom(ControlView::class)
    }
}
