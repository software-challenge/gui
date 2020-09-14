package sc.gui.model

import org.slf4j.LoggerFactory
import sc.gui.view.BoardView
import sc.gui.view.UndeployedPiecesFragment
import sc.plugin2021.*
import tornadofx.*

class BoardModel : ItemViewModel<BoardView>() {
    private var calculatedBlockSize: Double by property<Double>(16.0)
    fun calculatedBlockSizeProperty() = getProperty(BoardModel::calculatedBlockSize)

    private var board: Board by property<Board>()
    fun boardProperty() = getProperty(BoardModel::board)

    init {
        calculatedBlockSizeProperty().addListener { _, old, new ->
            logger.debug("Blocksize changed $old -> $new")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BoardModel::class.java)
    }
}
