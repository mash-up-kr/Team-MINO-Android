package team.mino.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project

// release 빌드의 코드 축소·난독화·최적화와 리소스 축소는 앱 모듈 build script가 아니라 여기서 켠다.
// keep 규칙 소유와 검증 절차는 docs/conventions/r8-keep-rules.md.
internal fun configureReleaseShrinking(extension: ApplicationExtension) {
    extension.buildTypes.named("release") {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            extension.getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
        )
    }
}

// 라이브러리는 keep 규칙만 consumer-rules.pro로 앱에 전달한다(자체 minify는 AGP 기본값대로 끔).
// 규칙이 필요한 모듈만 파일을 두므로, 빈 파일을 강제하지 않기 위해 존재할 때만 등록한다.
internal fun Project.configureConsumerProguardRules(extension: LibraryExtension) {
    val consumerRules = file("consumer-rules.pro")
    if (consumerRules.exists()) {
        extension.defaultConfig.consumerProguardFiles(consumerRules)
    }
}
