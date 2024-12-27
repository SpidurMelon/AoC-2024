import java.io.File
import java.lang.StringBuilder
import kotlin.math.max
import kotlin.random.Random

/**
 * This file is full of failed attempts, so please don't judge too harshly, most functions aren't even used anymore
 */

class MonitoringDevice(inputMap: Map<String, Boolean>, gateStrings: List<String>) {
    inner class Gate(val left: String, val operator: String, val right: String) {
        // Returns a set of all minimal sets of registers that could be flipped to flip the result
        // For example:
        // (left = true, operator = AND, right = true) -> true
        // Correct value is false
        // Returns {{left}, {right}}
        // Note that {left, right} is not included in this set, because only one needs to be flipped (so {left, right} is not minimal)
        //
        // Another example:
        // (left = false, operator = AND, right = false) -> false
        // Correct value is true
        // Returns {{left, right}}
        //
        fun getPossiblyWrongRegisters(correctValue: Boolean): Set<Set<Register>> {
            val myValue = calculate()
            if (myValue == correctValue) return emptySet()

            val leftRegister: Register = registers[left] ?: throw Exception("Left register does not exist: $left")
            val rightRegister: Register = registers[right] ?: throw Exception("Right register does not exist: $right")

            when (operator) {
                "AND" -> {
                    if (correctValue) {
                        // Result needs to change to ON
                        return setOf(setOf(leftRegister, rightRegister).filter { !it.getValue() }.toSet())
                    } else {
                        // Result needs to change to OFF
                        return setOf(setOf(leftRegister), setOf(rightRegister))
                    }
                }
                "OR" -> {
                    if (correctValue) {
                        // Result needs to change to ON
                        return setOf(setOf(leftRegister), setOf(rightRegister))
                    } else {
                        // Result needs to change to OFF
                        return setOf(setOf(leftRegister, rightRegister).filter { it.getValue() }.toSet())
                    }
                }
                "XOR" -> {
                    // Flipping either side will always flip XOR result
                    return setOf(setOf(leftRegister), setOf(rightRegister))
                }
                else -> throw Exception("Unidentified operator: $operator")
            }
        }
        fun calculate(): Boolean {
            val leftRegister: Register = registers[left] ?: throw Exception("Left register does not exist: $left")
            val rightRegister: Register = registers[right] ?: throw Exception("Right register does not exist: $right")
            return when (operator) {
                "AND" -> leftRegister.getValue() && rightRegister.getValue()
                "OR" -> leftRegister.getValue() || rightRegister.getValue()
                "XOR" -> leftRegister.getValue() xor rightRegister.getValue()
                else -> throw Exception("Unidentified operator: $operator")
            }
        }

        fun getDepth(): Int {
            return max(registers[left]!!.getDepth(), registers[right]!!.getDepth())
        }

        fun getRegisters(): Set<Register> {
            return setOf(registers[left]!!, registers[right]!!)
        }

        override fun toString(): String {
            val leftLabel = registers[left]!!.getLabel()
            val rightLabel = registers[right]!!.getLabel()
            return "[$left($leftLabel) $operator $right($rightLabel)]>"
        }
    }

