import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import team.mino.buildlogic.lib
import team.mino.buildlogic.libs

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("com.google.dagger.hilt.android")
            }

            val catalog = libs

            dependencies {
                add("implementation", catalog.lib("hilt-android"))
                add("ksp", catalog.lib("hilt-compiler"))
            }
        }
    }
}
