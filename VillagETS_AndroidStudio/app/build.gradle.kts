plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.villagets_androidstudio"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.villagets_androidstudio"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.viewpager2)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Jackson JSON
    implementation(libs.jackson.databind)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.recyclerview)

    // Glide
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.jackson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}