package com.example.rkwthringenapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rkwthringenapp.data.ApiClient
import com.example.rkwthringenapp.data.FormSummary
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Hält den Zustand für den Dashboard-Bildschirm
data class DashboardUiState(
    val forms: List<FormSummary> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Funktion zum Laden der Formulare vom Server
    fun loadForms(beraterId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Ruft das get_forms.php Skript auf und übergibt die Berater-ID
                val formsList: List<FormSummary> = ApiClient.client.get("https://formpilot.eu/get_forms.php") {
                    parameter("berater_id", beraterId)
                }.body()
                _uiState.update { it.copy(isLoading = false, forms = formsList) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Fehler beim Laden der Formulare: ${e.message}") }
            }
        }
    }
}