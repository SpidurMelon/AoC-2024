import java.awt.Point
import java.io.File

private val lines: List<String> = File("res/10.txt").readLines()
private fun getHeight(x: Int, y: Int): Int? {
    return lines.getOrNull(y)?.getOrNull(x)?.digitToInt()
}
private fun walk(x: Int, y: Int): List<Point> {
    val currentHeight: Int = getHeight(x, y)?: return listOf()
    if (currentHeight == 9) return listOf(Point(x, y))

    val validPaths = mutableListOf<Point>()
    if (getHeight(x + 1, y) == currentHeight + 1) validPaths.addAll(walk(x + 1, y))
    if (getHeight(x - 1, y) == currentHeight + 1) validPaths.addAll(walk(x - 1, y))
    if (getHeight(x, y + 1) == currentHeight + 1) validPaths.addAll(walk(x, y + 1))
    if (getHeight(x, y - 1) == currentHeight + 1) validPaths.addAll(walk(x, y - 1))
    return validPaths
}

fun main() {
    val reachablePeaks: HashMap<Point, List<Point>> = hashMapOf()
    for (y in lines.indices) {
        for ((x, c) in lines[y].withIndex()) {
            if (c == '0') reachablePeaks[Point(x, y)] = walk(x, y)
        }
    }
    println(reachablePeaks.values.map { it.toSet() }.sumOf { it.size })
    println(reachablePeaks.values.sumOf { it.size })
}