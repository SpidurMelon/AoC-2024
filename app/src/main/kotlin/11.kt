import java.io.File

// Common function
fun Long.blink(): List<Long> {
    if (this == 0L) return listOf(1)

    val s = this.toString()
    if (s.length % 2 == 0) {
        val left = s.substring(0, s.length/2).toLong()
        val right = s.substring(s.length/2, s.length).toLong()
        return listOf(left, right)
    }

    return listOf(this*2024)
}

// Part 1 functions (List based)
fun Long.blinks(count: Int): List<Long> {
    var result = listOf(this)
    for (i in 0..<count) {
        result = result.blink()
    }
    return result
}

fun List<Long>.blink(): List<Long> {
    return this.flatMap { it.blink() }
}

fun List<Long>.blinks(count: Int): List<Long> {
    val result = mutableListOf<Long>()
    for (l in this) {
        result.addAll(l.blinks(count))
    }
    return result
}

// Part 2 functions (Map based)
fun MutableMap<Long, Long>.blink(): MutableMap<Long, Long> {
    val result = mutableMapOf<Long, Long>()
    for (entry in this.entries) {
        for (l in entry.key.blink()) {
            result.putIfAbsent(l, 0)
            result[l] = result[l]!! + entry.value
        }
    }
    return result
}

fun MutableMap<Long, Long>.blinks(count: Int): MutableMap<Long, Long> {
    var result = this
    for (i in 0..<count) {
        result = result.blink()
    }
    return result
}


fun main() {
    val numbers = File("res/11.txt").readText().split(" ").map { it.toLong() }
    println(numbers.blinks(25).size)

    val counts = mutableMapOf<Long, Long>()
    numbers.forEach {
        counts.putIfAbsent(it, 0)
        counts[it] = counts[it]!! + 1
    }
    println(counts.blinks(75).values.sum())
}