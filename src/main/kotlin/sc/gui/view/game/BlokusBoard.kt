package sc.gui.view.game

import javafx.beans.property.Property
import javafx.beans.value.ObservableDoubleValue
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.layout.GridPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.RowConstraints
import org.slf4j.LoggerFactory
import sc.api.plugins.Coordinates
import sc.api.plugins.Team
import sc.gui.AppStyle
import sc.gui.controller.AppController
import sc.gui.controller.BlokusBoardController
import sc.gui.controller.BlokusController
import sc.gui.util.listenImmediately
import sc.gui.view.*
import sc.plugin2027.*
import sc.plugin2027.Field
import sc.plugin2027.util.Constants
import tornadofx.*
import tornadofx.runLater
import java.util.EnumMap
import java.util.Locale
import java.util.Locale.getDefault

val Color.borderStyle
    get() = when(this) {
        Color.BLUE -> AppStyle.borderBLUE
        Color.GREEN -> AppStyle.borderGREEN
        Color.YELLOW -> AppStyle.borderYELLOW
        Color.RED -> AppStyle.borderRED
    }

class BlokusBoard: GameBoard<GameState>() {
    private val gameController: BlokusController by inject()
    val controller: BlokusBoardController by inject()
    
    private val undeployedPieces = EnumMap(
        Color.values().associateWith { color ->
            UndeployedPiecesFragment(
                color,
                gameController.undeployedPieces.getValue(color),
                gameController.validPieces.getValue(color),
                gridSize
            )
        })
    
    private val leftPane = vbox {
        alignment = Pos.TOP_LEFT
        replaceChildren(*undeployedPieces.filterKeys { it.team == Team.ONE }.values.toTypedArray())
    }
    private val rightPane = vbox {
        alignment = Pos.TOP_RIGHT
        replaceChildren(*undeployedPieces.filterKeys { it.team == Team.TWO }.values.toTypedArray())
    }
    
    /**
     * Sets the size of each grid element.
     * This is set by the squareSize, which should update on resize.
     */
    val gridSize
        get() = squareSize.div(Constants.BOARD_LENGTH)
    
    /**
     * The 20x20 game grid.
     * Column and row constraints are set to uniform integer pixel sizes to prevent
     * subpixel accumulation errors that would cause pieces to misalign further from the origin.
     * The grid's preferred size is snapped to the exact integer multiple of the cell size.
     */
    val grid: GridPane = GridPane().addClass("grid").apply {
        squareSize.listenImmediately { size ->
            val cellSize = (size.toDouble() / Constants.BOARD_LENGTH).toInt().toDouble()
            columnConstraints.clear()
            rowConstraints.clear()
            repeat(Constants.BOARD_LENGTH) {
                columnConstraints.add(ColumnConstraints(cellSize, cellSize, cellSize))
                rowConstraints.add(RowConstraints(cellSize, cellSize, cellSize))
            }
            prefWidth = cellSize * Constants.BOARD_LENGTH
            prefHeight = cellSize * Constants.BOARD_LENGTH
        }
    }
    
    /**
     * The selected Piece-Node.
     */
    var selected: Node? = null
    
    /**
     * The current hovers.
     */
    val hovers = ArrayList<Node>()
    
    /**
     * This root contains a left pane, a grid and a right pane in that ordered.
     * These elements are always centered.
     * The left and right pane each hold two [UndeployedPiecesFragment] for the two colors of each player.
     */
    override val root = hbox {
        // Add left pane with undeployed pieces of player 1
        this.alignment = Pos.CENTER // Center the main pane and the side-panes globally
        this += leftPane
        // This is just the main grid.
        vbox {
            this.alignment = Pos.CENTER
            add(grid)
        }
        // Add right pane with undeployed pieces of player 2.
        this += rightPane
        // Set resizing for left and right pane
        runLater {
            leftPane.prefWidthProperty().bind(gridSize.multiply(12))
            rightPane.prefWidthProperty().bind(gridSize.multiply(12))
        }
        setOnMouseClicked {
            if (it.button == MouseButton.SECONDARY) {
                logger.debug{"Right-click, flipping piece"}
                gameController.flipPiece()
                it.consume()
            }
        }
    }
    
