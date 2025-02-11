package com.example.calculator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.CalculatorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.exp

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val repository: CalculatorRepository
) : ViewModel() {

    private val _input = MutableStateFlow("") // Current Enter
    val input: StateFlow<String> = _input

    private val _result = MutableStateFlow("") // Result
    val result: StateFlow<String> = _result

    private val _showAdvancedButtons = MutableStateFlow(false)
    val showAdvancedButtons: StateFlow<Boolean> = _showAdvancedButtons

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
//            "Доп." -> toggleAdvancedButtons()
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

    private fun calculateResult() {
        val expression = _input.value
        if (expression.isNotEmpty()) {
            viewModelScope.launch {
                val calculationResult = repository.calculate(expression)
                _result.value = calculationResult.result.toString()
            }
        }
    }

    private fun squareNumber() {
        val currentInput = _input.value
        if (currentInput.isNotEmpty()) {
            val lastNumber = extractLastNumber(currentInput)
            if (lastNumber != null) {
                _input.value = currentInput.dropLast(lastNumber.length) + "($lastNumber)^2"
            }
        }
    }

    private fun squareRoot() {
        val currentInput = _input.value
        if (currentInput.isNotEmpty()) {
            val lastNumber = extractLastNumber(currentInput)
            if (lastNumber != null){
                _input.value = currentInput.dropLast(lastNumber.length) + "sqrt($lastNumber)"
            }
        }
    }

    private fun extractLastNumber(input: String): String? {
        val regex = Regex("""(\d+\.?\d*)""")
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
        return lastChar.isDigit() || lastChar == ')'
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

    private fun toggleAdvancedButtons() {
        _showAdvancedButtons.value = !_showAdvancedButtons.value
    }
}