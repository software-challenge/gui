package sc.gui

import sc.gui.controller.ServerController
import sc.gui.view.AppView
import tornadofx.App
import tornadofx.launch
import tornadofx.reloadStylesheetsOnFocus
import kotlin.system.exitProcess

class GuiApp : App(AppView::class, AppStyle::class) {
    val server: ServerController by inject()
    override fun stop() {
        super.stop()
        server.stopServer()
        println("GuiApp stopped, ending program")
        exitProcess(0)
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