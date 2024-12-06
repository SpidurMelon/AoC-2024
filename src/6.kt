import java.awt.Point
import java.io.File

val lines = File("res/6.txt").readLines()

enum class Tile {
    EMPTY, OBSTACLE, OUTSIDE
}

enum class Direction(val x: Int, val y: Int) {
    UP(0, -1),
    RIGHT(1, 0),
    DOWN(0, 1),
    LEFT(-1, 0);

    fun rotated(): Direction {
        return when (this) {
            UP -> RIGHT
            RIGHT -> DOWN
            DOWN -> LEFT
            LEFT -> UP
        }
    }
}

private fun getTile(pos: Point, extraObstacles: Set<Point> = emptySet()): Tile {
    if (extraObstacles.contains(pos)) return Tile.OBSTACLE
    val char = lines.getOrNull(pos.y)?.getOrNull(pos.x)
    return when (char) {
        '#' -> Tile.OBSTACLE
        null -> Tile.OUTSIDE
        else -> Tile.EMPTY
    }
}

private fun find(c: Char): Point? {
    for ((y, line) in lines.withIndex()) {
        for ((x, char) in line.withIndex()) {
            if (char == c) return Point(x, y)
        }
    }
    return null
}

private fun Point.moved(direction: Direction): Point {
    return Point(this.x + direction.x, this.y + direction.y)
}

private fun <T> HashSet<T>.added(value: T): HashSet<T> {
    this.add(value)
    return this
}

private fun walk(pos: Point, direction: Direction, path: HashSet<Pair<Point, Direction>> = hashSetOf(), extraObstacles: Set<Point> = emptySet(), func: ((p: Point, d: Direction) -> Unit)? = null): Tile? {
    when (getTile(pos, extraObstacles)) {
        Tile.OBSTACLE -> return Tile.OBSTACLE
        Tile.EMPTY -> {
            val state = Pair(pos, direction)

            if (state in path) return null
            func?.invoke(pos, direction)
            if (getTile(pos.moved(direction), extraObstacles) == Tile.OBSTACLE) {
                return walk(pos, direction.rotated(), path.added(state), extraObstacles, func)
            } else {
                return walk(pos.moved(direction), direction, path.added(state), extraObstacles, func)
            }
        }
        Tile.OUTSIDE -> return Tile.OUTSIDE
    }
}

fun main() {
    val visited: MutableSet<Point> = hashSetOf()
    val path = HashSet<Pair<Point, Direction>>()
    val loopObstacles: MutableList<Point> = mutableListOf()
    walk(find('^')!!, Direction.UP) { p, d ->
        visited.add(p)
        path.add(Pair(p, d))

        val loops = walk(p, d.rotated(), path.toHashSet(), hashSetOf(p.moved(d)))

        if (p.moved(d) !in visited && loops == null)  {
            loopObstacles.add(p.moved(d))
        }
    }
    // Part 1
    println(visited.size)
    // Part 2
    println(loopObstacles.size)
}