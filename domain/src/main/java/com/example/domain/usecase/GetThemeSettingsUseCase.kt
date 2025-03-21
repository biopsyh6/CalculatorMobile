package com.example.domain.usecase

import com.example.domain.model.ThemeSettings
import com.example.domain.repository.DeviceRepository

class GetThemeSettingsUseCase (
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(uuid: String, onSuccess: (ThemeSettings) -> Unit, onFailure: (Exception) -> Unit) {
        deviceRepository.getThemeSettings(uuid, onSuccess, onFailure)
    }
}