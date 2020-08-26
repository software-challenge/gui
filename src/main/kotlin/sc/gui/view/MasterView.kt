package sc.gui.view

import tornadofx.*

class MasterView: View() {
    private val gameView: GameView by inject()
    override val root = pane {
        add(gameView)
    }
}

