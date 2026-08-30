# 비회원 사용자 구분은 Firebase 익명 인증이 소유하고, 앱은 세션·신원 증명을 저장하지 않는다

- **상태**: Accepted
- **작성일**: 2026-08-22
- **작성자**: Jaesung Lee

## 컨텍스트

이슈 #176 이전까지 이 앱은 `ANDROID_ID`를 읽어 DataStore에 저장하고 그 값으로 사용자를 구분했다(이슈 #89의 `DeviceRepository`·`EnsureDeviceIdUseCase`·`DeviceIdLocalDataSource`·`DeviceInfoProvider`). 이 방식의 근본 문제는 서버가 그 값을 검증할 수 없다는 것이다 — `ANDROID_ID`는 식별자이지 자격 증명이 아니라서, 서버는 앱이 보낸 문자열을 그대로 믿는 것 말고 할 수 있는 일이 없다.

서버가 요청 주체를 스스로 특정하려면 **발급자가 서명하고 수신자가 독립적으로 검증할 수 있는 증표**가 필요했다. 동시에 이 서비스는 회원가입·소셜 로그인 없이 이용자를 구분해야 한다(PRD §1 「비회원 익명 세션」).

제약이 하나 더 있었다. 저장소에는 이미 Firebase가 들어와 있다 — `app/google-services.json`, `mino.android.firebase` 컨벤션 플러그인(google-services + Crashlytics), `:core:analytics`가 BOM으로 Analytics를 쓴다. 인증 제공자를 새로 고르면 SDK·콘솔·백엔드 검증 경로가 하나씩 더 늘어난다.

## 결정

**익명 세션의 발급·저장·복원 주체를 Firebase Authentication 익명 인증에 둔다.** 앱은 세션을 만들지 않고 확보·복원만 한다.

- Firebase Auth 의존은 `:core:data`에만 추가한다. `:core:domain`과 feature 모듈은 SDK 타입을 알지 못하며, SDK 접근은 `:core:data`의 `auth/` 패키지에 둔 `internal` 원천 접근자 안에서 끝난다. 인증 전용 모듈(`:core:auth`)은 만들지 않는다.
- 신원 증명(ID 토큰) 첨부는 Ktor `createClientPlugin` 기반의 `:core:data` 내부 플러그인이 수행하고, `NetworkModule`의 `HttpClient` 구성에 설치한다. 요청 URL의 host가 `BuildConfig.API_BASE_URL`의 host와 **일치할 때만** `Authorization: Bearer`를 붙인다. `ktor-client-auth`의 bearer 제공자는 쓰지 않는다.
- **앱은 ID 토큰과 갱신 수단을 저장하지 않는다.** 유효 기간 관리·갱신은 SDK에 위임하고, 요청 시점마다 강제 갱신 없이 토큰을 요청한다. 신원 증명을 도메인 모델로 승격하지도 않는다 — `:core:data` 내부의 문자열로만 다룬다.
- 사용자 구분의 단일 출처를 이 세션으로 두고, `ANDROID_ID` 기반 경로를 남기지 않고 제거한다.
- 세션은 기기가 아니라 **앱 설치**에 묶인다. 재설치 시 계정·데이터 소멸을 수용하고 복구 수단을 제공하지 않으며, 백업·기기 이전에서 `sharedpref` 도메인을 통째로 제외해 두 기기가 같은 사용자로 잡히지 않게 한다(`android:allowBackup`은 유지).

결정별 상세 근거와 기각한 대안 전문은 [`research.md`](../specs/anonymous-auth-session/research.md) R-001·R-002·R-008·R-009·R-010·R-012·R-013·R-016이 소유한다.

## 근거

Firebase ID 토큰(JWT)이 "발급자가 서명하고 수신자가 독립 검증"이라는 요구를 그대로 충족한다. 서버는 헤더의 토큰을 검증해 사용자 식별자를 꺼내면 되고, 앱은 식별자를 보낼 필요조차 없어진다. 재실행 복원과 만료 전 자동 갱신이 SDK 기능이라 앱이 토큰 저장소를 소유하지 않아도 된다는 것도 크다.

SDK 소속을 `:core:data`로 둔 것은 선택이라기보다 귀결이다. `:core:domain`은 Kotlin JVM 모듈이라 Android·Firebase에 의존할 수 없고, 인증 제공자는 데이터 출처다. 이 기능의 산출물이 Repository 구현 1개·원천 접근자 2쌍·Ktor 플러그인 1개라 모듈을 하나 늘릴 근거도 없다.

첨부를 `HttpClient` 구성에 넣으면 호출하는 feature와 `ApiService`가 인증을 위해 쓰는 코드가 0줄이 된다. `createClientPlugin`의 `onRequest`가 suspend 컨텍스트라 토큰 획득을 그대로 호출할 수 있고, `ktor-client-core`에 포함된 API라 의존도 늘지 않는다. host를 비교하는 이유는 남의 서버로 우리 신원 증명이 나가면 안 되기 때문이다 — baseUrl 상대 경로만 신뢰하면 절대 URL 호출과 리다이렉트가 구멍이 된다.

토큰을 앱이 저장하지 않는 것은 이 설계의 핵심 제약이다. 저장을 이중화하면 SDK 캐시와 앱 캐시 중 어느 쪽이 맞는지 판정할 수 없게 되고, 만료 판정 책임이 앱으로 넘어온다.

