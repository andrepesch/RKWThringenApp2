package com.example.rkwthringenapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.rkwthringenapp.data.RkwFormData
import com.example.rkwthringenapp.ui.AppNavigation
import com.example.rkwthringenapp.ui.RkwFormViewModel
import com.example.rkwthringenapp.ui.theme.RKWThüringenAppTheme
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    // Wir holen uns das RkwFormViewModel für den Datenversand.
    // Das AuthViewModel wird innerhalb der Navigation geholt.
    private val rkwViewModel: RkwFormViewModel by viewModels()

    // Erstellen des Ktor HTTP-Clients, der für alle Anfragen wiederverwendet wird.
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RKWThüringenAppTheme {
                // Die AppNavigation steuert jetzt den gesamten sichtbaren Inhalt.
                AppNavigation(
                    onSendClick = {
                        // Starte eine Coroutine im LifecycleScope der Activity.
                        lifecycleScope.launch {
                            sendDataToServer(rkwViewModel.uiState.value)
                        }
                    }
                )
            }
        }
    }

    // Diese Funktion muss INNERHALB der MainActivity-Klasse sein.
    private suspend fun sendDataToServer(formData: RkwFormData) {
        val url = "https://formpilot.eu/process_form.php"

        try {
            val response: HttpResponse = client.post(url) {
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
