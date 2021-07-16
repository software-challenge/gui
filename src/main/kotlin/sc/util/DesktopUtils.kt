package sc.util

import mu.KotlinLogging
import java.awt.Desktop
import java.io.File
import java.net.URI

val logger = KotlinLogging.logger {}

fun String.browseUrl() =
    URI(this).openDesktop(Desktop.Action.BROWSE, Desktop::browse)

fun File.browse() =
    openDesktop(Desktop.Action.BROWSE_FILE_DIR, Desktop::browseFileDirectory)

fun <T> T.openDesktop(action: Desktop.Action, open: Desktop.(T) -> Unit) {
    val desktop = Desktop.getDesktop()
    if (desktop.isSupported(action)) {
        logger.debug("Opening {} on {}", this, desktop)
        open(desktop, this)
    } else {
        logger.debug("Opening {} with xdg-open", this)
        Runtime.getRuntime().exec("xdg-open $this")
    }
}
