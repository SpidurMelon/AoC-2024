import java.io.File
import java.util.Vector

fun Int.gcd(other: Int): Int {
    var a = this
    var b = other
    var r = a % b
    while (r != 0) {
        a = b
        b = r
        r = a % b
    }
    return b
}

fun Int.lcm(other: Int): Int {
    return (this*other)/this.gcd(other)
}

data class Vector2l(val x: Long, val y: Long) {
    fun isDivisible(other: Vector2l): Boolean {
        return this.x % other.x == 0L && this.y % other.y == 0L && this.x/other.x == this.y/other.y
    }
    operator fun times(factor: Long): Vector2l {
        return Vector2l(this.x * factor, this.y * factor)
    }
    operator fun plus(other: Vector2l): Vector2l {
        return Vector2l(this.x + other.x, this.y + other.y)
    }
    operator fun minus(other: Vector2l): Vector2l {
        return Vector2l(this.x - other.x, this.y - other.y)
    }
    operator fun div(other: Vector2l): Vector2l {
        return Vector2l(this.x / other.x, this.y / other.y)
    }
    fun alphaToward(target: Vector2l): Long? {
        if (this !in target) return null
        return target.x/this.x
    }
    operator fun contains(other: Vector2l): Boolean {
        return isDivisible(other)
    }
    fun dot(other: Vector2l): Long {
        return this.x*other.x+this.y*other.y
    }
    fun project(onto: Vector2l): Double {
        val dotProduct = this.dot(onto)
        val divisor = onto.dot(onto)
        return dotProduct.toDouble()/divisor
    }
    fun transformCoordinates(v1: Vector2l, v2: Vector2l): Vector2l? {
        val determinant = v1.x*v2.y-v1.y*v2.x
        if (determinant == 0L) return null
        if ((v2.y*this.x - v2.x*this.y)%(v1.x*v2.y-v1.y*v2.x) != 0L ||
            (v1.y*this.x - v1.x*this.y)%(v2.x*v1.y-v2.y*v1.x) != 0L) return null

        val x = (v2.y*this.x - v2.x*this.y)/(v1.x*v2.y-v1.y*v2.x)
        val y = (v1.y*this.x - v1.x*this.y)/(v2.x*v1.y-v2.y*v1.x)
        return Vector2l(x, y)
    }
}

data class Machine(val A: Vector2l, val B: Vector2l, val prize: Vector2l) {
    fun cheapestPath(limit: Long): Vector2l? {
        for (a in 0..limit) {
            val delta = prize-(A*a)
            if (B in delta) {
                val bAlpha = B.alphaToward(delta)!!
                if (bAlpha <= limit) return Vector2l(a, bAlpha)
            }
        }
        return null
    }

    fun cheapestPathProperlyThisTime(): Vector2l? {
        val transformedCoords = prize.transformCoordinates(A, B)
//        if (transformedCoords != null) println("$transformedCoords => (${A*transformedCoords.x+B*transformedCoords.y} == $prize) ")
        return transformedCoords
    }
}

fun main() {
    val text = File("res/13.txt").readText()
    val regex = Regex("""Button A: X\+(?<AX>[0-9]+), Y\+(?<AY>[0-9]+)\nButton B: X\+(?<BX>[0-9]+), Y\+(?<BY>[0-9]+)\nPrize: X=(?<PX>[0-9]+), Y=(?<PY>[0-9]+)""")
    var part1 = 0L
    var part2 = 0L
    for (match in regex.findAll(text)) {
        val AV = Vector2l(match.groups["AX"]!!.value.toLong(), match.groups["AY"]!!.value.toLong())
        val BV = Vector2l(match.groups["BX"]!!.value.toLong(), match.groups["BY"]!!.value.toLong())
        val PV = Vector2l(match.groups["PX"]!!.value.toLong(), match.groups["PY"]!!.value.toLong())

        val machinePart1 = Machine(AV, BV, PV)
        val pathPart1 = machinePart1.cheapestPath(100)
        if (pathPart1 != null) part1 += (pathPart1.x*3 + pathPart1.y*1)

        val machinePart2 = Machine(AV, BV, PV+Vector2l(10000000000000, 10000000000000))
        val pathPart2 = machinePart2.cheapestPathProperlyThisTime()
//        println(pathPart2)
        if (pathPart2 != null) part2 += (pathPart2.x*3 + pathPart2.y*1)
    }
    println(part1)
    println(part2)
}
// Too low 875318608908