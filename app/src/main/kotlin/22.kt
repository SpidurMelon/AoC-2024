import java.io.File

class Sequence() {
    var numbers = mutableListOf<Long>()

    fun append(number: Long) {
        if (numbers.size == 4) numbers.removeFirst()
        numbers.add(number)
    }

    fun size(): Int {
        return numbers.size
    }

    override fun equals(other: Any?): Boolean {
        if (other is Sequence) {
            return numbers == other.numbers
        }
        return false
    }

    override fun hashCode(): Int {
        return numbers.hashCode()
    }

    override fun toString(): String {
        return numbers.toString()
    }

    fun copy(): Sequence {
        val duplicate = Sequence()
        duplicate.numbers = numbers.toMutableList()
        return duplicate
    }
}

infix fun Long.mix(other: Long): Long {
    return this xor other
}

fun Long.prune(): Long {
    return this % 16777216
}

fun Long.evolve(): Long {
    var secret = this
    secret = (secret*64) mix secret
    secret = secret.prune()
    secret = (secret/32) mix secret
    secret = secret.prune()
    secret = (secret*2048) mix secret
    secret = secret.prune()
    return secret
}

fun Long.lastDigit(): Long {
    return this % 10
}

fun Long.evolve(count: Int, sequenceBucket: HashMap<Sequence, Long>): Long {
    val sequence = Sequence()
    var secret = this
    for (i in 0..<count) {
        val newSecret = secret.evolve()

        val oldPrice = secret.lastDigit()
        val newPrice = newSecret.lastDigit()
        val deltaPrice = newPrice-oldPrice
        sequence.append(deltaPrice)

        if (sequence.size() == 4) {
            sequenceBucket.putIfAbsent(sequence.copy(), newPrice)
        }

        secret = newSecret
    }
    return secret
}

fun main() {
    var lines = File("res/22.txt").readLines()
    val sequenceBucket: HashMap<Sequence, Long> = hashMapOf()
    val part1 = lines.sumOf {
        val partialSequenceBucket: HashMap<Sequence, Long> = hashMapOf()
        val part1Partial = it.toLong().evolve(2000, partialSequenceBucket)
        for (sequence in partialSequenceBucket) {
            sequenceBucket.putIfAbsent(sequence.key, 0L)
            sequenceBucket[sequence.key] = sequenceBucket[sequence.key]!! + sequence.value
        }
        part1Partial
    }
    val part2 = sequenceBucket.maxBy { it.value }
    println(part1)
    println(part2.value)
}