package com.pulxo.steps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pulxo.steps.domain.model.DailyStats
import com.pulxo.steps.domain.model.PulxoUser
import com.pulxo.steps.domain.repository.AppContainer
import com.pulxo.steps.domain.repository.AppContainerProvider
import com.pulxo.steps.ui.DashboardScreen
import com.pulxo.steps.ui.AuthScreen
import com.pulxo.steps.ui.AnalyticsScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val container = (application as AppContainerProvider).container
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when (modelClass) {
                    MainViewModel::class.java -> MainViewModel(
                        container.stepRepository,
                        container.authRepository,
                        container.syncRepository
                    ) as T
                    AuthViewModel::class.java -> AuthViewModel(container.authRepository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
        val viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
        val authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authUser by authViewModel.currentUser.collectAsState()
                    val navController = rememberNavController()

                    if (authUser == null) {
                        AuthScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = { /* NavHost handles state change */ }
                        )
                    } else {
                        NavHost(navController = navController, startDestination = "dashboard") {
                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToAnalytics = { navController.navigate("analytics") },
                                    onLogoutClick = { authViewModel.signOut() },
                                    userEmail = authUser?.email
                                )
                            }
                            composable("analytics") {
                                val stats by viewModel.historicalStats.collectAsState()
                                AnalyticsScreen(
                                    onBackClick = { navController.popBackStack() },
                                    stats = stats
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
