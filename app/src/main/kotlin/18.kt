import java.awt.Point
import java.awt.Rectangle
import java.io.File

class ByteMaze(val drops: List<Point>, val bounds: Rectangle) {
    enum class Direction(val x: Int, val y: Int) {
        UP(0, -1),
        RIGHT(1, 0),
        DOWN(0, 1),
        LEFT(-1, 0),
        NEUTRAL(0, 0);
        companion object {
            fun cardinals(): List<Direction> {
                return listOf(UP, RIGHT, DOWN, LEFT)
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

    val walls = hashMapOf<Point, Int>()

    init {
        drops.forEachIndexed{i, p -> walls[p] = i }
    }

    operator fun Point.plus(dir: Direction): Point {
        return Point(x+dir.x, y+dir.y)
    }

    fun isWall(pos: Point, byteIndex: Int = drops.size): Boolean {
        if (!bounds.contains(pos)) return true
        if (walls[pos] == null) return false
        if (walls[pos]!! < byteIndex) return true
        return false
    }

    fun walls(byteIndex: Int = drops.size): Set<Point> {
        val result = mutableSetOf<Point>()
        for (y in 0..<bounds.height) {
            for (x in 0..<bounds.width) {
                val pos = Point(x, y)
                if (isWall(pos, byteIndex)) result.add(pos)
            }
        }
        return result
    }

    fun minOf(vararg ints: Int?): Int? {
        return ints.minByOrNull { it ?: Integer.MAX_VALUE }
    }

    fun floodPos(pos: Point, byteIndex: Int, distanceFromStart: HashMap<Point, Int>, currentDistance: Int): Set<Point> {
        distanceFromStart[pos] = currentDistance
        val filled = mutableSetOf<Point>()
        for (direction in Direction.cardinals()) {
            val p = pos+direction
            if (!isWall(p, byteIndex) && p !in distanceFromStart) {
                filled.add(p)
            }
        }
        return filled
    }

    fun floodFill(start: Point, byteIndex: Int, distanceFromStart: HashMap<Point, Int>) {
        var currentDistance: Int = 0
        var adjacent = floodPos(start, byteIndex, distanceFromStart, currentDistance)
        while (adjacent.isNotEmpty()) {
            currentDistance += 1
            val newAdjacent = mutableSetOf<Point>()
            for (pos in adjacent) {
                newAdjacent.addAll(floodPos(pos, byteIndex, distanceFromStart, currentDistance))
            }
            adjacent = newAdjacent
        }
    }

//    fun track(pos: Point, byteIndex: Int, distanceFromStart: HashMap<Point, Int>): Set<Point>? {
//        if (isWall(pos, byteIndex)) return null
//        if (pos == )
//
//        val up = track(pos + Direction.UP, byteIndex, distanceFromStart)
//        val right = track(pos + Direction.RIGHT, byteIndex, distanceFromStart)
//        val down = track(pos + Direction.DOWN, byteIndex, distanceFromStart)
//        val left = track(pos + Direction.LEFT, byteIndex, distanceFromStart)
//
//        return listOf(up, right, down, left).minBy { it.size }
//    }

    fun mark(locations: Set<Point>, marker: Char, byteIndex: Int = drops.size): String {
        val result = StringBuilder()
        for (y in 0..<bounds.height) {
            for (x in 0..<bounds.width) {
                val pos = Point(x, y)
                if (pos in locations) result.append(marker)
                else result.append(if (isWall(pos, byteIndex)) "#" else ".")
            }
            if (y != bounds.height-1) result.append("\n")
        }
        return result.toString()
    }

    override fun toString(): String {
        val result = StringBuilder()
        for (y in 0..<bounds.height) {
            for (x in 0..<bounds.width) {
                val pos = Point(x, y)
                result.append(if (isWall(pos)) "#" else ".")
            }
            if (y != bounds.height-1) result.append("\n")
        }
        return result.toString()
    }
}

fun main() {
    val drops = File("res/18.txt").readLines().map{
        val split = it.split(",").map{it.toInt()}
        Point(split[0], split[1])
    }

    val byteMaze = ByteMaze(drops, Rectangle(71, 71))
    val distanceFromStart1024 = hashMapOf<Point, Int>()
    byteMaze.floodFill(Point(0, 0), 1024, distanceFromStart1024)
    println(distanceFromStart1024[Point(byteMaze.bounds.width-1, byteMaze.bounds.height-1)])

    for (i in drops.indices) {
        val distanceFromStart = hashMapOf<Point, Int>()
        byteMaze.floodFill(Point(0, 0), i, distanceFromStart)
        val result = distanceFromStart[Point(byteMaze.bounds.width-1, byteMaze.bounds.height-1)]
//        println(result)
//        println(byteMaze.mark(emptySet(), '?', i))
        if (result == null) {
            println(drops[i-1])
            break
        }
    }
}