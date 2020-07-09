package sc.gui

import sc.gui.controller.ServerController
import sc.gui.view.AppView
import tornadofx.App
import tornadofx.launch
import tornadofx.reloadStylesheetsOnFocus

class GuiApp : App(AppView::class, AppStyle::class) {
    val server: ServerController by inject()
    override fun stop() {
        super.stop()
        println("Stopping")
    }

    init {
        reloadStylesheetsOnFocus()
        server.startServer()
    }
}

fun main(args: Array<String>) {
    // TODO: handle arguments like --kiosk or --dev
    launch<GuiApp>(args)
}