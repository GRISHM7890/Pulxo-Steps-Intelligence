package com.pulxo.steps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulxo.steps.DashboardUiState
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pulxo.steps.MainViewModel
import com.pulxo.steps.service.StepTrackingService

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToAnalytics: () -> Unit
) {
    val state by viewModel.dashboardState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onNavigateToAnalytics) {
                Text("View History", fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = "Pulxo Step Intelligence",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 48.dp)
        )

        // Real-time Progress Ring
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.primaryContainer
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = secondaryColor, style = Stroke(width = 20.dp.toPx()))
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = (state.currentSteps / 10000f) * 360f,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${state.currentSteps}",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Today's Steps",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(title = "Distance", value = String.format("%.2f km", state.distanceMeters / 1000f))
            StatCard(title = "Calories", value = String.format("%.0f kcal", state.caloriesBurned))
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { 
                val intent = Intent(context, StepTrackingService::class.java)
                context.startForegroundService(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Resume Tracking", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { 
                val intent = Intent(context, StepTrackingService::class.java).apply {
                    action = StepTrackingService.ACTION_STOP_SERVICE
                }
                context.startService(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Pause Tracking", fontSize = 18.sp)
        }
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 14.sp)
        }
    }
}
