plugins {
    alias(libs.plugins.mino.android.library)
    alias(libs.plugins.mino.android.compose)
    alias(libs.plugins.mino.android.hilt)
}

android {
    namespace = "team.mino.core.analytics"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
}
