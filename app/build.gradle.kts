/**
 * @file app/build.gradle.kts
 * @brief Konfiguracja modułu Android aplikacji OffGrid.
 * @details Definiuje wersje SDK, View Binding, wariant wydania oraz biblioteki kont i wyszukiwania.
 */

/**
 * @brief Włącza moduł aplikacji Android i integrację konfiguracji projektu Firebase.
 */
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

/**
 * @brief Ustawia przestrzeń nazw, wersje SDK oraz opcje kompilacji modułu.
 */
android {
    namespace = "com.example.offgrid"
    compileSdk {
        version = release(37)
    }

/**
 * @brief Włącza generowanie powiązań widoków używanych przez aktywności i adapter.
 */
buildFeatures{
    viewBinding = true
}
    /**
     * @brief Definiuje identyfikator aplikacji, obsługiwane API i konfigurację testów.
     */
    defaultConfig {
        applicationId = "com.example.offgrid"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    /**
     * @brief Konfiguruje optymalizację wariantu release.
     */
    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    /**
     * @brief Ustawia zgodność źródeł i kodu wynikowego z Java 11.
     */
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

/**
 * @brief Deklaruje biblioteki interfejsu, uwierzytelniania, Firestore oraz testowania.
 */
dependencies {
    implementation("androidx.biometric:biometric:1.1.0")
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    /**
     * @brief Uzgadnia wersje bibliotek Firebase za pomocą wspólnego BoM.
     */
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-auth")
    /**
     * @brief Dodaje zapis profili oraz zapytania do katalogu użytkowników Firestore.
     */
    implementation("com.google.firebase:firebase-firestore")
    /**
     * @brief Dodaje komponent listy wyników i mechanizm porównywania jej elementów.
     */
    implementation("androidx.recyclerview:recyclerview:1.4.0")
}
