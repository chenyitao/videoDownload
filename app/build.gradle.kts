plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.download.video_download"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.download.video_download"
        minSdk = 26
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    api(libs.multidex)
    api(libs.media3.ui)
    api(libs.media3.exoplaye)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}