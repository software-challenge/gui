package sc.util

import mu.KotlinLogging
import tornadofx.*
import java.awt.Desktop
import java.io.File
import java.io.IOException

val logger = KotlinLogging.logger {}

fun browseUrl(url: String) {
    FX.application.hostServices.showDocument(url)
    //URI(url).openDesktop(Desktop.Action.BROWSE, Desktop::browse)
}

fun browse(file: File) {
    logger.trace { "Browsing $file" }
    file.openDesktop(Desktop.Action.BROWSE_FILE_DIR, Desktop::browseFileDirectory)
}

fun <T> T.openDesktop(action: Desktop.Action, open: Desktop.(T) -> Unit) {
    val desktop = Desktop.getDesktop()
    if(desktop.isSupported(action)) {
        logger.debug("Opening {} on {}", this, desktop)
        open(desktop, this)
    } else {
        logger.debug("Opening {} with xdg-open", this)
        try {
            Runtime.getRuntime().exec("xdg-open", arrayOf(this.toString()))
        } catch(ex: IOException) {
            ex.printStackTrace()
        }
    }
}
