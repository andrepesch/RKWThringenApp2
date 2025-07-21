package com.example.rkwthringenapp.data

import io.ktor.client.*
// import io.ktor.client.engine.cio.* // Alter Import
import io.ktor.client.engine.okhttp.OkHttp // NEUER IMPORT
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Ein Singleton-Objekt, das eine einzige, app-weite Instanz des Ktor HttpClient bereitstellt.
 * Verwendet jetzt die robustere OkHttp-Engine.
 */
object ApiClient {
    val client = HttpClient(OkHttp) { // HIER IST DIE ÄNDERUNG: CIO -> OkHttp
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }
}