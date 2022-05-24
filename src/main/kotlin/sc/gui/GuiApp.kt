package sc.gui

import mu.KLogging
import sc.gui.controller.ServerController
import sc.gui.model.AppModel
import sc.gui.view.AppView
import sc.server.logbackFromPWD
import tornadofx.*
import kotlin.reflect.KClass

open class ServerApp(primaryView: KClass<out UIComponent>) : App(primaryView, AppStyle::class) {
    private val server: ServerController by inject()
    
    override fun stop() {
        super.stop()
        server.stopServer()
        logger.debug("App stopped, ending program")
    }
    
    init {
        reloadStylesheetsOnFocus()
        server.startServer()
        addStageIcon(resources.image("/icon.png"))
    }
    
    companion object: KLogging()
}

class GuiApp : ServerApp(AppView::class) {
    override fun stop() {
        AppModel.save()
        super.stop()
    }
}

fun main(args: Array<String>) {
    logbackFromPWD()
    launch<GuiApp>(args)
}