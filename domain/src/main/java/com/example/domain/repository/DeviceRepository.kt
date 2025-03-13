package com.example.domain.repository

import com.example.domain.model.CalculationHistory

interface DeviceRepository {
    fun getOrCreateUuid(): String
    fun saveUuidToFirestore(uuid: String)

    fun saveCalculationHistory(uuid: String, history: CalculationHistory)
    fun getCalculationHistory(uuid: String, onSuccess: (List<CalculationHistory>) -> Unit,
                              onFailure: (Exception) -> Unit)
}