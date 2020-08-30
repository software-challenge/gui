package sc.gui.model

import sc.gui.view.PiecesFragment
import sc.plugin2021.Color
import sc.plugin2021.Coordinates
import sc.plugin2021.PieceShape
import sc.plugin2021.Rotation
import tornadofx.*


class PiecesModel(color: Color, shape: PieceShape) : ItemViewModel<PiecesFragment>() {
    private var color: Color by property(color)
    private var shape: PieceShape by property(shape)
    private var rotation: Rotation by property(Rotation.NONE)
    private var flip: Boolean by property(false)
    private var calculatedShape: Set<Coordinates> by property(shape.transform(rotation, flip))

    fun colorProperty() = getProperty(PiecesModel::color)
    fun shapeProperty() = getProperty(PiecesModel::shape)
    fun rotationProperty() = getProperty(PiecesModel::rotation)
    fun flipProperty() = getProperty(PiecesModel::flip)
    fun calculatedShapeProperty() = getProperty(PiecesModel::calculatedShape)

    init {
        // automatically update calculated shape
        shapeProperty().addListener { _, _, newValue ->
            calculatedShapeProperty().set(newValue.transform(rotationProperty().get(), flipProperty().get()))
        }
        rotationProperty().addListener { _, _, newValue ->
            calculatedShapeProperty().set(shapeProperty().get().transform(newValue, flipProperty().get()))
        }
        flipProperty().addListener { _, _, newValue ->
            calculatedShapeProperty().set(shapeProperty().get().transform(rotationProperty().get(), newValue))
        }
    }

    private fun rotate(rotation: Rotation) {
        rotationProperty().set(rotationProperty().get().rotate(rotation))
    }

    fun scroll(deltaY: Double) {
        rotate(when {
            deltaY > 0.0 -> Rotation.LEFT
            deltaY == 0.0 -> Rotation.NONE
            deltaY < 0.0 -> Rotation.RIGHT
            else -> Rotation.MIRROR
        })
    }

    fun flipPiece() {
        flipProperty().set(!flipProperty().get())
    }
}