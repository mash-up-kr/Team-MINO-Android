plugins {
    alias(libs.plugins.mino.android.library)
    alias(libs.plugins.mino.android.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "team.mino.core.navigation"
}

dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.serialization.json)
}
