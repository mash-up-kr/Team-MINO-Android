# Phase 0 리서치: 비회원 익명 인증 세션

**대상 스펙 경로**: `docs/specs/anonymous-auth-session`

**소속 문서**: [plan.md](./plan.md) — 이 문서는 plan에 종속된 부속 산출물이며 독자 버전을 갖지 않는다.

각 항목은 **어느 plan 버전에서 결정되었는지**를 함께 적는다. 뒤집힌 결정은 지우지 않고 취소선과 `재검토됨(plan X.Y.Z)` 표시를 남긴다.

---

## R-001. 인증 제공자 — Firebase Authentication 익명 인증 (plan 1.0.0)

**Decision**: 익명 세션의 발급·저장·복원 주체를 Firebase Authentication 익명 인증으로 둔다. 앱은 세션을 직접 만들지 않고 확보·복원만 한다.

**Rationale**

- FR-010이 요구하는 "발급자가 서명하고 수신자가 독립적으로 검증할 수 있는 증표"가 Firebase ID 토큰(JWT)으로 바로 충족된다. 서버가 검증해 사용자 식별자를 꺼내는 흐름은 앱 밖의 전제다(spec §3.2) → FR-009.
- 저장소에 Firebase가 이미 들어와 있다 — `app/google-services.json`, `libs.plugins.mino.android.firebase`(google-services + Crashlytics), `:core:analytics`가 BOM으로 Analytics를 쓴다. 인증 제공자를 새로 고르면 SDK·콘솔·백엔드 검증 경로가 하나 더 늘어난다.
- 재실행 복원(FR-002)·만료 전 자동 갱신(FR-012)이 SDK 기능이라 앱이 토큰 저장소를 소유하지 않아도 된다 → R-010.

**Alternatives considered**

- **서버 자체 발급 세션 토큰**: Mino 서버가 익명 계정을 만들고 자체 JWT를 발급한다. 서버 작업이 선행되어야 하고, 갱신·저장·만료 처리를 앱이 직접 구현해야 한다. 이슈 #176이 Firebase 익명 인증을 전제로 열렸고 §4 가정도 "인증 제공자가 발급한 익명 계정 식별자"로의 전환을 서버와 동시에 수행한다고 적고 있어 기각.
- **기존 ANDROID_ID 유지 + 서버 서명 추가**: ANDROID_ID는 식별자이지 자격 증명이 아니며, 앱이 보낸 값을 서버가 검증할 근거가 없다. 이슈 #176이 폐기 대상으로 지목한 방식이다.

---

## R-002. Firebase Auth SDK의 소속 모듈 — `:core:data` (plan 1.0.0)

**Decision**: `com.google.firebase:firebase-auth` 의존을 `:core:data`에만 추가한다. `:core:domain`은 Firebase 타입을 알지 못하며, feature 모듈도 마찬가지다. 버전 카탈로그에 `firebase-auth` 별칭을 추가하고 기존 `firebase-bom`으로 버전을 맞춘다.

**Rationale**

- 헌법 원칙 II와 [`modularization.md`](../../architecture/modularization.md): `:core:domain`은 Kotlin JVM이라 Android·Firebase SDK를 의존할 수 없다. 인증 제공자는 데이터 출처이므로 `:core:data`가 소유한다.
- `:core:analytics`가 `platform(libs.firebase.bom)` + 개별 아티팩트 형태로 이미 같은 패턴을 쓴다. `mino.android.firebase` 컨벤션 플러그인(google-services·Crashlytics 적용)은 `:app`에만 필요하다 — 라이브러리 모듈은 아티팩트 의존만 추가하면 `:app`이 처리한 `google-services.json` 산출물을 런타임에 공유한다.

**Alternatives considered**

- **`:core:auth` 신규 모듈**: 모듈 하나를 늘릴 만한 근거가 없다. 이 기능의 산출물은 Repository 구현 1개·원천 접근자 2개·Ktor 플러그인 1개이며, 전부 `:core:data`의 기존 패키지 구조에 들어간다. 모듈 추가는 헌법 §Governance의 "복잡도는 정당화되어야 한다"를 통과하지 못한다.
- **`:core:analytics`처럼 별도 Firebase 래퍼 모듈**: 위와 같은 이유로 기각. `:core:analytics`가 분리되어 있는 이유는 Compose 의존(`TrackScreenViews`) 때문이며 여기엔 해당하지 않는다.

