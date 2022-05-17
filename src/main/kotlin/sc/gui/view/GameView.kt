package sc.gui.view

import sc.gui.AppStyle
import sc.gui.model.AppModel
import tornadofx.*

class GameView: View() {
    override val root = borderpane {
        paddingAll = AppStyle.spacing
        top(StatusView::class)
        center(BoardView::class)
        if(AppModel.kiosk.value) {
            tornadofx.find<ControlView>().openWindow()
        } else {
            bottom(ControlView::class)
        }
    }
}