    inner class Register(val name: String, var staticValue: Boolean? = null) {
        // Will always return a label in the form of "[a-z][0-9][0-9]" or null
        fun getLabel(): String? {
            if (isInput()) return name
            val gate: Gate = getGate()!!
            val left = registers[gate.left]!!
            val leftLabel = left.getLabel() ?: return null
            val operator = gate.operator
            val right = registers[gate.right]!!
            val rightLabel = right.getLabel() ?: return null
            val number = leftLabel.drop(1).toInt()
            val prefixes = setOf(leftLabel.first(), rightLabel.first())
            if (number == 0) {
                if (prefixes == setOf('x', 'y') && operator == "AND") {
                    return makeName('c', 1)
                } else if (prefixes == setOf('x', 'y') && operator == "XOR") {
                    return makeName('z', 0)
                }
            } else {
                if (prefixes == setOf('x', 'y') && operator == "AND") {
                    return makeName('u', number)
                } else if (prefixes == setOf('x', 'y') && operator == "XOR") {
                    return makeName('v', number)
                } else if (prefixes == setOf('v', 'c') && operator == "AND") {
                    return makeName('w', number)
                } else if (prefixes == setOf('v', 'c') && operator == "XOR") {
                    return makeName('z', number)
                } else if (prefixes == setOf('u', 'w') && operator == "OR") {
                    if (number < 44) return makeName('c', number+1)
                    else return makeName('z', number+1)
                }
            }
            return null
        }

        fun findNullers(): Set<Register> {
            if (isInput() || getLabel() != null) return emptySet()
            val gate = getGate()!!
            val left = registers[gate.left]!!
            val leftLabel = left.getLabel()
            val right = registers[gate.right]!!
            val rightLabel = right.getLabel()
            val result = mutableSetOf<Register>()
            if (leftLabel == null) {
                result.addAll(left.findNullers())
            }
            if (rightLabel == null) {
                result.addAll(right.findNullers())
            }
            if (leftLabel != null && rightLabel != null) {
                result.add(this)
            }
            return result
        }

        fun getGate(): Gate? {
            return gates[name]
        }
        fun getDepth(): Int {
            if (isInput()) return 0
            return getGate()!!.getDepth() + 1
        }
        fun getValue(): Boolean {
            if (staticValue != null) return staticValue!!
            return getGate()!!.calculate()
        }

        fun getFlipReward(): Int {
            if (staticValue != null) return 0
            val costBefore = getCost()
            staticValue = !getValue()
            val costAfter = getCost()
            val flipReward = costBefore-costAfter
            staticValue = null
            return flipReward
        }

        fun influencesMe(): Set<Register> {
            if (isInput()) return setOf(this)
            val result: MutableSet<Register> = mutableSetOf(this)
            val directInfluences = getGate()!!.getRegisters()
            result.addAll(directInfluences.first().influencesMe())
            result.addAll(directInfluences.last().influencesMe())
            return result
        }

        fun allPossiblyWrongRegisters(): Set<Set<Register>> {
            if (isInput()) return emptySet()
            val myValue = getValue()
            val correctValue = !myValue
            val possiblyWrongRegisters = getGate()!!.getPossiblyWrongRegisters(correctValue)
                .filter { !it.any { r -> r.isInput() } }
                .toSet()

            // possiblyWrongRegisters = {{left}} or {{right}} or {{left},{right}} or {{left,right}}
            val possiblyWrongRegistersBelow = possiblyWrongRegisters.flatMap {myWrongRegisters ->

                // myWrongRegisters = {left} or {right} or {left,right}
                myWrongRegisters.flatMap { r ->

                    // r = left or b
                    r.allPossiblyWrongRegisters()

                }.toSet()
            }.toSet()
            return setOf(setOf(this)) + possiblyWrongRegisters + possiblyWrongRegistersBelow
        }

        override fun toString(): String {
            val label = getLabel()
            return "${getGate()} $name($label): ${if (getValue()) 1 else 0}"
        }

        override fun equals(other: Any?): Boolean {
            if (other is Register) {
                return this.name == other.name
            }
            return false
        }

        fun isInput(): Boolean {
            return name.matches(Regex("[xy].."))
        }

        fun isOutput(): Boolean {
            return name.matches(Regex("z.."))
        }

        fun pathsTo(): Set<Gate> {
            if (isInput()) return emptySet()
            return getGate()!!.getRegisters().flatMap { it.pathsTo() }.toSet() + getGate()!!
        }

        fun directInputs(): Set<Register> = getGate()!!.getRegisters()

        override fun hashCode(): Int {
            return name.hashCode()
        }
    }
    val registers: HashMap<String, Register> = hashMapOf()
    val gates: HashMap<String, Gate> = hashMapOf()
    init {
        setInputs(inputMap)
        gateStrings.forEach{
            val split = it.split(" ")
            val gate = Gate(split[0], split[1], split[2])

            gates[split[4]] = gate
            registers[split[4]] = Register(split[4])
        }
    }

    fun setInputs(newInputMap: Map<String, Boolean>) {
        newInputMap.forEach{
            registers[it.key] = Register(it.key, it.value)
        }
    }

    fun getRegistersMatching(regex: Regex): List<Register> {
        return registers.filter{ it.key.matches(regex) }.map{ it.value }
    }

    fun getCompleteValue(label: Char): String = this
        .getRegistersMatching(Regex("$label.."))
        .sortedByDescending{ it.name }
        .joinToString(separator = ""){ if (it.getValue()) "1" else "0" }

    fun getXInput() = getCompleteValue('x')
    fun getYInput() = getCompleteValue('y')
    fun getOutput() = getCompleteValue('z')

