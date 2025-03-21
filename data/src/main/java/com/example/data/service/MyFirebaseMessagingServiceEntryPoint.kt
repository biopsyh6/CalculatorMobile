package com.example.data.service

import com.example.domain.repository.NotificationRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MyFirebaseMessagingServiceEntryPoint {
    fun notificationRepository(): NotificationRepository
}