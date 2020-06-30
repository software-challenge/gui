package sc.gui

import sc.gui.view.AppView
import tornadofx.*

class GuiApp : App(AppView::class, AppStyle::class) {
    init {
        reloadStylesheetsOnFocus()
    }
}

fun main(args: Array<String>) {
    // TODO: Argumente wie beispielsweise --kiosk oder --dev erkennen
    launch<GuiApp>(args)
}