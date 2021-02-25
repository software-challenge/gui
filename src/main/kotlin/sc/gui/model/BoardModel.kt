package sc.gui.model

import org.slf4j.LoggerFactory
import sc.gui.view.BoardView
import sc.plugin2021.Board
import tornadofx.ItemViewModel
import tornadofx.objectProperty

class BoardModel : ItemViewModel<BoardView>() {
    val calculatedBlockSize = objectProperty(16.0)
    val board = objectProperty<Board>()

    init {
        calculatedBlockSize.addListener { _, old, new ->
            logger.debug("Blocksize changed $old -> $new")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BoardModel::class.java)
    }
}
