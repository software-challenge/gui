package sc.gui

import sc.gui.view.AppView
import tornadofx.*

class GuiApp : App(AppView::class, AppStyle::class) {
    override fun stop() {
        super.stop()
        println("Stopping")
        // TODO: kill backend-server
    }

    init {
        reloadStylesheetsOnFocus()

        // TODO: start backend-server
    }
}

fun main(args: Array<String>) {
    // TODO: Argumente wie beispielsweise --kiosk oder --dev erkennen
    launch<GuiApp>(args)
}