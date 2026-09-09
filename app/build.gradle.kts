plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Firebase plugin is intentionally NOT applied yet.
    // When we add login/accounts in a later step, uncomment this line AND
    // add your real google-services.json file into the app/ folder:
    // id("com.google.gms.google-services")
}

android {
    namespace = "com.editnova.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.editnova.app"
        minSdk = 24        // Covers ~98% of active Android devices, needed for modern media APIs
        targetSdk = 34
        versionCode = 1
        versionName = "0.5.0-step4b"

        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Will enable + configure ProGuard rules before real release
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            // applicationIdSuffix removed for Step 1 simplicity; can be re-added later
            // to allow installing debug + release builds side by side.
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
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- Core Android / Kotlin ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    // --- Jetpack Compose (UI toolkit) ---
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // --- Navigation between screens ---
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // --- Video playback (ExoPlayer, via AndroidX Media3) ---
    // Used only by the Editor Preview screen (Step 3) to play back a picked video.
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1") // Provides PlayerView, embedded via AndroidView.

    // --- Image loading (Coil) ---
    // Needed to safely display an arbitrary picked photo (any size) from a content URI
    // in Compose without manually decoding bitmaps on the main thread.
    implementation("io.coil-kt:coil-compose:2.6.0")

    // --- Testing (foundation for later steps) ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ------------------------------------------------------------------
    // FIREBASE — PREPARED BUT NOT YET ADDED (intentionally commented out)
    // ------------------------------------------------------------------
    // Uncomment these, add app/google-services.json, and apply the
    // com.google.gms.google-services plugin above when we build
    // user accounts / login in a later step.
    //
    // implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    // implementation("com.google.firebase:firebase-auth-ktx")
    // implementation("com.google.firebase:firebase-firestore-ktx")
    // implementation("com.google.firebase:firebase-analytics-ktx")

    // ------------------------------------------------------------------
    // BILLING — PREPARED FOR FUTURE SUBSCRIPTION STEP (not added yet)
    // ------------------------------------------------------------------
    // implementation("com.android.billingclient:billing-ktx:7.0.0")
}
