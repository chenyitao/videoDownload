// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.devtools.ksp") version "2.3.4" apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
val Project.applicationId: String
    get() = (project.extensions.getByName("android") as com.android.build.gradle.AppExtension).defaultConfig.applicationId!!

val Project.versionName: String
    get() = (project.extensions.getByName("android") as com.android.build.gradle.AppExtension).defaultConfig.versionName!!