package sc.gui

import tornadofx.*

class GuiApp : App(MasterView::class, AppStyle::class) {
    init {
        reloadStylesheetsOnFocus()
    }
}

fun main(args: Array<String>) {
    // TODO: Argumente wie beispielsweise --kiosk oder --dev erkennen
    launch<GuiApp>(args)
}