import java.awt.Point
import java.io.File

infix fun Point.hop(p2: Point): Point {
    return Point(this.x + (p2.x-this.x)*2, this.y + (p2.y-this.y)*2)
}

fun Point.hops(p2: Point, hops: Int): List<Point> {
    var result = mutableListOf<Point>()
    for (i in 1..hops) {
        result.add(Point(this.x + (p2.x-this.x)*(i), this.y + (p2.y-this.y)*(i)))
    }
    return result
}

fun Point.isInBounds(): Boolean {
    return this.x in 0..49 && this.y in 0..49
}

fun main() {
    val characterLocations = File("res/8.txt").readLines()
        .flatMapIndexed {y, line ->
            line.mapIndexed { x, char ->
                Pair(char, Point(x, y))
            }
        }
        .filter { it.first != '.' }
        .fold(hashMapOf<Char, MutableList<Point>>()) { hashMap, entry ->
            hashMap.putIfAbsent(entry.first, mutableListOf())
            hashMap[entry.first]!!.add(entry.second)
            hashMap
        }

    val part1 = characterLocations.flatMap {
        val antinodes = mutableSetOf<Point>()
        for (p in it.value) {
            for (p2 in it.value) {
                if (p == p2) continue
                antinodes.add(p hop p2)
                antinodes.add(p2 hop p)
            }
        }
        antinodes.filter { it.isInBounds() }
    }
    .toSet()
    println(part1.size)

    val part2 = characterLocations.flatMap {
        val antinodes = mutableSetOf<Point>()
        for (p in it.value) {
            for (p2 in it.value) {
                if (p == p2) continue
                antinodes.addAll(p.hops(p2, 50))
                antinodes.addAll(p2.hops(p, 50))
            }
        }
        antinodes.filter { it.isInBounds() }
    }
    .toSet()
    println(part2.size)
}