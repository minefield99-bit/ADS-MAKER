import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Read the fal.ai API key from local.properties (never committed) so the secret
// stays out of source control. Falls back to an empty string; the app surfaces a
// clear "missing key" state at runtime instead of crashing.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val falApiKey: String = (localProperties.getProperty("FAL_API_KEY") ?: "").trim()

android {
    namespace = "com.adsmaker.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.adsmaker.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-week1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Exposed to code via BuildConfig.FAL_API_KEY. Escaped as a string literal.
        buildConfigField("String", "FAL_API_KEY", "\"$falApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    implementation(libs.coil.compose)
    implementation(libs.coil.video)

    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
