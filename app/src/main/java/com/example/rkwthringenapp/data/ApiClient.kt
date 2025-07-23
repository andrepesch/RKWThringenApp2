package com.example.rkwthringenapp.data

import io.ktor.client.*
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Ein Singleton-Objekt, das eine einzige, app-weite Instanz des Ktor HttpClient bereitstellt.
 */
object ApiClient {
    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true // DIESE ZEILE IST DIE LÖSUNG
            })
        }
    }
}