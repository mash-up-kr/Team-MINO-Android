# R8 설정은 컨벤션 플러그인이 갖고, keep 규칙은 의존을 가진 모듈이 소유한다

- **상태**: Accepted
- **작성일**: 2026-09-14
- **작성자**: Jaesung Lee

## 컨텍스트

1.0.0 첫 스토어 심사 중 Play 콘솔이 "리소스 축소가 사용 설정되지 않음"을 권장 조치로 제시했다(이슈 #314). 당시 release 빌드는 `app/build.gradle.kts`에서만 `isMinifyEnabled = true`를 켜고 있었고, 라이브러리 모듈은 컨벤션 플러그인이 `isMinifyEnabled = false`로 고정했으며, keep 규칙은 `app/proguard-rules.pro`의 Crashlytics용 두 줄뿐이었다. `consumer-rules.pro`를 가진 모듈은 없었다.

이슈 본문은 "리플렉션·직렬화 의존 라이브러리 모듈마다 `consumer-rules.pro`를 둔다"를 완료 조건으로 걸었다. 실제로 규칙이 필요한지는 확인하지 않은 가정이었다.

기준선을 채취하기 위해 현행 설정으로 `assembleQaRelease`를 빌드하고 `app/build/outputs/mapping/qaRelease/configuration.txt`(R8이 실제 적용한 전체 규칙)·`seeds.txt`·`usage.txt`·`mapping.txt`를 분석했다.

- kotlinx.serialization(named companion 포함)·Ktor·Hilt·hilt-work·WorkManager(`ListenableWorker` 이름·생성자 keep)·Coil·Firebase·OkHttp·DataStore·Maps·Navigation 모두 자기 규칙을 아티팩트에 번들하고 있었고 AGP가 그것을 합치고 있었다.
- 프로젝트 클래스 중 keep된 것은 Manifest 컴포넌트(Activity·Service·Application)와 Hilt 생성 진입점, `@HiltWorker`뿐이었다. DTO는 난독화됐지만 컴파일러가 생성한 serializer가 이름에 의존하지 않아 문제없다. 1.0.0이 이 상태로 QA를 통과했다.
- R8 full mode는 AGP 9.1.1 기본값으로 이미 켜져 있었다. opt-out 플래그 `android.enableR8.fullMode=false`를 넘기면 "deprecated, AGP 10에서 제거" 경고가 나온다.
- 프로젝트에는 이름으로 리소스를 조회하는 코드(`getIdentifier`)·딥링크·named companion이 없었다.

## 결정

1. release 빌드의 `isMinifyEnabled`·`isShrinkResources`·proguard 파일 등록은 `AndroidApplicationConventionPlugin`이 갖는다. `app/build.gradle.kts`에는 `buildTypes` 블록을 두지 않는다.
2. 라이브러리는 자체 minify를 하지 않는다. `AndroidLibraryConventionPlugin`이 모듈 루트에 `consumer-rules.pro`가 있을 때만 `consumerProguardFiles`로 등록한다.
3. keep 규칙은 그 규칙을 필요하게 만든 의존(리플렉션·직렬화·클래스명 저장·이름 기반 리소스 조회)을 가진 모듈이 소유한다. `:app/proguard-rules.pro`는 앱 전역 속성(Crashlytics attributes·소스 파일명 마스킹)만 갖는다.
4. 라이브러리가 번들한 규칙은 프로젝트에서 반복 선언하지 않는다. 필요한 규칙이 없는 모듈에는 빈 `consumer-rules.pro`를 만들지 않는다. 현재는 어느 모듈도 파일을 두지 않는다.
5. R8 full mode는 AGP 기본값을 쓰고 `gradle.properties`에 플래그를 두지 않는다.

## 근거

- 축소·난독화는 앱이 전체 코드를 볼 때만 안전하고, 라이브러리 단독 minify는 앱 최적화와 충돌한다. 그래서 켜는 곳은 앱 하나이되, 그 설정을 build script가 아니라 컨벤션 플러그인에 두면 flavor·build type 정책과 같은 층에서 관리된다.
- keep 규칙을 앱에 모으면 어느 모듈의 어떤 코드 때문에 필요한지가 사라지고, 그 모듈을 지워도 규칙이 남는다. DI 바인딩 소유([ADR: Hilt 구현 바인딩은 구현을 소유한 모듈이 갖고, `:app`은 그래프 조립만 한다](2026-08-02-di-binding-ownership.md))와 같은 이유로 규칙을 의존 옆에 둔다.
- 번들 규칙을 프로젝트에서 다시 쓰면 라이브러리 업그레이드 때 두 사본이 어긋난다. `configuration.txt`가 적용된 규칙의 단일 출처이므로 그것을 보고 없는 것만 쓴다.
- 빈 `consumer-rules.pro`는 "여기 규칙이 있다"는 잘못된 신호를 주고 SSOT 원칙과 어긋난다.
- full mode 플래그는 곧 제거될 옵션이라 기록해 둘 가치가 없고, 1.0.0이 이미 full mode로 배포되어 되돌릴 이유도 없다.

## 결과

- `build-logic/.../Shrinking.kt`에 앱 release 축소 설정과 라이브러리 `consumer-rules.pro` 등록 함수를 두고 두 컨벤션 플러그인이 호출한다. `KotlinAndroid.kt`에 있던 라이브러리 `isMinifyEnabled = false`는 AGP 기본값과 같아 삭제했다.
- `app/build.gradle.kts`에서 `buildTypes` 블록이 제거됐다.
- 리소스 축소 결과(qaRelease): APK 10.65MB → 10.20MB, 파일 508 → 393. 제거된 리소스는 전부 라이브러리 것과 앱 템플릿 잔재(`purple_*`·`teal_*` 등)·미참조 문자열이었고, 프로젝트가 참조하는 리소스는 하나도 제거되지 않았다. Manifest 컴포넌트는 그대로 keep됐다(리소스 축소 모드에서는 R8이 Manifest를 직접 추적하므로 aapt keep 규칙 섹션이 비는 것이 정상이다).
- 규칙의 단일 출처는 [`r8-keep-rules.md`](../conventions/r8-keep-rules.md)다. 새 모듈이 리플렉션 의존을 들일 때 `consumer-rules.pro` 필요 여부는 이 문서로 판정한다.
- 이슈 #314의 완료 조건 "의존 모듈마다 `consumer-rules.pro`가 있다"는 "번들되지 않은 규칙이 필요한 모듈만 파일을 둔다"로 읽는다.
- 실기기 스모크와 Crashlytics 매핑 복원 확인은 이 결정의 검증 항목으로 남아 있다.

## 고려한 대안

**앱 build script에 그대로 두고 `isShrinkResources`만 추가한다.** 콘솔 권장 조치는 해소되지만 keep 규칙이 앱에 암묵적으로 위임되는 구조는 그대로다. 배제했다.

**리플렉션 의존이 있는 모듈마다 `consumer-rules.pro`를 둔다** (이슈 본문의 원안). 기준선 분석 결과 모든 대상 라이브러리가 규칙을 번들하고 있어, 파일을 만들면 전부 번들 규칙의 복사본이거나 빈 파일이 된다. 소유 원칙과 등록 장치만 만들고 파일은 두지 않는 쪽을 택했다.

**`gradle.properties`에 `android.enableR8.fullMode=true`를 명시한다.** 기본값과 같고 AGP 10에서 제거될 플래그라 문서 한 줄로 대신했다.

**라이브러리 모듈에도 minify를 켠다.** 앱에서 전체 코드를 보고 한 번에 최적화하는 것과 충돌하고, 모듈 간 호출 경계를 keep해야 해 오히려 축소율이 떨어진다. 배제했다.
