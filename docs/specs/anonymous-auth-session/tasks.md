# 작업 목록: 비회원 익명 인증 세션

**대상 스펙 경로**: `docs/specs/anonymous-auth-session`

**기준 plan 버전**: 1.1.0

**최초 작성일**: 2026-08-22

**최종 수정일**: 2026-08-23

**사전 조건**: [plan.md](./plan.md) · [spec.md](./spec.md) · [research.md](./research.md) · [data-model.md](./data-model.md) · [contracts/](./contracts/) · [quickstart.md](./quickstart.md)

**테스트**: 포함한다. plan §기술 컨텍스트가 `:core:data`의 JVM 단위 테스트를 산출물로 지정했고, [quickstart.md](./quickstart.md) §2가 자동 검증 범위를 확정했다. 테스트는 대응 구현보다 먼저 작성하고 실패(red)를 확인한다.

**구성 방식**: 각 스토리를 독립적으로 구현하고 테스트할 수 있도록 작업을 사용자 스토리별로 묶는다.

**범위 조정**: 세션 확보를 호출하는 코드는 이번 범위 밖이다 — plan 1.1.0이 §세션 확보의 호출자에서 이를 확정했다. 파급은 [미결 사항 및 커버리지 공백](#미결-사항-및-커버리지-공백) N-1이 소유한다.

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. **한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.** 개정으로 추가되는 작업은 문서에 존재하는(폐기 섹션 포함) 최대 번호 + 1부터 부여하므로, 개정을 거치면 문서 순서와 ID 순서는 어긋날 수 있다. 실행 순서는 Phase 순서와 "의존성 및 실행 순서" 섹션이 말한다.
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: 이 작업이 속한 사용자 스토리 (US1·US2·US3 — [spec.md](./spec.md) §1의 유저 플로우 1·2·3에 대응)
- 설명에는 정확한 파일 경로를 포함할 것

## 경로 규칙

다중 Gradle 모듈이다. 경로는 저장소 루트 기준 전체 경로로 적는다. 모듈별 소스 루트는 plan §프로젝트 구조가 소유한다.

---

## Phase 1: 셋업 (공통 인프라)

**목적**: 이후 모든 코드가 컴파일되기 위한 의존성과 실행 환경을 갖춘다

- [X] T001 [P] `gradle/libs.versions.toml`에 `firebase-auth`(버전 없이 `firebase-bom`으로 정렬)와 `kotlinx-coroutines-play-services`(`coroutines` 버전 참조) 라이브러리 별칭 추가 — 근거 [research.md](./research.md) R-002 · R-003
- [X] T002 `core/data/build.gradle.kts`에 `implementation(platform(libs.firebase.bom))` · `libs.firebase.auth` · `libs.kotlinx.coroutines.play.services` 추가 (T001 에 의존). `:core:domain`·`:core:error-handling`의 빌드 스크립트는 건드리지 않는다 — 헌법 원칙 II
- [ ] T003 [P] Firebase 콘솔에서 이 프로젝트의 익명 인증 제공자가 사용 설정되어 있고 `app/google-services.json`의 flavor별 applicationId(`com.mino.gguk.qa`·`com.mino.gguk`)가 등록되어 있는지 확인 — [quickstart.md](./quickstart.md) §1 P-1 · P-2. 코드 변경 없음

**체크포인트**: `./gradlew :core:data:assembleQaDebug`가 통과하고 Firebase Auth 타입을 `:core:data`에서 참조할 수 있다

---

## Phase 2: 기반 작업 (공통 인프라)

**목적**: 인증 제공자 SDK를 감싸는 원천 접근자 계층과 예외 매핑 지점. US1의 Repository와 US3의 Ktor 플러그인이 각각 이 위에 얹힌다

**⚠️ 중요**: T004는 US1·US3의 실패 표현이 공통으로 참조한다. T005는 US1이, T006은 US3이 쓴다. 실행 순서는 단계가 아니라 이 의존 관계가 정한다(아래 [의존성 및 실행 순서](#의존성-및-실행-순서))

- [X] T004 [P] `core/error-handling/src/main/kotlin/team/mino/core/errorhandling/MinoDomainException.kt`에 리프 `class Auth(cause: Throwable)` 추가 — 선언 형태는 [contracts/domain-exception-auth-leaf.md](./contracts/domain-exception-auth-leaf.md) §1이 소유한다. 기존 리프·생성자는 손대지 않는다
- [X] T005 [P] `core/data/src/main/java/team/mino/core/data/auth/AnonymousAuthProvider.kt`에 `internal interface AnonymousAuthProvider`(`currentUserId()` · `signInAnonymously()`) 선언 — 시그니처는 [contracts/identity-proof-attachment.md](./contracts/identity-proof-attachment.md) §1이 소유한다
- [X] T006 [P] `core/data/src/main/java/team/mino/core/data/auth/IdTokenProvider.kt`에 `internal interface IdTokenProvider`(`getIdToken()`) 선언 — 같은 계약 §1
- [X] T007 `core/data/src/test/java/team/mino/core/data/auth/FirebaseAuthExceptionMappingTest.kt` 작성 — 연결 실패 → `Network`, 발급 실패 → `Auth`, 열거 밖 예외 → 원본 그대로, `CancellationException` → 원본 그대로. 네 케이스는 [quickstart.md](./quickstart.md) §2 표가 소유한다 (T004 에 의존, T008 보다 먼저 작성하고 실패를 확인한다)
- [X] T008 `core/data/src/main/java/team/mino/core/data/auth/extension/`에 `Task<T>.awaitDomain()`과 그것이 호출하는 매핑 함수 구현(파일명은 이 작업에서 확정한다 — 계약이 디렉터리까지만 지목한다) — 화이트리스트 분류 기준은 [contracts/identity-proof-attachment.md](./contracts/identity-proof-attachment.md) §2, 매핑의 성질(열거 밖 rethrow · `CancellationException` 보존)은 [`error_handling.md`](../../conventions/error_handling.md) §3이 소유한다. 각 갈래의 SDK 예외 클래스 목록을 이 작업에서 확정하고, 열거를 넓히는 상위 타입 분기를 두지 않는다. 매핑 함수는 T007이 `Task` 없이 단위 검증할 수 있도록 `internal`로 분리한다 (T004 · T007 에 의존)
- [X] T009 [P] `core/data/src/main/java/team/mino/core/data/auth/AnonymousAuthProviderImpl.kt` 구현 — `FirebaseAuth`의 현재 사용자 조회(왕복 없음)와 익명 로그인을 T008을 통과시켜 노출한다 (T005 · T008 에 의존)
- [X] T010 [P] `core/data/src/main/java/team/mino/core/data/auth/IdTokenProviderImpl.kt` 구현 — 현재 세션의 ID 토큰을 강제 갱신 없이 획득하고, 세션이 없으면 `null`을 반환한다. 토큰을 앱 저장소에 쓰지 않는다 — [research.md](./research.md) R-010 (T006 · T008 에 의존)
- [X] T011 [P] `core/data/src/main/java/team/mino/core/data/auth/di/FirebaseAuthModule.kt`에 `FirebaseAuth` 인스턴스를 `@Provides`로 제공 (T002 에 의존)
- [X] T012 `core/data/src/main/java/team/mino/core/data/auth/di/AuthProviderModule.kt`에 두 원천 접근자의 `@Binds` 바인딩 추가 — 구현체는 `internal`로 닫는다. 바인딩 소유 규칙은 [`dependency-injection.md`](../../conventions/dependency-injection.md) (T009 · T010 에 의존)

**체크포인트**: T009까지 끝나면 US1을, T010까지 끝나면 US3을 시작할 수 있다. 다만 T018을 만든 뒤에는 T012의 바인딩이 있어야 `:core:data`가 컴파일된다

---

## Phase 3: 사용자 스토리 1 - 앱을 처음 켠 사용자의 세션 확보

**목표**: 세션이 없는 사용자에게 인증 제공자가 발급한 익명 세션을 하나 확보해 주고, 몇 번을 동시에 요청해도 세션이 하나만 생기게 한다

**독립 테스트**: `./gradlew :core:data:testQaDebugUnitTest`의 `AnonymousAuthRepositoryImplTest` — Fake가 세는 익명 로그인 호출 횟수로 최초 확보(1회) · 재호출(증가 없음) · 동시 N 호출(1회)을 판정한다. 실기기 판정(V-1·V-4·V-6)은 N-1이 소유한다

### 사용자 스토리 1 테스트 ⚠️

> **이 테스트들을 먼저 작성하고, 구현 전에 실패하는지 반드시 확인한다**

- [X] T013 [P] [US1] `core/data/src/test/java/team/mino/core/data/auth/FakeAnonymousAuthProvider.kt` 작성 — 현재 사용자 유무를 조작할 수 있고 `signInAnonymously()` 호출 횟수를 세며, 지정한 예외를 던질 수 있다 (T005 에 의존)
- [X] T014 [US1] `core/data/src/test/java/team/mino/core/data/repository/AnonymousAuthRepositoryImplTest.kt` 작성 — 최초 확보 · 재호출 무왕복(TS-004 · SC-001) · 동시 호출 1회 발급(TS-003 · SC-004) · 실패 시 도메인 예외 전파. 동시성 검증은 `kotlinx-coroutines-test`를 쓴다. T018이 없는 동안에는 컴파일 실패가 red다 (T013 · T016 에 의존)

### 사용자 스토리 1 구현

- [X] T015 [P] [US1] `core/domain/src/main/kotlin/team/mino/core/domain/model/AnonymousSession.kt`에 `data class AnonymousSession(val userId: String)` 선언 — 필드 의미·수명은 [data-model.md](./data-model.md) §1. 앱 측 값 검증을 넣지 않는다
- [X] T016 [US1] `core/domain/src/main/kotlin/team/mino/core/domain/repository/AnonymousAuthRepository.kt`에 `suspend fun ensureSession(): AnonymousSession` 선언 — 동작·실패 계약은 [contracts/anonymous-auth-repository.md](./contracts/anonymous-auth-repository.md) §2 (T015 에 의존)
- [X] T017 [US1] `core/domain/src/main/kotlin/team/mino/core/domain/usecase/EnsureAnonymousSessionUseCase.kt` 작성 — Repository로의 위임만 하고 규칙을 더하지 않는다. 형태는 같은 계약 §3 (T016 에 의존)
- [X] T018 [US1] `core/data/src/main/java/team/mino/core/data/repository/AnonymousAuthRepositoryImpl.kt` 구현 — 잠금 밖 현재 사용자 선확인(빠른 경로) → 없을 때만 `Mutex` 획득 → 잠금 안 재확인 → 익명 로그인. 구조와 그 이유는 [research.md](./research.md) R-004. 스스로 타임아웃·재시도를 두지 않는다(R-011) (T005 · T008 · T016 에 의존)
- [X] T019 [US1] `core/data/src/main/java/team/mino/core/data/repository/di/AnonymousAuthRepositoryModule.kt`에 `@Binds` 바인딩 추가 (T018 에 의존)

**체크포인트**: 세션 확보가 계약대로 동작하고 단위 테스트가 green이다. 이 시점의 호출자는 테스트뿐이다(N-1)

---

## Phase 4: 사용자 스토리 2 - 앱을 다시 켠 사용자의 세션 복원

**목표**: 재실행 시 네트워크 없이 같은 사용자로 이어지되, 그 세션이 백업·기기 이전을 타고 다른 기기로 복제되지 않게 한다

**독립 테스트**: 복원 경로 자체는 T018의 빠른 경로가 제공하며 T014의 재호출 케이스가 판정한다. 백업 제외는 [quickstart.md](./quickstart.md) §3 V-9(`adb shell bmgr`)로 판정하며, 실행 조건은 N-1이 소유한다

### 사용자 스토리 2 구현

- [X] T020 [P] [US2] `app/src/main/res/xml/backup_rules.xml`에서 `sharedpref` 도메인을 통째로 제외 (API 30 이하 경로) — 파일 단위가 아니라 도메인 단위로 제외하는 이유는 [research.md](./research.md) R-012. `android:allowBackup`은 그대로 둔다
- [X] T021 [P] [US2] `app/src/main/res/xml/data_extraction_rules.xml`의 `cloud-backup`·`device-transfer` **양쪽**에서 `sharedpref` 도메인 제외 (API 31 이상 경로) — 한쪽만 고치면 반대 구간이 뚫린다(R-012)

**체크포인트**: 두 파일 모두에서 `sharedpref`가 제외되어 `minSdk 29`~`targetSdk 36` 전 구간이 덮인다

---

## Phase 5: 사용자 스토리 3 - 서버가 요청자를 검증하는 흐름

**목표**: Mino 서버로 나가는 요청에만 신원 증명이 자동으로 실리고, 호출하는 feature 모듈은 인증 코드를 한 줄도 쓰지 않는다

**독립 테스트**: `./gradlew :core:data:testQaDebugUnitTest`의 헤더 첨부 테스트 — `ktor-client-mock`으로 요청을 가로채 Mino host 요청에는 `Authorization: Bearer`가 있고, 외부 host 요청에는 없으며, 어느 요청에도 앱이 만든 사용자 식별자가 없음을 판정한다

### 사용자 스토리 3 테스트 ⚠️

> **이 테스트들을 먼저 작성하고, 구현 전에 실패하는지 반드시 확인한다**

- [X] T022 [P] [US3] `core/data/src/test/java/team/mino/core/data/network/FakeIdTokenProvider.kt` 작성 — 반환할 토큰과 `null`, 예외를 지정할 수 있다 (T006 에 의존)
- [X] T023 [US3] `core/data/src/test/java/team/mino/core/data/network/IdentityProofAttachmentTest.kt` 작성 — Mino host 첨부(TS-013) · 외부 host 미첨부(TS-016 · FR-011) · 헤더·본문·쿼리에 앱 생성 식별자 없음(TS-014 · FR-009) · Mino host인데 토큰이 `null`이면 요청이 나가지 않고 예외가 전파됨(A-3). 기준 host는 `BuildConfig.API_BASE_URL`에서 얻는다. 기존 `DomainExceptionMappingTest`의 `ktor-client-mock` 구성을 따른다. T024가 없는 동안에는 컴파일 실패가 red다 (T022 에 의존, T024 보다 먼저 작성한다)

### 사용자 스토리 3 구현

- [X] T024 [US3] `core/data/src/main/java/team/mino/core/data/network/plugin/`에 `createClientPlugin` 기반 첨부 플러그인 구현(파일명은 이 작업에서 확정한다) — 요청 host와 `BuildConfig.API_BASE_URL`의 host를 비교해 일치할 때만 `Authorization: Bearer`를 붙인다. 계약 A-1~A-6은 [contracts/identity-proof-attachment.md](./contracts/identity-proof-attachment.md) §3이 소유한다. A-3 위반은 도메인 예외로 감싸지 않는다 (T006 · T023 에 의존)
- [X] T025 [US3] `core/data/src/main/java/team/mino/core/data/network/di/NetworkModule.kt` 수정 — `provideHttpClient`가 `IdTokenProvider`를 주입받아 T024의 플러그인을 설치한다. 기존 `convertDomainException`·`ContentNegotiation`·`Logging` 구성은 그대로 둔다 (T012 · T024 에 의존)

**체크포인트**: 첨부·비첨부가 단위 테스트로 판정되고, `ApiService`·feature 어느 쪽도 인증 코드를 갖지 않는다(SC-006)

---

## Phase 6: 마무리 및 공통 관심사

**목적**: 대체 경로가 선 뒤에 옛 경로를 걷어내고, 이 설계로 어긋나게 된 문서를 맞춘다

- [X] T026 기기 식별자 기반 경로 삭제 — 대상 13개 파일 목록은 [research.md](./research.md) R-013 표가 소유한다(`:core:domain` 2 · `:core:data` 8 · `:core:data` 테스트 3). `storage/DataStoreModule.kt`와 `androidx.datastore.preferences` 의존은 **남긴다**. 삭제 후 [quickstart.md](./quickstart.md) §2 V-10의 `grep`이 빈 결과인지 확인한다(TS-007 · FR-015) (T019 · T025 에 의존 — 대체 경로가 선 뒤에 지운다)
- [X] T027 [P] [`docs/conventions/error_handling.md`](../../conventions/error_handling.md) §2·§3 갱신 — 매핑 지점이 Ktor validator 하나라는 서술에 인증 제공자 지점을 더한다. 근거는 plan §복잡도 추적 V-1 (T008 에 의존)
- [X] T028 [P] [`core/error-handling/README.md`](../../../core/error-handling/README.md) §4 갱신 — "리프는 `:core:data`의 validator 화이트리스트와 짝으로 추가한다"의 짝이 둘이 된다 (T004 · T008 에 의존)
- [X] T029 [P] [`core/data/README.md`](../../../core/data/README.md) §3·§5 갱신 — 디렉터리 트리·패키지 역할 표에서 `device/`를 지우고 `auth/`를 넣는다 (T026 에 의존)
- [ ] T030 [quickstart.md](./quickstart.md) §2 검증 실행 — `:app:assembleQaDebug` · `:core:data:testQaDebugUnitTest` · `:core:data:lintQaDebug`·`:app:lintQaDebug`. Lint 데몬이 죽는 경우의 판정은 헌법 §검증 장치의 한계를 따른다 (T026 에 의존)

---

## 미결 사항 및 커버리지 공백

작업으로 만들지 않은 항목과 그 이유다. 근거 없는 작업을 만들지 않는 대신 여기에 드러낸다.

| # | 항목 | 사유 · 소유자 |
|---|---|---|
| N-1 | **세션 확보를 호출하는 코드가 없다** | plan §세션 확보의 호출자가 임시 배선을 범위 밖으로 확정했다(1.0.0에서 1.1.0으로 개정하며 이관). 파급: `ensureSession()`을 부르는 프로덕션 코드가 없어 [quickstart.md](./quickstart.md) §3의 V-1~V-9를 실행할 주체가 없고, 이번 범위의 판정선은 §2까지다. 호출자 계약 C-1~C-8과 그것이 지탱하는 FR-003·FR-005·FR-016·FR-018·FR-019·UX-001~004는 진입 화면 스펙(PRD [SCR-001])이 소유한다 |
| N-2 | **FR-007의 실기기 확인이 함께 미뤄진다** — TS-011 · SC-008 | T020·T021로 규칙 자체는 발효되지만, V-9는 세션이 확보된 앱을 백업·복원해야 판정된다. N-1로 그 상태를 만들 수단이 없다 |
| N-3 | **Mino host 판정이 실제로 성립하지 않는다** — FR-008 · FR-011 · TS-013 · TS-016 · SC-005 · SC-006 | plan §전제와 이연 항목. `BuildConfig.API_BASE_URL`이 `https://qa-api.example.com/` 플레이스홀더라 실제 요청에는 첨부가 일어나지 않는다. 계약과 구조는 T023·T024로 성립하며, `build-logic`의 `Flavor.apiBaseUrl`에 실서버 도메인이 들어가는 것은 서버 전환 시점의 별도 과제다 |
| N-4 | **인증 제공자 예외 클래스 목록이 미확정** | 분류 기준은 [contracts/identity-proof-attachment.md](./contracts/identity-proof-attachment.md) §2가 확정했고, 각 갈래에 속하는 SDK 클래스 열거는 T008에서 확정한다. 결정 대기 항목이 아니다 |
| N-5 | **`storage/DataStoreModule`의 소비자가 사라진다** | T026이 마지막 소비자를 지운다. 삭제는 plan §복잡도 추적 V-3에 따라 하지 않으며, 소비자 부재 상태는 후속 과제로 보고한다 |
| N-6 | **ADR 승격 3건 — 해소됨(plan 1.1.0)** | 2026-08-22에 3건이 작성되어 게이트 G-3의 판정 근거가 갱신됐다. 대응 관계는 [research.md](./research.md) 헤더의 승격표가 소유한다. 이 목록에 남길 작업은 없다 |

### 요구사항 커버리지

| 요구사항 | 대응 |
|---|---|
| FR-001 · FR-004 | T018 (T014가 판정) |
| FR-002 | T018 빠른 경로 (T014 재호출 케이스) |
| FR-003 | N-1 — 호출 시점은 진입 화면 소관 |
| FR-005 · FR-016 · FR-019 | N-1 — 재시도·차단·지연은 호출자 소유(R-011) |
| FR-006 | 인증 제공자 동작. 코드 작업 없음 (V-8 수동) |
| FR-007 | T020 · T021 (검증은 N-2) |
| FR-008 · FR-011 · FR-009 | T024 · T025 (발효는 N-3) |
| FR-010 · FR-012 | 인증 제공자·서버 소관. T010이 강제 갱신을 하지 않음으로써 위임한다 (R-010) |
| FR-013 · FR-018 | T004 · T007 · T008 |
| FR-014 · FR-017 | 폐기 경로를 두지 않는 것이 요구사항. 코드 작업 없음 — [contracts/anonymous-auth-repository.md](./contracts/anonymous-auth-repository.md) §2 영속성 계약 |
| FR-015 | T026 |
| UX-001~004 | N-1 |
| SC-001 · SC-004 | T014 |
| SC-002 · SC-003 | 측정 기준이 이번 범위에 없다 — SC-002의 임계 시간은 진입 화면 스펙 소관(N-1), SC-003의 단말 100대는 검증 수단이 없다 |
| SC-005 | 서버 소관 (spec §3.2 비목표) |
| SC-006 | T024 · T025 — 호출자가 인증 코드를 쓰지 않는 구조로 충족 |
| SC-007 | T010 (강제 갱신을 하지 않음으로써 SDK에 위임) |
| SC-008 | T020 · T021 (검증은 N-2) |
| SC-009 | T023이 앱 측 A-3(세션 없이 요청이 나가지 않음)만 판정한다. 진입 차단은 N-1 |
| SC-010 · SC-011 | 코드 작업 없음 — 각각 FR-017 · N-1과 같은 이유 |
| SC-012 | T004가 두 갈래를 가를 리프를 만든다. 두 안내의 표현 자체는 N-1 |

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **셋업 (Phase 1)**: 의존성 없음 — 즉시 시작 가능
- **기반 작업 (Phase 2)**: T004는 셋업과 무관하게 시작 가능(`:core:error-handling`은 Firebase를 모른다). 나머지는 T002에 의존
- **사용자 스토리 (Phase 3~5)**: 기반 단계 **전체**가 아니라 자기가 쓰는 산출물에만 의존한다
  - US1: T005 · T008 · T016. 단 T018을 포함해 `:core:data`를 컴파일하려면 T009 · T012의 바인딩이 함께 있어야 한다 — Dagger는 그래프를 컴파일 타임에 검증하므로 `@Binds` 누락은 런타임 오류가 아니라 빌드 실패다
  - US2: 의존 없음 — Phase 1 이전에도 시작 가능
  - US3: T006 · T008 · T012
- **마무리 (Phase 6)**: T026은 US1·US3의 배선 완료에 의존한다. 문서 작업 T027·T028은 T008 이후 언제든

### 사용자 스토리 간 의존성

- **US1**: 다른 스토리에 대한 의존 없음
- **US2**: 다른 스토리에 대한 의존 없음. 리소스 XML 두 개뿐이라 어느 시점에나 병렬 가능
- **US3**: US1에 대한 의존 없음 — `IdTokenProvider`만 알고 Repository를 모른다. 다만 런타임에 의미를 가지려면 세션이 확보되어 있어야 한다(계약 A-3)

### 각 사용자 스토리 내부

- 테스트를 먼저 작성하고 실패를 확인한 뒤 구현한다 (T007→T008, T014→T018, T023→T024)
- 도메인 모델 → Repository 인터페이스 → UseCase → 구현체 → DI 바인딩 순서
- 삭제(T026)는 대체 경로가 배선된 뒤에 수행한다

### 병렬 처리 기회

- Phase 1: T001과 T003이 병렬
- Phase 2: T004 · T005 · T006이 병렬. T008 이후 T009 · T010 · T011이 병렬
- Phase 3: T013과 T015가 병렬
- Phase 4: T020 · T021이 병렬이고, 다른 모든 Phase와도 병렬
- Phase 5: T022는 T006 직후 시작 가능
- Phase 6: T027 · T028 · T029가 병렬

---

## 병렬 실행 예시: 기반 작업

```bash
# T002 완료 직후 함께 시작:
Task: "MinoDomainException.kt에 Auth 리프 추가"
Task: "auth/AnonymousAuthProvider.kt 인터페이스 선언"
Task: "auth/IdTokenProvider.kt 인터페이스 선언"

# T008(예외 매핑) 완료 직후 함께 시작:
Task: "auth/AnonymousAuthProviderImpl.kt 구현"
Task: "auth/IdTokenProviderImpl.kt 구현"
Task: "auth/di/FirebaseAuthModule.kt 작성"
```

## 병렬 실행 예시: 사용자 스토리 1

```bash
# 테스트 픽스처와 도메인 모델을 함께:
Task: "core/data/src/test/.../auth/FakeAnonymousAuthProvider.kt 작성"
Task: "core/domain/src/main/.../model/AnonymousSession.kt 작성"
```

---

## 구현 전략

### MVP 우선 (US1만)

1. Phase 1 셋업 완료
2. Phase 2 전체 — T004 · T005 · T006 · T007 · T008 · T009 · T010 · T011 · T012. T012가 두 원천 접근자를 함께 바인딩하므로 US1 몫만 골라 담을 수 없다. T006 · T010은 US3용이지만 인터페이스 하나와 얇은 래퍼 하나라 비용이 작다
3. Phase 3 완료
4. **중단하고 검증**: `:core:data:testQaDebugUnitTest`로 멱등성·동시성·예외 매핑을 판정한다
5. 이 시점의 앱 동작은 변하지 않는다 — 호출자가 없기 때문이다(N-1)

### 점진적 전달

1. 셋업 → 기반은 끝나는 것부터 아래 스토리에 공급
2. US1 추가 → 단위 테스트 green (세션 확보 계약 성립)
3. US2 추가 → 백업 규칙 발효 (다른 스토리와 무관하게 언제든)
4. US3 추가 → 첨부 계약 성립 (실제 첨부는 N-3의 조건이 풀린 뒤)
5. Phase 6에서 옛 경로 삭제 → 사용자 구분의 출처가 하나로 남는다

### 팀 병렬 전략

1. 팀이 함께 Phase 1·2를 끝낸다
2. 개발자 A: US1 (도메인 계약 + Repository) / 개발자 B: US3 (Ktor 플러그인 + NetworkModule) / US2는 둘 중 누구든 짧게 처리
3. 삭제(T026)는 A·B의 배선이 모두 머지된 뒤 한 사람이 수행한다

---

## 참고 사항

- 피해야 할 것: 모호한 작업, 동일 파일 충돌, 독립성을 깨뜨리는 스토리 간 의존성
- 규약·설계 내용을 이 문서에 다시 풀어쓰지 않는다. 각 작업은 소유 문서를 링크로 지목한다
- 커밋 단위는 [`docs/conventions/commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다
