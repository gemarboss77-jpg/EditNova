// Top-level build file. Declares plugin versions used by sub-modules (currently just :app).
// We don't apply these plugins here — each module applies what it needs.
plugins {
    id("com.android.application") version "8.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    // Google Services plugin is required for Firebase. It's declared here so it's ready
    // to use later, but it is NOT applied in app/build.gradle.kts yet (see Step 1 README).
    id("com.google.gms.google-services") version "4.4.2" apply false
}
