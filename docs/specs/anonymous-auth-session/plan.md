# 구현 계획: 비회원 익명 인증 세션

**대상 스펙 경로**: `docs/specs/anonymous-auth-session`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 1.2.0

**최초 작성일**: 2026-08-20

**최종 수정일**: 2026-08-20

**버전**: 1.0.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

앱이 만든 기기 식별자(ANDROID_ID) 기반의 사용자 구분을 걷어내고, **인증 제공자가 발급하고 서버가 검증할 수 있는 익명 세션**으로 대체한다. 앱은 세션을 확보·복원할 뿐 만들지 않으며, 서버로 나가는 요청에는 앱이 만든 식별자 대신 서명된 신원 증명이 자동으로 실린다.

기술적 접근과 그 근거는 [research.md](./research.md)가 소유한다. 결정만 옮기면 다음과 같다.

- **인증 제공자**: Firebase Authentication 익명 인증 (R-001)
- **SDK 소속**: `:core:data`. 신규 모듈을 만들지 않는다 (R-002)
- **세션 확보**: `AnonymousAuthRepository.ensureSession()` 하나. 멱등성은 현재 사용자 선확인 + `Mutex` (R-004 · R-005)
- **신원 증명 첨부**: Ktor 커스텀 클라이언트 플러그인 + 요청 host 판정 (R-008 · R-009)
- **실패 표현**: `Task` 변환 지점에서 화이트리스트 매핑 → `Network` / 신설 리프 `Auth` (R-006 · R-007)
- **재시도·지연**: 데이터 레이어는 멱등한 1회 확보만 제공하고 호출자가 소유 (R-011)

이 계획의 범위는 `:core:domain`·`:core:data`·`:core:error-handling`과, FR-007이 요구하는 `:app`의 백업 규칙, 그리고 검증을 가능하게 하는 최소 배선(§검증용 임시 배선)까지다. 진입 화면 구현은 spec §3.2에 따라 범위 밖이며, 화면이 지켜야 할 조건은 계약으로만 명시한다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin 2.2.10 · JVM(`:core:domain`) / Android Library(`:core:data`)

**주요 의존성**: Firebase Authentication (기존 `firebase-bom 34.14.0`으로 버전 정렬, `firebase-auth` 별칭 신규) · `kotlinx-coroutines-play-services`(신규, `Task` → suspend 변환) · Ktor 3.3.0 (`ktor-client-core`의 `createClientPlugin`, 의존 추가 없음) · Hilt 2.59.2

**저장소**: 앱은 세션·신원 증명을 직접 저장하지 않는다. 영속화는 인증 제공자 SDK가 앱 프라이빗 `SharedPreferences`에 수행한다 (R-010). `DataStore`는 이 기능에서 쓰지 않는다 (R-013)

**테스트**: JUnit4 + `kotlinx-coroutines-test` + `ktor-client-mock`. 전부 `:core:data`에 두며 필요한 의존이 이미 선언돼 있다. `:core:domain`에는 테스트를 두지 않는다 — UseCase가 Repository로의 순수 위임이라 검증 대상이 없고, 계약은 `:core:data`의 구현 테스트가 판정한다. Firebase SDK를 띄우는 테스트도 두지 않는다 (R-015)

**대상 플랫폼**: Android `minSdk 29` / `targetSdk 36`

**프로젝트 유형**: mobile-app (다중 Gradle 모듈)

**성능 목표**: 세션이 이미 확보된 경로에서 인증 제공자 왕복 0회 (SC-001). 최초 확보의 정상·지연 경계는 SC-002가 정의하며, 그 임계 시간은 진입 화면 스펙 소관이다 (FR-019)

**제약 조건**: `:core:domain`은 Android·Firebase 타입에 의존할 수 없다(Kotlin JVM) · 신원 증명은 Mino 서버 host로만 나간다(FR-011) · 실패는 `MinoDomainException`으로만 전파한다(FR-013) · 재시도·지연 임계는 데이터 레이어가 소유하지 않는다(R-011)