    /**
     * Handles key presses for the board.
     *
     * @return true if the key event was handled, false otherwise.
     */
    override fun handleKeyPress(state: GameState, keyEvent: KeyEvent): Boolean {
        // The following keys are used to rotate a piece.
        // A: Rotate counter clockwise
        // D: Rotate clockwise
        // S: rotate 180 degrees
        // W: rotate 180 degrees
        // ctrl: Mirroring?
        // FIXME the key codes are caught by the speed text box, disabled the speed text box as a workaround.
        logger.debug{"Key pressed ${keyEvent.code}"}
        when(keyEvent.code) {
            KeyCode.A -> {
                logger.debug{"Recognized A, rotate counter clockwise"}
                // already handled by AppView menu action
                gameController.rotatePiece(Rotation.LEFT)
                return true
            }
            KeyCode.D -> {
                logger.debug{"Recognized D, rotate clockwise"}
                // already handled by AppView menu action
                gameController.rotatePiece(Rotation.RIGHT)
                return true
            }
            KeyCode.S -> {
                logger.debug{"Recognized S, rotate 180"}
                gameController.rotatePiece(Rotation.MIRROR)
                return true
            }
            KeyCode.W -> {
                logger.debug{"Recognized W, rotate 180"}
                gameController.rotatePiece(Rotation.MIRROR)
                return true
            }
            KeyCode.CONTROL -> {
                logger.debug{"Recognized CTRL, flipping"}
                gameController.flipPiece()
                return true
            }
            else -> {
                logger.debug{"Unrecognized key-input"}
                return false
            }
        }
    }
    
    override fun renderHumanControls(state: GameState) {
        // Let players click on desired piece.
        // When hovering over the grid highlight possible placements for the selected piece.
        // When clicking on the grid, place the piece if possible.
        hbox {
            alignment = Pos.BOTTOM_CENTER
            spacing = 8.0
            addClass(AppStyle.pieceUnselectable)
            gameController.isHumanTurn.addListener { _, _, humanTurn ->
                if(true) {
                    removeClass(AppStyle.pieceUnselectable)
                } else if(!humanTurn && !hasClass(AppStyle.pieceUnselectable)) {
                    addClass(AppStyle.pieceUnselectable)
                }
            }
        }
    }
    
    /**
     * Helper method to add a PieceImage to the grid at the given coordinates.
     */
    fun addToGrid(child: Node, coordinates: Coordinates) {
        grid.add(child, coordinates.x,coordinates.y)
    }
    
    /**
     * This will be called when the game state changes and updates the board accordingly.
     *
     * @param oldState the previous game state, can be null if there was no previous state
     * @param state the new game state, can be null if the game has ended
     */
    override fun onNewState(oldState: GameState?, state: GameState?) {
        selected = grid
        grid.isGridLinesVisible = true
        logger.debug { "New State: $state" }
        //         Clear out old PieceImages
        // FIXME I think this is inefficient. Why did the old BlockImage have an update function and the PieceIMage not?
        // And somehow this makes nothing appear at all. Why?
        grid.children.clear()
        hovers.clear()
        selected = null
        
        logger.debug { state }
        state?.let { state ->
            state.board.forEach { (pos: Coordinates, field: Field) ->
                // Add a mono piece on all fields if occupied.
                var content = "empty"
                if (field.content.toTeamColor() != null) {
                    content = field.content.toTeamColor()?.toEnString()?.lowercase(getDefault()) ?: "empty"
                }
                val piece = PieceImage(
                    gridSize, // Remove border
                    content
                )
                addToGrid(piece, pos)
                
                // Add interactivity to PieceImages on the board.
                piece.onMouseEntered = EventHandler {
                    paneHoverEnter(pos.x, pos.y)
                    it.consume()
                }
                piece.onMouseExited = EventHandler {
                    paneHoverExit()
                    it.consume()
                }
                piece.onMouseClicked = EventHandler {
                    if (!awaitingHumanMove.value) {
                        it.consume()
                    }
                    if(it.button == MouseButton.PRIMARY) {
                        println("Clicked on pane ${pos.x}, ${pos.y}")
                        cleanupHover()
                        controller.handleClick(pos.x, pos.y)
                    } else if(it.button == MouseButton.SECONDARY) {
                        logger.debug{"Right-click, flipping piece"}
                        controller.game.flipPiece()
                    }
                    it.consume()
                }
            }
        }
    }
    
