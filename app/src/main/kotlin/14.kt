import java.awt.Point
import java.io.File
import kotlin.math.max

operator fun Point.plus(other: Point): Point {
    return Point(this.x + other.x, this.y + other.y)
}

operator fun Point.rem(other: Point): Point {
    return Point(((this.x % other.x)+other.x)%other.x, ((this.y % other.y)+other.y)%other.y)
}

class Bathroom(lines: List<String>, val bounds: Point) {
    data class Robot(var p: Point, val v: Point) {
        fun step(bounds: Point) {
            p += v
            p %= bounds
        }
    }

    val robots = lines.map {
        val regex = Regex("""p=(?<pos>[0-9,\-]+) v=(?<vel>[0-9,\-]+)""")
        val matchResult = regex.find(it)
        val posSplit = matchResult!!.groups["pos"]!!.value.split(",")
        val pos = Point(posSplit[0].toInt(), posSplit[1].toInt())
        val velSplit = matchResult.groups["vel"]!!.value.split(",")
        val vel = Point(velSplit[0].toInt(), velSplit[1].toInt())
        Robot(pos, vel)
    }

    fun step() {
        robots.forEach{it.step(bounds)}
    }

    fun robotsInQuadrants(): List<Int> {
        val result = mutableListOf(0, 0, 0, 0)
        for (robot in robots) {
            if (robot.p.x < bounds.x/2 && robot.p.y < bounds.y/2) {
                result[0] += 1
            } else if (robot.p.x > bounds.x/2 && robot.p.y < bounds.y/2) {
                result[1] += 1
            } else if (robot.p.x < bounds.x/2 && robot.p.y > bounds.y/2) {
                result[2] += 1
            } else if (robot.p.x > bounds.x/2 && robot.p.y > bounds.y/2) {
                result[3] += 1
            }
        }
        return result
    }

    fun robotCounts(): HashMap<Point, Int> {
        val result: HashMap<Point, Int> = hashMapOf()
        for (robot in robots) {
            result.putIfAbsent(robot.p, 0)
            result[robot.p] = result[robot.p]!! + 1
        }
        return result
    }

    fun adjacencyValue(): Float {
        val counts = robotCounts()
        var totalAdjacency = 0

        for (robot in robots) {
            var adjacent = 0
            for (dy in -1..<1) {
                for (dx in -1..<1) {
                    if (dx == 0 && dy == 0) continue
                    val delta = Point(dx, dy)
                    if ((counts[robot.p + delta] ?: 0) > 0) {
                        adjacent += 1
                    }
                }
            }
            totalAdjacency += adjacent

        }
        return totalAdjacency.toFloat() / robots.size
    }

    override fun toString(): String {
        val counts = robotCounts()
        val result = StringBuilder()
        for (y in 0..<bounds.y) {
            for (x in 0..<bounds.x) {
                val count = counts[Point(x, y)] ?: 0
                if (count > 9) println("COUNT IS MORE THAN 9")
                if (count == 0) result.append(".")
                else result.append(count)
            }
            result.append("\n")
        }
        return result.toString()
    }
}

fun main() {
    val bounds = Point(101, 103)
    val bounds_test = Point(11, 7)
    val part1 = Bathroom(File("res/14.txt").readLines(), bounds)
    for (i in 0..99) {
        part1.step()
    }
    println(part1.robotsInQuadrants().reduce{i1, i2 -> i1 * i2})

    val part2 = Bathroom(File("res/14.txt").readLines(), bounds)
    for (i in 0..9999) {
        val adjacency = part2.adjacencyValue()
        if (adjacency > 1f) {
            println(part2)
            println(i)
        }
        part2.step()
    }
}