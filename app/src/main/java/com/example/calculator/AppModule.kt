package com.example.calculator

import android.content.Context
import android.content.SharedPreferences
import com.example.data.repository.CalculatorRepositoryImpl
import com.example.data.repository.NotificationRepositoryImpl
import com.example.domain.repository.CalculatorRepository
import com.example.domain.repository.DeviceRepository
import com.example.domain.repository.NotificationRepository
import com.example.domain.usecase.CalculateExpressionUseCase
import com.example.domain.usecase.GetCalculationHistoryUseCase
import com.example.domain.usecase.GetOrCreateUuidUseCase
import com.example.domain.usecase.GetThemeSettingsUseCase
import com.example.domain.usecase.SaveCalculationHistoryUseCase
import com.example.domain.usecase.SaveThemeSettingsUseCase
import com.example.utils.SoundManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideCalculateExpressionUseCase(): CalculateExpressionUseCase {
        return CalculateExpressionUseCase()
    }

    @Provides
    fun provideCalculatorRepository(
        calculateExpressionUseCase: CalculateExpressionUseCase
    ): CalculatorRepository {
        return CalculatorRepositoryImpl(calculateExpressionUseCase)
    }

    @Provides
    @Singleton
    fun provideSoundManager(@ApplicationContext context: Context): SoundManager {
        return SoundManager(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideGetOrCreateUuidUseCase(deviceRepository: DeviceRepository): GetOrCreateUuidUseCase {
        return GetOrCreateUuidUseCase(deviceRepository)
    }

    @Provides
    @Singleton
    fun provideSaveCalculationHistoryUseCase(deviceRepository: DeviceRepository): SaveCalculationHistoryUseCase {
        return SaveCalculationHistoryUseCase(deviceRepository)
    }

    @Provides
    @Singleton
    fun provideGetCalculationHistoryUseCase(deviceRepository: DeviceRepository): GetCalculationHistoryUseCase {
        return GetCalculationHistoryUseCase(deviceRepository)
    }

    @Provides
    @Singleton
    fun provideGetThemeSettingsUseCase(deviceRepository: DeviceRepository): GetThemeSettingsUseCase {
        return GetThemeSettingsUseCase(deviceRepository)
    }

    @Provides
    @Singleton
    fun provideSaveThemeSettingsUseCase(deviceRepository: DeviceRepository): SaveThemeSettingsUseCase {
        return SaveThemeSettingsUseCase(deviceRepository)
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(firebaseMessaging: FirebaseMessaging): NotificationRepository {
        return NotificationRepositoryImpl(firebaseMessaging)
    }
}