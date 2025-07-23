package com.example.rkwthringenapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
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
    authViewModel: AuthViewModel // Hinzugefügt, um die berater_id zu bekommen
) {
    val formData by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()

    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPublicationInfoDialog by remember { mutableStateOf(false) }

    // Zeigt eine Bestätigung nach dem Speichern/Senden und navigiert zurück
    LaunchedEffect(saveStatus) {
        if (saveStatus != null) {
            // Navigiere zum Dashboard zurück
            navController.navigate("dashboard") {
                // Löscht den gesamten Formularverlauf aus dem Backstack
                popUpTo("dashboard") { inclusive = true }
            }
            // Setzt den Status zurück, um eine Endlosschleife zu vermeiden
            viewModel.clearSaveStatus()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let { viewModel.addDocument(it) }
            }
        }
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.addDocument(it) }
        }
    )

    fun launchCamera() {
        val tempFile = File.createTempFile("temp_image_", ".jpg", context.cacheDir)
        val uri = FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            context.packageName + ".provider", tempFile
        )
        tempImageUri = uri
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        }
    }

    if (showPublicationInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPublicationInfoDialog = false },
            title = { Text("Publizitätspflichten") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Einzuhaltende Publizitätspflichten im Rahmen der Thüringer Beratungsrichtlinie",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Grundlage der Publizitätspflicht",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Zur Erfüllung und Einhaltung der Publizitätspflicht sind alle Antragstellenden gemäß Artikel 47 und Artikel 50 Abs. 1 i.V.m. Anhang IX der Verordnung (EU) 2021/1060 verpflichtet. Während der Durchführung eines ESF Plus geförderten Vorhaben ist über die Förderung aus dem ESF Plus zu informieren und in der Öffentlichkeitsarbeit sichtbar zu machen.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // ... (restlicher Text)
                }
            },
            confirmButton = {
                Button(onClick = { showPublicationInfoDialog = false }) {
                    Text("Schließen")
                }
            }
        )
    }

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

            // Logik für benötigte Dokumente (unverändert)
            val requiredDocs = when (formData.legalForm) {
                "Einzelunternehmen", "GbR" -> listOf("Gewerbeanmeldung / -ummeldung", "Gesellschaftsvertrag (bei GbR)")
                else -> emptyList()
            }
            if (requiredDocs.isNotEmpty()) {
                Text("Benötigte Anlagen für Ihre Rechtsform:", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        requiredDocs.forEach { doc ->
                            Text("• $doc", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Buttons für Dokumenten-Upload (unverändert)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { filePickerLauncher.launch("application/pdf, image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dokument auswählen")
                }
                Button(
                    onClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> launchCamera()
                            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dokument fotografieren")
                }
            }

            // Angehängte Dokumente (unverändert)
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
                        Text(Uri.parse(uriString).path?.substringAfterLast(':')?.substringAfterLast('/') ?: "Unbekannte Datei", modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.removeDocument(uriString) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Dokument entfernen")
                        }
                    }
                }
            }

            HorizontalDivider()

            // Zusammenfassung (unverändert)
            Text("Zusammenfassung zur Prüfung:", style = MaterialTheme.typography.titleMedium)
            SummaryView(formData = formData)

            // Publizitätspflichten (unverändert)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = formData.hasAcknowledgedPublicationObligations,
                    onCheckedChange = { viewModel.updateAcknowledgement(it) }
                )
                Text("Ich habe die Publizitätsverpflichtungen zur Kenntnis genommen.", style = MaterialTheme.typography.bodySmall, lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.2)
                IconButton(onClick = { showPublicationInfoDialog = true }) {
                    Icon(Icons.Outlined.Info, contentDescription = "Information anzeigen")
                }
            }

            // NEUE, FUNKTIONALE BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { navController.popBackStack() }) { Text("Zurück") }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        authState.beraterId?.let { viewModel.saveForm("entwurf", it) }
                    }) {
                        Text("Als Entwurf speichern")
                    }

                    Button(onClick = {
                        authState.beraterId?.let { viewModel.saveForm("gesendet", it) }
                    }, enabled = formData.hasAcknowledgedPublicationObligations) {
                        Text("Final Senden")
                    }
                }
            }
        }
    }
}

// SummaryView bleibt unverändert
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
    }
}