---

## R-003. Firebase `Task` → suspend 변환 — `kotlinx-coroutines-play-services` (plan 1.0.0)

**Decision**: `kotlinx-coroutines-play-services`를 `:core:data`에 추가하고 `Task<T>.await()`로 변환한다. 코루틴 버전은 기존 `coroutines` 카탈로그 버전에 맞춘다.

**Rationale**

- Firebase Auth API(`signInAnonymously()`·`getIdToken()`)는 `Task<T>` 반환이다. 데이터 레이어 계약은 전부 `suspend`이므로 변환 지점이 필요하다.
- 직접 `suspendCancellableCoroutine`으로 감싸면 취소 전파·예외 원본 보존·중복 재개 방지를 재구현하게 된다. 코루틴 취소가 깨지면 [`error_handling.md`](../../conventions/error_handling.md) §3의 `CancellationException` 보존 규칙이 무너진다.

**Alternatives considered**

- **직접 `suspendCancellableCoroutine` 래핑**: 의존을 늘리지 않는 대신 위 세 가지를 손으로 보장해야 한다. 변환 지점이 예외 매핑 지점(R-006)을 겸하므로 실수의 비용이 크다 — 기각.
- **`Tasks.await()`(블로킹)**: 스레드를 블로킹하며 취소를 지원하지 않는다 — 기각.

---

## R-004. 세션 확보의 멱등 보장 — 현재 사용자 선확인 + `Mutex` 직렬화 (plan 1.0.0)

**Decision**: `AnonymousAuthRepositoryImpl`이 (1) 잠금 밖에서 현재 사용자를 먼저 확인해 확보된 경우 즉시 반환하고, (2) 없을 때만 `Mutex`를 잡고 잠금 안에서 한 번 더 확인한 뒤 익명 로그인을 수행한다.

**Rationale**

- TS-004·SC-001: 이미 확보된 경우 네트워크 왕복 0회여야 한다. 현재 사용자 확인은 SDK가 로컬에 유지한 상태를 읽는 동작이라 왕복이 없다. 잠금 밖 빠른 경로가 이를 보장한다.
- TS-003·SC-004: 동시 호출에도 식별자는 1개여야 한다. 잠금 안 재확인(double-checked)이 없으면 두 호출이 모두 "없음"을 보고 로그인을 두 번 시도할 수 있다.
- 같은 문제를 같은 방식으로 푼 선례가 저장소에 있다 — 제거 대상인 `DeviceRepositoryImpl`이 `Mutex.withLock`으로 직렬화했다(R-013).

**Alternatives considered**

- **Firebase SDK의 내부 직렬화에 의존**: 이미 익명 사용자가 있으면 `signInAnonymously()`가 기존 사용자를 돌려주는 동작이 있으나, 이는 SDK 구현 세부이고 앱 코드에서 검증할 수 없다. TS-003이 앱 계약으로 요구하는 성질을 SDK 동작에 위임하지 않는다 — 기각.
- **`Mutex` 없이 `lazy`·`Deferred` 캐시**: 실패 시 캐시된 실패 `Deferred`가 남아 재시도(FR-005)가 영구히 같은 실패를 재생한다 — 기각.

---

## R-005. 도메인 노출 형태 — `AnonymousSession(userId)` 반환 + UseCase 유지 (plan 1.0.0)

**Decision**: `AnonymousAuthRepository.ensureSession()`이 도메인 모델 `AnonymousSession`을 반환하고, `EnsureAnonymousSessionUseCase`를 `:core:domain/usecase/`에 둔다.

**Rationale**

- TS-001·TS-004·TS-008·TS-010이 "식별자가 발급된다 / 같은 식별자가 복원된다 / 다른 식별자가 발급된다"를 판정 대상으로 삼는다. 반환값이 없으면 이 시나리오들을 계약 수준에서 검증할 수 없다.
- UseCase 유지 근거는 [`core/domain/README.md`](../../../core/domain/README.md) §4의 판정을 적용한 결과다 — 세션 확보는 "단순 표시" 조건을 만족하지 않는다(응답을 화면에 보여주는 조회가 아니라 계정 발급이라는 부수효과를 갖는 행위다).
- 제거되는 `EnsureDeviceIdUseCase`와 같은 자리에 같은 역할로 들어가므로 호출자 관점의 형태가 유지된다.

