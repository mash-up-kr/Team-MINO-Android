plugins {
    alias(libs.plugins.mino.android.library)
}

android {
    namespace = "team.mino.core.common.android"
}

dependencies {
    implementation(project(":core:common:kotlin"))

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
}
