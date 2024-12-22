import java.awt.Point
import java.io.File

enum class Tile {
    EMPTY, OBSTACLE, OUTSIDE
}

data class State(val pos: Point, val direction: Direction) {
    fun advanced(): State {
        return State(pos.moved(direction), direction)
    }
    fun rotated(): State {
        return State(pos, direction.rotated())
    }
    override fun toString(): String {
        return "(${pos.x},${pos.y}) $direction"
    }
}

data class Grid(private val data: List<CharArray>) {
    fun getTile(pos: Point): Tile {
        val char = data.getOrNull(pos.y)?.getOrNull(pos.x)
        return when (char) {
            '#' -> Tile.OBSTACLE
            null -> Tile.OUTSIDE
            else -> Tile.EMPTY
        }
    }

    fun setTile(pos: Point, char: Char) {
        val inBounds = (data.getOrNull(pos.y)?.getOrNull(pos.x) != null)
        if (inBounds) {
            data[pos.y][pos.x] = char
        }
    }

    fun find(c: Char): Point? {
        for ((y, line) in data.withIndex()) {
            for ((x, char) in line.withIndex()) {
                if (char == c) return Point(x, y)
            }
        }
        return null
    }

    fun walk(state: State, path: MutableSet<State> = mutableSetOf(), callback: ((State) -> Unit)? = null): Tile? {
        when (getTile(state.pos)) {
            Tile.OBSTACLE -> return Tile.OBSTACLE
            Tile.EMPTY -> {
                if (state in path) return null
                path.add(state)

                callback?.invoke(state)
                if (getTile(state.advanced().pos) == Tile.OBSTACLE) {
                    return walk(state.rotated(), path, callback)
                } else {
                    return walk(state.advanced(), path, callback)
                }
            }
            Tile.OUTSIDE -> return Tile.OUTSIDE
        }
    }

    fun addedObstacle(pos: Point): Grid {
        val dataCopy = data.toList().map { it.copyOf() }
        val gridCopy = Grid(dataCopy)
        gridCopy.setTile(pos, '#')
        return gridCopy
    }
}

private fun Point.moved(direction: Direction): Point {
    return Point(this.x + direction.x, this.y + direction.y)
}

fun main() {
    val lines = File("res/6.txt").readLines()
    val grid = Grid(lines.map { it.toCharArray() })

    val visited = mutableSetOf<Point>()
    val path = mutableSetOf<State>()
    val loopingObstacles = mutableListOf<Point>()

    val startState = State(grid.find('^')!!, Direction.UP)

    grid.walk(startState) { state ->
        visited.add(state.pos)
        path.add(state)

        val ahead = state.advanced()

        val wouldLoop = grid
            .addedObstacle(ahead.pos)
            .walk(state.rotated(), path.toMutableSet()) == null

        if (ahead.pos !in visited && wouldLoop)  {
            loopingObstacles.add(ahead.pos)
        }
    }

    // Part 1
    println(visited.size)
    // Part 2
    println(loopingObstacles.size)
}