**Alternatives considered**

- **`Unit` 반환(확보만 하고 값은 감춤)**: 제거 대상인 `DeviceRepository.ensureDeviceId()`의 형태다. FR-009(앱이 식별자를 서버로 보내지 않는다)는 **전송** 금지이지 **노출** 금지가 아니며, spec §2.3이 사용자 식별자를 도메인 개념으로 명시한다. 검증 불가를 대가로 얻는 이득이 없어 기각.
- **`Flow<AnonymousSession?>` 상태 스트림**: 세션은 확보 후 폐기되지 않으므로(FR-014·FR-017) 관찰할 상태 변화가 없다. 호출자가 다뤄야 할 상태를 늘리기만 한다 — 기각.

---

## R-006. Firebase 실패의 도메인 예외 매핑 지점 — `Task` 변환 지점의 화이트리스트 (plan 1.0.0)

**Decision**: Firebase 예외 → `MinoDomainException` 매핑을 **`Task` → suspend 변환 지점 한 곳**에서 화이트리스트 열거로 수행한다(`:core:data`의 `auth/extension/`). 열거 밖 예외는 rethrow해 CEH로 보낸다. `CancellationException`은 매핑하지 않는다.

**Rationale**

- FR-013은 실패를 기존 도메인 예외 체계로 전파하고 별도 에러 처리 경로를 만들지 말 것을 요구한다. Firebase Auth는 Ktor `HttpClient`를 거치지 않으므로 [`error_handling.md`](../../conventions/error_handling.md) §3의 `HttpResponseValidator` 매핑이 닿지 않는다.
- 같은 규약이 매핑에 요구하는 성질은 "지점이 하나여서 누락이 구조적으로 불가능할 것"과 "화이트리스트 열거일 것"이다. 모든 Firebase 호출이 `Task` 변환을 통과하므로 이 지점이 Ktor의 validator와 같은 역할을 한다.
- 이 결정이 유발하는 규약 문서 갱신은 [plan.md](./plan.md) §복잡도 추적 V-1과 §동반 갱신이 필요한 기존 문서가 소유한다.

**Alternatives considered**

- **`AnonymousAuthRepositoryImpl`에서 지역 `try/catch`**: Firebase 호출 지점이 늘어날 때마다 매핑을 따라 붙여야 하고, 빠뜨려도 컴파일이 통과한다. 규약이 데이터소스별 catch를 기각한 이유와 동일 — 기각.
- **Firebase 예외를 그대로 도메인까지 전파**: `:core:domain`이 Firebase 타입을 알게 되어 헌법 원칙 II 위반이며 FR-013도 어긴다 — 기각.

---

## R-007. 실패 2종의 표현 — `Network` 재사용 + `Auth` 리프 신설 (plan 1.0.0)

**Decision**: FR-018이 요구하는 두 갈래를 기존 리프 `Network` 재사용과 신설 리프 `Auth`로 표현한다. 리프의 선언 형태는 [contracts/domain-exception-auth-leaf.md](./contracts/domain-exception-auth-leaf.md), 어떤 원천 예외가 어느 갈래에 속하는지는 [contracts/identity-proof-attachment.md](./contracts/identity-proof-attachment.md) §2가 소유한다.

**Rationale**

- TS-006이 금지하는 것은 "이 기능 전용 에러 타입"이다. 즉 `MinoDomainException` **밖**의 별도 실패 모델(전용 sealed 결과 타입·전용 예외 계층)을 만들지 말라는 요구이며, FR-013도 "기존 도메인 예외 체계로 전파"를 요구한다. `MinoDomainException`에 리프를 더하는 것은 그 체계 안에 머무는 확장이고, [`core/error-handling/README.md`](../../../core/error-handling/README.md) §4가 정식 확장 규칙으로 규정한 동작이다.
- 기존 리프 두 개(`Network`·`Http`)만으로는 FR-018을 만족할 수 없다. "그 밖의 실패"는 HTTP 응답이 아니므로 `Http(code)`로 표현하면 코드 자리에 넣을 값이 없고, 화면이 두 갈래를 가르는 근거도 사라진다.
- 신설 리프는 §4가 금지하는 탈출구(`Unknown`류)가 아니다 — 매핑이 열거된 예외에 한정되기 때문이다.

