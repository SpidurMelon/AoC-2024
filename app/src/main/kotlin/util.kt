import java.awt.Point

enum class Direction(val x: Int, val y: Int, val char: Char) {
    UP(0, -1, '^'),
    RIGHT(1, 0, '>'),
    DOWN(0, 1, 'v'),
    LEFT(-1, 0, '<'),
    NEUTRAL(0, 0, 'o');
    companion object {
        fun cardinals(): List<Direction> {
            return listOf(UP, RIGHT, DOWN, LEFT)
        }
        fun fromChar(c: Char): Direction? {
            return Direction.entries.find{ it.char == c }
        }
    }

    fun isHorizontal(): Boolean {
        return this == LEFT || this == RIGHT
    }

    fun isVertical(): Boolean {
        return this == UP || this == DOWN
    }

    fun flip(): Direction = when (this) {
        UP -> DOWN
        RIGHT -> LEFT
        DOWN -> UP
        LEFT -> RIGHT
        NEUTRAL -> NEUTRAL
    }

    fun rotated(): Direction = rotatedRight()

    fun rotatedRight(): Direction = when (this) {
        UP -> RIGHT
        RIGHT -> DOWN
        DOWN -> LEFT
        LEFT -> UP
        NEUTRAL -> NEUTRAL
    }
    fun rotatedLeft(): Direction = when (this) {
        UP -> LEFT
        RIGHT -> UP
        DOWN -> RIGHT
        LEFT -> DOWN
        NEUTRAL -> NEUTRAL
    }
}

operator fun Point.plus(dir: Direction): Point {
    return Point(x+dir.x, y+dir.y)
}

operator fun Point.plus(other: Point): Point {
    return Point(this.x + other.x, this.y + other.y)
}

operator fun Point.rem(other: Point): Point {
    return Point(((this.x % other.x)+other.x)%other.x, ((this.y % other.y)+other.y)%other.y)
}