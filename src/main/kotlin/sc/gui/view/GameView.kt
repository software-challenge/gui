package sc.gui.view

import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.util.StringConverter
import org.slf4j.LoggerFactory
import sc.gui.controller.*
import sc.plugin2021.*
import sc.plugin2021.util.Constants
import tornadofx.*

class ColorConverter : StringConverter<Color>() {
    override fun toString(color: Color?): String {
        return color!!.name.toLowerCase()
    }

    override fun fromString(string: String?): Color {
        TODO("Not yet implemented")
    }
}

class PiecesScope(val pieces: ObservableList<Piece>) : Scope()

class ShapeConverter : StringConverter<PieceShape>() {
    override fun toString(piece: PieceShape?): String {
        return piece!!.name.toLowerCase()
    }

    override fun fromString(string: String?): PieceShape {
        TODO("Not yet implemented")
    }

}

class GameView : View() {
    private val appController: AppController by inject()
    private val clientController: ClientController by inject()
    private val gameController: GameController by inject()
    private val gameEndedView: GameEndedView by inject()
    private val redUndeployedPieces = PiecesListFragment(Color.RED, gameController.undeployedRedPiecesProperty(), gameController.validRedPiecesProperty())
    private val blueUndeployedPieces = PiecesListFragment(Color.BLUE, gameController.undeployedBluePiecesProperty(), gameController.validBluePiecesProperty())
    private val greenUndeployedPieces = PiecesListFragment(Color.GREEN, gameController.undeployedGreenPiecesProperty(), gameController.validGreenPiecesProperty())
    private val yellowUndeployedPieces = PiecesListFragment(Color.YELLOW, gameController.undeployedYellowPiecesProperty(), gameController.validYellowPiecesProperty())


    private val leftPane = vbox {
        this += blueUndeployedPieces
        this += redUndeployedPieces
    }
    private val rightPane = vbox {
        this += yellowUndeployedPieces
        this += greenUndeployedPieces
    }
    val game = borderpane {
        top(StatusView::class)
        center {
            this += find(BoardView::class)
        }
        bottom(ControlView::class)
    }
    override val root = hbox {
        paddingProperty().set(Insets(6.0))
        this += leftPane
        this += game
        this += rightPane

        setOnMouseClicked {
            if (it.button == MouseButton.SECONDARY) {
                logger.debug("Right-click, flipping piece")
                gameController.flipPiece()
            }
            it.consume()
        }
        setOnScroll {
            gameController.scroll(it.deltaY)
            it.consume()
        }

        setOnKeyPressed {
            logger.debug("Key pressed ${it.code}")
            when (it.code) {
                KeyCode.A -> {
                    logger.debug("Recognized A, rotate counter clockwise")
                    // already handled by AppView menu action
                    // gameController.rotatePiece(Rotation.LEFT)
                }
                KeyCode.D -> {
                    logger.debug("Recognized D, rotate clockwise")
                    // already handled by AppView menu action
                    // gameController.rotatePiece(Rotation.RIGHT)
                }
                KeyCode.S -> {
                    logger.debug("Recognized S, rotate 180")
                    gameController.rotatePiece(Rotation.MIRROR)
                }
                KeyCode.W -> {
                    logger.debug("Recognized W, rotate 180")
                    gameController.rotatePiece(Rotation.MIRROR)
                }
                KeyCode.CONTROL -> {
                    logger.debug("Recognized CTRL, flipping")
                    gameController.flipPiece()
                }
                else -> logger.debug("Unrecognized key-input")
            }
        }
    }

    fun resize() {
        val width = root.widthProperty().get()
        var height = root.heightProperty().get()
        val app = find(AppView::class).root
        // 50px buffer for menubar etc.
        if (app.height - 50 < root.height) {
            height = app.height - 50
        }
        game.prefWidthProperty().set(width * 0.5)
        leftPane.prefWidthProperty().set(width * 0.25)
        rightPane.prefWidthProperty().set(width * 0.25)

        val size = minOf(width * 0.5, height - find(StatusView::class).root.height - find(ControlView::class).root.height)

        val board = find(BoardView::class)
        board.grid.setMaxSize(size, size)
        board.grid.setMinSize(size, size)

        find(BoardView::class).model.calculatedBlockSizeProperty().set(size / Constants.BOARD_SIZE)
    }

    init {
        subscribe<StartGameRequest> { event ->
            gameController.gameEndedProperty().set(false)
            clientController.startGame("localhost", 13050, event.gameCreationModel)
        }
        subscribe<GameOverEvent> { event ->
            // usage of EmptyBoardView as a placeholder to remount BoardView at the same position for a new game
            gameEndedView.gameEnded(event.result)
            appController.changeViewTo(GameEndedView::class)
        }


        redUndeployedPieces.root.prefHeightProperty().bind(root.heightProperty())
        blueUndeployedPieces.root.prefHeightProperty().bind(root.heightProperty())
        yellowUndeployedPieces.root.prefHeightProperty().bind(root.heightProperty())
        greenUndeployedPieces.root.prefHeightProperty().bind(root.heightProperty())


        val resizer = ChangeListener<Number> { _, _, _ ->
            resize()
        }
        // responsive scaling
        root.widthProperty().addListener(resizer)
        root.heightProperty().addListener(resizer)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GameView::class.java)
    }
}