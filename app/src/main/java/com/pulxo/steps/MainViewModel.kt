package com.pulxo.steps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulxo.steps.domain.repository.StepRepository
import com.pulxo.steps.domain.usecase.CalculateCaloriesUseCase
import com.pulxo.steps.domain.usecase.CalculateDistanceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

import com.pulxo.steps.domain.repository.AuthRepository
import com.pulxo.steps.domain.repository.SyncRepository
import com.pulxo.steps.domain.model.DailyStats
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainViewModel(
    private val stepRepository: StepRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val calculateDistance: CalculateDistanceUseCase = CalculateDistanceUseCase(),
    private val calculateCalories: CalculateCaloriesUseCase = CalculateCaloriesUseCase()
) : ViewModel() {

    init {
        // Automatic sync to Firebase when steps change
        viewModelScope.launch {
            stepRepository.getTodayStepsFlow().collect { steps ->
                val user = authRepository.currentUser.firstOrNull() ?: return@collect
                val todayEpoch = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
                
                // Fetch the full stats object to sync
                val stats = DailyStats(
                    dateEpochDays = todayEpoch,
                    steps = steps,
                    distanceMeters = calculateDistance(steps),
                    caloriesBurned = calculateCalories(steps),
                    activeTimeMinutes = 0 // TODO: Track active time
                )
                syncRepository.syncDailyStats(user.uid, stats)
            }
        }
    }

    // Real-time UI state
    val dashboardState: StateFlow<DashboardUiState> = stepRepository.getTodayStepsFlow()
        .map { steps ->
            DashboardUiState(
                currentSteps = steps,
                distanceMeters = calculateDistance(steps),
                caloriesBurned = calculateCalories(steps)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    val historicalStats: StateFlow<List<DailyStats>> = stepRepository.getLastSevenDaysStatsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

data class DashboardUiState(
    val currentSteps: Int = 0,
    val distanceMeters: Float = 0f,
    val caloriesBurned: Float = 0f
)
