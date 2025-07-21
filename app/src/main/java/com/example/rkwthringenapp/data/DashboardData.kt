package com.example.rkwthringenapp.data

import kotlinx.serialization.Serializable

// Definiert, wie ein einzelner Eintrag in der Dashboard-Liste aussieht.
@Serializable
data class FormSummary(
    val id: Int,
    val companyName: String,
    val legalForm: String,
    val created_at: String
)

// Definiert die neue Antwort vom login.php-Skript
@Serializable
data class LoginResponse(
    val status: String,
    val message: String,
    val berater_id: Int? = null // Die ID des Beraters
)