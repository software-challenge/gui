package sc.gui.model

import javafx.scene.input.ScrollEvent
import sc.gui.view.PiecesFragment
import sc.plugin2021.Color
import sc.plugin2021.Coordinates
import sc.plugin2021.PieceShape
import sc.plugin2021.Rotation
import tornadofx.*


class PiecesModel(color: Color, shape: PieceShape) : ItemViewModel<PiecesFragment>() {
    var color: Color by property(color)
    var shape: PieceShape by property(shape)
    var rotation: Rotation by property(Rotation.NONE)
    var flip: Boolean by property(false)
    var calculatedShape: Set<Coordinates> by property(shape.transform(rotation, flip))

    fun colorProperty() = getProperty(PiecesModel::color)
    fun shapeProperty() = getProperty(PiecesModel::shape)
    fun rotationProperty() = getProperty(PiecesModel::rotation)
    fun flipProperty() = getProperty(PiecesModel::flip)
    fun calculatedShapeProperty() = getProperty(PiecesModel::calculatedShape)

    init {
        // automatically update calulated shape
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

    fun rotate(rotation: Rotation) {
        rotationProperty().set(rotationProperty().get().rotate(rotation))
    }

    fun scroll(event: ScrollEvent) {
        rotate(when {
            event.deltaY > 0.0 -> Rotation.LEFT
            event.deltaY == 0.0 -> Rotation.NONE
            event.deltaY < 0.0 -> Rotation.RIGHT
            else -> Rotation.MIRROR
        })
    }

    fun flipPiece() {
        flipProperty().set(!flipProperty().get())
    }
}