plugins {
    alias(libs.plugins.mino.android.library)
    alias(libs.plugins.mino.android.compose)
}

android {
    namespace = "team.mino.core.map"
}

dependencies {
    implementation(project(":core:common:kotlin"))

    implementation(libs.maps.compose)
}
