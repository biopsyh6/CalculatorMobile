package com.example.data.repository

import com.example.domain.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging
) : NotificationRepository {
    override suspend fun getToken(): String {
        return try {
            firebaseMessaging.token.await()
        } catch (e: Exception) {
            throw Exception("Failed to get FCM token", e)
        }
    }

    override fun subscribeToTopic(topic: String) {
        firebaseMessaging.subscribeToTopic(topic)
    }

    override fun unsubscribeFromTopic(topic: String) {
        firebaseMessaging.unsubscribeFromTopic(topic)
    }
}