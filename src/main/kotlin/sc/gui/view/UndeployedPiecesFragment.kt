package sc.gui.view

import javafx.beans.property.ObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.BoardController
import sc.gui.controller.GameController
import sc.plugin2021.Color
import sc.plugin2021.PieceShape
import tornadofx.*

class UndeployedPiecesFragment(
    private val color: Color,
    undeployedPieces: ObjectProperty<Collection<PieceShape>>,
    validPieces: ObjectProperty<Collection<PieceShape>>
) : Fragment() {
    val controller: GameController by inject()
    private val boardController: BoardController by inject()
    private val shapes: ObservableList<PieceShape> = FXCollections.observableArrayList(undeployedPieces.value)
    private val piecesList = HashMap<PieceShape, HBox>()
    private val pieces = HashMap<PieceShape, PiecesFragment>()

    private val unplayableNotice = stackpane {
        addClass(AppStyle.pieceUnselectable)
        style {
            backgroundColor += javafx.scene.paint.Color.BLACK
        }

        text("Kein Zug mehr möglich") {
            fill = javafx.scene.paint.Color.RED
            font = Font(20.0)
        }
		isVisible = false
    }

    init {
        for (shape in undeployedPieces.value) {
            val piece = PiecesFragment(color, shape)
            pieces[shape] = piece
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
                    removeClass(AppStyle.hoverColor)
                }
            }
        }
        boardController.board.calculatedBlockSizeProperty().addListener { _, _, _ ->
            pieces.forEach {
                it.value.updateImage()
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

        controller.currentTurn.addListener { _, _, turn ->
            unplayableNotice.isVisible = turn != 0 && !controller.isValidColor(color)
        }

        validPieces.addListener { _, _, value ->
            piecesList.forEach { (piece, box) ->
                if(value.contains(piece)) {
                    box.removeClass(AppStyle.pieceUnselectable)
                } else if (!box.hasClass(AppStyle.pieceUnselectable)) {
                    box.addClass(AppStyle.pieceUnselectable)
                }
            }

            if (controller.currentColor.get() == color) {
                logger.debug("Current color ${color.name} can place $value")
                if (value.isNotEmpty()) {
                    controller.selectPiece(pieces.filterKeys { it in value }.values.last().model)
                }
            }
        }
    }

    override val root = stackpane {
        flowpane {
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
        add(unplayableNotice)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UndeployedPiecesFragment::class.java)
    }
}
