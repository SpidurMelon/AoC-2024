import java.io.File

private var blinkCalls = 0

// Common function
fun Long.blink(): List<Long> {
    blinkCalls += 1
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

// Part 2 functions (Memoisation based)
data class MemoState(val value: Long, val count: Int)
val memory: HashMap<MemoState, Long> = hashMapOf()
fun Long.blinksMemo(count: Int): Long {
    val memoState = MemoState(this, count)
    if (memoState in memory) return memory[memoState]!!

    val result: Long
    if (count == 1) result = this.blink().size.toLong()
    else result = this.blink().sumOf { it.blinksMemo(count-1) }

    memory[memoState] = result
    return result
}

fun List<Long>.blinksMemo(count: Int): Long {
    var result = 0L
    for (l in this) {
        result += l.blinksMemo(count)
    }
    return result
}

fun main() {
    val numbers = File("res/11.txt").readText().split(" ").map { it.toLong() }
    // Part 1 using lists
    blinkCalls = 0
    println(numbers.blinks(25).size)
    println("blink() called $blinkCalls times for part 1 (using lists)")

    // Part 2 using grouping
    val counts = mutableMapOf<Long, Long>()
    numbers.forEach {
        counts.putIfAbsent(it, 0)
        counts[it] = counts[it]!! + 1
    }
    blinkCalls = 0
    println(counts.blinks(75).values.sum())
    println("blink() called $blinkCalls times for part 2 (using grouping)")

    // Part 2 using memoisation
    blinkCalls = 0
    println(numbers.blinksMemo(75))
    println("blink() called $blinkCalls times for part 2 (using memoisation)")
}