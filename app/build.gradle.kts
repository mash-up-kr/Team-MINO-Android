import java.util.Properties

plugins {
    alias(libs.plugins.mino.android.application.compose)
    alias(libs.plugins.mino.android.hilt)
    alias(libs.plugins.mino.android.flavor)
    alias(libs.plugins.mino.android.firebase)
}

// Maps SDK 키는 VCS에 올리지 않는 local.properties(MAPS_API_KEY)에서 읽어 Manifest placeholder로 주입한다.
val mapsApiKey: String =
    Properties()
        .apply {
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localPropertiesFile.inputStream().use { load(it) }
            }
        }.getProperty("MAPS_API_KEY", "")

android {
    namespace = "team.mino"

    defaultConfig {
        applicationId = "com.mino.gguk"
        versionCode = (project.findProperty("versionCode") as? String)?.toIntOrNull() ?: 1
        versionName = (project.findProperty("versionName") as? String) ?: "1.0"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    // 진입형 feature만 등록한다. 탭 feature는 셸(:feature:main)을 통해 들어온다.
    implementation(project(":feature:sample"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:roomform"))
    implementation(project(":feature:main"))

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
    implementation(libs.firebase.crashlytics)
}
