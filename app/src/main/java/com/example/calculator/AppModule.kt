package com.example.calculator

import com.example.data.repository.CalculatorRepositoryImpl
import com.example.domain.repository.CalculatorRepository
import com.example.domain.usecase.CalculateExpressionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

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
}