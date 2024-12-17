import java.io.File
import kotlin.math.pow

fun Long.pow(exp: Int): Long {
    if (exp == 0) return 1
    var result = this
    for (i in 0..<exp-1) {
        result *= this
    }
    return result
}

data class Program(var A: Long, var B: Long, var C: Long, val instructions: List<Int>) {
    var instructionPointer = 0
    val output = StringBuilder()

    fun combo(operand: Int): Long = when (operand) {
        0 -> 0
        1 -> 1
        2 -> 2
        3 -> 3
        4 -> A
        5 -> B
        6 -> C
        else -> throw IllegalStateException("Unknown combo operand")
    }

    fun execute(opcode: Int, operand: Int): Boolean {
        when (opcode) {
            0 -> {
                A = A shr combo(operand).toInt()
            }
            1 -> {
                B = B xor operand.toLong()
            }
            2 -> {
                B = combo(operand) % 8
            }
            3 -> {
                if (A == 0L) return true
                instructionPointer = operand
                return false
            }
            4 -> {
                B = B xor C
            }
            5 -> {
                output.append(combo(operand) % 8)
                output.append(",")
            }
            6 -> {
                B = A shr combo(operand).toInt()
            }
            7 -> {
                C = A shr combo(operand).toInt()
            }
        }
        return true
    }

    fun step(): Boolean {
        if (instructionPointer >= instructions.size) return false
        if (execute(instructions[instructionPointer], instructions[instructionPointer+1])) {
            instructionPointer += 2
        }
        return true
    }

    fun run(steps: Int = -1): Boolean {
        if (steps < 0) {
            while (step()) {

            }
            output.deleteCharAt(output.lastIndex)
            return false
        } else {
            for (i in 0..<steps) {
                if (!step()) {
                    output.deleteCharAt(output.lastIndex)
                    return false
                }
            }
            return true
        }
    }
}

// out((((A % 8) xor 1) xor (A shr ((A % 8) xor 1)) xor 6) % 8)
fun optimizedProgramPart2Step(a: Long): Int {
    val a1 = (a % 8) xor 1
    val bOutput = ((a1 xor (a shr a1.toInt())) xor 6) % 8
    return bOutput.toInt()
}

// Returns A values
fun getPossiblePaths(instructions: List<Int>): List<List<Long>> {
    val solutions = mutableListOf<List<Long>>()
    if (instructions.size == 1) {
        for (possibleA in 0L..<8L) {
            if (optimizedProgramPart2Step(possibleA) == instructions.first()) {
                solutions.add(listOf(possibleA))
            }
        }
    } else {
        val possiblePathsAfter = getPossiblePaths(instructions.drop(1))
        for (possiblePath in possiblePathsAfter) {
            for (aOffset in 0L..<8L) {
                val possibleA = possiblePath.first()*8 + aOffset
                if (optimizedProgramPart2Step(possibleA) == instructions.first()) {
                    solutions.add(listOf(possibleA) + possiblePath)
                }
            }
        }
    }
    return solutions
}

fun main() {
    val input = File("res/17.txt").readText()
    val regex = Regex("""Register A: (?<A>[0-9]+)\nRegister B: (?<B>[0-9]+)\nRegister C: (?<C>[0-9]+)\n\nProgram: (?<instructions>[0-9,]+)""")
    val matchResult = regex.find(input)!!
    val A = matchResult.groups["A"]!!.value.toLong()
    val B = matchResult.groups["B"]!!.value.toLong()
    val C = matchResult.groups["C"]!!.value.toLong()
    val instructionString = matchResult.groups["instructions"]!!.value
    val instructions = instructionString.split(",").map { it.toInt() }

    // Part 1
    val programPart1 = Program(A, B, C, instructions)
    programPart1.run()
    println(programPart1.output.toString())

    // Part 2
    val paths = getPossiblePaths(instructions)
    val part2 = paths.first()[0]
    println(part2)
}