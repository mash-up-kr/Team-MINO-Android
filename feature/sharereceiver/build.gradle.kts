plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.sharereceiver"
}

dependencies {
    testImplementation(libs.kotlinx.coroutines.test)
}
