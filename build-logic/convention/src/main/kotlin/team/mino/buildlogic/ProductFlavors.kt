package team.mino.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project

enum class FlavorDimension {
    env,
}

enum class Flavor(
    val dimension: FlavorDimension,
    // qa·prod가 같은 주소를 쓴다 — 서버가 환경을 분리하면 flavor별로 갈라진다
    val apiBaseUrl: String,
    val appName: String,
    val applicationIdSuffix: String? = null,
    val versionNameSuffix: String? = null,
) {
    qa(
        dimension = FlavorDimension.env,
        apiBaseUrl = "https://api.gguk.org/",
        appName = "GGUK QA",
        applicationIdSuffix = ".qa",
        versionNameSuffix = "-qa",
    ),
    prod(
        dimension = FlavorDimension.env,
        apiBaseUrl = "https://api.gguk.org/",
        appName = "GGUK",
    ),
}


internal fun Project.configureFlavors(extension: ApplicationExtension) {
    configureSigning(extension)
    with(extension) {
        buildFeatures.resValues = true
        FlavorDimension.entries.forEach { flavorDimensions += it.name }

        productFlavors {
            Flavor.entries.forEach { minoFlavor ->
                create(minoFlavor.name) {
                    dimension = minoFlavor.dimension.name
                    minoFlavor.applicationIdSuffix?.let { applicationIdSuffix = it }
                    minoFlavor.versionNameSuffix?.let { versionNameSuffix = it }
                    resValue("string", "app_name", minoFlavor.appName)
                    signingConfig = signingConfigs.findByName(minoFlavor.name)
                }
            }
        }
        productFlavors.getByName(Flavor.qa.name) { isDefault = true }
    }
}

internal fun configureFlavors(extension: LibraryExtension) {
    with(extension) {
        FlavorDimension.entries.forEach { flavorDimensions += it.name }

        productFlavors {
            Flavor.entries.forEach { minoFlavor ->
                create(minoFlavor.name) {
                    dimension = minoFlavor.dimension.name
                    buildConfigField("String", "API_BASE_URL", "\"${minoFlavor.apiBaseUrl}\"")
                }
            }
        }
        productFlavors.getByName(Flavor.qa.name) { isDefault = true }
    }
}
