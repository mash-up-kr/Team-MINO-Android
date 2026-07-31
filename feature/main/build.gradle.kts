plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.main"
}

dependencies {
    // 탭 셸은 탭 feature 모듈을 직접 의존한다(→ docs/adr/2026-07-30-single-feature-module.md)
    implementation(project(":feature:home"))
}
