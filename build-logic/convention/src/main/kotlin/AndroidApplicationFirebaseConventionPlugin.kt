import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationFirebaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.gms.google-services")
        }
    }
}
