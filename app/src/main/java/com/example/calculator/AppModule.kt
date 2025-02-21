package com.example.calculator

import android.content.Context
import com.example.data.repository.CalculatorRepositoryImpl
import com.example.domain.repository.CalculatorRepository
import com.example.domain.usecase.CalculateExpressionUseCase
import com.example.utils.SoundManager
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
}