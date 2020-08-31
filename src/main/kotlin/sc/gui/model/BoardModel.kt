package sc.gui.model

import org.slf4j.LoggerFactory
import sc.gui.view.BoardView
import sc.gui.view.PiecesListFragment
import sc.plugin2021.*
import tornadofx.*

class BoardModel : ItemViewModel<BoardView>() {
    private var calculatedBlockSize: Double by property<Double>(16.0)
    fun calculatedBlockSizeProperty() = getProperty(BoardModel::calculatedBlockSize)

    // NOTE that this is not how the ItemViewModel should be used, see https://edvin.gitbooks.io/tornadofx-guide/content/part1/11_Editing_Models_and_Validation.html
    private var board: Board by property<Board>()
    fun boardProperty() = getProperty(BoardModel::board)

    init {
        calculatedBlockSizeProperty().addListener { _, old, new ->
            logger.debug("Blocksize changed $old -> $new")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PiecesListFragment::class.java)
    }
}
