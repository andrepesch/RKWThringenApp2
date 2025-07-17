package com.example.rkwthringenapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import com.example.rkwthringenapp.data.RkwFormData
import com.example.rkwthringenapp.ui.AppNavigation
import com.example.rkwthringenapp.ui.RkwFormViewModel
import com.example.rkwthringenapp.ui.theme.RKWThüringenAppTheme
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: RkwFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RKWThüringenAppTheme {
                AppNavigation(
                    viewModel = viewModel,
                    onSendClick = { createAndSendEmail(viewModel.uiState.value) }
                )
            }
        }
    }

    private fun createAndSendEmail(formData: RkwFormData) {
        val pdfFile = File(cacheDir, "erfassungsbogen.pdf").also { file ->
            val writer = PdfWriter(file)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            document.add(Paragraph("Erfassungsbogen für: ${formData.companyName}"))
            document.add(Paragraph("Ansprechpartner: ${formData.mainContact.name}"))
            document.add(Paragraph("E-Mail: ${formData.mainContact.email}"))
            document.close()
        }

        val pdfUri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", pdfFile)

        // ÄNDERUNG: Wir wandeln die gespeicherten Strings zurück in Uris um
        val attachmentUris = ArrayList<Uri>().apply {
            add(pdfUri)
            formData.attachedDocuments.forEach { uriString ->
                add(Uri.parse(uriString))
            }
        }

        val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("pesch@rkw-thueringen.de"))
            putExtra(Intent.EXTRA_CC, arrayOf(formData.mainContact.email))
            putExtra(Intent.EXTRA_SUBJECT, "Neue Beratungsanfrage: ${formData.companyName}")
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachmentUris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(this, "Keine E-Mail-App gefunden.", Toast.LENGTH_SHORT).show()
        }
    }
}