plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.room"
}

dependencies {
    // 컨벤션 플러그인(team.mino.android.feature)이 :core:domain·:core:navigation·:core:design-system·
    // :core:common:android·:core:common:ui는 이미 의존으로 추가한다(AndroidFeatureConventionPlugin 참고).
    // 이 모듈만 추가로 필요한 지도 의존만 여기 더한다.
    implementation(project(":core:map"))
    implementation(libs.maps.compose)
}
