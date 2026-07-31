plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.main"
}

dependencies {
    // 탭 셸은 탭 feature 모듈을 직접 의존한다(→ docs/adr/2026-08-01-single-module-navigation-contract.md)
    implementation(project(":feature:home"))
}