    fun getCorrectOutput() = (getXInput().toLong(2) + getYInput().toLong(2))
        .toString(2)
        .padStart(46, '0')
    fun getWrongOutputMask() = (getOutput().toLong(2) xor getCorrectOutput().toLong(2))
        .toString(2)
        .padStart(46, '0')

    fun getCost(): Int {
        return getWrongOutputMask().sumOf{ it.digitToInt() }
    }

    fun getFlipRewards(): Map<String, Int> {
        return registers.values.associate{
            it.name to it.getFlipReward()
        }
    }

    fun getOutputRegister(number: Int): Register {
        return registers["z${number.toString().padStart(2, '0')}"]!!
    }

    fun getOutputRegisters(): Set<Register> {
        return registers.values.filter { it.isOutput() }.toSet()
    }

    fun isOutputWrong(number: Int): Boolean {
        val wrongOutputMask = getWrongOutputMask()
        return wrongOutputMask[wrongOutputMask.length-number-1] == '1'
    }

    fun getPossibleSwaps(): Set<Set<MonitoringDevice.Register>> {
        val allWrongRegisters = mutableSetOf<Set<MonitoringDevice.Register>>()
        for (i in 0..45) {
            if (isOutputWrong(i)) {
                val register = getOutputRegister(i)
                allWrongRegisters.addAll(register.allPossiblyWrongRegisters())
            }
        }
        if (allWrongRegisters.size < 2) return emptySet()
        val possibleSwaps = allWrongRegisters
            .filter{ it.size == 1 }
            .flatten()
            .toSet()
            .subSetsOfSize(2)
            .filter { it.first().getValue() != it.last().getValue()}
            .toSet()
        return possibleSwaps
    }

    fun canSwap(r1: String, r2: String): Boolean {
        val register1 = registers[r1]!!
        val register2 = registers[r2]!!
        val influences1 = register1.influencesMe()
        val influences2 = register2.influencesMe()
        return !influences1.contains(register2) && !influences2.contains(register1)
    }

    fun swapGates(r1: String, r2: String) {
        if (!canSwap(r1, r2)) throw Exception("Swapping would cause a loop")
        val temp = gates[r1]
        gates[r1] = gates[r2]!!
        gates[r2] = temp!!
    }

    fun randomizeInputs() {
        setInputs(generateRandomInputMap(45))
    }

    fun totalCost(testInputSet: List<Map<String, Boolean>>): Int {
        return testInputSet.sumOf{
            setInputs(it)
            getCost()
        }
    }

    fun doubleSwapRewards(testInputSet: List<Map<String, Boolean>>, alreadySwapped: Set<String>, onlyTakeHeighest: Int): List<Pair<Pair<String, String>, List<Pair<Pair<String, String>, Int>>>> {
        return swapRewards(testInputSet, alreadySwapped, onlyTakeHeighest).map {
            val r1 = it.first.first
            val r2 = it.first.second
            val newAlreadySwapped = alreadySwapped + setOf(r1, r2)
            swapGates(r1, r2)
            val doubleSwapRewards = swapRewards(testInputSet, newAlreadySwapped, onlyTakeHeighest)
            swapGates(r1, r2)
            it.first to doubleSwapRewards
        }.sortedByDescending { it.second.first().second }
    }

    fun swapRewards(testInputSet: List<Map<String, Boolean>>, alreadySwapped: Set<String>, onlyTakeHeighest: Int): List<Pair<Pair<String, String>, Int>> {
        val totalFlipRewards = hashMapOf<String, Int>()
        testInputSet.forEach{
            setInputs(it)
            val flipRewards = getFlipRewards()
            flipRewards.forEach{ (rName, flipR) ->
                totalFlipRewards.merge(rName, flipR) {old, new -> old + new}
            }
        }
        val highestFlipRewards = totalFlipRewards.entries
            .filter { !registers[it.key]!!.isInput() && it.key !in alreadySwapped }
            .sortedByDescending { it.value }
            .take(onlyTakeHeighest)
        val possibleSwaps = highestFlipRewards.map {r1 ->
            r1.key to highestFlipRewards.filter { r2 ->
                canSwap(r1.key, r2.key)
            }.map { it.key }
        }
        val totalCostBefore = totalCost(testInputSet)
        val swapRewards = possibleSwaps.flatMap {r1 ->
            r1.second.map {r2 ->
                swapGates(r1.first, r2)
                val totalCostAfter = totalCost(testInputSet)
                swapGates(r1.first, r2)
                val reward = totalCostBefore-totalCostAfter
                (r1.first to r2) to reward
            }
        }.sortedByDescending { it.second }
        return swapRewards
    }

