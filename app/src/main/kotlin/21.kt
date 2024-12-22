import java.awt.Point
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder

class Pad() {
    val buttons: HashMap<Point, Button> = hashMapOf()
    val charPositions: HashMap<Char, Point> = hashMapOf()

    inner class Button(val pos: Point, val char: Char) {
        var up: Button? = null
        var right: Button? = null
        var down: Button? = null
        var left: Button? = null

        var paths: HashMap<Char, MutableSet<List<Direction>>> = hashMapOf()

        fun distanceTo(c: Char): Int? {
            return paths[c]?.first()?.size
        }

        fun getNeighbour(direction: Direction): Button? = when (direction) {
            Direction.UP -> up
            Direction.RIGHT -> right
            Direction.DOWN -> down
            Direction.LEFT -> left
            Direction.NEUTRAL -> this
        }

        fun setNeighbour(direction: Direction, button: Button?) {
            when (direction) {
                Direction.UP -> up = button
                Direction.RIGHT -> right = button
                Direction.DOWN -> down = button
                Direction.LEFT -> left = button
                Direction.NEUTRAL -> throw Exception("Can't override self")
            }
        }

        fun getPaths(target: Char): Set<List<Direction>>? {
            return paths[target]
        }

        fun updatePaths() {
            paths = calcPaths()
        }

        private fun calcPaths(visited: Set<Button> = emptySet()): HashMap<Char, MutableSet<List<Direction>>> {
            if (this in visited) return hashMapOf()
            val result = hashMapOf<Char, MutableSet<List<Direction>>>()
            result[this.char] = mutableSetOf(emptyList())
            val newVisited = visited + this
            for (direction in Direction.cardinals()) {
                val neighbour = getNeighbour(direction) ?: continue
                for (pathsTo in neighbour.calcPaths(newVisited)) {
                    result.putIfAbsent(pathsTo.key, mutableSetOf())
                    result[pathsTo.key]!!.addAll(pathsTo.value.map { listOf(direction) + it }.filter { it.curves() <= 1 })
                }
            }
            return result
        }

        fun List<Direction>.curves(): Int {
            return this.foldIndexed(0){i, acc, dir ->
                if (i == 0) return@foldIndexed acc
                acc + (if (dir == this[i-1]) 0 else 1)
            }
        }
    }

    fun find(c: Char): Set<Button> {
        return buttons.filter{ it.value.char == c }.values.toSet()
    }

    fun addButton(pos: Point, char: Char) {
        charPositions[char] = pos
        buttons[pos] = Button(pos, char)
        for (direction in Direction.cardinals()) {
            if ((pos + direction) !in buttons) continue
            buttons[pos]!!.setNeighbour(direction, buttons[pos + direction])
            buttons[pos + direction]!!.setNeighbour(direction.flip(), buttons[pos])
        }
        buttons.values.forEach(Button::updatePaths)
    }

    fun addButton(x: Int, y: Int, char: Char) {
        addButton(Point(x, y), char)
    }

    fun getButton(char: Char): Button? {
        return buttons[charPositions[char]]
    }

    fun getPaths(from: Char, to: Char): Set<String>? {
        return getButton(from)?.getPaths(to)?.map{it.stringify()}?.toSet()
    }

    fun follow(path: String, pos: Point = find('A').first().pos): String {
        if (path.isEmpty()) return ""
        val result = StringBuilder()
        if (path.first() == 'A') result.append(buttons[pos]!!.char)
        result.append(follow(path.drop(1), pos + (Direction.fromChar(path.first()) ?: Direction.NEUTRAL)))
        return result.toString()
    }

    fun distance(c1: Char?, c2: Char?): Int? {
        if (c1 == null || c2 == null) return null
        if (c1 == c2) return 0
        return buttons[charPositions[c1]]?.distanceTo(c2)
    }

    fun cost(text: String, prefixA: Boolean = true): Int {
        if (prefixA) {
            return cost("A$text", false)
        }
        return text.foldIndexed(0){i, acc, c ->
            if (i == 0) return@foldIndexed acc
            acc + distance(text[i-1], c)!!
        }
    }

    fun List<Direction>.stringify(): String {
        return this.map { it.char }.joinToString(separator = "", postfix = "A")
    }
}

fun Set<String>.getMinCosts(): Set<String> {
    val minCost = dirPad.cost(this.minBy { dirPad.cost(it) })
    return this.filter { dirPad.cost(it) == minCost }.toSet()
}

val numPad: Pad = Pad()
val dirPad: Pad = Pad()

// Assumes a start from 'A'
// Robot 0 is you
// Robot MAX is the last directional pad robot
fun getBestPathLengthForRobot(code: String, memo: HashMap<Pair<String, Int>, Long>? = null, robot: Int = 0, pad: Pad): Long {
    if (code.last() != 'A') throw Exception("Has to end in A to make sense")
    val state = Pair(code, robot)
    if (memo != null && state in memo) return memo[state]!!
    val result: Long
    val allPaths: Set<String> = code.foldIndexed(setOf()){ i, acc, c ->
        if (i == 0) return@foldIndexed pad.getPaths('A', c)!!
        val nextAcc = mutableSetOf<String>()
        val subPaths = pad.getPaths(code[i-1], c)!!
        for (superPath in acc) {
            for (subPath in subPaths) {
                nextAcc.add(superPath + subPath)
            }
        }
        nextAcc
    }
    if (robot == 0) {
        result = allPaths.minBy{ it.length }.length.toLong()
    } else {
        result = allPaths.minOfOrNull { fullPath ->
            var pathLength = 0L
            val split = fullPath.split("A").map { it + "A" }.dropLast(1)
            for (subPath in split) {
                pathLength += getBestPathLengthForRobot(subPath, memo, robot - 1, dirPad)
            }
            pathLength
        }!!
    }

    if (memo != null) memo[state] = result
    return result
}

fun main() {
    numPad.addButton(0, 0, '7')
    numPad.addButton(1, 0, '8')
    numPad.addButton(2, 0, '9')
    numPad.addButton(0, 1, '4')
    numPad.addButton(1, 1, '5')
    numPad.addButton(2, 1, '6')
    numPad.addButton(0, 2, '1')
    numPad.addButton(1, 2, '2')
    numPad.addButton(2, 2, '3')
    numPad.addButton(1, 3, '0')
    numPad.addButton(2, 3, 'A')

    dirPad.addButton(1, 0, '^')
    dirPad.addButton(2, 0, 'A')
    dirPad.addButton(0, 1, '<')
    dirPad.addButton(1, 1, 'v')
    dirPad.addButton(2, 1, '>')

    val lines = File("res/21.txt").readLines()

    val memo: HashMap<Pair<String, Int>, Long> = hashMapOf()

    var part1 = 0L
    var part2 = 0L
    lines.forEach{line ->
        part1 += getBestPathLengthForRobot(line, memo, 2, numPad) * line.substring(0, 3).toInt()
        part2 += getBestPathLengthForRobot(line, memo, 25, numPad) * line.substring(0, 3).toInt()
    }
    println(part1)
    println(part2)
}