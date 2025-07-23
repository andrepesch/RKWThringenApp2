package com.example.rkwthringenapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
    authViewModel: AuthViewModel
) {
    val formData by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    val context = LocalContext.current

    // Reagiert auf das Speicherergebnis
    when (val result = saveResult) {
        is SaveResult.Loading -> {
            // Zeigt einen Lade-Dialog, der nicht weggeklickt werden kann
            AlertDialog(
                onDismissRequest = { /* Nichts tun */ },
                title = { Text("Speichern...") },
                text = {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator()
                    }
                },
                confirmButton = {}
            )
        }
        is SaveResult.Success -> {
            // Zeigt eine Erfolgsmeldung und navigiert dann zurück
            LaunchedEffect(result) {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                navController.navigate("dashboard") {
                    popUpTo("dashboard") { inclusive = true }
                }
                viewModel.clearSaveResult()
            }
        }
        is SaveResult.Error -> {
            // Zeigt einen Fehler-Dialog
            AlertDialog(
                onDismissRequest = { viewModel.clearSaveResult() },
                title = { Text("Fehler beim Speichern") },
                text = { Text(result.message) },
                confirmButton = {
                    Button(onClick = { viewModel.clearSaveResult() }) {
                        Text("OK")
                    }
                }
            )
        }
        is SaveResult.Idle -> { /* Nichts tun */ }
    }

    // --- Der Rest des Screens bleibt größtenteils gleich ---
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPublicationInfoDialog by remember { mutableStateOf(false) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success -> if (success) { tempImageUri?.let { viewModel.addDocument(it) } } }
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let { viewModel.addDocument(it) } }
    fun launchCamera() { val tempFile = File.createTempFile("temp_image_", ".jpg", context.cacheDir); val uri = FileProvider.getUriForFile(Objects.requireNonNull(context), context.packageName + ".provider", tempFile); tempImageUri = uri; cameraLauncher.launch(uri) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> if (isGranted) { launchCamera() } }

    Scaffold(topBar = { RkwAppBar(title = "Erfassungsbogen") }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            ProgressStepper(currentStep = 6, stepLabels = listOf("Unternehmensdaten", "Ansprechpartner", "Finanzdaten", "Beratung", "Berater", "Abschluss"))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Dokumente & Versand", style = MaterialTheme.typography.titleLarge)
            val requiredDocs = when (formData.legalForm) { "Einzelunternehmen", "GbR" -> listOf("Gewerbeanmeldung / -ummeldung", "Gesellschaftsvertrag (bei GbR)"); else -> emptyList() }
            if (requiredDocs.isNotEmpty()) { Text("Benötigte Anlagen für Ihre Rechtsform:", style = MaterialTheme.typography.titleMedium); Card(modifier = Modifier.fillMaxWidth()) { Column(modifier = Modifier.padding(16.dp)) { requiredDocs.forEach { doc -> Text("• $doc", style = MaterialTheme.typography.bodyMedium) } } } }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { filePickerLauncher.launch("application/pdf, image/*") }, modifier = Modifier.weight(1f)) { Text("Dokument auswählen") }
                Button(onClick = { when (PackageManager.PERMISSION_GRANTED) { ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> launchCamera(); else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA) } }, modifier = Modifier.weight(1f)) { Text("Dokument fotografieren") }
            }
            Text("Angehängte Dokumente:", style = MaterialTheme.typography.titleMedium)
            if (formData.attachedDocuments.isEmpty()) { Text("Keine Dokumente angehängt.", style = MaterialTheme.typography.bodyMedium) } else { formData.attachedDocuments.forEach { uriString -> Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text(Uri.parse(uriString).path?.substringAfterLast(':')?.substringAfterLast('/') ?: "Unbekannte Datei", modifier = Modifier.weight(1f)); IconButton(onClick = { viewModel.removeDocument(uriString) }) { Icon(Icons.Default.Delete, contentDescription = "Dokument entfernen") } } } }
            HorizontalDivider()
            Text("Zusammenfassung zur Prüfung:", style = MaterialTheme.typography.titleMedium)
            SummaryView(formData = formData)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = formData.hasAcknowledgedPublicationObligations, onCheckedChange = { viewModel.updateAcknowledgement(it) })
                Text("Ich habe die Publizitätsverpflichtungen zur Kenntnis genommen.", style = MaterialTheme.typography.bodySmall, lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.2)
                IconButton(onClick = { showPublicationInfoDialog = true }) { Icon(Icons.Outlined.Info, contentDescription = "Information anzeigen") }
            }

            // Buttons, die jetzt durch den Ladezustand deaktiviert werden
            val isLoading = saveResult is SaveResult.Loading
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { navController.popBackStack() }, enabled = !isLoading) { Text("Zurück") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { authState.beraterId?.let { viewModel.saveForm("entwurf", it) } },
                        enabled = !isLoading
                    ) {
                        Text("Als Entwurf speichern")
                    }
                    Button(
                        onClick = { authState.beraterId?.let { viewModel.saveForm("gesendet", it) } },
                        enabled = !isLoading && formData.hasAcknowledgedPublicationObligations
                    ) {
                        Text("Final Senden")
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryView(formData: RkwFormData) {
    // ... (unverändert)
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
    }
}