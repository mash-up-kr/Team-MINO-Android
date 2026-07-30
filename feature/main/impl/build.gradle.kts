plugins {
    alias(libs.plugins.mino.android.feature.impl)
}

android {
    namespace = "team.mino.feature.main"
}

dependencies {
    implementation(project(":feature:main:api"))
}
