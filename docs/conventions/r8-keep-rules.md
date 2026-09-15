# R8 축소·난독화와 keep 규칙 소유

release 빌드의 코드 축소·난독화·최적화와 리소스 축소는 `:app` build script가 아니라 컨벤션 플러그인이 켠다. keep 규칙은 그 규칙을 필요하게 만든 의존을 가진 모듈이 소유한다.

## 적용 위치

- **앱**: `AndroidApplicationConventionPlugin`이 release build type에 `isMinifyEnabled`·`isShrinkResources`·AGP 기본 최적화 규칙(`proguard-android-optimize.txt`)·`app/proguard-rules.pro`를 등록한다. `app/build.gradle.kts`에는 `buildTypes` 블록을 두지 않는다.
- **라이브러리**: `AndroidLibraryConventionPlugin`이 모듈 루트에 `consumer-rules.pro`가 있으면 `consumerProguardFiles`로 등록한다. 라이브러리 자체는 minify하지 않는다. 축소·난독화는 앱에서 전체 코드를 한 번에 볼 때만 안전하다.
- **R8 full mode**: AGP 기본값(켜짐)을 쓰고 `gradle.properties`에 플래그를 두지 않는다. opt-out 플래그는 AGP 9에서 deprecated이고 10에서 제거된다.
- 연결 코드: `build-logic/.../Shrinking.kt`.

## keep 규칙 소유

DI 바인딩 소유([`dependency-injection.md`](dependency-injection.md))와 같은 원칙이다.

1. **라이브러리가 번들한 규칙은 프로젝트에서 반복 선언하지 않는다.** kotlinx.serialization·Ktor·Hilt·WorkManager·Coil·Firebase·Maps 같은 라이브러리는 자기 규칙을 아티팩트에 싣고, AGP가 그것을 자동으로 합친다. 어떤 규칙이 이미 적용됐는지는 `app/build/outputs/mapping/<variant>/configuration.txt`가 단일 출처다.
2. **번들되지 않은 keep 규칙은 그 의존을 가진 모듈의 `consumer-rules.pro`에 둔다.** 리플렉션으로 클래스를 찾거나, 클래스 이름을 문자열로 저장·전송하거나, 이름으로 리소스를 조회하는 코드가 있는 모듈이 소유자다. `:app`이 남의 규칙을 대신 갖지 않는다.
3. **`:app/proguard-rules.pro`는 앱 전역 속성만 갖는다.** Crashlytics 스택 트레이스 복원용 attributes와 소스 파일명 마스킹이 여기 해당한다.
4. **규칙이 필요 없는 모듈에는 파일을 두지 않는다.** 빈 `consumer-rules.pro`는 만들지 않는다.
5. **모든 규칙에는 근거 주석을 단다.** 어느 라이브러리의 어떤 런타임 경로가 그 규칙을 요구하는지 적는다. 근거가 사라지면 규칙도 지운다.

## 리소스 축소

리소스 축소는 코드가 참조하지 않는 리소스를 제거한다. 이름으로 리소스를 조회하는 코드(`getIdentifier` 등)가 있으면 R8이 참조를 보지 못한다. 이 경우 `res/raw/keep.xml`로 예외를 두기보다 조회 자체를 리소스 ID 참조로 바꾸는 것을 우선한다.

## 검증

- 빌드: `./gradlew :app:assembleQaRelease :app:bundleProdRelease`
- 산출물(`app/build/outputs/mapping/<variant>/`): `configuration.txt`(적용된 전체 규칙), `seeds.txt`(keep된 항목), `usage.txt`(제거된 항목), `resources.txt`(리소스 축소 결과)
- 실기기 스모크: 리플렉션·직렬화·클래스명 저장 경로가 있는 플로우(인증, 지도, 푸시 수신, 이미지 로딩, 알림함, 공유 링크 저장)
- Crashlytics: 매핑 파일은 플러그인이 빌드 시 자동 업로드한다. 콘솔에서 난독화된 스택 트레이스가 복원되는지 확인한다.

## 배경

이 규칙이 정해진 배경은 [ADR: R8 설정은 컨벤션 플러그인이 갖고, keep 규칙은 의존을 가진 모듈이 소유한다](../adr/2026-09-14-r8-config-in-convention-plugin-keep-rule-ownership.md) 참조.
