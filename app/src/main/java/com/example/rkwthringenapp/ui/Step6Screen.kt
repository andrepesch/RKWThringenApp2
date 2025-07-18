package com.example.rkwthringenapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
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

    val universalLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedUri: Uri? = result.data?.data
            if (selectedUri != null) {
                viewModel.addDocument(selectedUri)
            } else {
                tempImageUri?.let { viewModel.addDocument(it) }
            }
        }
    }

    Scaffold(topBar = { RkwAppBar() }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            ProgressStepper(currentStep = 6, stepLabels = stepLabels)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Dokumente & Versand", style = MaterialTheme.typography.titleLarge)
            InfoBox(text = "Bitte fügen Sie je nach Unternehmensform benötigte Dokumente hinzu (z.B. Gewerbeanmeldung, Gesellschaftsvertrag etc.).")

            Button(
                onClick = {
                    val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "application/pdf"))
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }

                    val tempFile = File.createTempFile("temp_image_", ".jpg", context.cacheDir)
                    val uri = FileProvider.getUriForFile(
                        Objects.requireNonNull(context),
                        context.packageName + ".provider", tempFile
                    )
                    tempImageUri = uri
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    }

                    val chooserIntent = Intent.createChooser(galleryIntent, "Dokument auswählen")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

                    universalLauncher.launch(chooserIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dokument hochladen")
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
                        Text(Uri.parse(uriString).path?.substringAfterLast(':')?.substringAfterLast('/') ?: "Unbekannte Datei", modifier = Modifier.weight(1f))
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { navController.popBackStack() }) { Text("Zurück") }
                Button(onClick = onSendClick, enabled = formData.hasAcknowledgedPublicationObligations) { Text("Anfrage Senden") }
            }
        }
    }
}

// DIESE FUNKTION HATTE GEFEHLT
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