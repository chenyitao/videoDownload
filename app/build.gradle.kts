import org.gradle.language.nativeplatform.internal.Dimensions.applicationVariants

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
    id("com.google.firebase.firebase-perf")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.download.video_download"
    compileSdk  = 36

    defaultConfig {
        applicationId = "com.t.a"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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
        buildConfig = true
    }
    bundle {
        language{
            enableSplit = false
        }
    }
}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
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
    implementation(libs.media3.ui)
    implementation(libs.media3.exoplaye)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    implementation(libs.aria.core)
    ksp(libs.room.compiler)
    implementation(libs.glide)
    ksp(libs.glide.compiler)
    implementation(libs.aria.m3u8)
    implementation(libs.play.services.ads)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.perf)
    implementation(libs.firebase.crashlytics)
    implementation(libs.facebook.android.sdk)
    implementation(libs.multidex)
    implementation(libs.firebase.analytics)
    implementation(libs.installreferrer)
    implementation(libs.af.android.sdk)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
tasks.register("renameApk") {
    doLast {
        val releaseDir = File(project.projectDir, "release")
        if (releaseDir.exists()) {
            releaseDir.listFiles { file ->
                file.name.endsWith(".apk") && file.name.contains("release")
            }?.forEach { apkFile ->
                val appId = android.defaultConfig.applicationId
                val versionName = android.defaultConfig.versionName
                val newApkName = "${appId}-${versionName}.apk"
                val newApkFile = File(releaseDir, newApkName)

                if (apkFile.renameTo(newApkFile)) {
                    println("APK renamed to: ${newApkFile.absolutePath}")
                }
            }

            releaseDir.listFiles { file ->
                file.name.endsWith(".aab") && file.name.contains("release")
            }?.forEach { aabFile ->
                val appId = android.defaultConfig.applicationId
                val versionName = android.defaultConfig.versionName
                val newAabName = "${appId}-${versionName}.aab"
                val newAabFile = File(releaseDir, newAabName)

                if (aabFile.renameTo(newAabFile)) {
                    println("AAB renamed to: ${newAabFile.absolutePath}")
                }
            }
        }
    }
}

tasks.whenTaskAdded {
    if (name == "assembleRelease") {
        finalizedBy("renameApk")
    }
}