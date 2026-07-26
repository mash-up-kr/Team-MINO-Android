plugins {
    alias(libs.plugins.mino.android.library)
    alias(libs.plugins.mino.android.compose)
}

android {
    namespace = "team.mino.core.designsystem"
}

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(libs.junit)
}
