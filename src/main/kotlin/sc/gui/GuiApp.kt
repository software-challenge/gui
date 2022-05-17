package sc.gui

import mu.KLogging
import sc.gui.controller.ServerController
import sc.gui.model.AppModel
import sc.gui.view.AppView
import sc.server.logbackFromPWD
import tornadofx.*

class GuiApp : App(AppView::class, AppStyle::class) {
    private val server: ServerController by inject()

    override fun stop() {
        AppModel.save()
        super.stop()
        server.stopServer()
        logger.debug("GuiApp stopped, ending program")
    }

    init {
        reloadStylesheetsOnFocus()
        server.startServer()
        addStageIcon(resources.image("/icon.png"))
    }
    
    companion object: KLogging()
}

fun main(args: Array<String>) {
    logbackFromPWD()
    //if(args.contains("--kiosk"))
    // TODO: handle arguments like --kiosk or --dev
    launch<GuiApp>(args)
}