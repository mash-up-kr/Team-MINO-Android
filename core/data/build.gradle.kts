plugins {
    alias(libs.plugins.mino.android.library)
    alias(libs.plugins.mino.android.hilt)
    alias(libs.plugins.mino.android.flavor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktorfit)
}

android {
    namespace = "team.mino.core.data"
}

dependencies {
    implementation(project(":core:common:kotlin"))
    implementation(project(":core:common:android"))
    implementation(project(":core:domain"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.ktorfit.lib)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
}
