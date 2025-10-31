plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.nt118_marketingapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nt118_marketingapp"
        minSdk = 29
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
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Material Design Components (ô chọn ngày, button, card, textfield)
    implementation("com.google.android.material:material:1.11.0")
// MPAndroidChart - vẽ biểu đồ cột, tròn, đường, cột ngang
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
// RecyclerView hiển thị dạng danh sách/bảng
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // Firebase
    implementation("com.google.firebase:firebase-database:20.3.0")
}