import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import team.mino.buildlogic.configureAndroidCompose

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            when {
                pluginManager.hasPlugin("com.android.application") -> {
                    extensions.configure<ApplicationExtension> {
                        configureAndroidCompose(this)
                    }
                }
                pluginManager.hasPlugin("com.android.library") -> {
                    extensions.configure<LibraryExtension> {
                        configureAndroidCompose(this)
                    }
                }
                else -> error(
                    "team.mino.android.compose must be applied after com.android.application or com.android.library."
                )
            }
        }
    }
}
