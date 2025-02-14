package com.example.domain.usecase

import com.example.domain.model.CalculationResult
import net.objecthunter.exp4j.ExpressionBuilder

class CalculateExpressionUseCase {
    operator fun invoke(expression: String): CalculationResult {
        return try {
            val result = ExpressionBuilder(expression)
                .variables("π", "e")
                .build()
                .setVariable("π", Math.PI)
                .setVariable("e", Math.E)
                .evaluate()
            CalculationResult(result)
        } catch (e: Exception) {
            CalculationResult(0.0, e.message.toString())
        }

    }
}