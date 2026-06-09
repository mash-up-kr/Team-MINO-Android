plugins {
    alias(libs.plugins.mino.android.library)
    alias(libs.plugins.mino.android.compose)
}

android {
    namespace = "team.mino.core.navigation"
}

dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
