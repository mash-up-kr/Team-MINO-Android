plugins {
    alias(libs.plugins.mino.kotlin.jvm)
}

dependencies {
    implementation(project(":core:common:kotlin"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.kotlinx.coroutines.test)
}