**규모/범위**: 신규 파일 — 도메인 3개, 데이터 10개(원천 접근자 2쌍·예외 매핑·Ktor 플러그인·Repository·DI 3), 예외 리프 1개(기존 파일 수정), 테스트 4개. 삭제 파일 13개 (R-013). 새로 그리는 화면 0개

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

기준은 [`docs/constitution.md`](../../constitution.md) 2.1.0이다.

| # | 게이트 | 판정 근거 | Phase 0 전 | Phase 1 후 |
|---|---|---|---|---|
| G-1 | **원칙 I — SSOT** | 이 계획과 부속 산출물이 규약 본문을 복제하지 않고 링크로 지목하는가 | PASS | PASS |
| G-2 | **원칙 II — 레이어 경계** | `:core:domain`이 Android·Firebase에 의존하지 않고, DI 바인딩을 구현 소유 모듈이 갖고, feature가 `:core:data`를 모르는가 | PASS | PASS — Firebase 의존은 `:core:data`에만(R-002), 도메인 계약은 순수 Kotlin 타입만, 바인딩은 `:core:data`의 `di/` |
| G-3 | **원칙 III — 결정 기록** | 되돌리기 어려운 결정이 기록되고 ADR 승격 대상이 식별되었는가 | PASS | PASS — 16건이 [research.md](./research.md)에 기록됐고, 그중 다른 feature를 구속하는 3건을 완료 보고에서 ADR로 승격 제안한다 |
| G-4 | **원칙 IV — Spec-First** | 계획에만 있고 spec에 근거가 없는 요구사항이 없는가 | PASS | PASS — 모든 설계 항목이 FR·TS·EC·SC 또는 spec §4 가정으로 역추적된다. §전제와 이연 항목이 미충족분을 드러낸다 |
| G-5 | **원칙 V — 에러 처리 규약** | 실패가 `MinoDomainException`으로 매핑되고, 화이트리스트 열거이며, `CancellationException`을 삼키지 않는가 | PASS | **CONDITIONAL PASS** — §복잡도 추적 V-1 |
| G-6 | **원칙 V — 커밋·브랜치·PR** | 작업 브랜치·커밋·PR 규약을 따르는가 | PASS | PASS |
| G-7 | **기술 표준 — 디자인 토큰** | 디자인 값 접근이 토큰·실측 판정 규칙을 따르는가 | N/A | N/A — 이 계획은 UI를 만들지 않는다 |
| G-8 | **기술 표준 — 검증 장치의 한계** | "CI가 잡아 줄 것"을 전제하지 않는가 | PASS | PASS — 검증 최소선을 빌드·JVM 단위 테스트로 잡고, 나머지는 [quickstart.md](./quickstart.md)의 수동 절차로 명시했다 |
| G-9 | **기술 표준 — 모듈 추가** | 새 모듈을 정당화 없이 만들지 않는가 | PASS | PASS — 신규 모듈 0개 (R-002) |

