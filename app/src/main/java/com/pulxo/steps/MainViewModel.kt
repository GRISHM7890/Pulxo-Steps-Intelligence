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

class MainViewModel(
    private val stepRepository: StepRepository,
    private val calculateDistance: CalculateDistanceUseCase = CalculateDistanceUseCase(),
    private val calculateCalories: CalculateCaloriesUseCase = CalculateCaloriesUseCase()
) : ViewModel() {

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
