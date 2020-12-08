package sc.gui.controller.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sc.gui.model.PlayerType
import java.io.File
import java.io.OutputStream

/** Represents a client started by the GUI from an executable. */
class ExecClient(val host: String, val port: Int, val clientExecutable: File): ClientInterface {
    override fun joinAnyGame() {
        startClient(null)
    }
    
    override fun joinPreparedGame(reservation: String) {
        startClient(reservation)
    }
    
    private fun startClient(reservation: String?) {
        val command = mutableListOf(
            clientExecutable.absolutePath,
            "--host", host,
            "--port", port.toString()
        )
        if (reservation != null)
            command.addAll(listOf("--reservation", reservation))
        if (clientExecutable.absolutePath.endsWith(".jar", true))
            command.addAll(0, listOf("java", "-jar"))
        logger.debug("Starting ${command.joinToString(" ")}")
        val processBuilder = ProcessBuilder(command)
        val process = processBuilder.redirectErrorStream(true).start()
        
        Thread {
            process.inputStream.transferTo(LogOutputStream(logger))
        }
    }
    
    override val type = PlayerType.COMPUTER
    
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
