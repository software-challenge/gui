package sc.gui.view

import javafx.animation.FadeTransition
import javafx.beans.binding.Bindings
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.util.StringConverter
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.*
import sc.gui.model.UndeployedPiecesModel
import sc.plugin2021.Color
import sc.plugin2021.Piece
import sc.plugin2021.PieceShape
import sc.plugin2021.Rotation
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
    private val clientController: ClientController by inject()
    private val gameController: GameController by inject()
    private val redUndeployedPieces = PiecesListFragment(UndeployedPiecesModel(Color.RED))
    private val blueUndeployedPieces = PiecesListFragment(UndeployedPiecesModel(Color.BLUE))
    private val yellowUndeployedPieces = PiecesListFragment(UndeployedPiecesModel(Color.YELLOW))
    private val greenUndeployedPieces = PiecesListFragment(UndeployedPiecesModel(Color.GREEN))

    private val statusLabel = Label("whooohooo")
    private val leftPane = vbox {
        this += blueUndeployedPieces
        this += redUndeployedPieces
    }
    private val rightPane = vbox {
        this += yellowUndeployedPieces
        this += greenUndeployedPieces
    }
    private val game = borderpane {
        style {
            backgroundColor += javafx.scene.paint.Color.DARKCYAN
        }
        center = stackpane {
            this += find<BoardView>()
            statusLabel.style = "-fx-text-fill: white; -fx-font-size: 32pt;"
            statusLabel.isVisible = false
            this += statusLabel
        }

        bottom = hbox {
            label("Selected:")
            hbox {
                gameController.selectedColor.addListener { _, _, newValue ->
                    if (hasClass(AppStyle.borderRED)) {
                        removeClass(AppStyle.borderRED)
                    }
                    if (hasClass(AppStyle.borderBLUE)) {
                        removeClass(AppStyle.borderBLUE)
                    }
                    if (hasClass(AppStyle.borderGREEN)) {
                        removeClass(AppStyle.borderGREEN)
                    }
                    if (hasClass(AppStyle.borderYELLOW)) {
                        removeClass(AppStyle.borderYELLOW)
                    }
                    if (newValue != null) {
                        addClass(when (newValue) {
                            Color.RED -> AppStyle.borderRED
                            Color.BLUE -> AppStyle.borderBLUE
                            Color.GREEN -> AppStyle.borderGREEN
                            Color.YELLOW -> AppStyle.borderYELLOW
                        })
                    }
                }
                this += PiecesFragment(gameController.selectedColor, gameController.selectedShape, gameController.selectedRotation, gameController.selectedFlip)
            }
            button {
                text = "Pause / Play"
                setOnMouseClicked {
                    clientController.togglePause()
                }
            }
            button {
                text = "Previous"
                setOnMouseClicked {
                    clientController.previous()
                }
            }
            label {
                textProperty().bind(Bindings.concat(gameController.currentTurnProperty(), " / ", gameController.availableTurnsProperty()))
            }
            button {
                text = "Next"
                setOnMouseClicked {
                    clientController.next()
                }
            }
        }
    }
    override val root = hbox {
        paddingProperty().set(Insets(0.0, 10.0, 10.0, 10.0))
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
            logger.debug("Scrolling detected: rotating selected piece")
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

    private fun resize() {
        val width = root.widthProperty().get()
        val height = root.heightProperty().get()
        val size = minOf(width, height)
        game.prefWidthProperty().set(width * 0.5)
        leftPane.prefWidthProperty().set(width * 0.25)
        rightPane.prefWidthProperty().set(width * 0.25)
        /*
        if (size > 0.6 * width) {
            game.prefWidthProperty().set(width * 0.6)
            leftPane.prefWidthProperty().set(width * 0.2)
            rightPane.prefWidthProperty().set(width * 0.2)
        } else {
            game.prefWidthProperty().set(size)
            leftPane.prefWidthProperty().set((width - size) / 2)
            rightPane.prefWidthProperty().set((width - size) / 2)
        }
        */

        redUndeployedPieces.root.prefHeightProperty().set(height)
        blueUndeployedPieces.root.prefHeightProperty().set(height)
        yellowUndeployedPieces.root.prefHeightProperty().set(height)
        greenUndeployedPieces.root.prefHeightProperty().set(height)
    }

    override fun onDock() {
        super.onDock()
        resize()
    }

    init {
        subscribe<StartGameRequest> { event ->
            clientController.startGame("localhost", 13050, event.gameCreationModel)
        }
        subscribe<HumanMoveRequest> { event ->
            val player = event.gameState.currentPlayer.displayName
            val color = event.gameState.currentColor.name
            statusLabel.text = "Spieler $player mit $color ist am Zug!"
            statusLabel.isVisible = true
            val ft = FadeTransition(7.seconds, statusLabel)
            ft.fromValue = 1.0
            ft.toValue = 0.0
            ft.play()
            ft.setOnFinished { statusLabel.isVisible = false }
        }

        // responsive scaling
        root.widthProperty().addListener { _, _, _ ->
            resize()
        }
        root.heightProperty().addListener { _, _, _ ->
            resize()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GameView::class.java)
    }
}