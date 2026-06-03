plugins {
    `kotlin-dsl`
}

group = "team.mino.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
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
            id = "team.mino.android.firebase" // 앱 모듈에서 불러다 쓸 이름
            implementationClass = "AndroidFirebaseConventionPlugin"
        }
    }
}
