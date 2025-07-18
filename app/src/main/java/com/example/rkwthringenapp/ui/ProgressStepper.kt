package com.example.rkwthringenapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProgressStepper(
    currentStep: Int,
    stepLabels: List<String>
) {
    val totalSteps = stepLabels.size
    Column(modifier = Modifier.fillMaxWidth()) {
        // Text-Label für den aktuellen Schritt, zentriert über dem Balken
        Text(
            text = stepLabels[currentStep - 1],
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        // Der segmentierte Fortschrittsbalken
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp) // Kleiner Abstand zwischen den Segmenten
        ) {
            for (step in 1..totalSteps) {
                val isCompletedOrCurrent = step <= currentStep

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            // Füllt das Segment mit Orange, wenn es aktiv/erledigt ist
                            color = if (isCompletedOrCurrent) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            // Gibt allen Segmenten einen Rahmen
                            width = 1.dp,
                            color = if (isCompletedOrCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}