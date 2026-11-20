plugins {
    `kotlin-dsl`
}

group = "team.mino.buildlogic"

// 컨벤션 플러그인의 BuildJvm.kt와 같은 값. 이 스크립트가 그 코드를 컴파일하므로 가져다 쓸 수 없어 여기서 한 번 더 적는다.
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get().toInt()))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "team.mino.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "team.mino.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "team.mino.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "team.mino.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "team.mino.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "team.mino.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidFlavor") {
            id = "team.mino.android.flavor"
            implementationClass = "AndroidFlavorConventionPlugin"
        }
        register("kotlinJvm") {
            id = "team.mino.kotlin.jvm"
            implementationClass = "KotlinJvmConventionPlugin"
        }
        register("androidFirebase") {
            id = "team.mino.android.firebase"
            implementationClass = "AndroidFirebaseConventionPlugin"
        }
    }
}
