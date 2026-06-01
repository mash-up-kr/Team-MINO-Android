package team.mino.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project
import java.util.Properties

internal fun Project.configureSigning(extension: ApplicationExtension) {
    val keystorePropsFile = rootProject.file("keystore.properties")
    if (!keystorePropsFile.exists()) return

    val props = Properties().apply {
        keystorePropsFile.inputStream().use(::load)
    }

    extension.signingConfigs {
        Flavor.entries.forEach { flavor ->
            val storeFileKey = "storeFile${flavor.name.replaceFirstChar { it.uppercaseChar() }}"
            val storeFilePath = props.getProperty(storeFileKey) ?: return@forEach
            create(flavor.name) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }
}
