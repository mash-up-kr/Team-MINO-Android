import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFirebaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("com.google.gms.google-services")
                apply("com.google.firebase.crashlytics")
            }
        }
    }
}
