package com.example.domain.usecase

import com.example.domain.model.ThemeSettings
import com.example.domain.repository.DeviceRepository

class SaveThemeSettingsUseCase (
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(uuid: String, themeSettings: ThemeSettings, onComplete: (Boolean) -> Unit) {
        deviceRepository.saveThemeSettings(uuid, themeSettings, onComplete)
    }
}