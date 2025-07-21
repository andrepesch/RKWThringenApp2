package com.example.rkwthringenapp.data

import kotlinx.serialization.Serializable

// Diese Klasse repräsentiert die JSON-Daten, die wir an den Server senden.
@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

// Diese Klasse repräsentiert die JSON-Antwort, die wir vom Server erhalten.
@Serializable
data class ServerResponse(
    val status: String,
    val message: String
)