package team.mino.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project
import java.util.Properties

internal fun Project.configureSigning(extension: ApplicationExtension) {
    configureDebugSigning(extension)
    configureReleaseSigning(extension)
}

// AGP는 buildType의 signingConfig를 flavor의 것보다 우선한다. debug buildType에는 기본 signingConfig가
// 자동으로 붙으므로, flavor에 무엇을 지정하든 debug 빌드는 머신마다 다른 ~/.android/debug.keystore로 서명된다.
// 그 기본 config 자체를 공용 키로 덮어써 qaDebug·prodDebug의 SHA-1을 하나로 고정한다.
private fun Project.configureDebugSigning(extension: ApplicationExtension) {
    val debugKeystore = rootProject.file("keystore/debug.jks")
    // 추적 파일이라 정상 체크아웃에는 항상 있다. 없는 채로 조용히 넘기면 머신 로컬 키로 되돌아가
    // 지도가 안 뜨는 증상으로만 드러나므로, 빌드를 막지는 않되 경고를 남긴다.
    if (!debugKeystore.exists()) {
        logger.warn("공용 debug 키스토어(${debugKeystore.path})가 없어 머신 로컬 키로 서명한다 — Maps·Firebase 지문이 어긋난다.")
        return
    }

    extension.signingConfigs {
        named("debug") {
            storeFile = debugKeystore
            // AOSP 관례값. 비밀이 아니라서 키스토어 파일과 함께 레포에 둔다.
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
}

private fun Project.configureReleaseSigning(extension: ApplicationExtension) {
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
