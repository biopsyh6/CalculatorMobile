package com.example.domain.repository

import com.example.domain.model.CalculationResult

interface CalculatorRepository {
    fun calculate(expression: String): CalculationResult
}