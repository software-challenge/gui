package sc.gui.view

import javafx.beans.property.Property
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.input.MouseButton
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.BoardController
import sc.gui.controller.GameController
import sc.plugin2021.Color
import sc.plugin2021.PieceShape
import tornadofx.*

class PiecesListFragment(undeployedPieces: Property<Collection<PieceShape>>, private val color: Color) : Fragment() {
    val controller: GameController by inject()
    private val boardController: BoardController by inject()
    private val shapes: ObservableList<PieceShape> = FXCollections.observableArrayList(undeployedPieces.value)

    override val root = flowpane {
        hgap = 1.0
        vgap = 1.0
        alignment = when (color) {
            Color.RED -> Pos.BOTTOM_LEFT
            Color.BLUE -> Pos.TOP_LEFT
            Color.YELLOW -> Pos.TOP_RIGHT
            Color.GREEN -> Pos.BOTTOM_RIGHT
        }

        children.bind(shapes) {
            val piece = PiecesFragment(color, it)
            boardController.board.calculatedBlockSizeProperty().addListener { _, _, _ ->
                piece.updateImage()
            }
            hbox {
                addClass(AppStyle.undeployedPiece, when (color) {
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
                        if (event.button == MouseButton.PRIMARY) {
                            logger.debug("Clicked on $color $shape")
                            controller.selectPiece(piece.model)
                        } else if (event.button == MouseButton.SECONDARY) {
                            logger.debug("Right-click, flipping piece")
                            piece.model.flipPiece()
                        }
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
        undeployedPieces.addListener { _, old, new ->
            for (oldShape in old) {
                if (!new.contains(oldShape)) {
                    shapes.remove(oldShape)
                }
            }
            for (newShape in new) {
                if (!old.contains(newShape)) {
                    shapes.add(newShape)
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesListFragment::class.java)
    }
}
