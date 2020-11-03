package sc.gui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.OutputStream

/** Represents a client started by the GUI from an executable. */
class ExecClient(val host: String, val port: Int, val clientExecutable: File): ClientInterface {
    override fun joinPreparedGame(reservation: String) {
        val args = arrayOf(
            "--host", host,
            "--port", port.toString(),
            "--reservation", reservation
        )
        logger.debug("Starting ${clientExecutable.absolutePath} with arguments ${args.joinToString(" ")}")
        val command = mutableListOf(clientExecutable.absolutePath, *args)
        if (clientExecutable.absolutePath.endsWith(".jar", true))
            command.addAll(0, listOf("java", "-jar"))
        val processBuilder = ProcessBuilder(command)
        val process = processBuilder.redirectErrorStream(true).start()
        
        Thread {
            process.inputStream.transferTo(LogOutputStream(logger))
        }
    }
    
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ExecClient::class.java)
    }
}

/** This class logs all bytes written to it as output stream with a specified logging level. */
class LogOutputStream(val logger: Logger): OutputStream() {
    
    /** The internal memory for the written bytes. */
    private var mem: String = ""
    
    override fun write(b: Int) {
        val bytes = ByteArray(1)
        bytes[0] = (b and 0xff).toByte()
        mem += bytes
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
