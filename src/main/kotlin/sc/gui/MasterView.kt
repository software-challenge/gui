package sc.gui

import tornadofx.*

class MasterView: View() {
    private val gameView: GameView by inject()
    override val root = pane {
        add(gameView)
    }
}

