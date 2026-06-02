plugins {
    alias(libs.plugins.mino.android.feature)
}

android {
    namespace = "team.mino.feature.sample"
}

dependencies {
    implementation(project(":core:common:android"))
}
