package sc.gui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sc.api.plugins.IGameState
import sc.framework.plugins.Player
import sc.gui.controller.IGameListener
import sc.gui.controller.client.ClientInterface
import sc.networking.clients.IControllableGame
import sc.networking.clients.ILobbyClientListener
import sc.networking.clients.LobbyClient
import sc.plugin2021.GamePlugin
import sc.protocol.helpers.RequestResult
import sc.protocol.requests.ControlTimeoutRequest
import sc.protocol.requests.PrepareGameRequest
import sc.protocol.responses.GamePreparedResponse
import sc.protocol.responses.ProtocolErrorMessage
import sc.protocol.responses.ProtocolMessage
import sc.server.Configuration
import sc.shared.GameResult
import sc.shared.SlotDescriptor
import java.net.ConnectException
import kotlin.system.exitProcess

data class GameStartException(val error: ProtocolErrorMessage) : Exception("Failed to start game: ${error.message}")

class LobbyManager(host: String, port: Int) {
    var game: IControllableGame? = null
    
    private val lobbyListener: LobbyListener
    
    private val lobby: LobbyClient = try {
        LobbyClient(host, port)
    } catch (e: ConnectException) {
        logger.error("Could not connect to server: " + e.message)
        exitProcess(1)
    }
    
    init {
        lobby.start()
        lobby.authenticate(Configuration.get(Configuration.PASSWORD_KEY))
        lobbyListener = LobbyListener(logger)
        lobby.addListener(lobbyListener)
    }
    
    fun startNewGame(players: Collection<ClientInterface>, playerNames: Collection<String>, prepared: Boolean, paused: Boolean, listener: IGameListener) {
        logger.debug("Starting new game (prepared: {}, paused: {}, players: {})", prepared, paused, players)
        val observeRoom = { roomId: String ->
            game = lobby.observeAndControl(roomId, paused)
            lobbyListener.gameListeners[roomId] = listener
        }
        
        if (prepared) {
            val requestResult = lobby.prepareGameAndWait(PrepareGameRequest(
                GamePlugin.PLUGIN_UUID,
                SlotDescriptor(playerNames.first(), false),
                SlotDescriptor(playerNames.last(), false),
                paused
            ))
            
            when (requestResult) {
                is RequestResult.Success -> {
                    val preparation = requestResult.result
                    observeRoom(preparation.roomId)
                    players.forEachIndexed { i, player -> player.joinPreparedGame(preparation.reservations[i]) }
                    listener.onGameStarted(null)
                }
                is RequestResult.Error ->
                    listener.onGameStarted(GameStartException(requestResult.error))
            }
        } else {
            val iter = players.iterator()
            val join = {
                if (iter.hasNext())
                    iter.next().joinAnyGame()
                else
                    listener.onGameStarted(null)
            }
            lobbyListener.onAnyJoin { roomId ->
                logger.debug("LobbyManager started room $roomId")
                lobbyListener.onJoin(roomId, join)
                join()
                observeRoom(roomId)
                players.indices.forEach {
                    lobby.send(ControlTimeoutRequest(roomId, false, it))
                }
            }
            join()
        }
    }
    
    companion object {
        private val logger = LoggerFactory.getLogger(LobbyManager::class.java)
    }
}

class LobbyListener(val logger: Logger): ILobbyClientListener {
    
    val gameListeners = HashMap<String, IGameListener>()
    private val roomsJoined = HashMap<String, Int>()
    private val waiters: MutableMap<String?, MutableCollection<(String) -> Unit>> = HashMap()
    
    override fun onNewState(roomId: String, state: IGameState) {
        logger.debug("lobby: new gamestate in $roomId")
        gameListeners[roomId]?.onNewState(state)
    }
    
    override fun onError(roomId: String?, error: ProtocolErrorMessage) {
        logger.debug("lobby: new error $error in $roomId")
    }
    
    override fun onRoomMessage(roomId: String, data: ProtocolMessage) {
        logger.debug("lobby: new message in $roomId")
    }
    
    override fun onGamePrepared(response: GamePreparedResponse) {
        logger.debug("lobby: a game has been prepared")
    }
    
    override fun onGameLeft(roomId: String) {
        logger.debug("lobby: room $roomId was left")
    }
    
    override fun onGameJoined(roomId: String) {
        roomsJoined[roomId] = roomsJoined.getOrDefault(roomId, 0) + 1
        (waiters[roomId].orEmpty() + waiters.remove(null).orEmpty())
            .forEach { it(roomId) }
        logger.debug("lobby: room $roomId was joined (total: $roomsJoined)")
    }
    
    /** The callback is called once with a roomId as soon as a player joins. */
    fun onAnyJoin(callback: (String) -> Unit) {
        waiters.getOrPut(null) { mutableListOf() }.add(callback)
    }
    
    /** Calls the specified [callback] whenever a client joins into [roomId]. */
    fun onJoin(roomId: String, callback: () -> Unit) =
        waiters.getOrPut(roomId) { mutableListOf() }.add { callback() }
    
    /** @return number of received game joins in the specified room, or total if [roomId] is null. */
    fun getJoinsInRoom(roomId: String? = null) =
        roomId?.let {
            roomsJoined[it]
        } ?: roomsJoined.values.sum()
    
    override fun onGameOver(roomId: String, data: GameResult) {
        logger.debug("lobby: game over in room $roomId")
        gameListeners[roomId]?.onGameOver(data)
    }
    
    override fun onGamePaused(roomId: String, nextPlayer: Player) {
        logger.debug("lobby: game paused in room $roomId")
    }
    
    override fun onGameObserved(roomId: String) {
        logger.debug("lobby: room $roomId is being observed")
    }
    
}
