package com.example.domain.model

import java.util.Date


data class CalculationHistory(
    val expression: String = "",
    val result: String = "",
    val timestamp: Date = Date()
) {
    // Конструктор без аргументов
    constructor() : this("", "", Date())
}