**Alternatives considered**

- **`Http(code)` 재사용**: 위와 같은 이유로 기각.
- **리프를 늘리지 않고 `Network` 하나로 통합**: FR-018·SC-012가 두 갈래 구분을 요구하므로 요구사항 위반 — 기각.
- **`MinoDomainException` 밖의 전용 결과 타입(`AuthResult` 등)**: TS-006 위반 — 기각.

---

## R-008. 신원 증명 첨부 방식 — Ktor 커스텀 클라이언트 플러그인 (plan 1.0.0)

**Decision**: `createClientPlugin`으로 만든 `:core:data` 내부 플러그인이 요청 전송 직전에 ID 토큰을 얻어 `Authorization: Bearer` 헤더를 붙인다. 플러그인은 `NetworkModule`의 `HttpClient` 구성에 설치한다.

**Rationale**

- FR-008·SC-006: 첨부가 클라이언트 구성에 있으므로 호출자(feature·ApiService)가 인증 코드를 한 줄도 쓰지 않는다.
- `createClientPlugin`의 `onRequest`는 suspend 컨텍스트라 토큰 획득(`suspend`)을 그대로 호출할 수 있다. `defaultRequest`는 suspend가 아니라 이 조건을 만족하지 못한다.
- `ktor-client-core`에 포함된 API라 의존이 늘지 않는다.
- 토큰 획득 실패 시 예외가 요청 호출부로 전파된다. 이미 `MinoDomainException`(R-006·R-007)이므로 호출자의 `runCatchingDomain`이 그대로 잡는다 → EC-008(요청은 실패하되 세션은 폐기하지 않는다).

**Alternatives considered**

- **`ktor-client-auth`의 `bearer` 제공자**: 토큰 캐시·401 재시도 기반 갱신 모델을 갖고 있는데, 갱신 소유자는 Firebase SDK(R-010)라 두 개의 캐시가 생긴다. spec §4 가정 "저장을 이중화하면 어느 쪽이 맞는지 판정할 수 없게 된다"와 정면으로 어긋난다. 의존도 하나 늘어난다 — 기각.
- **OkHttp `Interceptor`(엔진 레벨)**: Ktor 위에 OkHttp 추상화를 섞게 되고, suspend 토큰 획득을 블로킹으로 바꿔야 한다 — 기각.

---

## R-009. 첨부 대상 호스트 판정 — `BuildConfig.API_BASE_URL`의 host 비교 (plan 1.0.0)

**Decision**: 플러그인이 요청 URL의 host를 `BuildConfig.API_BASE_URL`의 host와 비교해, 일치할 때만 헤더를 붙인다.

**Rationale**

- FR-011·TS-016: Mino 서버가 아닌 호스트로 신원 증명이 나가면 안 된다. baseUrl 상대 경로만으로 판정하면 절대 URL 호출·리다이렉트가 새는 구멍이 된다.
- `API_BASE_URL`은 이미 존재한다 — `build-logic`의 `configureFlavors(LibraryExtension)`이 flavor별 `buildConfigField`로 심고 `:core:data`가 `mino.android.flavor`를 적용하고 있다. 판정 기준을 새로 만들지 않고 flavor 단일 출처를 그대로 쓴다.
- **구현 주의**: `NetworkModule`의 현재 baseUrl은 데모용 GitHub API 하드코딩이며([`core/data/README.md`](../../../core/data/README.md) §4의 NOTE), 이를 `API_BASE_URL`로 교체하는 것은 이 스펙의 범위가 아니다. 그때까지는 판정이 항상 불일치라 실제로 첨부되는 요청이 없다 — 계약과 구조는 성립하고, spec §4 가정대로 서버 전환 시점에 baseUrl 교체와 함께 발효된다.

**Alternatives considered**

- **Mino 전용 `HttpClient`를 따로 만들고 그 클라이언트에만 플러그인 설치**: 판정이 인스턴스 경계로 옮겨가 명시적이지만, 클라이언트가 둘로 늘고 [`core/data/README.md`](../../../core/data/README.md) §9의 "단일 클라이언트에 여러 baseUrl 혼용" 회피 지침과 별개로 엔진·커넥션 풀이 이중화된다. 데모 baseUrl이 사라지면 클라이언트를 다시 합쳐야 해 기각.
- **`defaultRequest`의 상대 경로 신뢰**: 절대 URL 호출을 막지 못해 TS-016을 보장할 수 없다 — 기각.

