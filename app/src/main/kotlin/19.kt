import java.io.File

class Pattern(val char: Char?, val parent: Pattern?) {
    var w: Pattern? = null
    var u: Pattern? = null
    var b: Pattern? = null
    var r: Pattern? = null
    var g: Pattern? = null

    var proper = false

    val memoFails = mutableSetOf<String>()
    fun canConstruct(text: String): Boolean {
        if (text.isEmpty()) return true
        if (text in memoFails) return false
        val allPrefixes = prefixes(text).sortedByDescending{ it.length }
        for (prefix in allPrefixes) {
            if (canConstruct(text.drop(prefix.length))) return true
        }
        memoFails.add(text)
        return false
    }

    val memoConstructionCounts = hashMapOf<String, Long>()
    fun constructionCount(text: String): Long {
        if (text.isEmpty()) return 1
        if (text in memoConstructionCounts) return memoConstructionCounts[text]!!
        var result = 0L
        val allPrefixes = prefixes(text)
        for (prefix in allPrefixes) {
            result += constructionCount(text.drop(prefix.length))
        }
        memoConstructionCounts[text] = result
        return result
    }

    fun prefixes(text: String): Set<String> {
        if (text.isEmpty()) return emptySet()
        val first = text.first()
        val result = mutableSetOf<String>()
        val sub = getSubPattern(first) ?: return emptySet()
        if (sub.proper) result.add(sub.toString())
        result.addAll(sub.prefixes(text.drop(1)))
        return result
    }

    fun subPatterns(): HashMap<Char, Pattern?> {
        val result = hashMapOf<Char, Pattern?>()
        result['w'] = w
        result['u'] = u
        result['b'] = b
        result['r'] = r
        result['g'] = g
        return result
    }

    fun getSubPattern(prefix: String): Pattern? {
        if (prefix.isEmpty()) return null
        if (prefix.length == 1) return getSubPattern(prefix.first())
        return getSubPattern(prefix.first())?.getSubPattern(prefix.drop(1))
    }

    fun getSubPattern(char: Char?) = when(char) {
        'w' -> w
        'u' -> u
        'b' -> b
        'r' -> r
        'g' -> g
        else -> null
    }

    fun setSubPattern(char: Char, pattern: Pattern) = when(char) {
        'w' -> w = pattern
        'u' -> u = pattern
        'b' -> b = pattern
        'r' -> r = pattern
        'g' -> g = pattern
        else -> throw Exception("Unkown character")
    }


    fun addPattern(pattern: String) {
        if (pattern.isEmpty()) {
            proper = true
            return
        }

        val first = pattern.first()
        if (getSubPattern(first) == null) setSubPattern(first, Pattern(first, this))
        getSubPattern(first)!!.addPattern(pattern.drop(1))
    }

    override fun toString(): String {
        if (char == null) return ""
        return parent.toString() + char
    }
}

fun main() {
    val lines = File("res/19.txt").readLines()
    val availablePatterns = lines.first().split(", ")
    val towels = lines.drop(2)
    val rootPattern = Pattern(null, null)
    availablePatterns.forEach{rootPattern.addPattern(it)}
    println(towels.count{rootPattern.canConstruct(it)})
    println(towels.sumOf{rootPattern.constructionCount(it)})
}