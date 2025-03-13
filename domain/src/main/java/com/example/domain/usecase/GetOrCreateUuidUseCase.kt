package com.example.domain.usecase

import com.example.domain.repository.DeviceRepository


class GetOrCreateUuidUseCase (
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): String {
        return deviceRepository.getOrCreateUuid()
    }
}