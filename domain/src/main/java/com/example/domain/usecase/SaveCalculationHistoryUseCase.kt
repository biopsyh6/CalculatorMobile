package com.example.domain.usecase

import com.example.domain.model.CalculationHistory
import com.example.domain.repository.DeviceRepository

class SaveCalculationHistoryUseCase (
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(uuid: String, history: CalculationHistory) {
        deviceRepository.saveCalculationHistory(uuid, history)
    }
}