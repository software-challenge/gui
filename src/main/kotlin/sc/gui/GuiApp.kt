package sc.gui

import tornadofx.*

class GuiApp: App(MasterView::class, MyStyle::class) {
    init {
        reloadStylesheetsOnFocus()
    }
}
