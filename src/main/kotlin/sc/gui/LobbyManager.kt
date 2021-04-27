package sc.gui

import org.slf4j.LoggerFactory
import sc.api.plugins.IGameState
import sc.gui.controller.IGameListener
import sc.gui.controller.client.ClientInterface
import sc.networking.clients.AbstractLobbyClientListener
import sc.networking.clients.IControllableGame
import sc.networking.clients.LobbyClient
import sc.plugin2021.GamePlugin
import sc.protocol.requests.ControlTimeoutRequest
import sc.protocol.requests.PrepareGameRequest
import sc.protocol.responses.GamePreparedResponse
import sc.protocol.responses.ProtocolErrorMessage
import sc.server.Configuration
import sc.shared.GameResult
import sc.shared.SlotDescriptor
import java.net.ConnectException
import kotlin.system.exitProcess

data class GameStartException(val error: ProtocolErrorMessage): Exception("Failed to start game: ${error.message}")

class LobbyManager(host: String, port: Int) {
    var game: IControllableGame? = null
    
    private val lobby: LobbyClient = try {
        LobbyClient(host, port)
    } catch (e: ConnectException) {
        logger.error("Could not connect to server: " + e.message)
        exitProcess(1)
    }
    
    init {
        lobby.start()
        lobby.authenticate(Configuration.get(Configuration.PASSWORD_KEY))
    }
    
    fun startNewGame(players: Collection<ClientInterface>, playerNames: Collection<String>, prepared: Boolean, paused: Boolean, listener: IGameListener) {
        logger.debug("Starting new game (prepared: {}, paused: {}, players: {})", prepared, paused, players)
        val observeRoom = { roomId: String ->
            game = lobby.observeAndControl(roomId, paused)
            lobby.addListener(GameListener(listener, roomId))
        }
        
        if (prepared) {
            lobby.addListener(object: AbstractLobbyClientListener() {
                override fun onGamePrepared(response: GamePreparedResponse) {
                    observeRoom(response.roomId)
                    players.forEachIndexed { i, player -> player.joinPreparedGame(response.reservations[i]) }
                    listener.onGameStarted()
                }
                override fun onError(roomId: String?, error: ProtocolErrorMessage) {
                    listener.onGameStarted(GameStartException(error))
                }
            })
            
            lobby.send(PrepareGameRequest(
                    GamePlugin.PLUGIN_UUID,
                    SlotDescriptor(playerNames.first(), false),
                    SlotDescriptor(playerNames.last(), false),
                    paused
            ))
        } else {
            val iter = players.iterator()
            val join = {
                if (iter.hasNext())
                    iter.next().joinAnyGame()
                else
                    listener.onGameStarted()
            }
            lobby.addListener(object: AbstractLobbyClientListener() {
                var currentRoom: String? = null
                override fun onGameJoined(roomId: String) {
                    if (currentRoom == null) {
                        logger.debug("LobbyManager started room $roomId")
                        currentRoom = roomId
                        observeRoom(roomId)
                        players.indices.forEach {
                            lobby.send(ControlTimeoutRequest(roomId, false, it))
                        }
                    }
                    if (currentRoom == roomId)
                        join()
                }
            })
            join()
        }
    }
    
    companion object {
        private val logger = LoggerFactory.getLogger(LobbyManager::class.java)
    }
}

class GameListener(val listener: IGameListener, val roomId: String): AbstractLobbyClientListener() {
    override fun onNewState(roomId: String, state: IGameState) {
        if (this.roomId == roomId)
            listener.onNewState(state)
    }
    
    override fun onGameOver(roomId: String, data: GameResult) {
        if (this.roomId == roomId)
            listener.onGameOver(data)
    }
}