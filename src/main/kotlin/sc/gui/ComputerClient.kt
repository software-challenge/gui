package sc.gui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sc.plugin2021.PlayerType
import java.io.File
import java.io.OutputStream


// represents a client which is started by the GUI
class ComputerClient(val clientExecutable: File, val type: PlayerType, val host: String, val port: Int): ClientInterface {
    override fun joinPreparedGame(reservation: String) {
        val args = arrayOf(
                "--host", host,
                "--port", port.toString(),
                "--reservation", reservation
        )
        logger.debug("will start ${clientExecutable.absolutePath} with arguments ${args.joinToString(" ")}")
        val processBuilder = if (clientExecutable.absolutePath.endsWith(".jar", true)) {
            ProcessBuilder("java", "-jar", clientExecutable.absolutePath, *args)
        } else {
            ProcessBuilder(clientExecutable.absolutePath, *args)
        }
        val process = processBuilder.redirectErrorStream(true).start()

        Thread {
            process.inputStream.transferTo(LogOutputStream(logger))
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ComputerClient::class.java)
    }
}

/**
 * This class logs all bytes written to it as output stream with a specified logging level.
 *
 * @author [Christian Spannagel](mailto:cspannagel@web.de)
 * @version 1.0
 */
class LogOutputStream(val logger: Logger) : OutputStream() {

    /** The internal memory for the written bytes.  */
    private var mem: String = ""

    override fun write(b: Int) {
        val bytes = ByteArray(1)
        bytes[0] = (b and 0xff).toByte()
        mem = mem + String(bytes)
        if (mem.endsWith("\n")) {
            mem = mem.substring(0, mem.length - 1)
            flush()
        }
    }

    override fun flush() {
        logger.info(mem)
        mem = ""
    }

}