    fun findSwapsToZero(testInputSet: List<Map<String, Boolean>>, alreadySwapped: Set<String>, onlyTakeHeighest: Int, swapsLeft: Int): Set<String>? {
        if (swapsLeft == 0 && totalCost(testInputSet) == 0) return alreadySwapped
        for (reward in swapRewards(testInputSet, alreadySwapped, onlyTakeHeighest)) {
            val r1 = reward.first.first
            val r2 = reward.first.second
            val newAlreadySwapped = alreadySwapped + setOf(r1, r2)
            swapGates(r1, r2)
            val result = findSwapsToZero(testInputSet, newAlreadySwapped, onlyTakeHeighest, swapsLeft-1)
            if (result != null) {
                return result
            }
            swapGates(r1, r2)
        }
        return null
    }

    fun incalculatableRegisters(): Set<Register> {
        return registers.values.filter {
            it.getLabel() != null
        }.toSet()
    }

    fun getWronglyLabeled(): Set<Register> {
        return registers.values.flatMap { it.findNullers() }.toSet() + getOutputRegisters().filter { it.getLabel() != null && it.name != it.getLabel() }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.appendLine("x..: ${getXInput().padStart(46, '0')}")
        builder.appendLine("y..: ${getYInput().padStart(46, '0')}")
        builder.appendLine("out: ${getOutput()}")
        builder.appendLine("cor: ${getCorrectOutput()}")
        builder.appendLine("wro: ${getWrongOutputMask()}")
        return builder.toString()
    }
}

fun generateRandomInputMap(size: Int): Map<String, Boolean> {
    val result: MutableMap<String, Boolean> = mutableMapOf()
    for (i in 0..<size) {
        val valueString = i.toString().padStart(2, '0')
        result["x$valueString"] = Random.nextBoolean()
        result["y$valueString"] = Random.nextBoolean()
    }
    return result
}

fun generateInputMap(x: String, y: String): Map<String, Boolean> {
    val result: MutableMap<String, Boolean> = mutableMapOf()
    for (i in x.indices) {
        val valueString = i.toString().padStart(2, '0')
        result["x$valueString"] = (x[i] == '1')
        result["y$valueString"] = (y[i] == '1')
    }
    return result
}

fun generateInputMap(x: Long, y: Long, length: Int): Map<String, Boolean> {
    return generateInputMap(x.toString().padStart(length, '0'), y.toString().padStart(length, '0'))
}

fun verify(device: MonitoringDevice, rounds: Int): Boolean {
    device.setInputs(generateRandomInputMap(45))
    for (i in 0..<rounds) {
        if (device.getOutput() != device.getCorrectOutput()) return false
    }
    return true
}

fun makeName(c: Char, number: Int): String {
    return "$c${number.toString().padStart(2, '0')}"
}

data class FakeGate(val left: String, val operator: String, val right: String, val out: String)

fun generateAddingGates(): List<String> {
    val gates = mutableListOf<FakeGate>()

    (0..44).forEach{
        if (it == 0) {
            gates.add(FakeGate(makeName('x', 0), "XOR", makeName('y', 0), makeName('z', 0)))
            gates.add(FakeGate(makeName('x', 0), "AND", makeName('y', 0), makeName('c', 1)))
        } else {
            gates.add(FakeGate(makeName('x', it), "AND", makeName('y', it), makeName('u', it)))
            gates.add(FakeGate(makeName('x', it), "XOR", makeName('y', it), makeName('v', it)))
            gates.add(FakeGate(makeName('v', it), "AND", makeName('c', it), makeName('w', it)))
            gates.add(FakeGate(makeName('v', it), "XOR", makeName('c', it), makeName('z', it)))
            if (it < 44) {
                gates.add(FakeGate(makeName('u', it), "OR", makeName('w', it), makeName('c', it+1)))
            } else {
                gates.add(FakeGate(makeName('u', it), "OR", makeName('w', it), makeName('z', it+1)))
            }
        }
    }

    return gates.map { "${it.left} ${it.operator} ${it.right} -> ${it.out}" }
}

fun Set<MonitoringDevice.Gate>.roughlyEquals(other: Set<MonitoringDevice.Gate>): Boolean {
    val XORs = this.filter{ it.operator == "XOR" }.size == other.filter{ it.operator == "XOR" }.size
    val ANDs = this.filter{ it.operator == "AND" }.size == other.filter{ it.operator == "AND" }.size
    val ORs = this.filter{ it.operator == "OR" }.size == other.filter{ it.operator == "OR" }.size
    return XORs && ANDs && ORs
}

