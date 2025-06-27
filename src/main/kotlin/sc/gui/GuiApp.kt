package sc.gui

import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.stage.Stage
import sc.gui.controller.ServerController
import sc.gui.events.*
import sc.gui.model.AppModel
import sc.gui.view.AppView
import sc.server.Configuration
import sc.server.logbackFromPWD
import tornadofx.*
import kotlin.reflect.KClass

open class ServerApp(primaryView: KClass<out UIComponent>) : App(primaryView, AppStyle::class) {
    private val server: ServerController by inject()
    override fun start(stage: Stage) {
        super.start(stage)
        
        try {
            Class.forName("com.tangorabox.componentinspector.fx.FXComponentInspectorHandler")
                    .getDeclaredMethod("handleAll").invoke(null)
            dumpStylesheets()
            // reloading stylesheets breaks "Zug" font color in ControlView
            // reloadStylesheetsOnFocus()
        } catch(_: ClassNotFoundException) {
        }
    }
    
    override fun stop() {
        fire(TerminateGame())
        AppModel.save()
        super.stop()
        server.stopServer()
        logger.info { "App stopped, Terminating" }
    }
    
    init {
        server.startServer()
        addStageIcon(resources.image("/icon.png"))
    }
    
    companion object {
        val logger = KotlinLogging.logger {}
    }
}

class GuiApp : ServerApp(AppView::class)

fun main(args: Array<String>) {
    logbackFromPWD()
    Configuration.setIfNotNull(Configuration.PORT_KEY, args.firstOrNull()?.toIntOrNull()?.toString())
    launch<GuiApp>(args)
}