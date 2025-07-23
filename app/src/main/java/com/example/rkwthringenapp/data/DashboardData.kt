package com.example.rkwthringenapp.data

import kotlinx.serialization.Serializable

// Angepasst mit Standardwerten, um die App robuster zu machen
@Serializable
data class FormSummary(
    val id: Int,
    val status: String,
    val companyName: String,
    val address: String = "", // Standardwert: leerer Text
    val mainContact: String = "", // Standardwert: leerer Text
    val scopeInDays: Int = 0, // Standardwert: 0
    val dailyRate: Int = 0, // Standardwert: 0
    val updated_at: String
)

// LoginResponse bleibt unverändert
@Serializable
data class LoginResponse(
    val status: String,
    val message: String,
    val berater_id: Int? = null
)