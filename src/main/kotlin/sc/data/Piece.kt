package sc.data

enum class PlayerColor {
    RED, GREEN, BLUE, YELLOW
}

val pieceShapes = arrayOf(
        PieceShape(0, listOf(Coordinates(0, 0))),
        PieceShape(1, listOf(Coordinates(0, 0), Coordinates(1, 0))),
        PieceShape(2, listOf(Coordinates(0, 0), Coordinates(1, 0), Coordinates(1, 1)))
)

class Piece(val kind: Int, val color: PlayerColor) {
    override fun toString(): String {
        return "$color Piece $kind"
    }
}

// data structure to represent one shape of piece of Blokus. There are 21 different kinds, see https://en.wikipedia.org/wiki/Blokus
// The shapes are represented as coordinate list of occupied fields, where the left upper corner of a shape is the origin (0,0), x-axis going to the right and y-axis going down
class PieceShape(val id: Int, val coordinates: List<Coordinates>)