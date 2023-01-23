import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val MAIN_COLOR = 0xFFFAFCFB // alfa, beta, 15, 2.5
const val OPERATOR_COLOR = 0xFFFF5733 // + - * / += -= *= /= print
const val STRING_COLOR = 0xFF1BA67A // "string"
const val ONE_LINE_COMMENTARY_COLOR = 0xFF6495ED // //_
const val MULTIPLE_LINE_COMMENTARY_COLOR = 0xFFDE3163 //    /*_*/

class CodeTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return codeFilter(text)
    }
}

fun codeFilter(text: AnnotatedString): TransformedText =
    TransformedText(buildAnnotatedString {

        // work with commentary
        var isOneLineComment = false
        var isMultipleLineComment = false

        // work with operators
        var isNewWord = false
        var isOperator = false
        var lengthOfTheOperator = 0

        // work with strings in "_"
        var isInStr = false

        for ((ind, symbol) in text.text.withIndex()) {
            if (ind < (text.text.length - 1)) {
                if (symbol == '/' && text.text[ind + 1] == '/')
                    isOneLineComment = true
                else if (symbol == '\n') {
                    isOneLineComment = false
                    isNewWord = true
                }

                if (symbol == '/' && text.text[ind + 1] == '*')
                    isMultipleLineComment = true
                else if (ind > 1 && text.text[ind - 2] == '*' && text.text[ind - 1] == '/') {
                    isMultipleLineComment = false
                    isOperator = false
                }
            } else if (ind > 1 && isMultipleLineComment && text.text[ind - 2] == '*' && text.text[ind - 1] == '/')
                isMultipleLineComment = false

            if (isOneLineComment || isMultipleLineComment) {
                isNewWord = false
                withStyle(
                    style = SpanStyle(
                        color = Color(
                            if (isOneLineComment)
                                ONE_LINE_COMMENTARY_COLOR
                            else
                                MULTIPLE_LINE_COMMENTARY_COLOR
                        )
                    )
                ) {
                    append(symbol)
                }
            } else {
                if(lengthOfTheOperator <= 1)
                    isOperator = false
                else
                    lengthOfTheOperator--

                if (isNewWord) {
                    isOperator = false
                    try {
                        for (keyWord in KEY_WORD)
                            if (text.text.substring(ind, ind + keyWord.length).trim().uppercase() == keyWord) {
                                lengthOfTheOperator = keyWord.length
                                isOperator = true
                                break
                            }
                    } catch (e: Exception) {
                    }
                }


                when {
                    symbol == '"' -> {
                        withStyle(style = SpanStyle(color = Color(STRING_COLOR))) {
                            append(symbol)
                        }
                        isInStr = !isInStr
                    }

                    isInStr -> withStyle(style = SpanStyle(color = Color(STRING_COLOR))) {
                        append(symbol)
                    }

                    KEY_WORD.contains(symbol.toString()) -> withStyle(
                        style = SpanStyle(
                            color = Color(OPERATOR_COLOR),
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        lengthOfTheOperator = 1
                        append(symbol)
                    }

                    isOperator -> withStyle(
                        style = SpanStyle(
                            color = Color(OPERATOR_COLOR),
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(symbol)
                    }

                    else -> append(symbol)
                }

                isNewWord = (symbol == ' ' || symbol == ';' || symbol == '\n')
            }
        }
    }, object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = offset

        override fun transformedToOriginal(offset: Int): Int = offset
    })