---

## R-010. 신원 증명의 저장·갱신 소유자 — Firebase SDK에 위임 (plan 1.0.0)

**Decision**: 앱은 ID 토큰과 갱신 수단을 저장하지 않는다. 요청 시점마다 SDK에 토큰을 요청하고(강제 갱신 없이), 유효 기간 관리·갱신은 SDK가 수행한다.

**Rationale**

- FR-012·SC-007·EC-007: 만료를 원인으로 한 요청 실패가 0건이어야 한다. SDK는 유효한 캐시 토큰이 있으면 그대로 주고, 만료가 임박하면 스스로 갱신한 뒤 준다.
- spec §4 가정이 명시적으로 앱의 이중 저장을 금지한다 — "저장을 이중화하면 어느 쪽이 맞는지 판정할 수 없게 된다".
- 앱이 토큰을 보관하지 않으므로 [DataStore 채택 ADR](../../adr/2026-07-27-preferences-datastore-local-storage.md)이 남긴 "실제 자격 증명을 저장하게 되면 평문 결정을 재검토한다"는 재검토 트리거가 발동하지 않는다.

**Alternatives considered**

- **토큰을 DataStore에 캐시하고 만료를 앱이 판정**: 자격 증명 평문 저장 재검토를 유발하고 SDK 캐시와 이중화된다 — 기각.
- **매 요청 강제 갱신**: 요청마다 인증 제공자 왕복이 생겨 SC-001·성능 목표에 어긋난다 — 기각.

---

## R-011. 자동 재시도·지연 판정의 소유자 — 호출자(진입 화면) (plan 1.0.0)

**Decision**: FR-005의 자동 재시도와 FR-019의 지연 판정은 호출자가 소유한다. `:core:domain`·`:core:data`는 **멱등한 1회 확보**만 계약으로 제공하고 재시도 루프·백오프·임계 시간을 갖지 않는다.

**Rationale**

- spec §3.2가 임계 시간·안내 문구·표시 위치를 진입 화면 스펙 소관으로 명시했고, FR-019도 "구체 임계 시간은 진입 화면 스펙이 정한다"고 못박았다. 데이터 레이어에 임계를 두면 두 곳이 같은 값을 갖게 된다.
- 재시도의 종료 조건이 "진입 화면을 벗어났는가"(FR-016)라 화면 수명주기에 묶인다. 데이터 레이어는 그 수명주기를 알지 못한다.
- 재호출이 멱등하고 부작용이 없으므로(FR-004·R-004) 호출자가 루프를 도는 것으로 충분하다.
- 호출자가 지연 상한을 거는 방식과 그때의 함정은 계약 C-7이 소유한다.

**Alternatives considered**

- **UseCase가 백오프 재시도를 소유**: 임계·간격이 화면 정책인데 domain에 상수로 박히게 되고, 화면이 "지연 중" 상태를 표시할 시점을 알 수 없다(UseCase가 반환하기 전까지 진행 상황이 보이지 않는다) — 기각.
- **`WorkManager`·`ProcessLifecycle` 기반 백그라운드 재시도**: FR-016이 진입 화면 체류를 요구하므로 화면 밖 실행 주체가 필요 없다 — 기각.

---

## R-012. 백업·기기 이전 제외 — `sharedpref` 도메인 전체 제외 (plan 1.0.0)

**Decision**: `:app`의 `res/xml/backup_rules.xml`(API 30 이하)과 `res/xml/data_extraction_rules.xml`(API 31 이상, `cloud-backup`·`device-transfer` 양쪽)에서 `sharedpref` 도메인을 통째로 제외한다. `android:allowBackup="true"`는 유지한다.

**Rationale**

- FR-007·EC-005·SC-008: 백업·기기 이전을 거친 단말이 원본과 같은 사용자로 잡히면 안 된다. Firebase Auth는 세션을 앱 프라이빗 `SharedPreferences`에 유지하므로 이 도메인이 제외 대상이다.
- `minSdk = 29`라 두 파일이 모두 유효 구간을 갖는다. 한쪽만 고치면 API 30 이하 또는 31 이상 중 한쪽이 뚫린다.
- 파일 단위가 아니라 도메인 단위로 제외하는 이유는 판정이 SDK 내부 파일명 규칙에 의존하지 않게 하기 위해서다. SDK가 파일명을 바꾸면 파일 단위 규칙은 조용히 무력화된다.
- 현재 저장소에서 `SharedPreferences`를 쓰는 앱 코드가 없어(로컬 저장은 DataStore) 과잉 제외로 잃는 것이 없다. DataStore는 `file` 도메인이라 이 규칙에 걸리지 않는다.

