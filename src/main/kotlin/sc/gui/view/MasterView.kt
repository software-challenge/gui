package sc.gui.view

import tornadofx.*

class MasterView: View() {
    override val root = borderpane {
        center(GameCreationView::class)
    }
}

