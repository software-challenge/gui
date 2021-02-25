package sc.gui.view

import javafx.geometry.Insets
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import org.slf4j.LoggerFactory
import sc.gui.controller.GameController
import sc.plugin2021.Color
import sc.plugin2021.Rotation
import sc.plugin2021.Team
import sc.plugin2021.util.Constants
import tornadofx.*
import java.util.EnumMap

class GameView : View() {
    private val gameController: GameController by inject()
    private val undeployedPieces = EnumMap(
        Color.values().associateWith { color ->
            UndeployedPiecesFragment(color, gameController.undeployedPieces.getValue(color), gameController.validPieces.getValue(color))
        })
    
    private val leftPane = vbox {
        replaceChildren(*undeployedPieces.filterKeys { it.team == Team.ONE }.values.toTypedArray())
    }
    private val rightPane = vbox {
        replaceChildren(*undeployedPieces.filterKeys { it.team == Team.TWO }.values.toTypedArray())
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

    override fun onDock() {
        super.onDock()
        resize()
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
        board.model.calculatedBlockSize.set(size / Constants.BOARD_SIZE)
    }

    init {
        undeployedPieces.forEach { (_, pieces) -> pieces.root.prefHeightProperty().bind(root.heightProperty()) }

        val resizer = ChangeListener<Number> { _, _, _ -> resize() }
        // responsive scaling
        root.widthProperty().addListener(resizer)
        root.heightProperty().addListener(resizer)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GameView::class.java)
    }
}