**게이트 판정**: 정당화되지 않은 위반 없음.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/anonymous-auth-session/
├── spec.md                                  # 입력 (이 계획이 고치지 않는다)
├── plan.md                                  # 이 파일 (/mino-plan 산출물)
├── research.md                              # Phase 0 산출물 — 설계 결정 16건
├── data-model.md                            # Phase 1 산출물 — 도메인 모델·실패 모델
├── quickstart.md                            # Phase 1 산출물 — 검증 절차
├── contracts/                               # Phase 1 산출물
│   ├── anonymous-auth-repository.md         #   :core:domain 공개 계약 + 호출자 계약
│   ├── identity-proof-attachment.md         #   :core:data 내부 계약 (원천 접근자·매핑·Ktor 플러그인)
│   └── domain-exception-auth-leaf.md        #   :core:error-handling 리프 추가 계약
├── quality/                                 # /mino-spec 산출물
└── tasks.md                                 # /mino-task 산출물 (이 계획이 생성하지 않음)
```

### 소스 코드 (Repository Root 기준)

삭제 대상의 전체 목록은 [research.md](./research.md) R-013이 소유한다. 여기서는 신규·수정만 나열하고 삭제는 모듈 단위로 표시한다.

```text
core/domain/src/main/kotlin/team/mino/core/domain/
├── model/AnonymousSession.kt                        # 신규 — 도메인 모델
├── repository/AnonymousAuthRepository.kt            # 신규 — 세션 확보 계약
├── usecase/EnsureAnonymousSessionUseCase.kt         # 신규
└── (repository·usecase의 기기 식별자 계열 2개 삭제 — R-013)

core/data/src/main/java/team/mino/core/data/
├── auth/
│   ├── AnonymousAuthProvider.kt                     # 신규 — 세션 확보 원천 접근자 (internal)
│   ├── AnonymousAuthProviderImpl.kt                 # 신규 — Firebase Auth 구현
│   ├── IdTokenProvider.kt                           # 신규 — 신원 증명 획득 (internal)
│   ├── IdTokenProviderImpl.kt                       # 신규 — Firebase Auth 구현
│   ├── extension/                                   # 신규 — Task → suspend 변환 + 예외 매핑 (단일 지점)
│   └── di/                                          # 신규 — FirebaseAuth 제공(@Provides) + 접근자 바인딩(@Binds)
├── network/
│   ├── di/NetworkModule.kt                          # 수정 — 첨부 플러그인 설치, IdTokenProvider 주입
│   └── plugin/                                      # 신규 — 신원 증명 첨부 Ktor 플러그인 (internal)
├── repository/
│   ├── AnonymousAuthRepositoryImpl.kt               # 신규 — 멱등 확보 (Mutex)
│   └── di/AnonymousAuthRepositoryModule.kt          # 신규 — @Binds
├── storage/DataStoreModule.kt                       # 유지 — R-013
└── (device/ 패키지 전체 · datasource·repository의 기기 식별자 계열 6개 삭제 — R-013)

core/data/src/test/java/team/mino/core/data/
├── auth/                                            # 신규 — 원천 접근자 Fake + 예외 매핑 테스트
├── network/                                         # 기존 디렉터리에 파일 추가 — 헤더 첨부·비첨부 테스트
├── repository/AnonymousAuthRepositoryImplTest.kt    # 신규 — 멱등성·동시 호출
└── (기기 식별자 계열 테스트 3개 삭제 — R-013)

core/error-handling/src/main/kotlin/team/mino/core/errorhandling/
└── MinoDomainException.kt                           # 수정 — Auth 리프 추가

app/src/main/res/xml/
├── backup_rules.xml                                 # 수정 — sharedpref 제외 (API 30 이하)
└── data_extraction_rules.xml                        # 수정 — cloud-backup·device-transfer 제외 (API 31 이상)

