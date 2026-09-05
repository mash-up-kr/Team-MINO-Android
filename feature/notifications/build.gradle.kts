plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.notifications"
}

dependencies {
    testImplementation(libs.kotlinx.coroutines.test)
}
