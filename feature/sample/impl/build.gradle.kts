plugins {
    alias(libs.plugins.mino.android.feature.impl)
}

android {
    namespace = "team.mino.feature.sample"
}

dependencies {
    implementation(project(":feature:sample:api"))
    implementation(project(":feature:home:api"))
    implementation(project(":core:map"))

    implementation(libs.maps.compose)
}
