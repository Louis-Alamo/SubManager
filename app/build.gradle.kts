plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.submanager"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.submanager"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Charts
    implementation(libs.mpandroidchart)

    // Image Loading
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Shimmer Effect
    implementation(libs.shimmer)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}