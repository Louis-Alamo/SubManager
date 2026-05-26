plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.submanager"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.submanager"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SUPABASE_URL", "\"https://vldcmehfryzztsnsyqgn.supabase.co\"")
        buildConfigField("String", "SUPABASE_KEY", "\"sb_publishable_BxBq_SjBjU2mA2JMOQ8upw_TINKs-6x\"")
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
        buildConfig = true
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


    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)


    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)


    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)


    implementation("androidx.work:work-runtime:2.9.0")


    implementation(libs.mpandroidchart)


    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)


    implementation(libs.shimmer)


    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}