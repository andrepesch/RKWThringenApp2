package com.example.rkwthringenapp.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    // Dieser StateFlow hält den aktuellen Login-Status.
    // `private set` bedeutet, dass nur das ViewModel selbst den Wert ändern kann.
    private val _isLoggedIn = MutableStateFlow(false) // Standardmäßig ist der Nutzer nicht eingeloggt
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    // Eine einfache Funktion, um einen Login zu simulieren.
    fun login(email: String, pass: String) {
        // HIER würde später die echte Überprüfung mit dem Server stattfinden.
        // Fürs Erste setzen wir den Status einfach auf `true`.
        _isLoggedIn.value = true
        // Hier würdest du auch ein Token oder die Benutzer-ID speichern.
    }

    // Funktion zum Ausloggen
    fun logout() {
        _isLoggedIn.value = false
        // Hier würdest du gespeicherte Tokens etc. löschen.
    }
}