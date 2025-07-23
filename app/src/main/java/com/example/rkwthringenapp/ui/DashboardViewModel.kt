package com.example.rkwthringenapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rkwthringenapp.data.ApiClient
import com.example.rkwthringenapp.data.FormSummary
import com.example.rkwthringenapp.data.ServerResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Zustand für die Dashboard-Daten
data class DashboardUiState(
    val forms: List<FormSummary> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

// Zustand für die "Teilen"-Aktion
sealed class ShareResult {
    object Idle : ShareResult()
    object Loading : ShareResult()
    data class Success(val message: String) : ShareResult()
    data class Error(val message: String) : ShareResult()
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // NEU: Zustand für die "Teilen"-Aktion
    private val _shareResult = MutableStateFlow<ShareResult>(ShareResult.Idle)
    val shareResult: StateFlow<ShareResult> = _shareResult.asStateFlow()

    fun loadForms(beraterId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val formsList: List<FormSummary> = ApiClient.client.get("https://formpilot.eu/get_forms.php") {
                    parameter("berater_id", beraterId)
                }.body()
                _uiState.update { it.copy(isLoading = false, forms = formsList) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Fehler beim Laden der Formulare: ${e.message}") }
            }
        }
    }

    // NEU: Funktion, um ein Formular mit dem Kunden zu teilen
    fun shareForm(formId: Int) {
        viewModelScope.launch {
            _shareResult.value = ShareResult.Loading
            try {
                val response: ServerResponse = ApiClient.client.get("https://formpilot.eu/share_form.php") {
                    parameter("form_id", formId)
                }.body()

                if (response.status == "success") {
                    _shareResult.value = ShareResult.Success(response.message)
                } else {
                    _shareResult.value = ShareResult.Error(response.message)
                }
            } catch (e: Exception) {
                _shareResult.value = ShareResult.Error("Client-Fehler: ${e.message}")
            }
        }
    }

    // NEU: Setzt den "Teilen"-Zustand zurück
    fun clearShareResult() {
        _shareResult.value = ShareResult.Idle
    }
}