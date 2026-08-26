plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.roomform"

    testOptions {
        unitTests {
            // ViewModel이 진입점을 SavedStateHandle.toRoute로 복원한다. 그 경로가 Bundle을 거치는데,
            // JVM 단위 테스트의 스텁 android.jar는 기본적으로 미구현 메서드에서 예외를 던진다.
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    testImplementation(libs.kotlinx.coroutines.test)
}
