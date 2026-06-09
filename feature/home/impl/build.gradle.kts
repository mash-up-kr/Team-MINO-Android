plugins {
    alias(libs.plugins.mino.android.feature.impl)
}

android {
    namespace = "team.mino.feature.home"
}

dependencies {
    implementation(project(":feature:home:api"))
    implementation(project(":feature:sample:api"))
}
