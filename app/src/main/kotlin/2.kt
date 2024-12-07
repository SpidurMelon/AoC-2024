import java.io.File
import kotlin.math.*

private fun part1(lines: List<String>): Int {
    return lines.fold(0, {acc, l ->
        val numbers = l.split(" ").map{ s -> s.toInt() }
        var strictly_increasing = true
        var strictly_decreasing = true
        var dif_predicate = true

        for (i in 0..numbers.size-2) {
            var dif = numbers[i+1]-numbers[i]
            if (dif < 0) strictly_increasing = false
            if (dif > 0) strictly_decreasing = false
            if (abs(dif) !in 1..3) dif_predicate = false
        }

        if (dif_predicate && (strictly_increasing || strictly_decreasing)) {
            return@fold acc+1
        }
        return@fold acc
    })
}

fun part2Helper(numbers: List<Int>): Boolean {
    var strictly_increasing = true
    var strictly_decreasing = true
    var dif_predicate = true

    for (i in 0..numbers.size-2) {
        var dif = numbers[i+1]-numbers[i]
        if (dif < 0) strictly_increasing = false
        if (dif > 0) strictly_decreasing = false
        if (abs(dif) !in 1..3) dif_predicate = false
    }

    if (dif_predicate && (strictly_increasing || strictly_decreasing)) {
        return true
    }
    return false
}

private fun part2(lines: List<String>): Int {
    return lines.fold(0, {acc, l ->
        val numbers = l.split(" ").map{ s -> s.toInt() }
        for (index in numbers.indices) {
            val smol = numbers.toMutableList()
            smol.removeAt(index)
            if (part2Helper(smol)) {
                return@fold acc + 1
            }
        }

        return@fold acc
    })
}

fun main() {
    val lines: List<String> = File("res/2.txt").readLines()
    val safeCases = listOf("1 2 3", "1 2 1 3", "6 3 4 1", "1 2 500", "500 2 1", "500 2 3", "1")
    val unsafeCases = listOf("1 2 100 200", "500 2 1 3", "30 40 50 10 40")
    println("${part2(safeCases)}=${safeCases.size}")
    println("${part2(unsafeCases)}=0")

    println(part1(lines))
    println(part2(lines))
}