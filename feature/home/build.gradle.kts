plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.home"
}

dependencies {
    testImplementation(libs.kotlinx.coroutines.test)
}
