import java.awt.Rectangle
import java.io.File
import java.lang.Exception

class Template(lines: List<String>) {
    val bounds = Rectangle(5, 7)
    val isLock = lines[0] == "#####"
    val heights = (0..<bounds.width).map {x ->
        (0..<bounds.height).sumOf { y ->
            (lines[y][x] == '#').toInt()
        } - 1
    }

    fun overlaps(other: Template): Boolean {
        if (this.isLock == other.isLock) throw Exception("Overlapping a lock/key with the same type does not make sense")
        return this.heights.mapIndexed{i, h ->
            h+other.heights[i]
        }.max() >= 6
    }

    override fun toString(): String {
        return "${if (isLock) "lck:" else "key:"}[${heights.joinToString(separator = "")}]"
    }
}

fun Boolean.toInt(): Int = if (this) 1 else 0

fun main() {
    val templateStrings = File("res/25.txt").readText().split("\n\n")
    val templateLines = templateStrings.map {it.split("\n") }
    val templates = templateLines.map { Template(it) }
    val locks = templates.filter { it.isLock }
    val keys = templates.filter { !it.isLock }
    val lockFitsCount = locks.map {lock ->
        keys.sumOf { key ->
            (!lock.overlaps(key)).toInt()
        }
    }
    println(lockFitsCount.sum())
}