import java.util.Properties
import java.io.FileInputStream

val localProps = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.example.leadrive"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.leadrive"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProps = Properties().apply {
            load(FileInputStream(rootProject.file("local.properties")))
        }

        buildConfigField(
            "String",
            "MIDTRANS_SERVER_KEY",
            "\"${localProps["MIDTRANS_SERVER_KEY"]}\""
        )




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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }



}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-compose:2.11.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")




    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:realtime-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:storage-kt:3.2.6")
    implementation("io.ktor:ktor-client-android:3.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("org.osmdroid:osmdroid-android:6.1.17")
    implementation("com.github.MKergall:osmbonuspack:6.9.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation(libs.androidx.navigation.runtime.ktx)
//    implementation(libs.androidx.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}