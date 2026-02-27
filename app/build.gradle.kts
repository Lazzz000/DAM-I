plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.nexushardware.app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.nexushardware.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    //agrego para el binding
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Librería Glide para carga de imágenes
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // Retrofit para consumir la API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Convertidor Gson para transformar el JSON a Data Classes de Kotlin
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Corrutinas para llamadas asíncronas (para no congelar la pantalla Dark Tech)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}