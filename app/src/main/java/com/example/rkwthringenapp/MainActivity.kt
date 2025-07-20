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

    private val viewModel: RkwFormViewModel by viewModels()

    // Erstellen des Ktor HTTP-Clients, der für alle Anfragen wiederverwendet wird.
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true // Wichtig, damit die App nicht abstürzt, wenn der Server unbekannte Felder sendet.
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RKWThüringenAppTheme {
                AppNavigation(
                    viewModel = viewModel,
                    onSendClick = {
                        // Starte eine Coroutine im LifecycleScope der Activity.
                        // Das sorgt dafür, dass die Anfrage sauber abgebrochen wird, wenn die App geschlossen wird.
                        lifecycleScope.launch {
                            sendDataToServer(viewModel.uiState.value)
                        }
                    }
                )
            }
        }
    }

    private suspend fun sendDataToServer(formData: RkwFormData) {
        // Die URL zu deinem PHP-Skript
        val url = "https://formpilot.eu/process_form.php"

        try {
            // Führe eine POST-Anfrage aus
            val response: HttpResponse = client.post(url) {
                // Setze den Content-Type auf application/json
                contentType(ContentType.Application.Json)
                // Hänge das serialisierte formData-Objekt als Body an die Anfrage an
                setBody(formData)
            }

            // Werte die Antwort des Servers aus
            if (response.status == HttpStatusCode.OK) {
                // Zeige eine Erfolgsmeldung an
                Toast.makeText(this@MainActivity, "Anfrage erfolgreich versendet!", Toast.LENGTH_LONG).show()
                // Optional: Formular nach erfolgreichem Versand zurücksetzen
                viewModel.startNewForm()
            } else {
                // Zeige eine Fehlermeldung mit Details vom Server an
                val errorBody = response.bodyAsText()
                Toast.makeText(this@MainActivity, "Fehler vom Server: ${response.status.description}\n$errorBody", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            // Fange generelle Netzwerkfehler ab (z.B. keine Internetverbindung)
            Toast.makeText(this@MainActivity, "Senden fehlgeschlagen: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}