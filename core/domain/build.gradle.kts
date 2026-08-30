plugins {
    alias(libs.plugins.mino.kotlin.jvm)
}

dependencies {
    implementation(project(":core:common:kotlin"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
