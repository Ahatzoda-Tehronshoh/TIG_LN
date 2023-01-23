import java.util.*

val KEY_WORD = listOf("PRINT", "+", "-", "/", "*", "=", ";", ",", "(", ")", "^")

fun String.deleteBlanks() = this.filter { it != ' ' }

fun String.deleteOneLineCommentary(): String = try {
    var str = ""
    var index = 0
    var isComment = false
    while (index < (this.length - 1)) {
        if (!isComment && this[index] == '/' && this[index + 1] == '/') {
            index += 2
            isComment = true
        } else if (this[index] == '\n')
            isComment = false

        if (!isComment)
            str += this[index]

        index++
    }
    if (!isComment)
        str += this[index]

    str
} catch (e: Exception) {
    throw Exception(" -> deleteOneLineCommentary - ${e.message}")
}

fun String.deleteMultipleLinesCommentary(): String  = try {
    var index = 0;
    var str = ""
    var isComment = false

    while (index < (length - 1)) {
        if (!isComment && this[index] == '/' && this[index + 1] == '*') {
            index += 2
            isComment = true
        } else if (isComment && this[index] == '*' && this[index + 1] == '/') {
            index += 2
            isComment = false
            continue
        }

        if (!isComment)
            str += this[index]

        index++
    }

    if (!isComment && index < length)
        str += this[index]

    str
} catch (e: Exception) {
    throw Exception(" -> deleteMultipleLinesCommentary - ${e.message}")
}

fun String.deleteToNewLineCharacter(): String = this.filter { it != '\n' }

fun String.toUppercase(): String = try {
    var inString = false
    var str = ""
    var index = 0
    while (index < length)
        if (this[index] == '\"') {
            str += this[index++]
            inString = !inString
        } else
            str += if (!inString && (this[index] in 'a'..'z'))
                this[index++] - 32
            else
                this[index++]

    str
} catch (e: Exception) {
    throw Exception(" -> toUppercase - ${e.message}")
}

fun String.createSymbolTable(): Pair<String, List<MutableList<String>>> = try{
    var result = ""

    var currentWord = ""
    var isInStr = false

    val tableSymbol: List<MutableList<String>> =
        listOf(mutableListOf("1"), mutableListOf("@%04d".format(1)), mutableListOf("1"))

    for (symbol in this) {
        if (symbol == '"') {
            isInStr = !isInStr
            if (KEY_WORD.contains(currentWord)) {
                result += currentWord
                currentWord = ""
            }
            if (!isInStr) {
                result += symbol
                continue
            }
        }
        if (isInStr) {
            result += symbol
            continue
        }

        if (KEY_WORD.contains(symbol.toString())) {
            if (currentWord != "") {

                result += if (tableSymbol[0].contains(currentWord))
                    tableSymbol[1][tableSymbol[0].indexOf(currentWord)]
                else {
                    tableSymbol[0].add(currentWord)
                    tableSymbol[1].add("@%04d".format(tableSymbol[0].size))
                    tableSymbol[2].add(
                        if (currentWord.toDoubleOrNull() != null)
                            currentWord
                        else
                            "0"
                    )

                    "@%04d".format(tableSymbol[0].size)
                }
                currentWord = ""
            }
            result += symbol
            continue
        }
        if (KEY_WORD.contains(currentWord)) {
            result += currentWord
            currentWord = ""
        }

        currentWord += symbol
    }

    Pair(result, tableSymbol)
} catch (e: Exception) {
    throw Exception(" -> createSymbolTable - ${e.message}")
}

// += -= /= *= %= ^= ++ --
fun String.openOperators(symbolTable: List<List<String>>): String = try{
    var result = ""
    var currentWord = ""

    var isInStr = false
    var index = 0
    while (index in indices) {
        if (this[index] == '"')
            isInStr = !isInStr

        if (isInStr) {
            result += this[index]
            index++
            continue
        }
        if (KEY_WORD.contains(this[index].toString()) && index < (length - 1))
            if (this[index + 1] == '=') {
                result += "=$currentWord${this[index]}"
                currentWord = ""
                index += 2
                continue
            } else if (this[index + 1] in setOf('+', '-')) {
                result += "=$currentWord${this[index]}${symbolTable[1][0]}"
                currentWord = ""
                index += 2
                continue
            }

        result += this[index]
        if (this[index] in setOf(';', ','))
            currentWord = ""
        else
            currentWord += this[index]

        index++
    }
    result
} catch (e: Exception) {
    throw Exception(" -> openOperators - ${e.message}")
}

