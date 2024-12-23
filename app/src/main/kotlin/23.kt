import java.io.File
import java.lang.Exception

class Network(connections: List<String>) {
    data class Computer(val name: String) {
        val connections: MutableSet<Computer> = mutableSetOf()
        fun connect(to: Computer) {
            connections.add(to)
        }
        fun getReachable(steps: Int = -1): Set<Computer> {
            val visited: MutableSet<Computer> = mutableSetOf()
            getReachable(visited, steps)
            return visited
        }
        fun getReachable(visited: MutableSet<Computer>, steps: Int = -1) {
            if (this in visited) return
            visited.add(this)
            if (steps == 0) return
            for (connected in connections) {
                connected.getReachable(visited, steps-1)
            }
        }
        override fun toString(): String {
            return "c-$name"
        }
    }

    val computers: HashMap<String, Computer> = hashMapOf()

    fun getComputer(name: String): Computer? {
        return computers[name]
    }

    fun addComputer(name: String) {
        if (name in computers) return
        computers[name] = Computer(name)
    }

    fun connectComputers(name1: String, name2: String) {
        val c1 = getComputer(name1) ?: return
        val c2 = getComputer(name2) ?: return
        c1.connect(c2)
        c2.connect(c1)
    }

    fun getStronglyConnectedIslands(size: Int): Set<Set<Computer>> {
        val result = mutableSetOf<Set<Computer>>()
        for (computer in computers) {
            result.add(computer.value.getReachable(1))
        }
        return result.flatMap{
            if (it.size >= size) it.subSetsOfSize(size)
            else emptySet()
        }.toSet().filter{ it.isStronglyConnected() }.toSet()
    }

    fun Set<Computer>.isStronglyConnected(): Boolean {
        val reachables = this.map{c ->
            c.getReachable(1).filter{ it in this }.toSet()
        }.toSet()
        return reachables.size == 1
    }

    init {
        for (line in connections) {
            val split = line.split("-")
            val leftName = split[0]
            val rightName = split[1]
            addComputer(leftName)
            addComputer(rightName)
            connectComputers(leftName, rightName)
        }
    }
}

fun <T> Set<T>.subSetsOfSize(targetSize: Int): Set<Set<T>> {
    if (targetSize == 0) return emptySet()
    if (targetSize == 1) return this.map{ setOf(it) }.toSet()
    if (this.size < targetSize) throw Exception("Size can not be less than target size")
    if (targetSize == this.size) return setOf(this)
    val take = this.drop(1).toSet().subSetsOfSize(targetSize-1).map{it + this.first()}.toSet()
    val leave = this.drop(1).toSet().subSetsOfSize(targetSize)
    return take + leave
}

fun main() {
    val lines = File("res/23.txt").readLines()
    val network = Network(lines)
    println(network.getStronglyConnectedIslands(3).filter { it.any{c -> c.name[0] == 't'} }.size)
    for (n in network.computers.size downTo 0) {
        val islands = network.getStronglyConnectedIslands(n)
        if (islands.isNotEmpty()) {
            println(islands.first().map { c -> c.name }.sorted().joinToString(separator = ","))
            break
        }
    }
}
