plugins {
    alias(libs.plugins.mino.android.library)
}

android {
    namespace = "team.mino.core.common.android"
}

dependencies {
    implementation(project(":core:common:kotlin"))
    implementation(project(":core:error-handling"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
