plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

android {
    namespace = "com.example.rkwthringenapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rkwthringenapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/native-image/reflect-config.json"
            excludes += "META-INF/native-image/resource-config.json"
        }
    }
}

dependencies {
    // Standard-Bibliotheken
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ViewModel für die Datenhaltung
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
    // NEU: Notwendig für viewModelScope (automatisches Speichern)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1")

    // Navigation für die mehrstufige Führung
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // iText7 für die PDF-Erstellung
    implementation("com.itextpdf:itext7-core:8.0.4")
    implementation("com.itextpdf:io:8.0.4")
    implementation("com.itextpdf:forms:8.0.4")
    implementation("com.itextpdf:svg:8.0.4")
    implementation("com.itextpdf:layout:8.0.4")

    // Bibliothek zum Speichern und Laden von Daten (JSON)
    implementation("com.google.code.gson:gson:2.10.1")

    // Test-Bibliotheken
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Ktor Client für Netzwerk-Anfragen
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12") // Die Engine für die Ausführung
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
}