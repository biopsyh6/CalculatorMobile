package com.example.calculator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.CalculatorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.StrictMath.round
import java.math.BigInteger
import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val repository: CalculatorRepository
) : ViewModel() {

    private val _input = MutableStateFlow("") // Current Enter
    val input: StateFlow<String> = _input

    private val _result = MutableStateFlow("") // Result
    val result: StateFlow<String> = _result

    private val _useDegrees = MutableStateFlow(true)
    val useDegrees: StateFlow<Boolean> = _useDegrees

    private val _showAdvancedButtons = MutableStateFlow(false)

    fun onButtonClick(buttonValue: String) {
        when (buttonValue) {
            "C" -> {
                _input.value = ""
                _result.value = ""
            }
            "=" -> calculateResult()
            "x²" -> squareNumber()
            "√" -> squareRoot()
            "%" -> calculatePercentage()
            "!" -> calculateFactorial()
            "^" -> calculatePower()
            "1/x" -> calculateInverse()
            "sin" -> addTrigonometricFunction("sin")
            "cos" -> addTrigonometricFunction("cos")
            "tan" -> addTrigonometricFunction("tan")
            "π" -> _input.value += "π"
            "e" -> _input.value += "e"
            "lg" -> calculateDecimalLogarithm()
            "ln" -> calculateNaturalLogarithm()
            in "0" .. "9" -> {
                val lastOperand = getLastOperand()
                if (lastOperand.length < 16) {
                    _input.value += buttonValue
                }
            }
            "." -> {
                if (canAddDecimal()) {
                    _input.value += buttonValue
                }
            }
            "+", "-", "*", "/" -> {
                if (canAddOperator()) {
                    _input.value += buttonValue
                }
            }
            "(" -> {
                if (canAddOpenBracket()) {
                    _input.value += buttonValue
                }
            }
            ")" -> {
                if (canAddCloseBracket()) {
                    _input.value += buttonValue
                }
            }
            else -> {
                _input.value += buttonValue
            }
        }
    }

    fun toggleUseDegrees() {
        _useDegrees.value = !_useDegrees.value
    }

    private fun calculateResult() {
        val expression = _input.value
        if (expression.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    val calculationResult = repository.calculate(expression)
                    if (calculationResult.error == null) {
//                        _result.value = calculationResult.result.toString()
                        _result.value = formatResult(calculationResult.result)
                    } else {
                        _result.value = "Error: ${calculationResult.error}"
                    }
                } catch (e: ArithmeticException) {
                    _result.value = "Error: Division by zero"
                }
            }
        }
    }

    private fun squareNumber() {
        val currentInput = _input.value
        if (currentInput.isNotEmpty()) {
            val lastNumber = extractLastNumber(currentInput)
            if (lastNumber != null) {
                val isInBrackets = currentInput.endsWith("($lastNumber)")
                val newExpression = if (isInBrackets) {
                    currentInput.dropLast(lastNumber.length + 2) + "($lastNumber)^2"
                } else {
                    currentInput.dropLast(lastNumber.length) + "($lastNumber)^2"
                }
                _input.value = newExpression
            }
        }
    }

    private fun squareRoot() {
        val currentInput = _input.value
        if (currentInput.isNotEmpty()) {
            val lastNumber = extractLastNumber(currentInput)
            if (lastNumber != null){
                try {
                    val number = lastNumber.toDouble()
                    if (number < 0) {
                        _result.value = "Error"
                        return
                    }
                    val isInBrackets = currentInput.endsWith("($lastNumber)")
                    val newExpression = if (isInBrackets) {
                        currentInput.dropLast(lastNumber.length + 2) + "sqrt($lastNumber)"
                    } else {
                        currentInput.dropLast(lastNumber.length) + "sqrt($lastNumber)"
                    }
                    _input.value = newExpression
                } catch (e: NumberFormatException) {
                    _result.value = "Error: Invalid number"
                }
            }
        }
    }

    private fun extractLastNumber(input: String): String? {
//        val regex = Regex("""-?\d+\.?\d*|π|e""")
        val regex = Regex("""-?\d+\.?\d*|π|e|\([^()]+\)""")
        val matches = regex.findAll(input)
        return matches.lastOrNull()?.value
    }

    private fun canAddOperator(): Boolean {
        val currentInput = _input.value
        if (currentInput.isEmpty()) return false

        val lastChar = currentInput.last()

        return lastChar !in listOf('+', '-', '*', '/')
    }

    private fun canAddDecimal(): Boolean {
        val currentInput = _input.value
        val lastNumber = extractLastNumber(currentInput)

        val lastChar = currentInput.lastOrNull()
        val isAfterOperator = lastChar in listOf('+', '-', '*', '/', '(', ')')

        return lastNumber != null && !lastNumber.contains(".") && !isAfterOperator
    }

    private fun canAddOpenBracket(): Boolean {
        val currentInput = _input.value
        return currentInput.isEmpty() || currentInput.last() in listOf('+', '-', '*', '/', '(')
    }

    private fun canAddCloseBracket(): Boolean {
        val currentInput = _input.value
        val openCount = currentInput.count { it == '(' }
        val closeCount = currentInput.count { it == ')' }

        if (openCount <= closeCount) return false
        if (currentInput.isEmpty()) return false

        val lastChar = currentInput.last()
        return lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e'
    }

    private fun calculatePercentage() {
        val currentInput = _input.value
        if (currentInput.isNotEmpty()) {
            val lastNumber = extractLastNumber(currentInput)
            if (lastNumber != null) {
                val percentageValue = lastNumber.toDouble() / 100

                // Operator before last number
                val operatorIndex = findLastOperatorIndex(currentInput)
                val operator = if (operatorIndex != -1) currentInput[operatorIndex] else null

                // Check operator
                val newExpression = when (operator) {
                    '+', '-' -> {
                        viewModelScope.launch {
                            val baseNumber = extractBaseNumber(currentInput, operatorIndex)
                            if (baseNumber != null) {
                                val percentageOfBase = baseNumber * percentageValue
                                val newValue = if (operator == '+') {
                                    currentInput.dropLast(lastNumber.length) + percentageOfBase.toString()
                                } else {
                                    currentInput.dropLast(lastNumber.length) + (percentageOfBase).toString()
                                }
                                _input.value = newValue
                            }
                        }
                        return
                    }
                    '*', '/' -> {
                        currentInput.dropLast(lastNumber.length) + percentageValue.toString()
                    }
                    else -> {
                        currentInput.dropLast(lastNumber.length) + percentageValue.toString()
                    }
                }
                _input.value = newExpression
            }
        }
//        val currentInput = _input.value
//        if (currentInput.isNotEmpty()) {
//            val lastNumber = extractLastNumber(currentInput)
//            if (lastNumber != null) {
//                val percentageValue = lastNumber.toDouble() / 100
//                _input.value = currentInput.dropLast(lastNumber.length) + percentageValue.toString()
//            }
//        }
    }

    private fun findLastOperatorIndex(input: String): Int {
        for (i in input.length - 1 downTo 0) {
            if (input[i] in listOf('+', '-', '*', '/')) {
                return i
            }
        }
        return -1
    }

    // Extract number before operator
    private fun extractBaseNumber(input: String, operatorIndex: Int): Double? {
        val expressionBeforeOperator = input.substring(0, operatorIndex).trim()
        if (expressionBeforeOperator.isEmpty()) {
            return null
        }
        val calculationResult = repository.calculate(expressionBeforeOperator)
        return if (calculationResult.error == null) {
            calculationResult.result
        } else {
            null
        }
    }

    private fun factorial(n: Int): BigInteger {
        return if (n <= 1) BigInteger.ONE else BigInteger.valueOf(n.toLong()) * factorial(n - 1)
    }

    private fun extractLastNumberWithBrackets(input: String): String? {
        val regex = Regex("""\(([-]?\d+)\)|(-?\d+)""")
        val matches = regex.findAll(input)
        return matches.lastOrNull()?.groupValues?.filterNotNull()?.last()
    }

    private fun calculateFactorial() {
        var currentInput = _input.value
        if (currentInput.isNotEmpty()) {
            val lastChar = currentInput.last()
            if (lastChar in listOf('+', '-', '*', '/')) {
                _input.value = currentInput.dropLast(1)
                currentInput = _input.value
            }

            val isInBrackets = currentInput.endsWith(")") && currentInput.contains("(")
            val lastNumber = if (isInBrackets) {
                val startIndex = currentInput.lastIndexOf("(") + 1
                val endIndex = currentInput.lastIndexOf(")")
                currentInput.substring(startIndex, endIndex)
            } else {
                extractLastNumber(currentInput)
            }

            if (lastNumber != null) {
                try {
                    val number = lastNumber.toInt()
                    if (number >= 0) {
                        val factorialResult = factorial(number)
                        _input.value = if (isInBrackets) {
                            currentInput.dropLast(lastNumber.length + 2) + factorialResult.toString()
                        } else {
                            currentInput.dropLast(lastNumber.length) + factorialResult.toString()
                        }
                    } else {
                        _result.value = "Error"
                    }
                } catch (e:NumberFormatException) {
                    _result.value = "Error: Invalid number"
                }
            }
        }
    }

    private fun calculateInverse() {
        val currentInput = _input.value
        if (currentInput.isNotEmpty()) {
            val lastNumber = extractLastNumber(currentInput)
            if (lastNumber != null) {
                try {
                    val number = evaluateLastNumber(lastNumber)
                    if (number == 0.0) {
                        _result.value = "Error: Division by zero"
                    } else {
                        _input.value = currentInput.dropLast(lastNumber.length) + "($lastNumber)^(-1)"
                    }
                } catch (e: NumberFormatException) {
                    _result.value = "Error: Invalid number"
                }
            }
        }
    }

    private fun calculatePower() {
        val currentInput = _input.value
        if (currentInput.isNotEmpty()) {
            val lastChar = currentInput.last()
            if (lastChar in listOf('+', '-', '*', '/')) {
                _input.value = currentInput.dropLast(1) + "^"
            } else {
                _input.value = currentInput + "^"
            }
        }
    }

    private fun addTrigonometricFunction(function: String) {
        val currentInput = _input.value
        if (currentInput.isNotEmpty()) {
            val lastNumber = extractLastNumber(currentInput)
            if (lastNumber != null) {
                val newExpression = if (_useDegrees.value && "π" !in lastNumber) {
                    if (lastNumber.startsWith("(") && lastNumber.endsWith(")")) {
                        "$function($lastNumber * π / 180)"
                    } else {
                        "$function($lastNumber * π / 180)"
                    }
                } else {
                    if (lastNumber.startsWith("(") && lastNumber.endsWith(")")) {
                        "$function$lastNumber"
                    } else {
                        "$function($lastNumber)"
                    }
                }
                _input.value = currentInput.dropLast(lastNumber.length) + newExpression
            }
        }
    }

    private fun calculateDecimalLogarithm() {
        val currentInput = _input.value
        if (currentInput.isNotEmpty()) {
            val lastNumber = extractLastNumber(currentInput)
            if (lastNumber != null) {
                try {
                    val number = evaluateLastNumber(lastNumber)
                    if (number > 0) {
                        val logValue = log10(number)
                        val newExpression = if (currentInput.endsWith("($lastNumber)")) {
                            currentInput.dropLast(lastNumber.length + 2) + logValue.toString()
                        } else {
                            currentInput.dropLast(lastNumber.length) + logValue.toString()
                        }
                        _input.value = newExpression
                    } else {
                        _result.value = "Error"
                    }
                } catch (e: NumberFormatException) {
                    _result.value = "Error: incorrect number"
                }
            }
        }
    }

    private fun calculateNaturalLogarithm() {
        val currentInput = _input.value
        if (currentInput.isNotEmpty()) {
            val lastNumber = extractLastNumber(currentInput)
            if (lastNumber != null) {
                try {
                    val number = evaluateLastNumber(lastNumber)
                    if (number > 0) {
                        val lnValue = ln(number)
                        val newExpression = if (currentInput.endsWith("($lastNumber)")) {
                            currentInput.dropLast(lastNumber.length + 2) + lnValue.toString()
                        } else {
                            currentInput.dropLast(lastNumber.length) + lnValue.toString()
                        }
                        _input.value = newExpression
                    } else {
                        _result.value = "Error"
                    }
                } catch (e: NumberFormatException) {
                    _result.value = "Error: incorrect number"
                }
            }
        }
    }

    private fun evaluateLastNumber(lastNumber: String): Double {
        return if (lastNumber == "π") {
            Math.PI
        } else if (lastNumber == "e") {
            Math.E
        } else if (lastNumber.startsWith("(") && lastNumber.endsWith(")")) {
            val expression = lastNumber.removeSurrounding("(", ")")
            repository.calculate(expression).result ?: throw NumberFormatException()
        } else {
            lastNumber.toDouble()
        }
    }

    private fun formatResult(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            roundResult(value, 10).toString()
        }
    }

    private fun getLastOperand(): String {
        val expression = _input.value
        val parts = expression.split("+", "-", "*", "/")
        return parts.lastOrNull() ?: ""
    }


    private fun roundResult(value: Double, precision: Int = 10): Double {
        val scale = 10.0.pow(precision)
        return round(value * scale) / scale
    }
}