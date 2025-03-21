package com.example.data.service

import com.example.domain.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    override fun onCreate() {
        super.onCreate()
        // Get dependencies throw EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            MyFirebaseMessagingServiceEntryPoint::class.java
        )
        notificationRepository = entryPoint.notificationRepository()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}

