package sc.gui.view

import javafx.beans.binding.DoubleBinding
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import org.slf4j.LoggerFactory
import sc.gui.AppStyle
import sc.gui.controller.BlokusController
import sc.plugin2027.Color
import sc.plugin2027.PieceShape
import tornadofx.*
import java.util.Locale
import java.util.Locale.getDefault

/**
 * This class handles the pieces of each playercolor in the left and right pane.
 */
class UndeployedPiecesFragment(
    private val color: Color,
    undeployedPieces: ObservableValue<Collection<PieceShape>>,
    /**
     * These are the pieces of each color that can currently be placed on the board.
     */
    validPieces: ObservableValue<Collection<PieceShape>>,
    /**
     * Makes sure that everything rescales properly.
     */
    sizeProperty: DoubleBinding
) : Fragment() {
    val controller: BlokusController by inject()
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
        
        visibleProperty().bind(controller.currentTurn.booleanBinding {
            it != 0 && !controller.isValidColor(color)
        })
    }
    
    init {
        for (shape in undeployedPieces.value) {
            // Initialize all pieces with a border and make them unselectable.
            // The content should have to form: ${playerColor.toEnString()}_$piece e.g. red_mono
            val pieceName = shape.name.lowercase(getDefault())
            val content = "${color.toEnString()}_${pieceName}"
            // Use PieceImage to make the polyminos automatically scale.
            val pieceImage = PieceImage(sizeProperty.multiply(1.5), content)
            val piece = PiecesFragment(color, shape, pieceImage)
            pieces[shape] = piece
            piecesList[shape] = hbox {
                addClass(AppStyle.undeployedPiece, when (color) {
                    Color.RED -> AppStyle.borderRED
                    Color.BLUE -> AppStyle.borderBLUE
                    Color.GREEN -> AppStyle.borderGREEN
                    Color.YELLOW -> AppStyle.borderYELLOW
                }, AppStyle.pieceUnselectable)
                // Only add the PieceImage tothe hbox, i.e. "this".
                this += piece.pieceImage
                
                // Handle selection of pieces in the left and right pane.
                setOnMouseClicked { event ->
                    val currentColor = controller.gameState.get()?.currentColor
                    // Check whether the clicked piece is valid.
                    // Also check the color of the clicked element.
                    if (color == currentColor && controller.validPieces.get(color)?.get()?.contains(shape)?: false) {
                        if (event.button == MouseButton.PRIMARY) {
                            logger.debug("Clicked on $color $shape")
                            controller.selectPiece(piece.model)
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
        //        boardController.view.gridSize.addListener { _, _, _ ->
        //            // FIXME This triggers itself
        //            pieces.forEach {
        //                it.value.updateImage()
        //            }
        //        }
        //        boardController.boardModel.calculatedBlockSize.addListener { _, _, _ ->
        //            // FIXME never fires
        //            pieces.forEach {
        //                it.value.updateImage()
        //            }
        //        }
        
        // Update the undeployed pieces.
        // This is triggered once the BlokusController.undeployedPieces(color) changes.
        undeployedPieces.addListener { _, _, new ->
            // FIXME the runtime of this algorithm seems awful.
            // Why not just removeAll and add all? Check this.
            
            // we need to use an extra list to prevent an ConcurrentModificationException
            val toRemove = ArrayList<PieceShape>()
            // Remove all elements not in the new list.
            for (shape in shapes) {
                if (!new.contains(shape)) {
                    toRemove.add(shape)
                }
            }
            shapes.removeAll(toRemove)
            
            // Add all elements from the new list not already in the old list.
            // FIXME to my understanding of the game, this should never happen.
            for (shape in new) {
                if (!shapes.contains(shape)) {
                    shapes.add(shape)
                }
            }
        }
        
        // Selects the selectable pieces for human players.
        // This is triggered once the valid pieces or the current color changes.
        // currentColor changes on new turn.
        arrayOf(validPieces, controller.currentColor).map { it.onChange {
            val vp = validPieces.value
            if(logger.isTraceEnabled)
                logger.trace("$color (current: ${controller.currentColor.value}) can place $vp")
            
            // Check whether the current color is the same color of the undeloyed pieces fragment.
            // If this is the case, make the valid pieces selectable and select the last one in the list.
            // Else delect the pieces.
            if (controller.currentColor.value == color) {
                // Make piece selectable
                piecesList.forEach { (piece, box) ->
                    when {
                        // Un-unselect all pieces that are valid.
                        vp.contains(piece) -> {
                            box.removeClass(AppStyle.pieceUnselectable)
                        }
                        // For all pieces that were previously unselectable, remove the property.
                        // FIXME this would make all pieces that are in vp unselectable again?
                        !box.hasClass(AppStyle.pieceUnselectable) -> {
                            box.addClass(AppStyle.pieceUnselectable)
                        }
                    }
                }
                
                logger.debug("Current color ${color.name} can place $vp")
                // Select the last piece in the list of valid pieces as the current piece to place.
                // This should select the biggest piece that can be placed.
                if (vp.isNotEmpty()) {
                    controller.selectPiece(pieces.filterKeys { it in vp }.values.last().model)
                }
            } else {
                // Deselect all pieces.
                piecesList.forEach { (_, box) ->
                    box.addClass(AppStyle.pieceUnselectable) }
            }
        }}
    }
    
    /**
     * This creates an undeployed pieces pane for one of the colors.
     */
    override val root = stackpane {
        flowpane {
            // FIXME all sizes should depend on the squareSize in some way.
            hgap = 1.0
            vgap = 1.0
            padding = Insets(5.0, 5.0, 5.0, 5.0)
            alignment = when (color) {
                Color.RED -> Pos.BOTTOM_LEFT
                Color.BLUE -> Pos.TOP_LEFT
                Color.YELLOW -> Pos.TOP_RIGHT
                Color.GREEN -> Pos.BOTTOM_RIGHT
            }
            
            // Put all pieces currently used on the flowpane.
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