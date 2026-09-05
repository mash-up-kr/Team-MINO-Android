plugins {
    alias(libs.plugins.mino.android.library)
    alias(libs.plugins.mino.android.hilt)
}

android {
    namespace = "team.mino.core.notification"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
}
