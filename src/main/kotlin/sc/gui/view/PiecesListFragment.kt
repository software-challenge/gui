package sc.gui.view

import javafx.geometry.Orientation
import javafx.geometry.Pos
import org.slf4j.LoggerFactory
import sc.gui.controller.GameController
import sc.gui.controller.NewGameState
import sc.gui.model.UndeployedPiecesModel
import tornadofx.*

class PiecesListFragment : Fragment() {
    val undeployedPiecesModel: UndeployedPiecesModel by param()

    val controller: GameController by inject()

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
    }

    override val root = flowpane {
        hgap = 1.0
        vgap = 1.0
        alignment = Pos.TOP_LEFT

        // fill column by column and not row by row
        orientation = Orientation.VERTICAL

        children.bind(undeployedPiecesModel.undeployedPieces) {
            hbox {
                this += PiecesFragment(undeployedPiecesModel.color, it)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        logger.debug("$this is docked")
    }

    override fun onUndock() {
        super.onUndock()
        logger.debug("$this is UNdocked")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesListFragment::class.java)
    }
}
