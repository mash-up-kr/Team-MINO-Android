package team.mino.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

// AGP 9에서 buildFeatures가 ApplicationExtension/LibraryExtension의 concrete 멤버로 분리되어
// CommonExtension 단일 시그니처로 합칠 수 없음.
internal fun Project.configureAndroidCompose(extension: ApplicationExtension) {
    extension.buildFeatures.compose = true
    extension.lint.lintConfig = rootProject.file("lint.xml")
    addComposeDependencies()
}

internal fun Project.configureAndroidCompose(extension: LibraryExtension) {
    extension.buildFeatures.compose = true
    extension.lint.lintConfig = rootProject.file("lint.xml")
    addComposeDependencies()
}

private fun Project.addComposeDependencies() {
    dependencies {
        val bom = libs.lib("androidx-compose-bom")
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))

        add("implementation", libs.lib("androidx-compose-ui"))
        add("implementation", libs.lib("androidx-compose-ui-graphics"))
        add("implementation", libs.lib("androidx-compose-material3"))
        add("implementation", libs.lib("androidx-compose-ui-tooling-preview"))

        add("debugImplementation", libs.lib("androidx-compose-ui-tooling"))
        add("debugImplementation", libs.lib("androidx-compose-ui-test-manifest"))

        add("androidTestImplementation", libs.lib("androidx-compose-ui-test-junit4"))

        add("lintChecks", libs.lib("slack-compose-lints"))
    }
}
