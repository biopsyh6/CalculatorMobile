package com.example.domain.usecase

import com.example.domain.model.CalculationHistory
import com.example.domain.repository.DeviceRepository

class GetCalculationHistoryUseCase (
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(uuid: String, onSuccess: (List<CalculationHistory>) -> Unit,
                        onFailure: (Exception) -> Unit) {
        deviceRepository.getCalculationHistory(uuid, onSuccess, onFailure)
    }
}