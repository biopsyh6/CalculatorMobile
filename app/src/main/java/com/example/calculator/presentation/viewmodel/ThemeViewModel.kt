package com.example.calculator.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.domain.model.ThemeSettings
import com.example.domain.usecase.GetThemeSettingsUseCase
import com.example.domain.usecase.SaveThemeSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val getThemeSettingsUseCase: GetThemeSettingsUseCase,
    private val saveThemeSettingsUseCase: SaveThemeSettingsUseCase
) : ViewModel() {
    private val _themeSettings = MutableStateFlow(ThemeSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings

    fun loadThemeSettings(uuid: String) {
        getThemeSettingsUseCase(
            uuid = uuid,
            onSuccess = { settings ->
                _themeSettings.value = settings
            },
            onFailure = { e ->
                Log.e("ThemeViewModel", "Error loading theme settings", e)
            }
        )
    }

    fun saveThemeSettings(uuid: String, themeSettings: ThemeSettings) {
        saveThemeSettingsUseCase(
            uuid = uuid,
            themeSettings = themeSettings,
            onComplete = { success ->
                if (success) {
                    _themeSettings.value = themeSettings
                } else {
                    Log.e("ThemeViewModel", "Failed to save theme settings")
                }
            }
        )
    }
}