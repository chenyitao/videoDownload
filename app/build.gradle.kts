plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.serialization)
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
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "DEBUG_MODE", "false")
        }
        debug {
            buildConfigField("boolean", "DEBUG_MODE", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
    bundle {
        language{
            enableSplit = false
        }
    }
    buildFeatures {
        buildConfig = true
    }
}
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.lottie)
    implementation(libs.kotlin.serialization.json)
    ksp(libs.aria.compile)
    api(libs.media3.ui)
    api(libs.media3.exoplaye)
    api(libs.room.ktx)
    api(libs.room.runtime)
    api(libs.aria.core)
    ksp(libs.room.compiler)
    api(libs.glide)
    ksp(libs.glide.compiler)
    api(libs.aria.m3u8)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}