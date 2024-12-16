import java.awt.Point
import java.awt.Rectangle
import java.io.File

class Maze(lines: List<String>) {
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
    enum class Tile(val char: Char) {
        START('S'),
        END('E'),
        WALL('#'),
        EMPTY('.');

        companion object {
            fun fromChar(c: Char): Tile? {
                return Tile.entries.find{ it.char == c }
            }
        }
    }

    val bounds = Rectangle(0, 0, lines[0].length, lines.size)

    val tiles: List<MutableList<Tile>> = lines.map{ it.map{ Tile.fromChar(it)!! }.toMutableList() }

    operator fun Point.plus(dir: Direction): Point {
        return Point(x+dir.x, y+dir.y)
    }

    fun getTile(pos: Point): Tile? {
        return getTile(pos.x, pos.y)
    }

    fun getTile(x: Int, y: Int): Tile? {
        return tiles.getOrNull(y)?.getOrNull(x)
    }

    fun find(c: Char): List<Point> {
        return tiles.flatMapIndexed{y, row ->
            List(row.size){ x -> Point(x, y) }.filterIndexed{ x, _ -> row[x].char == c }
        }
    }

    fun minOf(vararg ints: Int?): Int? {
        return ints.minByOrNull { it ?: Integer.MAX_VALUE }
    }

    fun walk(pos: Point, dir: Direction, pointsMap: HashMap<Point, Int>, points: Int = 0) {
        if (points >= pointsMap[pos]!!) return
        val tile = getTile(pos)
        if (tile == Tile.WALL) return

        pointsMap[pos] = points

        if (tile == Tile.END) return

        walk(pos + dir.rotatedLeft(), dir.rotatedLeft(), pointsMap, points + 1001)
        walk(pos + dir, dir, pointsMap, points + 1)
        walk(pos + dir.rotatedRight(), dir.rotatedRight(), pointsMap, points + 1001)
    }

    fun track(pos: Point, dir: Direction, pointsMap: HashMap<Point, Int>, points: Int = 0): Set<Point> {
        val tile = getTile(pos)

        val expectedPoints = pointsMap[pos]!!
        // The second clause is for an edge case in my implementation.
        // My implementation stores the point count to get to a node.
        // But at intersections, paths that don't curve should have an advantage.
        // This is because the step afterwards is cheaper for them.
        // This clause is to make sure straight paths also get considered, even if they technically get to the current node slower.
        if (points != expectedPoints && pointsMap[pos + dir] != points + 1) return emptySet()

        if (tile == Tile.END) return setOf(pos)
        if (tile == Tile.WALL) return emptySet()

        val left = track(pos + dir.rotatedLeft(), dir.rotatedLeft(), pointsMap, points + 1001)
        val straight = track(pos + dir, dir, pointsMap, points + 1)
        val right = track(pos + dir.rotatedRight(), dir.rotatedRight(), pointsMap, points + 1001)

        val result = left + straight + right
        if (result.isNotEmpty()) {
            return result + pos
        } else {
            return emptySet()
        }
    }

    fun mark(locations: Set<Point>, marker: Char): String {
        val result = StringBuilder()
        for (y in 0..<bounds.height) {
            for (x in 0..<bounds.width) {
                val location = Point(x, y)
                if (location in locations) result.append(marker)
                else result.append(getTile(location)!!.char)
            }
            if (y != bounds.height-1) result.append("\n")
        }
        return result.toString()
    }

    override fun toString(): String {
        val result = StringBuilder()
        for (y in 0..<bounds.height) {
            for (x in 0..<bounds.width) {
                result.append(getTile(x, y)!!.char)
            }
            if (y != bounds.height-1) result.append("\n")
        }
        return result.toString()
    }
}

fun main() {
    val maze = Maze(File("res/16.txt").readLines())
    val pointsMap: HashMap<Point, Int> = hashMapOf()
    for (y in 0..<maze.bounds.height) {
        for (x in 0..<maze.bounds.width) {
            pointsMap[Point(x, y)] = Int.MAX_VALUE
        }
    }
    val startPoint = maze.find('S')[0]
    maze.walk(startPoint, Maze.Direction.RIGHT, pointsMap)
    // Part 1
    println(pointsMap[maze.find('E')[0]])
    // Part 2
    println(maze.track(startPoint, Maze.Direction.RIGHT, pointsMap).size)
}