import java.awt.Point
import java.io.File

class Garden(lines: List<String>) {
    class Plot(val label: Char, val location: Point): Iterable<Plot> {
        var up: Plot? = null
        var right: Plot? = null
        var down: Plot? = null
        var left: Plot? = null

        var alreadyCountedUpSide: Boolean = false
        var alreadyCountedRightSide: Boolean = false
        var alreadyCountedLeftSide: Boolean = false
        var alreadyCountedDownSide: Boolean = false

        override fun iterator(): Iterator<Plot> {
            val region = mutableSetOf<Plot>()
            visitRegion(region)
            return region.iterator()
        }

        fun perimeter(): Int {
            return listOf(up, right, down, left).map{ if (it == null) 1 else 0 }.sum()
        }

        fun perimeterDiscount(): Int {
            var result = 0

            if (up == null && !alreadyCountedUpSide) {
                result += 1
                alreadyCountedUpSide = true
                var currentPlot = this
                while (currentPlot.right != null && currentPlot.right!!.up == null) {
                    currentPlot = currentPlot.right!!
                    currentPlot.alreadyCountedUpSide = true
                }
                currentPlot = this
                while (currentPlot.left != null && currentPlot.left!!.up == null) {
                    currentPlot = currentPlot.left!!
                    currentPlot.alreadyCountedUpSide = true
                }
            }

            if (down == null && !alreadyCountedDownSide) {
                result += 1
                alreadyCountedDownSide = true
                var currentPlot = this
                while (currentPlot.right != null && currentPlot.right!!.down == null) {
                    currentPlot = currentPlot.right!!
                    currentPlot.alreadyCountedDownSide = true
                }
                currentPlot = this
                while (currentPlot.left != null && currentPlot.left!!.down == null) {
                    currentPlot = currentPlot.left!!
                    currentPlot.alreadyCountedDownSide = true
                }
            }

            if (right == null && !alreadyCountedRightSide) {
                result += 1
                alreadyCountedRightSide = true
                var currentPlot = this
                while (currentPlot.up != null && currentPlot.up!!.right == null) {
                    currentPlot = currentPlot.up!!
                    currentPlot.alreadyCountedRightSide = true
                }
                currentPlot = this
                while (currentPlot.down != null && currentPlot.down!!.right == null) {
                    currentPlot = currentPlot.down!!
                    currentPlot.alreadyCountedRightSide = true
                }
            }

            if (left == null && !alreadyCountedLeftSide) {
                result += 1
                alreadyCountedLeftSide = true
                var currentPlot = this
                while (currentPlot.up != null && currentPlot.up!!.left == null) {
                    currentPlot = currentPlot.up!!
                    currentPlot.alreadyCountedLeftSide = true
                }
                currentPlot = this
                while (currentPlot.down != null && currentPlot.down!!.left == null) {
                    currentPlot = currentPlot.down!!
                    currentPlot.alreadyCountedLeftSide = true
                }
            }
            return result
        }



        fun visitRegion(visited: MutableSet<Plot> = mutableSetOf()) {
            if (this in visited) return
            visited.add(this)

            up?.visitRegion(visited)
            right?.visitRegion(visited)
            down?.visitRegion(visited)
            left?.visitRegion(visited)
        }

        override fun toString(): String {
            return "$label(${location.x},${location.y})"
        }
    }

    val plots: List<List<Plot>> = lines.mapIndexed {y, line ->
        line.mapIndexed {x, char ->
            Plot(char, Point(x, y))
        }
    }

    init {
        connectPlots()
    }

    private fun connectPlots() {
        for ((y, row) in plots.withIndex()) {
            for ((x, plot) in row.withIndex()) {
                plot.up = getPlot(x, y-1, plot.label)
                plot.right = getPlot(x+1, y, plot.label)
                plot.down = getPlot(x, y+1, plot.label)
                plot.left = getPlot(x-1, y, plot.label)
            }
        }
    }

    fun extractRoots(): HashMap<Point, Plot> {
        val map = hashMapOf<Point, Plot>()
        for (row in plots) {
            for (root in row) {
                if (root.location in map) continue
                for (plot in root) map[plot.location] = root
            }
        }
        return map
    }

    private fun getPlot(x: Int, y: Int, label: Char? = null): Plot? {
        val result = plots.getOrNull(y)?.getOrNull(x) ?: return null
        if (label == null || result.label == label) return result
        return null
    }
}

fun main() {
    val garden = Garden(File("res/12.txt").readLines())

    val rootsProcessed = mutableSetOf<Garden.Plot>()
    var part1: Int = 0
    var part2: Int = 0
    for ((location, root) in garden.extractRoots().entries) {
        if (root in rootsProcessed) continue
        rootsProcessed.add(root)
        var area: Int = 0
        var perimeter: Int = 0
        var discountedPerimeter: Int = 0
        for (plot in root) {
            area++
            perimeter += plot.perimeter()
            discountedPerimeter += plot.perimeterDiscount()
        }
        part1 += area*perimeter
        part2 += area*discountedPerimeter
    }
    println(part1)
    println(part2)
}