plugins {
    alias(libs.plugins.mino.android.library)
}

android {
    namespace = "team.mino.core.common.android"
}

dependencies {
    implementation(project(":core:common:kotlin"))

    implementation(libs.androidx.core.ktx)
    api(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
}
