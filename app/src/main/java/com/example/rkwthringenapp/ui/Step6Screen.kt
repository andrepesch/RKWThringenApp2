package com.example.rkwthringenapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.rkwthringenapp.data.RkwFormData
import java.io.File
import java.util.Objects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step6Screen(
    navController: NavController,
    viewModel: RkwFormViewModel,
    onSendClick: () -> Unit
) {
    val formData by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val stepLabels = listOf("Unternehmensdaten", "Ansprechpartner", "Finanzdaten", "Beratung", "Berater", "Abschluss")

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> uri?.let { viewModel.addDocument(it) } }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success -> if (success) { tempImageUri?.let { viewModel.addDocument(it) } } }
    )

    // HINWEIS: Die Seite wird jetzt auch von einem Scaffold umschlossen
    Scaffold(
        topBar = { RkwAppBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            // HINWEIS: Der ProgressStepper wurde hinzugefügt
            ProgressStepper(currentStep = 6, stepLabels = stepLabels)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Dokumente & Versand", style = MaterialTheme.typography.titleLarge)
            Text(
                "Bitte fügen Sie je nach Unternehmensform benötigte Dokumente hinzu (z.B. Gewerbeanmeldung, Gesellschaftsvertrag etc.).",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val tempFile = File.createTempFile("temp_image_", ".jpg", context.cacheDir)
                    val uri = FileProvider.getUriForFile(
                        Objects.requireNonNull(context),
                        context.packageName + ".provider", tempFile
                    )
                    tempImageUri = uri
                    cameraLauncher.launch(uri)
                }) { Text("📷 Kamera") }
                Button(onClick = { filePickerLauncher.launch("*/*") }) { Text("📂 Dateien") }
            }

            Text("Angehängte Dokumente:", style = MaterialTheme.typography.titleMedium)
            if (formData.attachedDocuments.isEmpty()) {
                Text("Keine Dokumente angehängt.", style = MaterialTheme.typography.bodyMedium)
            } else {
                formData.attachedDocuments.forEach { uriString ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(Uri.parse(uriString).path?.substringAfterLast('/') ?: "Unbekannte Datei", modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.removeDocument(uriString) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Dokument entfernen")
                        }
                    }
                }
            }

            HorizontalDivider()

            Text("Zusammenfassung zur Prüfung:", style = MaterialTheme.typography.titleMedium)
            SummaryView(formData = formData)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = formData.hasAcknowledgedPublicationObligations,
                    onCheckedChange = { viewModel.updateAcknowledgement(it) }
                )
                Text("Ich habe die Publizitätsverpflichtungen zur Kenntnis genommen.", style = MaterialTheme.typography.bodySmall, lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.2)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { navController.popBackStack() }) { Text("Zurück") }
                Button(onClick = onSendClick, enabled = formData.hasAcknowledgedPublicationObligations) { Text("Anfrage Senden") }
            }
        }
    }
}

@Composable
fun SummaryView(formData: RkwFormData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.medium)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Firma: ${formData.companyName}, ${formData.legalForm}")
        Text("Ansprechpartner: ${formData.mainContact.name}")
        Text("E-Mail: ${formData.mainContact.email}")
        // Fügen Sie hier bei Bedarf weitere Felder aus der Zusammenfassung hinzu
    }
}