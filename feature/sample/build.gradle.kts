plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.sample"
}

dependencies {
    implementation(project(":core:map"))

    implementation(libs.maps.compose)
}
