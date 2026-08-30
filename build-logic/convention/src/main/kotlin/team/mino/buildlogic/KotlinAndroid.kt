package team.mino.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

// AGP 9에서 CommonExtension의 block 메서드(compileSdk/defaultConfig/compileOptions/buildTypes)가
// ApplicationExtension/LibraryExtension 등 concrete 타입으로 이동되어, 공통 베이스로 단일화 불가.
internal fun Project.configureKotlinAndroid(extension: ApplicationExtension) {
    with(extension) {
        compileSdk = intVersion("compileSdk")
        defaultConfig {
            minSdk = intVersion("minSdk")
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
    configureKotlinAndroidCommon()
}

internal fun Project.configureKotlinAndroid(extension: LibraryExtension) {
    with(extension) {
        compileSdk = intVersion("compileSdk")
        defaultConfig {
            minSdk = intVersion("minSdk")
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        buildTypes {
            named("release") {
                isMinifyEnabled = false
            }
        }
    }
    configureKotlinAndroidCommon()
}

private fun Project.configureKotlinAndroidCommon() {
    extensions.configure(KotlinAndroidProjectExtension::class.java) {
        jvmToolchain(17)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}