    init {
        // Make sure to update the hover effect if the selected piece changes or is rotated or flipped.
        controller.game.selectedCalculatedShape.addListener { _ ->
            cleanupHover()
            controller.currentHover?.run {
                paneHoverEnter(x, y)
            }
        }
    }
    
    
    private fun getPane(x: Int, y: Int): Node =
        grid.children.find { node ->
            GridPane.getColumnIndex(node) == x && GridPane.getRowIndex(node) == y
        } ?: throw Exception("Pane of ($x, $y) is not part of the BoardView")
    
    /** Remove all applied Stylesheets during the hover-effect. */
    private fun cleanupHover() =
        grid.children.forEach { it.styleClass.clear() }
    
    /**
     * This handles the hover effect when hovering over a field on the board.
     *
     * @param x the x coordinate of the hovered field
     * @param y the y coordinate of the hovered field
     */
    private fun paneHoverEnter(x: Int, y: Int) {
        // No hover effect when the game has ended.
        if(gameController.gameEnded.value)
            return
        
        controller.currentHover = Coordinates(x, y)
        controller.hoverable = controller.isHoverable(x, y, controller.game.selectedCalculatedShape.get())
        controller.currentPlaceable = controller.isPlaceable(x, y, controller.game.selectedCalculatedShape.get())
        
        for(place in controller.game.selectedCalculatedShape.get()) {
            if(controller.hoverInBound(x + place.x, y + place.y)) {
                if(!controller.hoverable) {
                    // Case a field covered by the piece is already blocked -> black hover, unplaceable
                    getPane(x + place.x, y + place.y).addClass(AppStyle.fieldUnplaceable)
                } else if(controller.currentPlaceable) {
                    // Case the piece can be placed here -> colored hover -> ligth color, placeable
                    getPane(x + place.x, y + place.y).addClass(when(controller.game.selectedColor.get()) {
                        Color.RED -> AppStyle.placeableRED
                        Color.BLUE -> AppStyle.placeableBLUE
                        Color.GREEN -> AppStyle.placeableGREEN
                        Color.YELLOW -> AppStyle.placeableYELLOW
                        else -> throw Exception("Unknown player color for hover effect")
                    })
                } else {
                    // Case the place is not blocked but a move is also not possible -> dark color, unplaceable
                    getPane(x + place.x, y + place.y).addClass(when(controller.game.selectedColor.get()) {
                        Color.RED -> AppStyle.colorRED
                        Color.BLUE -> AppStyle.colorBLUE
                        Color.GREEN -> AppStyle.colorGREEN
                        Color.YELLOW -> AppStyle.colorYELLOW
                        else -> throw Exception("Unknown player color for hover effect")
                    })
                }
            }
        }
    }
    
    /**
     * This handles the exit of the hover effect when leaving a field on the board.
     * It just removes the current hover and updates whether the current selection is placeable or not.
     */
    private fun paneHoverExit() {
        controller.currentPlaceable = false
        if(controller.currentHover != null) {
            cleanupHover()
        }
        controller.currentHover = null
    }
    
    companion object {
        private val logger = LoggerFactory.getLogger(BlokusBoard::class.java)
    }
}