fun String.setValues(symbolTable: List<MutableList<String>>): Pair<Pair<String, String>, List<MutableList<String>>> = try{
    var result = ""
    var postfixExp = ""

    split(';').forEach {
        var rightExp: String
        var leftExp: String
        it.split('=').also { listOfExps ->
            if (listOfExps[0] in symbolTable[1]) {
                leftExp = listOfExps[0]
                postfixExp += "$leftExp="
                rightExp = listOfExps[1]

                symbolTable[2][symbolTable[1].indexOf(leftExp)] =
                    rightExp.toPostFixExp().onEach { str ->
                        postfixExp += str
                    }.calculateResultOfPostfixExp(symbolTable)
            } else {
                listOfExps.forEachIndexed { ind, exp ->
                    postfixExp += exp + if (ind != (listOfExps.size - 1)) "=" else ""
                }

                result += it.operatorPrint(symbolTable)
            }
        }
        postfixExp += "\n"
    }

    Pair(Pair(result, postfixExp), symbolTable)
} catch (e: Exception) {
    throw Exception(" -> setValues(toPostFix, calculateValues and operators!) - ${e.message}")
}

fun String.operatorPrint(symbolTable: List<MutableList<String>>): String {
    var result = ""
    split('\n').forEach { line ->
        var word = ""
        for ((index, symbol) in line.withIndex()) {
            if (word == "PRINT") {
                line.substring(index).split(',').forEach {
                    result += if (it[0] == '"')
                        it.substring(1, it.length - 1)
                    else
                        symbolTable[2][symbolTable[1].indexOf(it)]
                }

                result += '\n'
                word = ""
            }

            word += symbol
        }
    }

    return result
}

private fun String.toPostFixExp(): List<String> {
    val operatorsStack = Stack<Char>()

    val operatorsPriority: (Char) -> (Int) = { operator ->
        when (operator) {
            '^' -> 3
            '*', '/' -> 2
            '+', '-' -> 1
            else -> -1
        }
    }

    val result: MutableList<String> = mutableListOf()

    var currentVal = ""
    for (it in this) {
        if ((it == '@') || (it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z')) {
            currentVal += it
            continue
        } else if (it == '(' || it == '^') {
            operatorsStack.push(it)
            if (currentVal != "")
                result.add(currentVal)
        } else if (it == ')') {
            if (currentVal != "")
                result.add(currentVal)
            while (operatorsStack.peek() != '(')
                result.add(operatorsStack.pop().toString())

            operatorsStack.pop()
        } else {
            if (currentVal != "")
                result.add(currentVal)

            while (operatorsStack.isNotEmpty() && operatorsPriority(it) <= operatorsPriority(operatorsStack.peek()))
                result.add(operatorsStack.pop().toString())

            operatorsStack.push(it)
        }

        currentVal = ""
    }

    if (currentVal != "")
        result.add(currentVal)

    // Pop all the remaining elements from the stack
    while (operatorsStack.isNotEmpty())
        result.add(operatorsStack.pop().toString())

    return result
}

private fun List<String>.calculateResultOfPostfixExp(symbolTable: List<List<String>>): String {
    val operandsStack = Stack<Double>()
    var result = ""

    var rightOperand: Double
    forEach {
        operandsStack.push(
            if (it in listOf("+", "-", "*", "/", "^")) {
                rightOperand = operandsStack.pop()
                when (it) {
                    "+" ->
                        operandsStack.pop() + rightOperand
                    "-" ->
                        operandsStack.pop() - rightOperand
                    "*" ->
                        operandsStack.pop() * rightOperand
                    "/" ->
                        operandsStack.pop() / rightOperand
                    else ->
                        Math.pow(operandsStack.pop(), rightOperand)
                }
            } else
                symbolTable[2][symbolTable[1].indexOf(it)].toDouble()
        )
    }

    while (operandsStack.isNotEmpty())
        result += operandsStack.pop().toString()

    return result
}