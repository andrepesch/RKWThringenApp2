package com.example.rkwthringenapp.data

import kotlinx.serialization.Serializable

// Definiert, wie ein einzelner Eintrag in der Dashboard-Liste aussieht.
// Angepasst an die neue JSON-Antwort von get_forms.php
@Serializable
data class FormSummary(
    val id: Int,
    val status: String,
    val companyName: String,
    val scopeInDays: Int,
    val honorar: Int,
    val updated_at: String
)

// Definiert die neue Antwort vom login.php-Skript
@Serializable
data class LoginResponse(
    val status: String,
    val message: String,
    val berater_id: Int? = null // Die ID des Beraters
)