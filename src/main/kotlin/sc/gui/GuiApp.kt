package sc.gui

import javafx.scene.image.Image
import mu.KLogging
import sc.gui.controller.ServerController
import sc.gui.view.AppView
import tornadofx.App
import tornadofx.addStageIcon
import tornadofx.launch
import tornadofx.reloadStylesheetsOnFocus
import kotlin.system.exitProcess

class GuiApp : App(AppView::class, AppStyle::class) {
    private val server: ServerController by inject()

    override fun stop() {
        super.stop()
        server.stopServer()
        logger.debug("GuiApp stopped, ending program")
        exitProcess(0)
    }

    init {
        reloadStylesheetsOnFocus()
        server.startServer()
        addStageIcon(Image(GuiApp::class.java.getResource("/icon.png").toExternalForm()))
    }
    
    companion object: KLogging()
}

fun main(args: Array<String>) {
    // TODO: handle arguments like --kiosk or --dev
    launch<GuiApp>(args)
}