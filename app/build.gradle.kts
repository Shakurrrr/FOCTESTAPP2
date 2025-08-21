plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.foc_test_app"

    // Stable, widely supported SDK levels
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.foc_test_app"
        minSdk = 21
        targetSdk = 34

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

    // You’re using XML layouts + Fragments (not Compose)
    buildFeatures {
        viewBinding = true
        // compose = false  // (default; keep it off)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core + AppCompat
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Fragments (for Fragment(), replace transactions, etc.)
    implementation("androidx.fragment:fragment-ktx:1.8.2")

    // Material Components (BottomNavigationView)
    implementation("com.google.android.material:material:1.12.0")

    // Layout libs used by your screens
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("com.google.android.material:material:1.12.0")

    // Optional: Activity KTX helpers
    implementation("androidx.activity:activity-ktx:1.9.2")

    // Test deps (keep simple)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
