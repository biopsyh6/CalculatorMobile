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

    fun onButtonClick(buttonValue: String) {
        when (buttonValue) {
            "C" -> {
                _input.value = ""
                _result.value = ""
            }
            "=" -> calculateResult()
            else -> _input.value += buttonValue
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

}