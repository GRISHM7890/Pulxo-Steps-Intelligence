package com.pulxo.steps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pulxo.steps.ui.DashboardScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pulxo.steps.domain.repository.AppContainerProvider
import com.pulxo.steps.ui.DashboardScreen

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pulxo.steps.ui.AnalyticsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val container = (application as AppContainerProvider).container
        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(container.stepRepository) as T
            }
        })[MainViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToAnalytics = { navController.navigate("analytics") }
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