fun main() {
    val file = File("res/24.txt").readText()
    val fileSplit = file.split("\n\n")
    val initialValues = fileSplit[0].split("\n")
    val inputMap = initialValues.map{
        val split = it.split(": ")
        val name = split[0]
        val value = (split[1] == "1")
        name to value
    }.toMap()

    val gates = fileSplit[1].split("\n")

    // Part 1
    val device = MonitoringDevice(inputMap, gates)
    println(device.getOutput().toLong(2))

    // Part 2
    val correctDevice = MonitoringDevice(inputMap, generateAddingGates())
    println(correctDevice)
    val testInputSet = (0..200).map { generateRandomInputMap(45) }
    println(correctDevice.totalCost(testInputSet))

    val wrongOutputs = (0..45).filter{
        val name = makeName('z', it)
        val correctPath = correctDevice.registers[name]!!.pathsTo()
        val wrongPath = device.registers[name]!!.pathsTo()

        !correctPath.roughlyEquals(wrongPath)
    }

    val wronglyLabeled = device.getWronglyLabeled()
    println(wronglyLabeled)
    // [[fsf(w10) OR gpr(z10)]> tdj(null): 1,
    // [x10(x10) AND y10(y10)]> z10(u10): 0]
    device.swapGates("gpr", "z10")

    val wronglyLabeled2 = device.getWronglyLabeled()
    println(wronglyLabeled2)
    // [[rhk(u21) OR nks(z21)]> hhc(null): 1,
    // [ptd(v21) AND scj(c21)]> z21(w21): 0]
    device.swapGates("nks", "z21")

    val wronglyLabeled3 = device.getWronglyLabeled()
    println(wronglyLabeled3)
    // [[ghp(z33) AND hbg(v34)]> khh(null): 0,
    // [ghp(z33) XOR hbg(v34)]> z34(null): 0,
    // [jtg(u33) OR trf(w33)]> z33(c34): 1]
    device.swapGates("ghp", "z33")

    val wronglyLabeled4 = device.getWronglyLabeled()
    println(wronglyLabeled4)
    //[[mhr(c39) AND krs(u39)]> krb(null): 0,
    // [krs(u39) XOR mhr(c39)]> z39(null): 0]
//    println(device.registers["krs"])
//    println(device.registers.values.filter { it.getLabel() == "v39" })
//    println(device.gates.values.filter { it.left == "cpm" || it.right == "cpm" })
    device.swapGates("cpm", "krs")


    val wronglyLabeled5 = device.getWronglyLabeled()
    println(wronglyLabeled5)

    setOf("gpr", "z10", "nks", "z21", "ghp", "z33", "cpm", "krs").sorted().forEach { print("$it,") }

//
//    val testInputSet = listOf(inputMap)
//    println(device)
//    println(device.findSwapsToZero(testInputSet, emptySet(), 4, 4))


//    device.swapGates(highestFlipRewards[0].key, highestFlipRewards[2].key)
//    println(device.totalCost(testInputSet))

//    for (swapIndex in 0..<4) {
//        val mostLikelySwaps = hashMapOf<Set<MonitoringDevice.Register>, Long>()
//        device.getPossibleSwaps().forEach{
//            mostLikelySwaps.putIfAbsent(it, 0L)
//            mostLikelySwaps[it] = mostLikelySwaps[it]!! + 1
//        }
//        for (i in 0..200) {
//            device.randomizeInputs()
//            device.getPossibleSwaps().forEach{
//                mostLikelySwaps.putIfAbsent(it, 0L)
//                mostLikelySwaps[it] = mostLikelySwaps[it]!! + 1
//            }
//        }
//        val mostLikelySwap = mostLikelySwaps.entries.maxByOrNull { it.value }!!.key
//        println(mostLikelySwap)
//        device.swapGates(mostLikelySwap.first().name, mostLikelySwap.last().name)
//    }
//
//    if (verify(device, 200)) {
//        device.setInputs(inputMap)
//        println(device.getOutput())
//        println(device.getCorrectOutput())
//        println(device.getWrongOutputMask())
//    }
}

// [fsf(w10) OR gpr(z10)]> tdj(null): 1
// [x10(x10) AND y10(y10)]> z10(u10): 0

//
//
