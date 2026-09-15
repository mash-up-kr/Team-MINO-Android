package team.mino.buildlogic

import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JvmVendorSpec

// 빌드 JVM은 벤더까지 고정한다. 버전만 지정하면 로컬의 JBR이 잡히는데, JBR 17.0.7의 C1 JIT 버그가
// lint·컴파일 데몬을 죽인다. 데몬 JVM 조건은 gradle/gradle-daemon-jvm.properties가 같은 값을 갖는다.
// 바이트코드 타깃(compileOptions·jvmTarget)은 따로 두지 않는다. AGP·KGP가 툴체인 버전을 기본값으로 쓴다.
internal fun Project.configureBuildJvm(spec: JavaToolchainSpec) {
    spec.languageVersion.set(JavaLanguageVersion.of(intVersion("jdk")))
    spec.vendor.set(JvmVendorSpec.ADOPTIUM)
}
