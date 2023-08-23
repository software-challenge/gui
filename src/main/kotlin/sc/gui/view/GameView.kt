package sc.gui.view

import sc.gui.AppStyle
import tornadofx.*

class GameView: View() {
    override val root = borderpane {
        paddingAll = AppStyle.spacing
        top(StatusView::class)
        center(MississippiBoard::class)
        bottom(ControlView::class)
    }
}
