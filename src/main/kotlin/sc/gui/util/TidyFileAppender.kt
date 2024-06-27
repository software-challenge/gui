package sc.gui.util

import ch.qos.logback.core.FileAppender
import java.io.File

/** Rotates files and automatically cleans up old ones. */
class TidyFileAppender<E>: FileAppender<E>() {
    /** Defaults to the parent dir of the current logfile. */
    var directory: String? = null
    /** How many previous files to keep, by last modified attribute. */
    var maxHistory: Int = 20
    /** Threshold for extra files that may be kept to reduce file system accesses. */
    var threshold: Int = 0
    
    override fun start() {
        if (directory == null)
            directory = File(fileName).parent
        File(directory).list()?.let { files ->
            if (files.size > maxHistory + threshold) {
                files.map { File(directory, it) }
                        .sortedBy { it.lastModified() }
                        .take(files.size - maxHistory)
                        //.also { println("Removing $it") }
                        .forEach(File::delete)
            }
        }
        super.start()
    }
}