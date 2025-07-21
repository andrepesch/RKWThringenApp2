package com.example.rkwthringenapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.rkwthringenapp.data.ApiClient // <-- NEUER IMPORT
import com.example.rkwthringenapp.data.RkwFormData
import com.example.rkwthringenapp.ui.AppNavigation
import com.example.rkwthringenapp.ui.RkwFormViewModel
import com.example.rkwthringenapp.ui.theme.RKWThüringenAppTheme
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val rkwViewModel: RkwFormViewModel by viewModels()

    // HINWEIS: Die lokale client-Definition wurde entfernt. Wir verwenden jetzt ApiClient.client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RKWThüringenAppTheme {
                AppNavigation(
                    onSendClick = {
                        lifecycleScope.launch {
                            sendDataToServer(rkwViewModel.uiState.value)
                        }
                    }
                )
            }
        }
    }

    private suspend fun sendDataToServer(formData: RkwFormData) {
        val url = "https://formpilot.eu/process_form.php"

        try {
            // Wir verwenden jetzt den zentralen ApiClient
            val response: HttpResponse = ApiClient.client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(formData)
            }

            if (response.status == HttpStatusCode.OK) {
                Toast.makeText(this@MainActivity, "Anfrage erfolgreich versendet!", Toast.LENGTH_LONG).show()
                rkwViewModel.startNewForm()
            } else {
                val errorBody = response.bodyAsText()
                Toast.makeText(this@MainActivity, "Fehler vom Server: ${response.status.description}\n$errorBody", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "Senden fehlgeschlagen: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}