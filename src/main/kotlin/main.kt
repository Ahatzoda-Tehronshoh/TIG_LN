import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.io.File
import java.util.*


@OptIn(ExperimentalUnitApi::class)
@Composable
@Preview
fun App(window: ComposeWindow) {
    var code by remember {
        mutableStateOf(
            TextFieldValue(
                """Alfa = 10; //первый оператор
beta = /*- 1 -*/15 + /*- 2 -*/2.5*alfa+(2+1);
i=1;
beta+=i; i++; print beta;
beta/= /*перенос оператора*/
        3;
print "beta=",beta;"""
            )
        )
    }

    var fontSize = remember {
        mutableStateOf(25.sp)
    }

    val fieldWidth = remember {
        mutableStateOf(1.0F)
    }

    val showOptimization = remember {
        mutableStateOf(false)
    }

    var showTokens = remember {
        mutableStateOf(false)
    }

    var showOpeningOperators = remember {
        mutableStateOf(false)
    }

    var showPostfix = remember {
        mutableStateOf(false)
    }

    var name by remember {
        mutableStateOf("")
    }

    var key by remember {
        mutableStateOf("")
    }

    var value by remember {
        mutableStateOf("")
    }

    var resultStr by remember {
        mutableStateOf("")
    }

    var scrollStateMain by remember {
        mutableStateOf(1000)
    }

    MaterialTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color.LightGray)
                .fillMaxSize()
                .verticalScroll(ScrollState(scrollStateMain))
                .padding(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                MaterialTheme(
                    colors = lightColors(primary = Color.White)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            code = it
                        },
                        visualTransformation = CodeTransformation(),
                        textStyle = TextStyle(fontSize = fontSize.value),
                        modifier = Modifier
                            .background(Color.DarkGray)
                            .padding(8.dp)
                            .fillMaxWidth(fieldWidth.value)
                            .height(625.dp)
                            .verticalScroll(ScrollState(0)),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            Color(MAIN_COLOR),
                            cursorColor = Color.White
                        ),
                        label = { Text("Enter your code!") }
                    )
                }
                Row(
                    modifier = Modifier
                        .background(Color.Gray)
                        .padding(8.dp)
                        .height(625.dp)
                        .wrapContentWidth()
                        .verticalScroll(ScrollState(0))
                ) {
                    Text(
                        text = name,
                        fontSize = fontSize.value,
                        color = Color.Green,
                        modifier = Modifier.padding(end = 24.dp)
                    )
                    Text(
                        text = key,
                        fontSize = fontSize.value,
                        color = Color.Green,
                        modifier = Modifier.padding(end = 24.dp)
                    )
                    Text(
                        text = value,
                        fontSize = fontSize.value,
                        color = Color.Green,
                        modifier = Modifier.padding(end = 24.dp)
                    )
                }
            }
            Text(
                text = resultStr,
                color = Color.White,
                fontSize = fontSize.value,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .background(Color.Black)
                    .padding(16.dp)
                    .defaultMinSize(minWidth = 1600.dp, minHeight = 30.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showOptimization.value,
                        onCheckedChange = {
                            showOptimization.value = it
                        }
                    )
                    Text("Оптимизация", fontSize = 22.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showTokens.value,
                        onCheckedChange = {
                            showTokens.value = it
                        }
                    )
                    Text("Таблица символов", fontSize = 22.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showOpeningOperators.value,
                        onCheckedChange = {
                            showOpeningOperators.value = it
                        }
                    )
                    Text("Разбивка операций", fontSize = 22.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showPostfix.value,
                        onCheckedChange = {
                            showPostfix.value = it
                        }
                    )
                    Text("Полиз операций", fontSize = 22.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        "${fontSize.value}", fontSize = 22.sp,
                        modifier = Modifier.padding(end = 8.dp).background(color = Color.White).padding(8.dp, 4.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "+",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable(
                                onClick = {
                                    if (fontSize.value.value < 55)
                                        fontSize.value = (fontSize.value.value + 1).sp
                                }
                            ).width(30.dp).height(30.dp).padding(vertical = 2.dp).background(color = Color.White)
                        )
                        Text(
                            "-",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable(
                                onClick = {
                                    if (fontSize.value.value > 1)
                                        fontSize.value = (fontSize.value.value - 1).sp
                                }
                            ).width(30.dp).height(30.dp).padding(top = 2.dp).background(color = Color.White)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = {
                            openFileDialog(window, "Choose file with code!", listOf(".txt")).forEach { file ->
                                code = TextFieldValue("")
                                file.forEachLine {
                                    code = TextFieldValue(code.text + it + '\n')
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).height(30.dp)
                    ) {
                        Text("Open file!")
                    }
                    Button(
                        onClick = {
                            openFileDialog(window, "Choose file for saving code!", listOf(".txt")).forEach { file ->
                                file.printWriter().use {
                                    it.print(code.text)
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp).height(30.dp)
                    ) {
                        Text("Save in file!")
                    }
                }
            }
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        var symbolTable: List<List<String>> = listOf()
                        try {
                            resultStr = ""
                            val time = Calendar.getInstance().timeInMillis

                            //1 - optimizied code
                            var newCode = optimizationCode(code.text).also {
                                if (showOptimization.value) {
                                    resultStr += "======================Оптимизация===========================\n"
                                    resultStr += it
                                    resultStr += "\n===========================================================\n"
                                }
                            }

                            //2 - Create symbols table
                            symbolTable = createSymbolTable(newCode).also {
                                newCode = it.first

                                if (showTokens.value) {
                                    resultStr += "===================Создание таблица символов========================\n"
                                    resultStr += it.first
                                    resultStr += "\n==================================================================\n"
                                }
                            }.second

                            //3 - operator
                            newCode = openOperator(newCode, symbolTable).also {
                                if (showOpeningOperators.value) {
                                    resultStr += "===================Разбивка операций========================\n"
                                    resultStr += it.replace(";", ";\n")
                                    resultStr += "\n===========================================================\n"
                                }
                            }

                            // 4 - to Postfix Expressions and calculate the value of the varuables
                            newCode = calculateValueOfVariables(newCode, symbolTable).also {
                                fieldWidth.value = 1.0F
                                if (showTokens.value) {
                                    fieldWidth.value = 0.675F
                                    symbolTable = it.second
                                    name = ""
                                    key = ""
                                    value = ""
                                    for (index in it.second[0].indices) {
                                        name += "${it.second[0][index]}\n"
                                        key += "${it.second[1][index]}\n"
                                        value += "${it.second[2][index]}\n"
                                    }
                                }
                                if (showPostfix.value) {
                                    resultStr += "======================Полиз операций=========================\n"
                                    resultStr += it.first.second
                                    resultStr += "\n============================================================\n"
                                }
                            }.first.first +
                                    "\n\n Time = ${(Calendar.getInstance().timeInMillis - time) / 1000.0}"

                            resultStr += newCode
                        } catch (e: Exception) {
                            if (showTokens.value) {
                                fieldWidth.value = 0.675F
                                name = ""
                                key = ""
                                value = ""
                                for (index in symbolTable[0].indices) {
                                    name += "${symbolTable[0][index]}\n"
                                    key += "${symbolTable[1][index]}\n"
                                    value += "${symbolTable[2][index]}\n"
                                }
                            }
                            resultStr += "Error please check your code and try again!\n       Exception:  " + e.message
                        }
                    }
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .height(50.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Text("Выполнить!")
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Tehron Language - msu",
        resizable = false,
        state = WindowState(placement = WindowPlacement.Maximized)
    ) {
        App(window)
    }
}

// 1
fun optimizationCode(code: String): String = try {
    code
        .deleteOneLineCommentary()
        .deleteMultipleLinesCommentary()
        .deleteBlanks()
        .deleteToNewLineCharacter()
        .toUppercase()
} catch (e: Exception) {
    throw Exception("1) OptimizationCode - ${e.message}")
}

//2
fun createSymbolTable(code: String): Pair<String, List<MutableList<String>>> {
    try {
        return code.createSymbolTable()
    } catch (e: Exception) {
        throw Exception("2) CreateSymbolTable - ${e.message}")
    }
}

//3
fun openOperator(code: String, symbolTable: List<List<String>>): String = try {
    code.openOperators(symbolTable)
} catch (e: Exception) {
    throw Exception("3) OpenOperator - ${e.message}")
}

//4
fun calculateValueOfVariables(
    code: String,
    symbolTable: List<MutableList<String>>
): Pair<Pair<String, String>, List<MutableList<String>>> = try {
    code.setValues(symbolTable)
} catch (e: Exception) {
    throw Exception("4) CalculateValueOfVariables - ${e.message}")
}

//File
fun openFileDialog(
    window: ComposeWindow,
    title: String,
    allowedExtensions: List<String>,
    allowMultiSelection: Boolean = true
): Set<File> = try {
    FileDialog(window, title, FileDialog.LOAD).apply {
        isMultipleMode = allowMultiSelection

        // windows
        file = allowedExtensions.joinToString(";") {
            "*$it"
        } // e.g. '*.jpg'

        isVisible = true
    }.files.toSet()
} catch (e: Exception) {
    throw Exception("Working with file - ${e.message}")
}