package sc.gui.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.BoardController
import sc.gui.controller.GameController
import sc.plugin2021.Color
import sc.plugin2021.PieceShape
import tornadofx.*

class PiecesListFragment(private val color: Color, undeployedPieces: Property<Collection<PieceShape>>, validPieces: ObjectProperty<ArrayList<PieceShape>>) : Fragment() {
    val controller: GameController by inject()
    private val boardController: BoardController by inject()
    private val shapes: ObservableList<PieceShape> = FXCollections.observableArrayList(undeployedPieces.value)
    private val piecesList = HashMap<PieceShape, HBox>()

    init {
        for (shape in undeployedPieces.value) {
            val piece = PiecesFragment(color, shape)
            boardController.board.calculatedBlockSizeProperty().addListener { _, _, _ ->
                piece.updateImage()
            }
            piecesList[shape] = hbox {
                addClass(AppStyle.undeployedPiece, when (color) {
                    Color.RED -> AppStyle.borderRED
                    Color.BLUE -> AppStyle.borderBLUE
                    Color.GREEN -> AppStyle.borderGREEN
                    Color.YELLOW -> AppStyle.borderYELLOW
                }, AppStyle.pieceUnselectable)
                this += piece


                setOnScroll { event ->
                    if (validPieces.value.contains(shape)) {
                        piece.model.scroll(event.deltaY)
                    }
                    event.consume()
                }

                setOnMouseClicked { event ->
                    if (validPieces.value.contains(shape)) {
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
                    if (validPieces.value.contains(shape)) {
                        addClass(AppStyle.hoverColor)
                    }
                }

                setOnMouseExited {
                    if (hasClass(AppStyle.hoverColor)) {
                        removeClass(AppStyle.hoverColor)
                    }
                }
            }
        }

        undeployedPieces.addListener { _, _, new ->
            // we need to use an extra list to prevent an ConcurrentModificationException
            val toRemove = ArrayList<PieceShape>()
            for (shape in shapes) {
                if (!new.contains(shape)) {
                    toRemove.add(shape)
                }
            }
            shapes.removeAll(toRemove)

            for (shape in new) {
                if (!shapes.contains(shape)) {
                    shapes.add(shape)
                }
            }
        }
        validPieces.addListener { _, _, new ->
            piecesList.forEach {
                if (new.contains(it.key)) {
                    if (it.value.hasClass(AppStyle.pieceUnselectable)) {
                        it.value.removeClass(AppStyle.pieceUnselectable)
                    }
                } else if (!it.value.hasClass(AppStyle.pieceUnselectable)) {
                    it.value.addClass(AppStyle.pieceUnselectable)
                }
            }
        }
    }

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
            piecesList[it]
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesListFragment::class.java)
    }
}
