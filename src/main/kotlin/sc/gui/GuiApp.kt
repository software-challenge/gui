package sc.gui

import tornadofx.*

class GuiApp : App(MasterView::class, AppStyle::class) {
    init {
        reloadStylesheetsOnFocus()
    }
}
