import java.io.File
import kotlin.math.max

class Graph<T> {
    var nodes: HashMap<T, Node<T>> = HashMap()

    data class Node<T>(val value: T) {
        var incoming: MutableList<Node<T>> = mutableListOf()
        var outgoing: MutableList<Node<T>> = mutableListOf()

        fun hasIn(value: T): Boolean {
            return incoming.any { it.value == value }
        }

        fun hasOut(value: T): Boolean {
            return outgoing.any { it.value == value }
        }
    }

    fun connect(from: T, to: T) {
        nodes.putIfAbsent(from, Node(from))
        nodes.putIfAbsent(to, Node(to))

        val fromNode = nodes[from]!!
        val toNode = nodes[to]!!

        fromNode.outgoing.add(toNode)
        toNode.incoming.add(fromNode)
    }
}

fun isProperlyOrdered(numbers: List<Int>, rules: Graph<Int>): Boolean {
    for ((i, numberBefore) in numbers.withIndex()) {
        for ((j, numberAfter) in numbers.withIndex()) {
            if (j <= i) continue

            val beforeNode = rules.nodes[numberBefore]
            val afterNode = rules.nodes[numberAfter]

            if (beforeNode == null || afterNode == null) continue

            if (beforeNode.hasIn(afterNode.value)) return false
        }
    }
    return true
}

fun main() {
    val rules = File("res/5_rules.txt").readLines()
    val lines = File("res/5.txt").readLines()

    val graph = Graph<Int>()
    for (rule in rules) {
        val split = rule.split("|")
        val left = split[0].toInt()
        val right = split[1].toInt()
        graph.connect(left, right)
    }

    var part1Total = 0
    var part2Total = 0
    for ((i, line) in lines.withIndex()) {
        val numbers = line.split(",").map { it.toInt() }
        if (isProperlyOrdered(numbers, graph)) {
            part1Total += numbers[numbers.size/2]
        } else {
            part2Total += numbers.sortedWith{i1, i2 -> if (graph.nodes[i1]!!.hasOut(i2)) -1 else 1 }[numbers.size/2]
        }
    }
    println(part1Total)
    println(part2Total)

    sameButShort()
}

fun sameButShort() {
    val rules = File("res/5_rules.txt").readLines()
    val lines = File("res/5.txt").readLines()
    var part1Result = 0
    var part2Result = 0
    for (line in lines) {
        val numbers = line.split(",").map { it.toInt() }
        val sortedNumbers = numbers.sortedWith{i1, i2 -> if ("$i1|$i2" in rules) -1 else 1 }
        if (sortedNumbers == numbers) {
            part1Result += numbers[numbers.size/2]
        } else {
            part2Result += sortedNumbers[sortedNumbers.size/2]
        }
    }
    println(part1Result)
    println(part2Result)
}