**Alternatives considered**

- **`android:allowBackup="false"`**: 확실하지만 앱 전체의 백업을 끄는 결정이라 이 스펙의 범위(세션 제외)를 넘고, 이후 백업하고 싶은 데이터가 생겼을 때 되돌려야 한다 — 기각.
- **Firebase Auth의 저장 파일명만 제외**: 파일명이 SDK 내부 규약이라 버전 업에 취약하다 — 기각.

---

## R-013. 기존 기기 식별자 경로의 제거 범위 (plan 1.0.0)

**Decision**: FR-015에 따라 아래를 삭제한다. `storage/DataStoreModule`과 `androidx.datastore.preferences` 의존은 **남긴다**.

| 모듈 | 삭제 대상 |
|---|---|
| `:core:domain` | `repository/DeviceRepository.kt` · `usecase/EnsureDeviceIdUseCase.kt` |
| `:core:data` | `repository/DeviceRepositoryImpl.kt` · `repository/di/DeviceRepositoryModule.kt` · `datasource/DeviceIdLocalDataSource.kt` · `datasource/DeviceIdLocalDataSourceImpl.kt` · `datasource/di/DeviceDataSourceModule.kt` · `device/` 패키지 전체(`DeviceInfoProvider`·`DeviceInfoProviderImpl`·`di/DeviceInfoProviderModule.kt`) |
| `:core:data` 테스트 | `repository/DeviceRepositoryImplTest.kt` · `datasource/FakeDeviceIdLocalDataSource.kt` · `device/FakeDeviceInfoProvider.kt` |

**Rationale**

- 조사 결과 `EnsureDeviceIdUseCase`·`DeviceRepository`를 호출하는 코드가 `:core` 밖에 하나도 없다. spec §4 가정("현재 어떤 화면에서도 사용되지 않으므로 제거해도 기존 사용자 경험이 변하지 않는다")이 실제 코드와 일치한다 → TS-007.
- `device/` 패키지가 통째로 사라지면서 `ANDROID_ID` 접근과 `@SuppressLint("HardwareIds")`도 함께 사라진다.
- `DataStoreModule`을 남기는 이유: 그 결정의 소유자는 [DataStore 채택 ADR](../../adr/2026-07-27-preferences-datastore-local-storage.md)이고, spec은 기기 식별자 **경로**의 제거만 요구한다. 소비자가 없다는 이유로 인프라와 ADR 결정을 함께 걷어내는 것은 요청 범위를 넘는다(헌법 §에이전트 행동 규칙). 소비자 부재 상태 자체는 완료 보고에 후속 과제로 남긴다.

**Alternatives considered**

