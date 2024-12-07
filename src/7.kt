import java.io.File

infix fun Long.conc(other: Long): Long {
    return (toString() + other.toString()).toLong()
}

fun Long.apply(op: Char, other: Long): Long? {
    return when (op) {
        '+' -> return this + other
        '*' -> return this * other
        '|' -> return this conc other
        else -> null
    }
}

fun Long.endsWith(other: Long): Boolean {
    return this.toString().endsWith(other.toString())
}

fun determineOperator(left: Long, right: Long, target: Long, operators: String): Char? {
    for (op in operators) {
        if (left.apply(op, right) == target) return op
    }
    return null
}

fun determineOperators(numbers: List<Long>, target: Long, operators: String): List<Char>? {
    if (numbers.size == 2) {
        val operator = determineOperator(numbers[0], numbers[1], target, operators) ?: return null
        return listOf(operator)
    } else {
        if ('*' in operators && target % numbers.last() == 0L) {
            val mulTarget: Long = target / numbers.last()
            val mulHeadOperators = determineOperators(numbers.dropLast(1), mulTarget, operators)
            if (mulHeadOperators != null) {
                return mulHeadOperators + listOf('*')
            }
        }

        if ('+' in operators) {
            val plusTarget: Long = target - numbers.last()
            val plusHeadOperators = determineOperators(numbers.dropLast(1), plusTarget, operators)
            if (plusHeadOperators != null) {
                return plusHeadOperators + listOf('+')
            }
        }
        
        if ('|' in operators && target.endsWith(numbers.last())) {
            val concTarget: Long = target.toString().removeSuffix(numbers.last().toString()).toLongOrNull() ?: return null
            val concHeadOperators = determineOperators(numbers.dropLast(1), concTarget, operators)
            if (concHeadOperators != null) {
                return concHeadOperators + listOf('|')
            }
        }

        return null
    }
}
fun main() {
    val lines = File("res/7.txt").readLines()
    var part1 = 0L
    var part2 = 0L
    lines.forEach {
        val split = it.split(": ")
        val target = split[0].toLong()
        val numbers = split[1].split(" ").map { it.toLong() }

        val operatorsPart1 = determineOperators(numbers, target, "+*")
        if (operatorsPart1 != null) {
            part1 += target
        }

        val operatorsPart2 = determineOperators(numbers, target, "+*|")
        if (operatorsPart2 != null) {
            part2 += target
        }
    }
    println(part1)
    println(part2)
}
