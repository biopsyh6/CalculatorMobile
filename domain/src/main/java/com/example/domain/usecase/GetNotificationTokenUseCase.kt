package com.example.domain.usecase

import com.example.domain.repository.NotificationRepository
import javax.inject.Inject

class GetNotificationTokenUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(): String {
        return notificationRepository.getToken()
    }
}