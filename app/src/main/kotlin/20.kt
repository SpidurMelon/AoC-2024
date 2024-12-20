import java.awt.Point
import java.awt.Rectangle
import java.io.File
import kotlin.math.abs

class RaceTrack(lines: List<String>) {
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

    val bounds = Rectangle(lines[0].length, lines.size)

    val tiles: List<MutableList<Tile>> = lines.map{ it.map{ Tile.fromChar(it)!! }.toMutableList() }

    operator fun Point.plus(dir: Direction): Point {
        return Point(x+dir.x, y+dir.y)
    }

    fun Point.manhattan(other: Point): Int {
        return abs(other.x-this.x) + abs(other.y-this.y)
    }

    fun manhattanLength(p: Point): Int {
        return abs(p.x) + abs(p.y)
    }

    fun Point.inManhattan(distance: Int): Set<Point> {
        val result = mutableSetOf<Point>()
        for (y in -distance..distance) {
            for (x in -distance..distance) {
                val to = Point(x, y)
                if (manhattanLength(to) <= distance) result.add(this+to)
            }
        }
        return result
    }

    fun getTile(pos: Point): Tile? {
        return getTile(pos.x, pos.y)
    }

    fun getTile(x: Int, y: Int): Tile? {
        return tiles.getOrNull(y)?.getOrNull(x)
    }

    fun setTile(pos: Point, tile: Tile) {
        setTile(pos.x, pos.y, tile)
    }

    fun setTile(x: Int, y: Int, tile: Tile) {
        tiles[y][x] = tile
    }

    fun find(c: Char): List<Point> {
        return tiles.flatMapIndexed{y, row ->
            List(row.size){ x -> Point(x, y) }.filterIndexed{ x, _ -> row[x].char == c }
        }
    }

    fun minOf(vararg ints: Int?): Int? {
        return ints.minByOrNull { it ?: Integer.MAX_VALUE }
    }

    fun cheats(from: Point, distanceFromStart: HashMap<Point, Int>, allowedDistance: Int): HashMap<Point, Int> {
        val result = hashMapOf<Point, Int>()
        val fromDistance = distanceFromStart[from]!!
        for (to in from.inManhattan(allowedDistance)) {
            if (from == to || getTile(to) == null || getTile(to) == RaceTrack.Tile.WALL) continue
            val cheatDistance = from.manhattan(to)
            val toDistance = distanceFromStart[to]!!
            if (fromDistance+cheatDistance < toDistance) {
                result[to] = toDistance-(fromDistance+cheatDistance)
            }
        }
        return result
    }

    fun floodPos(pos: Point, distanceFromStart: HashMap<Point, Int>, currentDistance: Int): Set<Point> {
        distanceFromStart[pos] = currentDistance
        val filled = mutableSetOf<Point>()
        for (direction in Direction.cardinals()) {
            val p = pos+direction
            val tile = getTile(p)
            if (tile != Tile.WALL && p !in distanceFromStart) {
                filled.add(p)
            }
        }
        return filled
    }

    fun floodFill(start: Point, distanceFromStart: HashMap<Point, Int>) {
        var currentDistance: Int = 0
        var adjacent = floodPos(start, distanceFromStart, currentDistance)
        while (adjacent.isNotEmpty()) {
            currentDistance += 1
            val newAdjacent = mutableSetOf<Point>()
            for (pos in adjacent) {
                newAdjacent.addAll(floodPos(pos, distanceFromStart, currentDistance))
            }
            adjacent = newAdjacent
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
    val raceTrack = RaceTrack(File("res/20.txt").readLines())
    val startPoint = raceTrack.find('S')[0]
    val distanceFromStart: HashMap<Point, Int> = hashMapOf()
    raceTrack.floodFill(startPoint, distanceFromStart)

    val part1CheatValues = hashMapOf<Int, Int>()
    val part2CheatValues = hashMapOf<Int, Int>()
    for (y in 1..<raceTrack.bounds.height-1) {
        for (x in 1..<raceTrack.bounds.width-1) {
            val cheatFrom = Point(x, y)
            if (raceTrack.getTile(cheatFrom) == RaceTrack.Tile.WALL) continue
            val part1Cheats = raceTrack.cheats(cheatFrom, distanceFromStart, 2)
            part1Cheats.values.forEach{v ->
                part1CheatValues.putIfAbsent(v, 0)
                part1CheatValues[v] = part1CheatValues[v]!! + 1
            }
            val part2Cheats = raceTrack.cheats(cheatFrom, distanceFromStart, 20)
            part2Cheats.values.forEach{v ->
                part2CheatValues.putIfAbsent(v, 0)
                part2CheatValues[v] = part2CheatValues[v]!! + 1
            }
        }
    }
    println(part1CheatValues.filter{ it.key >= 100 }.values.sum())
    println(part2CheatValues.filter{ it.key >= 100 }.values.sum())
}