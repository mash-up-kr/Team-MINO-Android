import java.util.Properties

plugins {
    alias(libs.plugins.mino.android.application.compose)
    alias(libs.plugins.mino.android.hilt)
    alias(libs.plugins.mino.android.flavor)
    alias(libs.plugins.mino.android.firebase)
}

// Maps SDK 키는 VCS에 올리지 않는 local.properties(MAPS_API_KEY)에서 읽어 Manifest placeholder로 주입한다.
// local.properties가 없는 CI에서는 같은 이름의 환경변수(GitHub Secret)로 받는다. 둘 다 없으면 빈 키가 들어가
// 지도만 조용히 안 뜨므로 경고를 남긴다.
val mapsApiKey: String =
    Properties()
        .apply {
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localPropertiesFile.inputStream().use { load(it) }
            }
        }.getProperty("MAPS_API_KEY")
        ?: System.getenv("MAPS_API_KEY")
        ?: "".also { logger.warn("MAPS_API_KEY가 local.properties에도 환경변수에도 없어 빈 키로 빌드한다 — 지도가 렌더링되지 않는다.") }

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
    implementation(project(":core:notification"))
    implementation(project(":core:design-system"))
    implementation(project(":core:common:ui"))
    // 진입형 feature만 등록한다. 탭 feature는 셸(:feature:main)을 통해 들어온다.
    implementation(project(":feature:profile"))
    implementation(project(":feature:roomform"))
    implementation(project(":feature:main"))
    implementation(project(":feature:sharereceiver"))
    implementation(project(":feature:splash"))
    implementation(project(":feature:onboarding"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    implementation(libs.timber)

    // MinoApplication이 SingletonImageLoader.Factory를 구현하려면 coil3 핵심 타입(ImageLoader 등)이 필요하다 —
    // core:design-system은 coil-compose를 `implementation`으로만 갖고 있어 이 모듈 컴파일 classpath에
    // 노출되지 않는다. coil-network-ktor3는 그 위에 실제 네트워크 fetcher를 더한다(없으면 모든 http(s)
    // 이미지 요청이 즉시 실패해 fallback 글리프만 보인다).
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
}
