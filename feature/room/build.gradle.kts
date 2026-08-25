plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.room"

    testOptions {
        unitTests {
            // RoomListViewModel이 LocationManager·권한 조회에 Android 프레임워크 API를 직접 쓴다.
            // 테스트에서는 FakeLocationContext로 대체하지만, 그 밖의 우발적 스텁 호출이 예외 대신
            // 기본값을 돌려주도록 한다(feature:profile과 동일한 이유).
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // 컨벤션 플러그인(team.mino.android.feature)이 :core:domain·:core:navigation·:core:design-system·
    // :core:common:android·:core:common:ui는 이미 의존으로 추가한다(AndroidFeatureConventionPlugin 참고).
    // 이 모듈만 추가로 필요한 지도 의존만 여기 더한다.
    implementation(project(":core:map"))
    implementation(libs.maps.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
