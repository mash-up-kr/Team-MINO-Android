import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import team.mino.buildlogic.lib
import team.mino.buildlogic.libs

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("team.mino.android.library")
                apply("team.mino.android.compose")
                apply("team.mino.android.hilt")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            // dependencies {} 람다 안에서는 Project 수신자가 가려져 libs 확장 프로퍼티가 보이지 않으므로 캡처.
            val catalog = libs

            dependencies {
                add("implementation", project(":core:common:kotlin"))
                add("implementation", project(":core:common:android"))
                add("implementation", project(":core:common:ui"))
                add("implementation", project(":core:design-system"))
                add("implementation", project(":core:domain"))

                add("implementation", catalog.lib("androidx-core-ktx"))
                add("implementation", catalog.lib("androidx-activity-compose"))
                add("implementation", catalog.lib("androidx-lifecycle-runtime-ktx"))
                add("implementation", catalog.lib("androidx-lifecycle-runtime-compose"))
                add("implementation", catalog.lib("androidx-lifecycle-viewmodel-compose"))
                add("implementation", catalog.lib("androidx-navigation-compose"))
                add("implementation", catalog.lib("androidx-hilt-navigation-compose"))
                add("implementation", catalog.lib("kotlinx-coroutines-android"))
                add("implementation", catalog.lib("kotlinx-collections-immutable"))
                add("implementation", catalog.lib("kotlinx-serialization-json"))

                add("testImplementation", catalog.lib("junit"))
                add("androidTestImplementation", catalog.lib("androidx-junit"))
                add("androidTestImplementation", catalog.lib("androidx-espresso-core"))
            }
        }
    }
}
