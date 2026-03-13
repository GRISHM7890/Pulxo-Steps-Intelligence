package com.pulxo.steps.domain.usecase

class CalculateDistanceUseCase(
    private val strideLengthMeters: Float = 0.72f // Average stride length
) {
    operator fun invoke(steps: Int): Float {
        return steps * strideLengthMeters
    }
}

class CalculateCaloriesUseCase(
    private val userWeightKg: Float = 70f // Default user weight
) {
    operator fun invoke(steps: Int): Float {
        // Simple heuristic: ~0.04 kcal per step for average person
        // Realistic calculation depends on speed and exact weight, 
        // but this serves as a baseline estimation.
        return steps * userWeightKg * 0.00057f 
    }
}
