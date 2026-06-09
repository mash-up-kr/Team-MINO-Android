plugins {
    alias(libs.plugins.mino.android.application.compose)
    alias(libs.plugins.mino.android.hilt)
    alias(libs.plugins.mino.android.flavor)
    alias(libs.plugins.mino.android.firebase)
}

android {
    namespace = "team.mino"

    defaultConfig {
        applicationId = "team.mino"
        versionCode = (project.findProperty("versionCode") as? String)?.toIntOrNull() ?: 1
        versionName = (project.findProperty("versionName") as? String) ?: "1.0"
    }

    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(project(":core:common:kotlin"))
    implementation(project(":core:common:android"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:design-system"))
    implementation(project(":core:common:ui"))
    implementation(project(":feature:sample:impl"))
    implementation(project(":feature:sample:api"))
    implementation(project(":feature:home:impl"))
    implementation(project(":feature:home:api"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}
