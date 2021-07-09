package sc.util

import mu.KotlinLogging
import java.awt.Desktop
import java.io.File
import java.net.URI

val logger = KotlinLogging.logger {}

fun String.browseUrl() {
    URI(this).openDesktop(Desktop.Action.BROWSE, Desktop::browse)
}

fun File.browse() {
    openDesktop(Desktop.Action.OPEN, Desktop::browseFileDirectory)
}

fun <T> T.openDesktop(action: Desktop.Action, open: Desktop.(T) -> Unit) {
    val desktop = Desktop.getDesktop()
    logger.debug("Opening {} on {}", this, desktop)
    if (desktop.isSupported(action))
        open(desktop, this)
    else
        Runtime.getRuntime().exec("xdg-open $this")
}