gradle/libs.versions.toml                            # 수정 — firebase-auth · kotlinx-coroutines-play-services 별칭
core/data/build.gradle.kts                           # 수정 — 위 두 의존 추가
```

**구조 결정**: 신규 모듈을 만들지 않고 기존 `:core:domain`·`:core:data`·`:core:error-handling` 안에서 해결한다. 모듈 책임·의존 방향은 [`modularization.md`](../../architecture/modularization.md), DI 바인딩 소유는 [`dependency-injection.md`](../../conventions/dependency-injection.md), 패키지 배치 규칙은 [`core/data/README.md`](../../../core/data/README.md) §3·§5·§6과 [`core/domain/README.md`](../../../core/domain/README.md) §5를 따른다. 배치 판단의 근거는 R-002·R-014에 있다.

이 구조가 만족하는 경계 조건:

- Firebase SDK 타입은 `:core:data`의 `auth/` 안에서 끝난다. `repository/`도 `network/`도 SDK 타입을 보지 않는다.
- 첨부 로직이 `HttpClient` 구성에 있어 feature·`ApiService`가 인증을 다루지 않는다 (SC-006).
- 인증 제공자 실패의 매핑이 `auth/extension/` 한 곳을 통과한다.

### 검증용 임시 배선

진입 화면이 아직 없으므로, spec §4 가정("그 화면이 만들어지기 전까지는 앱의 실제 진입 지점이 임시로 같은 역할을 맡는다")에 따라 최소 배선을 이번 범위에 포함한다. 이것이 없으면 [quickstart.md](./quickstart.md) §3의 수동 검증을 실행할 주체가 없다.

| 항목 | 내용 |
|---|---|
| 위치 | 앱의 실제 진입 지점 — `:feature:main`의 `MainActivity` |
| 내용 | 첫 데이터 요청 전에 `EnsureAnonymousSessionUseCase`를 호출하고, 확보·실패를 QA 빌드 한정 로그로 남긴다 |
| 성격 | **검증 수단이지 제품 요구사항이 아니다.** 화면 표현(FR-005·FR-018·UX-001~003)은 구현하지 않으며, 진입 화면 스펙이 생기면 통째로 이관된다 |
| 로그의 근거 | spec §2.3이 정한 `userId`의 앱 내 용도(크래시·분석 대조) 밖이므로 prod 빌드에 남기지 않는다 |

### 동반 갱신이 필요한 기존 문서

이 설계가 발효되면 아래 문서가 코드와 어긋난다. 규약·모듈 문서 수정은 Plan 단계의 범위 밖이므로 여기서는 목록만 남기고, `/mino-task`가 작업으로 만든다.

| 문서 | 갱신 사유 |
|---|---|
| [`error_handling.md`](../../conventions/error_handling.md) §2·§3 | 매핑 지점이 Ktor validator 하나라는 서술 → 인증 제공자 지점 추가 (V-1) |
| [`core/error-handling/README.md`](../../../core/error-handling/README.md) §4 | "리프는 `:core:data`의 **validator** 화이트리스트와 짝으로 추가한다" → 짝의 대상이 둘이 된다 (V-1) |
| [`core/data/README.md`](../../../core/data/README.md) §3·§5 | 디렉터리 트리·패키지 역할 표의 `device/` 항목이 사라지고 `auth/`가 들어온다 |

### 구현 순서 (권고)

작업 분해는 `/mino-task`의 몫이지만, 의존 관계상 아래 순서가 강제된다.

1. `MinoDomainException.Auth` 리프 추가 → 매핑 지점이 참조할 타입이 먼저 있어야 한다
2. 버전 카탈로그·Gradle 의존 추가 → 이후 모든 코드의 컴파일 선행 조건
3. `auth/` 원천 접근자 + 예외 매핑 → 그 위에 Repository와 Ktor 플러그인이 각각 얹힌다
4. 도메인 계약(모델·Repository·UseCase) + `AnonymousAuthRepositoryImpl` + DI
5. Ktor 첨부 플러그인 + `NetworkModule` 배선
6. 기기 식별자 경로 삭제 (R-013) → 대체 경로가 선 뒤에 지운다
7. 백업 규칙 수정 · 검증용 임시 배선
8. 검증 — [quickstart.md](./quickstart.md)

## 복잡도 추적 (Complexity Tracking)

> **헌장 준수 확인에서 정당화가 필요한 위반이 있는 경우에만 작성**

| 위반 사항 | 필요한 이유 | 더 단순한 대안을 기각한 이유 |
|---|---|---|
| **V-1.** 도메인 예외 매핑 지점이 둘로 늘어난다. [`error_handling.md`](../../conventions/error_handling.md) §2·§3과 [`core/error-handling/README.md`](../../../core/error-handling/README.md) §4는 `:core:data`의 매핑 지점을 Ktor validator 하나로 전제한다 | 인증 제공자는 `HttpClient`를 거치지 않아 기존 validator가 닿지 않는다. FR-013은 도메인 예외 체계로의 전파를, FR-018은 두 갈래 구분을 요구한다 | 기각한 대안과 그 이유는 R-006이 소유한다. 새 지점도 "모든 호출이 통과하는 한 곳 + 화이트리스트 열거"라는 규약의 성질은 유지한다. 규약 본문 갱신은 §동반 갱신이 필요한 기존 문서 |
| **V-2.** `MinoDomainException`에 리프 `Auth`를 추가한다 | 기존 리프 `Network`·`Http`로는 FR-018의 "그 밖의 실패"를 표현할 수 없다 | 기각한 대안과 그 이유는 R-007이 소유한다. 리프 추가는 [`core/error-handling/README.md`](../../../core/error-handling/README.md) §4가 규정한 정식 확장이라 체계를 벗어나지 않는다 |
| **V-3.** 소비자가 없어진 `storage/DataStoreModule`과 `datastore-preferences` 의존을 남긴다 | 판단 근거는 R-013이 소유한다 | 함께 삭제하면 요청 범위를 넘어 ADR이 소유한 인프라 결정을 되돌리게 되어 헌법 §에이전트 행동 규칙에 어긋난다. 소비자 부재 상태는 후속 과제로 보고한다 |
| **V-4.** 화이트리스트 밖 예외가 CEH로 가는 경로가 진입 화면에서만 다른 의미를 갖는다 | [`error_handling.md`](../../conventions/error_handling.md) §6의 CEH 전제는 "죽는 것은 코루틴 하나이며 UI·네비게이션은 살아 있다"인데, 진입 화면은 FR-016 때문에 넘어갈 곳이 없어 사용자가 안내도 재시도도 없는 화면에 남을 수 있다 | **탈출구 리프를 만들어 전부 도메인 예외로 흡수**: 버그가 조용히 소비되어 `core/error-handling/README.md` §4 위반. 대신 호출자 계약 C-5가 재시도 루프를 도메인 예외에 종속시키지 않도록 규정한다 |

## 전제와 이연 항목

`[TBD]`(설계가 갈려 사람에게 되묻는 지점)는 남아 있지 않다. 아래는 **결정은 끝났으나 이번 범위에서 발효되지 않는** 항목이다. `/mino-task`가 이를 "결정 대기"로 오독하지 않도록 분리해 적는다.

| 항목 | 상태 | 발효 조건·소유자 |
|---|---|---|
| Mino 서버 host 판정의 실효 | `NetworkModule`의 baseUrl이 데모용 GitHub API라 첨부 판정이 항상 불일치다. FR-008·FR-011·TS-013·TS-016·SC-005·SC-006은 **계약과 구조로만** 충족되며 실제 첨부는 일어나지 않는다 | baseUrl을 `BuildConfig.API_BASE_URL`로 교체하는 별도 과제([`core/data/README.md`](../../../core/data/README.md) §4 NOTE). spec §4 가정이 그 시점을 서버 전환과 묶는다. 근거는 R-009 |
| 인증 제공자 예외 클래스의 열거 | 세 갈래의 **분류 기준**은 계약으로 확정됐고, 각 갈래에 속하는 SDK 예외 클래스 목록만 구현 시 확정한다 | [contracts/identity-proof-attachment.md](./contracts/identity-proof-attachment.md) §2 |
| 진입 화면으로의 이관 | 검증용 임시 배선(§검증용 임시 배선)이 진입 화면의 역할을 대신한다. 화면 표현(FR-005·FR-018·FR-019·UX-001~003)은 이번 범위에서 구현되지 않는다 | 진입 화면 스펙 (PRD [SCR-001]) |
