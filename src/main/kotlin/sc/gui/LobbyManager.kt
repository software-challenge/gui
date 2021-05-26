package sc.gui

import org.slf4j.LoggerFactory
import sc.api.plugins.IGamePlugin
import sc.api.plugins.IGameState
import sc.gui.controller.Player
import sc.gui.controller.client.ClientInterface
import sc.gui.model.PlayerType
import sc.gui.view.PauseGame
import sc.gui.view.StepGame
import sc.gui.view.TerminateGame
import sc.networking.clients.AdminClient
import sc.networking.clients.LobbyClient
import sc.plugin2021.GameState
import sc.protocol.ResponsePacket
import sc.protocol.requests.PrepareGameRequest
import sc.protocol.responses.ErrorPacket
import sc.protocol.responses.GamePreparedResponse
import sc.protocol.room.ErrorMessage
import sc.protocol.room.GamePaused
import sc.protocol.room.MementoMessage
import sc.server.Configuration
import sc.shared.GameResult
import sc.shared.SlotDescriptor
import tornadofx.Controller
import tornadofx.FXEvent
import java.net.ConnectException
import java.util.ArrayDeque
import java.util.Queue
import java.util.function.Consumer
import kotlin.system.exitProcess

sealed class GameUpdateEvent: FXEvent()
class GameReadyEvent: GameUpdateEvent()
data class NewGameState(val gameState: IGameState): GameUpdateEvent()
class GamePausedEvent: GameUpdateEvent()
data class GameOverEvent(val result: GameResult): GameUpdateEvent()

class LobbyManager(host: String, port: Int): Controller(), Consumer<ResponsePacket> {
    private val pendingPlayers: Queue<ClientInterface> = ArrayDeque()
    
    private val client: AdminClient = try {
        LobbyClient(host, port).authenticate(Configuration.get(Configuration.PASSWORD_KEY), this)
    } catch (e: ConnectException) {
        logger.error("Could not connect to server: " + e.message)
        exitProcess(1)
    }
    
    override fun accept(packet: ResponsePacket) {
        when (packet) {
            is GamePreparedResponse -> {
                enterRoom(packet.roomId)
                var reservationIndex = 0
                pendingPlayers.forEach { player ->
                    if (player.type == PlayerType.EXTERNAL)
                        player.joinGameRoom(packet.roomId)
                    else
                        player.joinPreparedGame(packet.reservations[reservationIndex++])
                }
            }
            is ErrorPacket -> {
                // TODO error popup
                logger.error("Error, probably failed to prepare game: $packet")
            }
        }
    }
    
    /** Take over the prepared room and start observing. */
    private fun enterRoom(roomId: String) {
        val controller = client.control(roomId)
        subscribe<PauseGame> { event -> controller.pause(event.pause) }
        subscribe<StepGame> { event ->
            if(event.steps > 0)
                controller.step()
            // TODO consider step size
        }
        subscribe<TerminateGame> { controller.cancel() }
        client.observe(roomId) { msg ->
            logger.trace("New RoomMessage in {}: {}", roomId, msg)
            when (msg) {
                is MementoMessage -> fire(NewGameState(msg.state as GameState)) // TODO save
                is GameResult -> fire(GameOverEvent(msg))
                is ErrorMessage -> {
                    logger.warn("Error in $roomId: $msg")
                }
                is GamePaused -> fire(GamePausedEvent())
            }
        }
        fire(GameReadyEvent())
    }
    
    fun startNewGame(players: List<Player>, paused: Boolean) {
        logger.debug("Starting new game (paused: {}, players: {})", paused, players)
        pendingPlayers.addAll(players.map { it.client })
        
        client.prepareGame(PrepareGameRequest(
                IGamePlugin.loadPluginId(),
                players.map {
                    SlotDescriptor(it.name,
                            it.client.type != PlayerType.HUMAN,
                            it.client.type != PlayerType.EXTERNAL)
                }.toTypedArray(),
                paused))
    }
    
    companion object {
        private val logger = LoggerFactory.getLogger(LobbyManager::class.java)
    }
}