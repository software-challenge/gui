package sc.gui

import javafx.scene.image.Image
import sc.gui.controller.ServerController
import sc.gui.view.AppView
import tornadofx.*
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
        // TODO: use local path to resources/icon.png
        addStageIcon(Image("https://raw.githubusercontent.com/CAU-Kiel-Tech-Inf/socha-gui/master/assets/build-resources/icon.png"))
    }
}

fun main(args: Array<String>) {
    // TODO: handle arguments like --kiosk or --dev
    launch<GuiApp>(args)
}