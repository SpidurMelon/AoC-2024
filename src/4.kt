import java.io.File

const val target = "XMAS"

fun getLetter(lines: List<String>, x: Int, y: Int): Char? {
    return lines.getOrNull(y)?.getOrNull(x)
}

fun checkXmas(lines: List<String>, x: Int, y: Int, direction: Pair<Int, Int>): Boolean {
    for ((i, c) in target.withIndex()) {
        val pos = Pair(x+direction.first*i, y+direction.second*i)
        val char = getLetter(lines, pos.first, pos.second)
        if (char != c) return false
    }
    return true
}

fun countXmas(lines: List<String>, x: Int, y: Int): Int {
    if (lines[y][x] != target.first()) {
        return 0
    } else {
        var total = 0
        for (dy in -1..1) {
            for (dx in -1..1) {
                if (dx == 0 && dy == 0) continue
                if (checkXmas(lines, x, y, Pair(dx, dy))) total++
            }
        }
        return total
    }
}

fun checkMasMas(lines: List<String>, x: Int, y: Int): Boolean {
    if (getLetter(lines, x, y) != 'A') return false
    val clockwiseCorners = listOf(getLetter(lines, x+1, y-1), getLetter(lines, x+1, y+1), getLetter(lines, x-1, y+1), getLetter(lines, x-1, y-1))
    var mCount = 0
    var sCount = 0
    for (c in clockwiseCorners) {
        if (c == 'M') mCount++
        if (c == 'S') sCount++
    }
    return mCount == 2 && sCount == 2 && (clockwiseCorners[0] != clockwiseCorners[2])
}

private fun part1(lines: List<String>): Int {
    var total = 0
    for (y in lines.indices) {
        val line = lines[y]
        for (x in line.indices) {
            total += countXmas(lines, x, y)
        }
    }
    return total
}

private fun part2(lines: List<String>): Int {
    var total = 0
    for (y in lines.indices) {
        val line = lines[y]
        for (x in line.indices) {
            if (checkMasMas(lines, x, y)) total++
        }
    }
    return total
}

fun main() {
    val lines = File("res/4.txt").readLines()
    println(part1(lines))
    println(part2(lines))
}