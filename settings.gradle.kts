pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EditNova"
include(":app")

// NOTE FOR FUTURE STEPS:
// As EditNova grows, heavy feature areas (video engine, AI tools, export pipeline)
// should become their own Gradle modules, e.g.:
//   include(":core:videoengine")
//   include(":feature:export")
// This keeps build times fast and keeps unrelated features from breaking each other.
// For Step 1 we intentionally keep everything in a single ":app" module to stay simple.
