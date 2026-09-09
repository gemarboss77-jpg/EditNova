# EditNova — Step 1: Foundation

EditNova is a professional Android video editing app. **This repository currently
contains only Step 1**: the foundational app shell — no real video editing exists yet.

---

## 1. How to run this project

**Requirements:**
- [Android Studio](https://developer.android.com/studio) (Koala/2024.1 or newer recommended)
- JDK 17 (Android Studio bundles this automatically — you don't need to install it separately)
- An Android emulator or a physical Android device (Android 7.0 / API 24 or higher)

**Steps:**
1. Open Android Studio → **File → Open** → select the `EditNova` folder (the one
   containing `settings.gradle.kts`).
2. Let Android Studio sync the project. On first open it will:
   - Download the Gradle 8.7 distribution referenced in
     `gradle/wrapper/gradle-wrapper.properties`
   - Download the Android Gradle Plugin, Kotlin, Compose, and Navigation dependencies
     listed in `app/build.gradle.kts`
   - This requires an internet connection and may take a few minutes the first time.
3. Once sync finishes, click the green **Run ▶** button with an emulator or device
   selected.
4. The app should launch showing: **Splash → Onboarding (swipe through 3 slides) → Home**.

> **Note on this build being unverified by me:** the environment I built this project
> in has no Android SDK, no Gradle binary, and no internet access, so I could not
> actually run `./gradlew build` myself. I have manually reviewed every Kotlin and
> XML file for syntax errors, import correctness, matching package names, and balanced
> braces/parentheses, and fixed every issue I found (see the "Errors found and fixed"
> section in my report to you). But the very first real compile has to happen on your
> machine in Android Studio — please let me know immediately if you hit any build error
> and paste it to me so I can fix it.

---

## 2. Project structure

```
EditNova/
├── app/
│   ├── build.gradle.kts          # App-level dependencies & Android config
│   ├── proguard-rules.pro        # (placeholder, unused while minify is off)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/                  # XML resources (strings, colors, launcher icon)
│       └── java/com/editnova/app/
│           ├── EditNovaApplication.kt   # App entry point (Firebase init goes here later)
│           ├── MainActivity.kt          # Single Activity hosting all Compose screens
│           │
│           ├── core/                    # Shared code used across features
│           │   ├── navigation/          # Screen routes + NavHost wiring
│           │   ├── theme/               # Dark-first color palette, typography
│           │   ├── ui/components/       # Reusable UI: logo, gradient button, cards
│           │   └── di/                  # Simple manual dependency container
│           │
│           ├── domain/                  # Business rules & models (no Android/UI code)
│           │   ├── subscription/        # Free/Premium plan model + repository interface
│           │   ├── watermark/           # Watermark decision logic (Free = watermark)
│           │   └── project/             # Video project data model (placeholder)
│           │
│           ├── data/                    # Implementations of domain interfaces
│           │   └── subscription/        # In-memory fake repo (always returns Free)
│           │
│           └── feature/                 # One package per screen
│               ├── splash/
│               ├── onboarding/
│               └── home/
│
├── build.gradle.kts               # Root Gradle config (plugin versions)
├── settings.gradle.kts            # Module declarations
└── gradle/wrapper/                # Gradle wrapper configuration
```

**Why this structure?** `core` / `domain` / `data` / `feature` is a standard, scalable
layering used in production Android apps:
- **feature/** — one folder per screen. Easy to find, easy to delete/replace without
  touching other screens.
- **domain/** — plain Kotlin business rules (e.g. "who gets a watermark") with no
  Android or UI dependencies, so they're simple to test and to reuse later in a
  background export process.
- **data/** — the actual implementations (today: fake/in-memory; later: Firebase,
  Google Play Billing) behind the `domain` interfaces, so swapping them doesn't
  require touching UI code.
- **core/** — things every feature shares (theme, navigation, reusable buttons/cards).

---

## 3. What has been completed in Step 1

- ✅ Android project created (Kotlin + Jetpack Compose + Material 3)
- ✅ Dark-first, premium-styled visual theme (violet → cyan gradient accent)
- ✅ Navigation: **Splash → Onboarding → Home**, using Jetpack Navigation Compose
- ✅ Splash screen: logo placeholder, "EditNova" name, fade/scale-in animation
- ✅ Onboarding screen: 3 swipeable slides + Skip button
- ✅ Home screen placeholders: New Project, Recent Projects (empty state), AI Tools,
  Templates, Settings — the last three are visibly present but **disabled** (tapping
  does nothing) since they aren't built yet
- ✅ Reusable components: `EditNovaLogo`, `GradientButton`, `FeatureCard`
- ✅ Subscription **architecture** (data model + repository interface): Free plan,
  Premium plan, Monthly/Yearly billing period — **no real billing/payment code**
- ✅ Watermark **policy architecture**: a `WatermarkPolicy` class that can answer
  "should this export get a watermark?" based on the user's plan — **not connected to
  any real video export, because no export feature exists yet**
- ✅ Firebase **preparation only**: the Google Services Gradle plugin and Firebase
  library versions are written into the Gradle files but commented out/not applied,
  and no `google-services.json` is included — **no login, no accounts, no Firebase
  calls happen anywhere in this codebase**

## 4. What will be built in Step 2 (not started)

- Firebase project connection + user accounts / login (still not real auth yet — that
  comes after)
- Video import from device storage
- Actual project creation/storage so "Recent Projects" shows real data
- Beginning of the video editing engine (trim/split/merge)

---

## 5. Errors found and fixed during review

While reviewing the code before calling Step 1 done, these real issues were found and corrected:

1. **`app/src/main/res/values/strings.xml`** and **`themes.xml`** were both missing
   their closing `</resources>` tag — this would have broken the Android resource
   compiler immediately. Fixed.
2. **`OnboardingScreen.kt`** imported `androidx.compose.ui.background`, which does not
   exist (`Modifier.background()` actually lives in `androidx.compose.foundation`).
   This would have caused an "unresolved reference" compile error. Fixed.
3. **`OnboardingScreen.kt`** had a private helper function named `Row(...)`, shadowing
   Compose's own `Row` layout — confusing and a risk for future naming collisions.
   Renamed to `PageIndicator`.
4. **`OnboardingScreen.kt`**: the "Next" button on non-final onboarding slides did
   nothing (only "Get Started" worked). Fixed by advancing the pager with
   `pagerState.animateScrollToPage(...)`.
5. **`Theme.kt`** had `darkTheme: Boolean = isSystemInDarkTheme() || true`, which is
   dead code (always evaluates true) and calls an unnecessary Composable. Simplified
   to `darkTheme: Boolean = true` with an explanatory comment.
6. Removed an unused `PlayArrow` icon import in `HomeScreen.kt` and a comment in the
   same file that referenced a `HomeViewModel.kt` file that doesn't exist yet.

---

## 6. Free vs. Premium (for context, not yet enforced anywhere)

| | Free | Premium |
|---|---|---|
| Export | With EditNova watermark | No watermark |
| Billing | — | Monthly or Yearly |

This table describes the **intended** product behavior. As of Step 1, there is no
export feature and no billing integration — this is documentation of the plan the
architecture (`domain/subscription/`, `domain/watermark/`) was built to support.
