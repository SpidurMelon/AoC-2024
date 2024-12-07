import java.io.File

private fun part1(text: String): Int {
    val regex = Regex("(?<=mul\\()([0-9]+,[0-9]+)(?=\\))")
    var total = 0
    for (match: MatchResult in regex.findAll(text)) {
        total += match.value.split(",").map { it.toInt() }.reduce(Int::times)
    }
    return total
}

private fun part2(text: String): Int {
    val regex = Regex("""(mul\((?<left>[0-9]+),(?<right>[0-9]+)\))|(do(n't)?\(\))""")
    var enabled = true
    var total = 0
    for (match: MatchResult in regex.findAll(text)) {
        if (match.value == "do()") {
            enabled = true
        } else if (match.value == "don't()") {
            enabled = false
        } else if (enabled) {
            total += match.groups["left"]!!.value.toInt() * match.groups["right"]!!.value.toInt()
        }
    }
    return total
}

fun main() {
    val text = File("res/3.txt").readText()
    println(part1(text))
    println(part2(text))
}