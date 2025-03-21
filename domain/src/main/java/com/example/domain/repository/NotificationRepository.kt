package com.example.domain.repository

interface NotificationRepository {
    suspend fun getToken(): String
    fun subscribeToTopic(topic: String)
    fun unsubscribeFromTopic(topic: String)
}