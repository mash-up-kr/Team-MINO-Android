plugins {
    alias(libs.plugins.mino.android.library)
    alias(libs.plugins.mino.android.hilt)
    alias(libs.plugins.mino.android.flavor)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "team.mino.core.data"

    testOptions {
        unitTests {
            // Firebase 예외 생성자가 android.text.TextUtils를 거친다. JVM 단위 테스트의 스텁 android.jar는
            // 기본적으로 미구현 메서드에서 예외를 던져 예외 인스턴스조차 만들 수 없다.
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(project(":core:common:kotlin"))
    implementation(project(":core:common:android"))
    implementation(project(":core:domain"))
    implementation(project(":core:error-handling"))

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.androidx.work.testing)
}
