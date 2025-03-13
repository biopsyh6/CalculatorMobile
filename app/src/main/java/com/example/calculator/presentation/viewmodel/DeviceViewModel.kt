package com.example.calculator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.domain.usecase.GetOrCreateUuidUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val getOrCreateUuidUseCase: GetOrCreateUuidUseCase
) : ViewModel() {
    fun getOrCreateUuid(): String {
        return getOrCreateUuidUseCase()
    }
}