import java.awt.Point
import java.awt.Rectangle
import java.io.File

class Warehouse(lines: List<String>, robotInstructions: String) {
    enum class Direction(val x: Int, val y: Int) {
        UP(0, -1),
        RIGHT(1, 0),
        DOWN(0, 1),
        LEFT(-1, 0);

        companion object {
            fun fromChar(c: Char) = when (c) {
                '^' -> Direction.UP
                '>' -> Direction.RIGHT
                'v' -> Direction.DOWN
                '<' -> Direction.LEFT
                else -> null
            }
        }

    }
    enum class Tile(val char: Char) {
        ROBOT('@'),
        BOX('O'),
        WALL('#'),
        EMPTY('.'),
        LEFTBOX('['),
        RIGHTBOX(']');

        companion object {
            fun fromChar(c: Char): Tile? {
                return Tile.entries.find{ it.char == c }
            }
        }
    }

    val bounds = Rectangle(0, 0, lines[0].length, lines.size)
    var robot: Point = Point(-1, -1)
    val tiles: List<MutableList<Tile>> = lines.mapIndexed { y, line ->
        line.mapIndexed { x, char ->
            if (char == '@') robot = Point(x, y)
            Tile.fromChar(char)!!
        }.toMutableList()
    }

    val instructions = robotInstructions.map { Direction.fromChar(it)!! }
    var instructionIndex = 0

    operator fun Point.plus(dir: Direction): Point {
        return Point(x+dir.x, y+dir.y)
    }

    fun getTile(pos: Point): Tile? {
        return getTile(pos.x, pos.y)
    }

    fun getTile(x: Int, y: Int): Tile? {
        return tiles.getOrNull(y)?.getOrNull(x)
    }

    fun setTile(pos: Point, tile: Tile) {
        tiles[pos.y][pos.x] = tile
    }

    fun moveTile(pos: Point, direction: Direction, move: Boolean = true): Boolean {
        val tile = getTile(pos)!!
        if (tile == Tile.EMPTY) return true
        if (tile == Tile.WALL) return false

        // Double-wide boxes
        if ((direction == Direction.UP || direction == Direction.DOWN)) {
            if (tile == Tile.LEFTBOX) {
                val leftCanMove = moveTile(pos + direction, direction, false)
                val rightCanMove = moveTile(pos + Direction.RIGHT + direction, direction, false)
                if (leftCanMove && rightCanMove && move) {
                    moveTile(pos + direction, direction, true)
                    moveTile(pos + Direction.RIGHT + direction, direction, true)
                    setTile(pos+direction, tile)
                    setTile(pos, Tile.EMPTY)
                    setTile(pos+Direction.RIGHT+direction, Tile.RIGHTBOX)
                    setTile(pos+Direction.RIGHT, Tile.EMPTY)
                }
                return leftCanMove && rightCanMove
            } else if (tile == Tile.RIGHTBOX) {
                val leftCanMove = moveTile(pos + Direction.LEFT + direction, direction, false)
                val rightCanMove = moveTile(pos + direction, direction, false)
                if (leftCanMove && rightCanMove && move) {
                    moveTile(pos + Direction.LEFT + direction, direction, true)
                    moveTile(pos + direction, direction, true)
                    setTile(pos+direction, tile)
                    setTile(pos, Tile.EMPTY)
                    setTile(pos+Direction.LEFT+direction, Tile.LEFTBOX)
                    setTile(pos+Direction.LEFT, Tile.EMPTY)
                }
                return leftCanMove && rightCanMove
            }
        }

        val canMove = moveTile(pos + direction, direction, move)
        if (canMove && move) {
            setTile(pos+direction, tile)
            setTile(pos, Tile.EMPTY)
        }
        return canMove
    }

    fun moveRobot(direction: Direction): Boolean {
        if (moveTile(robot, direction)) {
            robot += direction
            return true
        } else {
            return false
        }
    }

    fun step() {
        moveRobot(instructions[instructionIndex])
        instructionIndex++
    }

    fun steps(count: Int) {
        for (i in 0..<count) {
            step()
        }
    }

    fun part1(): Long {
        var result = 0L
        tiles.forEachIndexed{y, row ->
            row.forEachIndexed{x, tile ->
                if (tile == Tile.BOX) result += y*100 + x
            }
        }
        return result
    }

    fun part2(): Long {
        var result = 0L
        tiles.forEachIndexed{y, row ->
            row.forEachIndexed{x, tile ->
                if (tile == Tile.LEFTBOX) result += y*100 + x
            }
        }
        return result
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
    val lines = File("res/15_warehouse.txt").readLines()
    val instructions = File("res/15_instructions.txt").readText().replace("\n", "")

    val warehousePart1 = Warehouse(lines, instructions)
    warehousePart1.steps(instructions.length)
    println(warehousePart1.part1())

    val part2Lines = lines.map {it
        .replace("#", "##")
        .replace("O", "[]")
        .replace(".", "..")
        .replace("@", "@.")
    }
    val warehousePart2 = Warehouse(part2Lines, instructions)
    warehousePart2.steps(instructions.length)
    println(warehousePart2.part2())
}