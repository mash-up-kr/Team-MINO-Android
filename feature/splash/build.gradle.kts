plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.splash"
}

dependencies {
    testImplementation(libs.kotlinx.coroutines.test)
}
