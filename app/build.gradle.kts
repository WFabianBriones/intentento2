plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

android {
    namespace = "com.example.ergonomic"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ergonomic"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // 🆕 NUEVO: Configuración para TensorFlow Lite
        // Evita errores de compresión del modelo .tflite
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // 🆕 NUEVO: Configuración para evitar compresión de .tflite
    // TensorFlow Lite necesita que el modelo NO sea comprimido
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // No comprimir archivos .tflite
            pickFirsts += "**/*.tflite"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // 🆕 CRÍTICO: Iconos de Material (necesario para MenuLauncherActivity)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // Activity y Navigation
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // TensorFlow Lite (ML)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    // 🆕 OPCIONAL: TensorFlow Lite GPU Delegate (para mejor rendimiento)
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")

    // JSON parsing (Knowledge Base)
    implementation("com.google.code.gson:gson:2.10.1")

    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.google.android.material:material:1.12.0")

    // 🆕 RECOMENDADO: Debug tools (solo en desarrollo)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}