- **`DeviceRepository`를 남기고 내부 구현만 Firebase로 교체**: 이름이 기기 식별을 가리키는데 실제로는 사용자 세션을 다루게 되어 FR-015가 요구한 "단일 출처" 표현이 흐려진다. 푸시 기기 식별(이슈 #90)이 나중에 같은 이름을 다시 필요로 한다 — 기각.
- **`DataStoreModule`까지 삭제**: 위 이유로 기각.

---

## R-014. `:core:data` 내부 배치 — `auth/` 원천 접근자 + `repository/` (plan 1.0.0)

**Decision**: Firebase Auth SDK 접근자를 `auth/` 패키지에 인터페이스·구현체 쌍으로 두고, `AnonymousAuthRepositoryImpl`이 이를 직접 주입받는다. 별도의 `AnonymousAuthRemoteDataSource` 계층은 두지 않는다.

**Rationale**

- [`core/data/README.md`](../../../core/data/README.md) §3이 "DataStore 밖의 기기 원천(시스템 설정 등)"을 `device/`에 두는 선례를 갖고 있고, 삭제되는 `DeviceRepositoryImpl`이 그 접근자를 DataSource 없이 직접 주입받았다. 인증 제공자 SDK도 같은 성격의 원천이다.
- DataSource 계층을 끼우면 DTO도 매퍼도 없는 순수 위임 클래스가 인터페이스·구현체·DI 모듈 3개로 늘어난다. §5가 DataSource에 요구하는 "데이터 출처 추상화" 역할은 `auth/`의 인터페이스가 이미 수행한다.
- 인터페이스로 감싸는 것 자체는 유지한다 — Firebase 타입을 `Repository`에서 지우고 JVM 단위 테스트를 가능하게 하는 경계가 여기다(R-015).
- 관심사별로 인터페이스를 둘로 나눈다: 세션 확보(`AnonymousAuthProvider`)와 신원 증명 획득(`IdTokenProvider`). 사용자가 다르다 — 전자는 Repository, 후자는 네트워크 플러그인이다.

**Alternatives considered**

- **`datasource/AnonymousAuthRemoteDataSource` 추가**: 위임만 하는 계층이라 기각. 로컬 저장이 생기거나 Firebase 외 출처가 붙으면 그때 도입한다.
- **단일 인터페이스에 세션 확보와 토큰 획득을 함께 둠**: 네트워크 플러그인이 필요 없는 `signInAnonymously`까지 알게 된다 — 기각.

---

## R-015. 테스트 전략 — Fake 기반 JVM 단위 테스트 (plan 1.0.0)

**Decision**: `auth/` 인터페이스의 Fake 구현으로 `AnonymousAuthRepositoryImpl`을 JVM 단위 테스트하고, 예외 매핑은 매핑 함수 단위로 검증한다. 헤더 첨부·비첨부는 `ktor-client-mock`으로 검증한다. Firebase SDK 자체를 띄우는 테스트는 두지 않는다.

**Rationale**

- 저장소의 기존 방식과 같다 — 삭제되는 `DeviceRepositoryImplTest`가 `FakeDeviceIdLocalDataSource`·`FakeDeviceInfoProvider`로 같은 구조를 썼고, `DomainExceptionMappingTest`가 `ktor-client-mock`으로 매핑을 검증한다. 새 도구가 필요 없다.
- TS-003(동시 호출)·TS-004(재호출 무왕복)는 Fake의 호출 횟수로 판정할 수 있어 계측 테스트가 필요 없다.
- 실기기에서만 판정 가능한 시나리오(TS-009 오프라인 복원, TS-010 재설치, TS-011 백업 제외)는 [quickstart.md](./quickstart.md)의 수동 검증 절차로 남긴다.

**Alternatives considered**

- **Firebase Auth Emulator 연동 계측 테스트**: 에뮬레이터 구성·CI 배선이 선행되어야 하는데 저장소에 PR CI 자체가 없다(헌법 §검증 장치의 한계). 이 스펙의 범위를 넘는다 — 기각. PR을 검증하는 CI가 도입되면(헌법 §검증 장치의 한계) 재검토한다.

---

## R-016. 신원 증명을 도메인 모델로 승격하지 않는다 (plan 1.0.0)

**Decision**: spec §2.3의 "신원 증명"에 대응하는 도메인 모델을 만들지 않는다. `:core:data` 내부의 문자열로만 다룬다.

**Rationale**

- 앱은 신원 증명을 저장·판정하지 않고 요청 첨부 직전에 얻어 쓰고 버린다(R-010). 도메인·feature 어느 쪽도 이 값을 읽지 않으며, 유일한 소비자가 네트워크 플러그인이다.
- 모델로 승격하면 유효 기간·갱신 상태를 앱이 들고 있다는 인상을 주어, 저장 이중화를 금지한 spec §4 가정과 어긋나는 구현을 유도한다.
- spec §2.3이 도메인 개념으로 서술한 것과 도메인 **모델**이 필요한 것은 다르다 — 개념은 계약([contracts/identity-proof-attachment.md](./contracts/identity-proof-attachment.md))이 표현한다.

**Alternatives considered**

- **`IdentityProof(value, expiresAt)` 모델 신설**: 만료 시각을 앱이 들고 판정하게 되어 R-010과 충돌한다 — 기각.
- **`AnonymousSession`에 신원 증명 필드 추가**: 세션은 영속이고 신원 증명은 요청 단위라 수명이 다르다. 한 모델에 담으면 세션을 얻을 때마다 증명을 발급하게 된다 — 기각.
