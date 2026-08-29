plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.placedetail"
}

dependencies {
    implementation(project(":core:map"))

    implementation(libs.maps.compose)
}
