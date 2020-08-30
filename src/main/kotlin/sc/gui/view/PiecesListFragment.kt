package sc.gui.view

import javafx.geometry.Pos
import javafx.scene.input.MouseButton
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.BoardController
import sc.gui.controller.GameController
import sc.gui.controller.NewGameState
import sc.gui.model.UndeployedPiecesModel
import sc.plugin2021.Color
import tornadofx.*

class PiecesListFragment(private val undeployedPiecesModel: UndeployedPiecesModel) : Fragment() {
    val controller: GameController by inject()
    private val boardController: BoardController by inject()
    private var dragging: Boolean = false
    private var dragStartX: Double = 0.0
    private var dragStartY: Double = 0.0

    override val root = flowpane {
        hgap = 1.0
        vgap = 1.0
        alignment = when (undeployedPiecesModel.color) {
            Color.RED -> Pos.BOTTOM_LEFT
            Color.BLUE -> Pos.TOP_LEFT
            Color.YELLOW -> Pos.TOP_RIGHT
            Color.GREEN -> Pos.BOTTOM_RIGHT
        }

        children.bind(undeployedPiecesModel.undeployedPieces) {
            val piece = PiecesFragment(undeployedPiecesModel.color, it)
            boardController.board.calculatedBlockSizeProperty().addListener { _, _, _ ->
                piece.updateImage()
            }
            hbox {
                addClass(AppStyle.undeployedPiece, when (undeployedPiecesModel.color) {
                    Color.RED -> AppStyle.borderRED
                    Color.BLUE -> AppStyle.borderBLUE
                    Color.GREEN -> AppStyle.borderGREEN
                    Color.YELLOW -> AppStyle.borderYELLOW
                })
                this += piece


                setOnScroll { event ->
                    if (controller.turnColorProperty().get() == piece.model.colorProperty().get()) {
                        piece.model.scroll(event.deltaY)
                    }
                    event.consume()
                }

                setOnMouseClicked { event ->
                    if (controller.turnColorProperty().get() == piece.model.colorProperty().get()) {
                        if (event.button == MouseButton.SECONDARY) {
                            logger.debug("Right-click, flipping piece")
                            piece.model.flipPiece()
                        }
                    }
                    event.consume()
                }
                setOnMousePressed { event ->
                    if (controller.turnColorProperty().get() == piece.model.colorProperty().get()) {
                        if (event.button == MouseButton.PRIMARY) {
                            if (dragging) {
                                logger.debug("Dragging to ${event.sceneX}, ${event.sceneY} from initial $dragStartX, $dragStartY")
                                translateXProperty().set(event.sceneX - dragStartX)
                                translateYProperty().set(event.sceneY - dragStartY)
                            } else {
                                logger.debug("Clicked on ${undeployedPiecesModel.color} $shape")
                                controller.selectPiece(piece.model)
                                mouseTransparentProperty().set(true)
                                dragging = true
                                dragStartX = event.sceneX
                                dragStartY = event.sceneY
                            }
                        }
                    }
                    event.consume()
                }
                setOnMouseMoved { event ->
                    if (controller.turnColorProperty().get() == piece.model.colorProperty().get()) {
                        if (event.button == MouseButton.PRIMARY) {
                            if (dragging) {
                                logger.debug("Dragging to ${event.sceneX}, ${event.sceneY} from initial $dragStartX, $dragStartY")
                                translateXProperty().set(event.sceneX - dragStartX)
                                translateYProperty().set(event.sceneY - dragStartY)
                            }
                        }
                    }
                    event.consume()
                }
                setOnMouseReleased { event ->
                    if (controller.turnColorProperty().get() == piece.model.colorProperty().get()) {
                        if (event.button == MouseButton.PRIMARY) {
                            dragging = false
                            mouseTransparentProperty().set(false)
                        }
                    }
                    event.consume()
                }

                setOnDragDetected { event ->
                    if (controller.turnColorProperty().get() == piece.model.colorProperty().get()) {
                        logger.debug("Drag detected of ${undeployedPiecesModel.color}, $shape")
                        controller.selectPiece(piece.model)
                        startFullDrag()
                    }
                    event.consume()
                }

                setOnMouseEntered {
                    addClass(AppStyle.hoverColor)
                }

                setOnMouseExited {
                    removeClass(AppStyle.hoverColor)
                }
            }
        }
    }

    init {
        logger.debug("pieces fragment list for ${undeployedPiecesModel.color}")
        subscribe<NewGameState> { event ->
            logger.debug("new event $event")
            val shapes = event.gameState.undeployedPieceShapes[undeployedPiecesModel.color]
            if (shapes != null) {
                undeployedPiecesModel.update(shapes.toSet())
            } else {
                logger.error("got NewGameState event but gameState was null!")
            }
        }

        root.widthProperty().addListener { _, _, _ ->
            resize()
        }
        root.heightProperty().addListener { _, _, _ ->
            resize()
        }
    }

    private fun resize() {

    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesListFragment::class.java)
    }
}
