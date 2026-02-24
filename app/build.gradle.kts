
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.download.video_download"
    compileSdk  = 36

    defaultConfig {
        applicationId = "com.download.video_download"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
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
    api(libs.media3.ui)
    api(libs.media3.exoplaye)
    api(libs.room.ktx)
    api(libs.room.runtime)
    api(libs.aria.core)
    ksp(libs.room.compiler)
    api(libs.glide)
    ksp(libs.glide.compiler)
    api(libs.aria.m3u8)
    api(libs.multidex)
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
                // 获取应用ID和版本名
                val appId = android.defaultConfig.applicationId
                val versionName = android.defaultConfig.versionName
                val newApkName = "${appId}-${versionName}.apk"
                val newApkFile = File(releaseDir, newApkName)

                if (apkFile.renameTo(newApkFile)) {
                    println("APK renamed to: ${newApkFile.absolutePath}")
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