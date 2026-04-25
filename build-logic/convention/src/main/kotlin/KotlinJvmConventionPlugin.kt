import org.gradle.api.Plugin
import org.gradle.api.Project
import team.mino.buildlogic.configureKotlinJvm

class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            configureKotlinJvm()
        }
    }
}
