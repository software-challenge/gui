package sc.gui.controller

import org.slf4j.LoggerFactory
import sc.framework.plugins.Player
import sc.framework.plugins.protocol.MoveRequest
import sc.networking.clients.ControllingClient
import sc.networking.clients.IControllableGame
import sc.networking.clients.ILobbyClientListener
import sc.networking.clients.LobbyClient
import sc.plugin2021.*
import sc.plugin2021.util.Configuration.classesToRegister
import sc.protocol.responses.PrepareGameProtocolMessage
import sc.protocol.responses.ProtocolErrorMessage
import sc.server.Configuration
import sc.shared.GameResult
import sc.shared.SlotDescriptor
import sc.shared.WelcomeMessage
import tornadofx.*
import java.net.ConnectException
import kotlin.system.exitProcess

class UITestClient(playerType: PlayerType): AbstractClient("localhost", 13050, playerType) {

    override fun onError(roomId: String, error: ProtocolErrorMessage) {
        println("onError ")
    }

    override fun onGameJoined(roomId: String) {
        println("onGameJoined ")
    }

    override fun onGameLeft(roomId: String) {
        println("onGameLeft ")
    }

    override fun onGameObserved(roomId: String) {
        println("onGameObserved ")
    }

    override fun onGameOver(roomId: String, data: GameResult) {
        println("onGameOver ")
    }

    override fun onGamePaused(roomId: String, nextPlayer: Player) {
        println("onGamePaused ")
    }

    override fun onGamePrepared(response: PrepareGameProtocolMessage) {
        println("onGamePrepared ")
    }

    override fun onNewState(roomId: String, state: Any) {
        println("onNewState ")
    }

    override fun onRoomMessage(roomId: String, data: Any) {
        println("onRoomMessage ")
    }
}

class UIObserverClient constructor(
            host: String,
            port: Int,
            private val id: PlayerType = PlayerType.PLAYER_ONE
    ): ILobbyClientListener, IGameHandler {

    var reservations: List<String> = emptyList()
    var clientOne: AbstractClient? = null
    var clientTwo: AbstractClient? = null
    var controller: IControllableGame? = null

    companion object {
        private val logger = LoggerFactory.getLogger(UIObserverClient::class.java);
        private val gameType = GamePlugin.PLUGIN_UUID
    }

    fun createAndObserve(clientOne: AbstractClient, clientTwo: AbstractClient) {
        this.clientOne = clientOne
        this.clientTwo = clientTwo
        start()
        client.authenticate(Configuration.get(Configuration.PASSWORD_KEY))
        var requestResult = client.prepareGameAndWait(
                GamePlugin.PLUGIN_UUID,
                SlotDescriptor("One", false, false),
                SlotDescriptor("Two", false, false)
        )

        var preparation = requestResult.result
        if (preparation != null) {
            clientOne.joinPreparedGame(preparation.reservations[0])
            clientTwo.joinPreparedGame(preparation.reservations[1])
        }
    }

    /** The handler reacts to messages from the server received by the lobby client.
     *  It *must* be initialised before start.
     */
    protected var handler: IGameHandler = this

    /** The lobby client that connects to the room. Stops on connection failure. */
    private val client = try {
        LobbyClient(Configuration.getXStream(), classesToRegister, host, port)
    } catch(e: ConnectException) {
        logger.error("Could not connect to Server: " + e.message)
        exitProcess(1)
    }

    /** Storage for the reason of a rule violation, if any occurs. */
    private lateinit var error: String
    fun getError() = error

    private lateinit var roomID: String

    /** Tell this client to observe the game given by the preparation handler.
     *
     * @return controllable game
     */
    fun observeGame(handle: PrepareGameProtocolMessage): IControllableGame =
            client.observe(handle)

    /** Called for any new message sent to the game room, e.g., move requests. */
    override fun onRoomMessage(roomId: String, data: Any) {
        if(data is MoveRequest) {
            handler.onRequestAction()
        }
        //roomID = roomId
    }

    /** Sends the selected move to the server. */
    fun sendMove(move: Move) =
            client.sendMessageToRoom(roomID, move)

    /** Called when an erroneous message is sent to the room. */
    override fun onError(roomId: String, error: ProtocolErrorMessage) {
        logger.debug("onError: Client {} received error {}", this, error.message)
        this.error = error.message
    }

    override fun onNewState(roomId: String, state: Any) {
        val gameState = state as GameState
        logger.debug("{} got a new state {}", this, gameState)

        if(id == PlayerType.OBSERVER) return

        handler.onUpdate(gameState)
        handler.onUpdate(gameState.currentPlayer, gameState.otherPlayer)
    }

    private fun start() {
        client.start()
        client.addListener(this)
    }

    fun joinAnyGame() {
        start()
        client.joinRoomRequest(gameType)
    }

    override fun onGameJoined(roomId: String) {}
    override fun onGamePrepared(response: PrepareGameProtocolMessage) {
        logger.info("{} observing game {}", this, response.roomId)
        reservations = response.reservations
        controller = client.observeAndControl(response)
    }
    override fun onGamePaused(roomId: String, nextPlayer: Player) {}
    override fun onGameObserved(roomId: String) {
    }

    override fun onGameLeft(roomId: String) {
        logger.info("{} got game left {}", this, roomId)
        client.stop()
    }

    override fun onGameOver(roomId: String, data: GameResult) {
        logger.info("{} on Game Over with game result {}", this, data)
    }

    fun joinPreparedGame(reservation: String) {
        start()
        client.joinPreparedGame(reservation)
    }

    // The following methods are from IGameHandler and are called to update the game state, once the game has begun

    override fun gameEnded(data: GameResult, team: Team?, errorMessage: String) {
        TODO("Not yet implemented")
    }

    override fun onRequestAction() {
        TODO("Not yet implemented")
    }

    override fun onUpdate(player: Player, otherPlayer: Player) {
        TODO("Not yet implemented")
    }

    override fun onUpdate(gamestate: GameState) {
        TODO("Not yet implemented")
    }

    override fun sendAction(move: Move) {
        TODO("Not yet implemented")
    }
}


class ClientController : Controller() {
    val playerOne = UITestClient(PlayerType.PLAYER_ONE)
    val playerTwo = UITestClient(PlayerType.PLAYER_TWO)
    val observer = UIObserverClient("localhost", 13050)

    fun startGame() {
        println("creating and observing")
        observer.createAndObserve(playerOne, playerTwo)
    }
}
