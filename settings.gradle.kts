pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Mino-Android"
include(":app")
include(":core:data")
include(":core:domain")
include(":core:common:android")
include(":core:common:kotlin")
include(":core:common:ui")
include(":core:design-system")
include(":core:navigation")
include(":core:map")
include(":feature:sample:impl")
include(":feature:sample:api")
include(":feature:home:impl")
include(":feature:home:api")
