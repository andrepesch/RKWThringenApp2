package com.example.rkwthringenapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProgressStepper(
    currentStep: Int,
    stepLabels: List<String>
) {
    val totalSteps = stepLabels.size
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Die Reihe mit den Kreisen und Linien
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (step in 1..totalSteps) {
                val isCompleted = step < currentStep
                val isCurrent = step == currentStep
                val circleColor = if (isCompleted || isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

                // Der Kreis für den Schritt
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = circleColor,
                    modifier = Modifier.size(if (isCurrent) 16.dp else 12.dp)
                ) {}

                // Die Verbindungslinie zum nächsten Schritt (außer beim letzten)
                if (step < totalSteps) {
                    val lineColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    HorizontalDivider(
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        thickness = 2.dp,
                        color = lineColor
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Die Beschriftung für den aktuellen Schritt
        Text(
            text = stepLabels[currentStep - 1],
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}