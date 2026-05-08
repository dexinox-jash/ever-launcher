package com.everlauncher.domain.usecase

import com.everlauncher.data.repository.AnalyticsRepository

class TrackAppLaunchUseCase(private val analyticsRepository: AnalyticsRepository) {
    suspend fun trackLaunch() = analyticsRepository.incrementAppsLaunched()
    suspend fun trackGatedBypass() = analyticsRepository.incrementGatedBypasses()
    suspend fun trackModeOverride() = analyticsRepository.incrementModeOverrides()
}
