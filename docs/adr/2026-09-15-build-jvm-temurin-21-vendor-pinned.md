# 빌드 JVM은 Temurin 21로 벤더까지 고정하고, 바이트코드 타깃도 21로 올린다

- **상태**: Accepted
- **작성일**: 2026-09-15
- **작성자**: Jaesung Lee

## 컨텍스트

`gradle/gradle-daemon-jvm.properties`는 데몬 JVM 조건을 "버전 17"로만 지정하고 있었고, 컨벤션 플러그인의 `jvmToolchain(17)`도 벤더를 지정하지 않았다. 이 머신의 유일한 JDK 17은 Android Studio가 쓰는 JBR 17.0.7이라 데몬과 컴파일 툴체인 모두 JBR로 돌았다.

JBR 17.0.7에는 C1 JIT의 OSR 컴파일 경로에서 데몬을 죽이는 버그가 있다(`assembler_aarch64.hpp:267 "Field too big for insn"`, 프레임 `FirKotlinUastResolveProviderService`). 8월 25일 `lintDebug`에서 처음 겪어 `-XX:-UseOnStackReplacement`로 회피했고, 9월 15일에는 release 빌드(`assembleQaRelease`·`installQaRelease`)까지 같은 크래시로 실패했다. AGP가 release 변형에 `lintVitalAnalyze*`를 넣어 UAST 코드가 데몬 안에서 실행되기 때문이다. 타이밍에 따라 통과하기도 해서 "이 머신에선 못 돈다"는 오해와 검증 누락을 낳았다(이 오해로 Compose Lint 위반 7건을 놓친 전례가 있다).

CI(`setup-android-build` 액션)는 Temurin을 쓰고 있어 CI에는 이 버그가 없었다. 저장소에는 foojay 툴체인 리졸버가 이미 있어 조건에 맞는 JDK가 없으면 Gradle이 자동으로 내려받는다. 이 머신에는 그렇게 받은 Temurin 21이 이미 있었다.

## 결정

1. 데몬 JVM 조건을 `toolchainVendor=ADOPTIUM`·`toolchainVersion=21`로 재생성한다(`./gradlew updateDaemonJvm --jvm-version=21 --jvm-vendor=ADOPTIUM`).
2. 컴파일 툴체인도 `BuildJvm.kt`에서 같은 벤더·버전으로 고정하고, 세 컨벤션(Android 앱·라이브러리, Kotlin JVM)이 이를 공유한다. `build-logic` 자신은 이 코드를 컴파일하는 쪽이라 가져다 쓸 수 없어 build script에 같은 값을 한 번 더 적는다.
3. 버전 숫자는 버전 카탈로그 `jdk` 하나만 갖는다. 바이트코드 타깃(`compileOptions`·`jvmTarget`·`sourceCompatibility`)은 명시하지 않는다. AGP·KGP·Gradle이 툴체인 버전을 기본값으로 쓰므로 결과적으로 21이 된다.
4. CI의 `setup-java`를 Temurin 21로 맞춘다.
5. `-XX:-UseOnStackReplacement` 같은 JIT 회피 플래그는 두지 않는다.

## 근거

- 버그의 원인은 "17"이 아니라 "JBR 17.0.7"이다. 버전만 올리면 다른 머신에서 JBR 21이 잡힐 수 있고(최신 Android Studio가 JBR 21을 번들해 Current JVM으로 우선권을 갖는다) JBR 21에 같은 버그가 없다는 보장이 없다. 벤더 고정이 본질이다.
- Temurin 21은 이미 로컬에 있어 다운로드 없이 즉시 쓸 수 있었고, Gradle 9.3.1·AGP 9.1.1·Kotlin 2.2.10 모두 JDK 21을 지원한다.
- 툴체인 21과 타깃 17을 섞으면 KGP가 Android `compileOptions`를 툴체인 버전으로 덮어써 "Inconsistent JVM targets: 21 and 17"로 빌드가 깨진다(실측). 타깃을 명시하지 않고 툴체인에서 파생시키면 이 불일치가 구조적으로 생기지 않고, 다음 JDK 상향 때 고칠 곳이 카탈로그 한 줄과 데몬 조건 파일·CI 액션뿐이다.
- 회피 플래그는 증상만 가리고 컴파일 속도를 떨어뜨린다. JDK를 바꾸면 필요 없다.

## 결과

- 로컬·CI·Android Studio 모두 같은 Temurin 21로 빌드한다. Android Studio는 Ladybug(2024.2)부터 `gradle-daemon-jvm.properties`를 IDE의 Gradle JDK 설정보다 우선하므로 IDE 설정을 바꿀 필요가 없다. JDK가 없는 머신은 첫 빌드에서 자동으로 내려받는다.
- 산출 클래스 파일은 major 65(Java 21)다. 기기에서 돌아가는 dex는 D8이 만들므로 `minSdk`에는 영향이 없다. 실기기 스모크는 타깃 21 빌드로 다시 확인한다.
- `-Dorg.gradle.java.home`은 데몬 JVM 조건에 밀려 무시되므로 JDK를 바꾸려면 이 조건 파일을 고쳐야 한다.
- 이 결정은 이슈 #314(R8 전역 적용) 브랜치에 함께 실린다. release 빌드가 lintVital 때문에 같은 크래시를 겪어 R8 검증을 막고 있었기 때문이다.

## 고려한 대안

**Temurin 17로 벤더만 고정한다.** 근본 해결이고 CI 변경도 없지만, 로컬에 Temurin 17이 없어 다운로드가 필요하고 이미 받아 둔 21을 버리게 된다. 데몬을 21로 두면서 컴파일만 17로 남기는 절충은 JDK가 둘이 되어 CI에서도 17을 추가로 내려받아야 한다. 배제했다.

**데몬만 21로 두고 바이트코드 타깃은 17을 유지한다.** 툴체인을 지정하지 않으면 가능하지만 KGP 검증 설정을 손봐야 하고, 툴체인을 지정하면 위의 불일치 오류가 난다. 사용자가 타깃 21 상향을 택했다.

**`org.gradle.jvmargs`에 `-XX:-UseOnStackReplacement`를 넣는다.** 즉시 회피는 되지만 모든 머신의 컴파일 성능을 깎고 원인을 남긴다. 임시로만 썼다.

**`-Dorg.gradle.java.home`으로 다른 JDK를 지정한다.** 데몬 JVM 조건이 우선해 무시된다. 동작하지 않는다.
