package com.example.domain.repository

import com.example.domain.model.CalculationHistory
import com.example.domain.model.ThemeSettings

interface DeviceRepository {
    fun getOrCreateUuid(): String
    fun saveUuidToFirestore(uuid: String)

    fun saveCalculationHistory(uuid: String, history: CalculationHistory)
    fun getCalculationHistory(uuid: String, onSuccess: (List<CalculationHistory>) -> Unit,
                              onFailure: (Exception) -> Unit)

    fun saveThemeSettings(uuid: String, themeSettings: ThemeSettings, onComplete: (Boolean) -> Unit)
    fun getThemeSettings(uuid: String, onSuccess: (ThemeSettings) -> Unit, onFailure: (Exception) -> Unit)
}