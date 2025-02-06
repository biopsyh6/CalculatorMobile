package com.example.data.repository

import com.example.domain.model.CalculationResult
import com.example.domain.repository.CalculatorRepository
import com.example.domain.usecase.CalculateExpressionUseCase
import javax.inject.Inject

class CalculatorRepositoryImpl @Inject constructor(
    private val calculateExpressionUseCase: CalculateExpressionUseCase
) : CalculatorRepository {
    override fun calculate(expression: String): CalculationResult {
        return calculateExpressionUseCase(expression)
    }
}