package com.example.rkwthringenapp.ui

import android.Manifest
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
import android.content.pm.PackageManager

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
    var showPublicationInfoDialog by remember { mutableStateOf(false) }

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
                    Text(
                        text = "Anbringen einen A3 Plakates",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ein Plakat im Format A3 ist an einer für die Öffentlichkeit deutlich sichtbaren Stelle in den Räumlichkeiten des Zuwendungsempfängers anzubringen. Das Plakat im A3-Format zu Ihrem Vorhaben erhalten Sie von der Thüringer Aufbaubank mit dem Zuwendungsbescheid.\nDas Plakat beinhaltet allgemeinen Informationen zur Förderung, allerdings keine detaillierten Beratungsinhalte oder sonstige Rückschlüsse auf den individuellen Beratungsgegenstand.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Veröffentlichung auf der Webseite",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Eine kurze Beschreibung des Vorhabens ist ebenfalls auf der Webseite/ Social Media Site des Zuwendungsempfängers (soweit vorhanden) zu veröffentlichen. Die Beschreibung muss auf die Ziele und Ergebnisse des Vorhabens eingehen sowie die finanzielle Unterstützung aus dem ESF Plus und durch das Land Thüringen hervorheben. Darüber hinaus sind neben dem zwingend darzustellenden EU-Logo das Logo des Thüringer Ministeriums für Wirtschaft, Wissenschaft und Digitale Gesellschaft darzustellen. Gleichzeitig ist darauf zu achten, dass das Logo direkt nach Aufrufen der Webseite sichtbar ist ohne dass ein Scrollen nach unten nötig ist.",
                        style = MaterialTheme.typography.bodySmall
                    )
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
            ProgressStepper(currentStep = 6, stepLabels = stepLabels)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Dokumente & Versand", style = MaterialTheme.typography.titleLarge)

            val requiredDocs = when (formData.legalForm) {
                "Einzelunternehmen", "GbR" -> listOf("Gewerbeanmeldung / -ummeldung", "Gesellschaftsvertrag (bei GbR)")
                "e.K.", "Kommanditgesellschaft (KG)", "Offene Handelsgesellschaft (OHG)" -> listOf("Gewerbeanmeldung / -ummeldung")
                "Freie Berufe" -> listOf("Nachweis der Tätigkeit (z.B. Zulassung, Steuernummer)")
                "e. V." -> listOf("Nachweis der wirtschaftlichen Tätigkeit (Gewerbeanmeldung o.Ä.)")
                "GmbH", "GmbH & Co. KG", "UG (haftungsbeschränkt)", "Aktiengesellschaft (AG)", "Limited (Ltd.)", "Ltd. & Co. KG", "Eingetragene Genossenschaft (eG)", "KG auf Aktien (KGaA)", "Partnerschaftsgesellschaft", "Societas Europaea (SE)", "Stiftung" -> listOf("Gesellschaftsvertrag", "Handelsregisterauszug")
                else -> emptyList()
            }
            var documentsWillBeSentLater by remember { mutableStateOf(false) }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        filePickerLauncher.launch("application/pdf, image/*")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dokument auswählen")
                }
                Button(
                    onClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                                launchCamera()
                            }
                            else -> {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dokument fotografieren")
                }
            }

            if (requiredDocs.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = documentsWillBeSentLater,
                        onCheckedChange = { documentsWillBeSentLater = it }
                    )
                    Text("Benötigte Anlagen werden später nachgereicht", style = MaterialTheme.typography.bodySmall)
                }
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
                IconButton(onClick = { showPublicationInfoDialog = true }) {
                    Icon(Icons.Outlined.Info, contentDescription = "Information anzeigen")
                }
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
