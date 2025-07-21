package com.example.rkwthringenapp.ui

import android.app.Application
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rkwthringenapp.data.ApiClient
import com.example.rkwthringenapp.data.AuthRequest
import com.example.rkwthringenapp.data.LoginResponse
import com.example.rkwthringenapp.data.ServerResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val beraterId: Int? = null // NEU: Speichert die ID des eingeloggten Beraters
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        val loggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val beraterId = sharedPreferences.getInt("berater_id", -1).takeIf { it != -1 }
        _uiState.update { it.copy(isLoggedIn = loggedIn, beraterId = beraterId) }
    }

    fun testConnection() { /* ... unverändert ... */ }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Bitte füllen Sie alle Felder aus.") }
            return
        }
        performAuthRequest("https://formpilot.eu/login.php", email, password, isLogin = true)
    }

    fun register(email: String, password: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(error = "Bitte geben Sie eine gültige E-Mail-Adresse ein.") }
            return
        }
        if (password.length < 8) {
            _uiState.update { it.copy(error = "Das Passwort muss mindestens 8 Zeichen lang sein.") }
            return
        }
        performAuthRequest("https://formpilot.eu/register.php", email, password, isLogin = false)
    }

    private fun performAuthRequest(url: String, email: String, password: String, isLogin: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response: HttpResponse = ApiClient.client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(AuthRequest(email, password))
                }

                if (response.status == HttpStatusCode.OK) {
                    if (isLogin) {
                        val loginResponse: LoginResponse = response.body()
                        sharedPreferences.edit()
                            .putBoolean("isLoggedIn", true)
                            .putInt("berater_id", loginResponse.berater_id ?: -1)
                            .apply()
                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true, beraterId = loginResponse.berater_id) }
                    } else {
                        login(email, password)
                    }
                } else {
                    val serverResponse: ServerResponse = response.body()
                    _uiState.update { it.copy(isLoading = false, error = serverResponse.message) }
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unbekannter Fehler"
                _uiState.update { it.copy(isLoading = false, error = "Client-Fehler: $errorMsg") }
            }
        }
    }

    fun logout() {
        sharedPreferences.edit()
            .putBoolean("isLoggedIn", false)
            .remove("berater_id")
            .apply()
        _uiState.update { AuthUiState() } // Setzt den kompletten Zustand zurück
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null, info = null) }
    }
}