## 결과

- **사실상 되돌릴 수 없는 결정이다.** 실제 사용자 계정이 Firebase 쪽에 쌓이므로, 인증 제공자를 교체하면 기존 사용자 전원과 그들의 데이터가 소멸한다.
- 앱이 자격 증명을 저장하지 않으므로 [로컬 키-값 저장소로 Preferences DataStore를 채택한다](2026-07-27-preferences-datastore-local-storage.md)가 남긴 재검토 트리거("민감도가 다른 값(실제 자격 증명 등)을 저장하게 되면 평문 결정을 재검토한다")는 발동하지 않는다. 그 ADR의 결정 자체는 유지되지만, 첫 소비자였던 디바이스 ID 경로가 사라져 `storage/DataStoreModule`은 소비자 없는 상태로 남는다.
- host 판정은 `Flavor.apiBaseUrl`이 플레이스홀더(`https://qa-api.example.com/`)인 동안 **항상 불일치**라 실제로 첨부되는 요청이 없다. 계약과 구조만 성립하며, 실서버 도메인이 확정되는 시점에 자동으로 발효된다.
- 사용자 구분이 필요한 새 작업은 기기 식별자를 다시 만들지 말고 `AnonymousAuthRepository.ensureSession()`을 쓴다. 푸시 알림 수신 대상을 특정하는 **기기 단위** 식별(이슈 #90)은 이 결정의 범위 밖이며, 사용자 단위 식별과 섞지 않는다.
- 실제 계정 승격(소셜 로그인 연동)과 그때의 사용자 식별자 이관, 세션 남용 방어는 범위 밖이다. 승격 경로가 도입될 때 재설치·기기 변경 시의 데이터 복구도 함께 정의한다.
- 이 결정이 유발하는 실패 표현과 재시도 정책은 [도메인 예외 매핑 지점은 원천마다 하나씩 두고, 인증 실패용 `Auth` 리프를 추가한다](2026-08-22-domain-exception-mapping-per-source.md)와 [익명 세션 확보의 재시도·지연 판정은 호출 화면이 소유한다](2026-08-22-session-retry-owned-by-caller.md)가 소유한다.

## 고려한 대안

**서버가 자체 세션 토큰을 발급한다.** Mino 서버가 익명 계정을 만들고 자체 JWT를 발급하는 방식. 서버 작업이 선행되어야 하고 갱신·저장·만료 처리를 앱이 직접 구현해야 한다. 이슈 #176이 Firebase 익명 인증을 전제로 열렸고 서버 전환도 그 전제로 동시에 이뤄지기로 되어 있어 배제했다.

**`ANDROID_ID`를 유지하고 서버 서명을 덧붙인다.** 앱이 보낸 값을 서버가 검증할 근거가 없다는 원래 문제가 그대로 남는다. 이슈 #176이 폐기 대상으로 지목한 방식이다.

**`:core:auth` 신규 모듈을 만든다.** 모듈을 하나 늘릴 만한 산출물 규모가 아니다. `:core:analytics`가 분리되어 있는 이유는 Compose 의존 때문이며 여기엔 해당하지 않는다.

**`ktor-client-auth`의 bearer 제공자를 쓴다.** 토큰 캐시와 401 재시도 기반 갱신 모델을 갖고 있는데 갱신 소유자는 Firebase SDK다. 캐시가 둘이 되어 "어느 쪽이 맞는지 판정할 수 없다"는 문제를 그대로 불러온다. 의존도 하나 늘어난다.

**OkHttp `Interceptor`(엔진 레벨)에서 첨부한다.** Ktor 위에 OkHttp 추상화를 섞게 되고, suspend 토큰 획득을 블로킹으로 바꿔야 한다.

**Mino 전용 `HttpClient`를 따로 만들고 그 클라이언트에만 플러그인을 설치한다.** 판정이 인스턴스 경계로 옮겨가 명시적이지만 클라이언트가 둘로 늘고 엔진·커넥션 풀이 이중화된다. 플레이스홀더 baseUrl이 사라지면 다시 합쳐야 한다.

**토큰을 DataStore에 캐시하고 만료를 앱이 판정한다.** 자격 증명 평문 저장 재검토를 유발하고 SDK 캐시와 이중화된다.

**`IdentityProof(value, expiresAt)` 도메인 모델을 만든다.** 만료 시각을 앱이 들고 판정하게 되어 저장 위임 결정과 충돌한다.

**`DeviceRepository`를 남기고 내부 구현만 Firebase로 교체한다.** 이름은 기기 식별을 가리키는데 실제로는 사용자 세션을 다루게 되어 "단일 출처"라는 표현이 흐려진다. 기기 단위 식별(이슈 #90)이 나중에 같은 이름을 다시 필요로 한다.

**`android:allowBackup="false"`로 앱 전체 백업을 끈다.** 확실하지만 세션 제외라는 범위를 넘고, 이후 백업하고 싶은 데이터가 생겼을 때 되돌려야 한다. Firebase Auth의 저장 파일명만 제외하는 방식도 검토했으나 파일명이 SDK 내부 규약이라 버전 업에 취약해 배제했다.
