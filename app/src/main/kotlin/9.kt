import java.io.File
import java.util.*
import kotlin.collections.HashMap

private class FileSystem(private val numbers: List<Int>) {
    data class File(val id: Int, val size: Int) {
        override fun toString(): String {
            return id.toString().repeat(size)
        }
    }
    private val storage: Int = numbers.sum()
    private val filled: Int = numbers.filterIndexed{i, _ -> i % 2 == 0}.sum()
    private val free: Int = storage-filled
    private val indexedFiles: SortedMap<Int, File> = run {
        val map: HashMap<Int, File> = hashMapOf()
        var id = 0
        var index = 0
        numbers
            .filterIndexed{i, _ -> i % 2 == 0}
            .forEachIndexed{i, n ->
                val currentID = id
                val currentIndex = index
                id++
                if (i*2+1 in numbers.indices) index += n + numbers[i*2+1]
                else index += n
                map[currentIndex] = File(currentID, n)
            }
        map.toSortedMap()
    }

    fun SortedMap<Int, File>.copy(): SortedMap<Int, File> {
        return this.toSortedMap()
    }

    fun SortedMap<Int, File>.findEmptySpace(length: Int): Int? {
        for (entry in this.entries) {
            val after = this.tailMap(entry.key + 1)
            if (after.isEmpty()) continue
            val dist = after.firstKey() - (entry.key+entry.value.size)
            if (dist >= length) {
                return entry.key + entry.value.size
            }
        }
        return null
    }

    private fun getFileIDAt(index: Int, indexMap: SortedMap<Int, File> = indexedFiles): Int? {
        val fileEntry: Map.Entry<Int, File> = indexMap.headMap(index+1).lastEntry() ?: return null
        val fileIndex = fileEntry.key
        val leadingFile: File = fileEntry.value
        if (index in fileIndex..<fileIndex+leadingFile.size) {
            return leadingFile.id
        } else {
            return null
        }
    }

    fun compactedPart1(): List<Int> {
        val result = mutableListOf<Int>()
        var takenFromBehind: Int = 0
        for (i in 0..<filled) {
            result.add(getFileIDAt(i) ?: run {
                var currentIndex = storage-takenFromBehind-1
                while(getFileIDAt(currentIndex) == null) {
                    currentIndex -= 1
                    takenFromBehind++
                }
                takenFromBehind++
                getFileIDAt(currentIndex)!!
            })
        }
        return result
    }

    fun compactedPart2(): List<Int> {
        val compactedMap = indexedFiles.copy()
        for (entry in indexedFiles.reversed()) {
            val emptySpaceIndex = compactedMap.findEmptySpace(entry.value.size)

            if (emptySpaceIndex != null && emptySpaceIndex < entry.key) {
                compactedMap.remove(entry.key)
                compactedMap[emptySpaceIndex] = entry.value
            }
        }

        val result = mutableListOf<Int>()
        for (i in 0..<storage) {
            result.add(getFileIDAt(i, compactedMap) ?: 0)
        }

        return result
    }

    override fun toString(): String {
        val builder = StringBuilder()
        for (i in 0..<storage) {
            builder.append(getFileIDAt(i) ?: ".")
        }
        return builder.toString()
    }
}

fun main() {
    val numbers = File("res/9.txt").readText().map { it.digitToInt() }
    val fileSystem = FileSystem(numbers)
    println(fileSystem)
//    println(fileSystem.compactedPart1())
    println(fileSystem.compactedPart1().mapIndexed {i, n -> i.toLong() * n.toLong() }.sum())
    println()
//    println(fileSystem.compactedPart2())
    println(fileSystem.compactedPart2().mapIndexed {i, n -> i.toLong() * n.toLong() }.sum())
}

// Not 90095094087 -> Low
// Not 85535077891 -> Low
// 6337921897505 -> Huzzah