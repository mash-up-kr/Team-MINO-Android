plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.onboarding"
}

dependencies {
    testImplementation(libs.kotlinx.coroutines.test)
}
