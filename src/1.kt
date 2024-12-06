import java.io.File
import kotlin.math.abs

private fun part_1(lines: List<String>): Int {
    val left_list = List(1000){i -> lines[i].split("   ")[0].toInt()}.sorted()
    val right_list = List(1000){i -> lines[i].split("   ")[1].toInt()}.sorted()
    val dif_list = List(1000){i -> abs(left_list[i] - right_list[i])}
    return dif_list.sum()
}

private fun part_2(lines: List<String>): Int {
    val left_list = List(1000){i -> lines[i].split("   ")[0].toInt()}.sorted()
    val right_list = List(1000){i -> lines[i].split("   ")[1].toInt()}.sorted()
    val sim_list = List(1000){i ->
        left_list[i] * right_list.count{j -> j == left_list[i]}
    }
    return sim_list.sum()
}

fun main() {
    val lines: List<String> = File("res/1.txt").readLines()
    println(part_1(lines))
    println(part_2(lines))
}