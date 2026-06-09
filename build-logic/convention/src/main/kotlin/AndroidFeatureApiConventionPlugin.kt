import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import team.mino.buildlogic.lib
import team.mino.buildlogic.libs

/**
 * feature 진입점 계약(`:feature:x:api`)을 위한 경량 컨벤션.
 *
 * Launcher 인터페이스와 `@Serializable` 진입 인자만 노출하므로 compose·hilt를 적용하지 않는다.
 * 다른 feature는 이 모듈에만 의존해 전환한다.
 */
class AndroidFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("team.mino.android.library")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            val catalog = libs

            dependencies {
                add("implementation", project(":core:navigation"))
                add("implementation", project(":core:common:kotlin"))

                add("implementation", catalog.lib("androidx-activity"))
                add("implementation", catalog.lib("kotlinx-serialization-json"))
            }
        }
    }
}
