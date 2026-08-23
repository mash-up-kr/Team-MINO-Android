# 구현 계획: 스플래시 화면

**대상 스펙 경로**: `docs/specs/splash-screen`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 3.0.1

**최초 작성일**: 2026-08-23

**최종 수정일**: 2026-08-23

**버전**: 2.1.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

앱의 런처 진입점을 새 진입형 feature `:feature:splash`로 만든다. 브랜드 화면을 최소 3초 노출하는 동안 「비회원 익명 세션」을 확보하고, 프로필 유무에 따라 온보딩 또는 메인 탭으로 전환한다. 세션이 확보되기 전에는 어느 화면으로도 진입시키지 않고, 지연·실패를 스피너와 두 종류의 토스트로 알리며 자동으로 재시도한다.

세션 확보는 이 스펙이 만들지 않는다. `anonymous-auth-session` 스펙([이슈 #176](https://github.com/mash-up-kr/Team-MINO-Android/issues/176))이 확정한 `EnsureAnonymousSessionUseCase`를 주입받아 호출하며, **이 계획이 그 스펙이 예고한 스플래시 배선을 수행한다** — 그때까지의 임시 자리였던 `MainActivity` 호출을 이관한다. 프로필 판정과 목적지 결정만 `ResolveSplashEntryUseCase`로 잘라 Android 없이 JVM 테스트로 검증한다.

**남은 미확정은 2건이다.** 프로필 미생성을 나타내는 응답이 정의되지 않았고(TBD-P2), 온보딩 feature가 아직 없다(TBD-P4). 둘 다 **`UserRepositoryImpl`과 전환 배선 한 줄에만** 닿고, `:core:domain` 인터페이스와 `:feature:splash` 화면은 지금 착수할 수 있다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin · Jetpack Compose · Hilt (버전은 `gradle/libs.versions.toml`이 단일 출처)

**주요 의존성**: `:core:domain` · `:core:data` · `:core:navigation` · `:core:design-system` · `:core:error-handling` · `:core:common:ui`

**선행 스펙**: [`anonymous-auth-session`](https://github.com/mash-up-kr/Team-MINO-Android/issues/176) — `EnsureAnonymousSessionUseCase`와 `MinoDomainException.Auth` 리프를 소유한다. 이 계획은 그 계약의 **소비자**다.

**저장소**: 익명 세션은 인증 제공자 SDK가 앱 프라이빗 저장소에 영속화하며 앱이 직접 캐싱하지 않는다(`anonymous-auth-session` 소유). 프로필은 캐시하지 않는다.

**테스트**: `:core:domain`은 JVM 단위 테스트(분기 규칙). 화면 동작은 [quickstart.md](./quickstart.md)의 수동 시나리오. 이 저장소에는 PR을 검증하는 CI가 없다(헌법 §검증 장치의 한계).

**대상 플랫폼**: Android (`minSdk`·`targetSdk`는 build-logic이 단일 출처)

**프로젝트 유형**: mobile-app — 다중 Gradle 모듈

**성능 목표**: 정상 경로에서 앱 실행 후 3초(±0.5초) 내 전환 (SC-001)

**제약 조건**:
- 세션 미확보 상태로 스플래시 밖 화면이 노출되면 안 된다 (SC-006, FR-010)
- 최초 실행의 세션 발급 대기는 3초 노출 안에 흡수되어야 한다
- 재실행은 네트워크 없이 세션을 복원한다 (FR-011)

**규모/범위**: 화면 1개(상태 4종) · 신규 모듈 1개 · 신규 도메인 계약 2개(`UserRepository`·`ResolveSplashEntryUseCase`)와 모델 2개 · 기존 모듈 변경 2개(`:feature:main` 매니페스트, `:app` 의존)

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

| # | 게이트 (출처: [constitution.md](../../constitution.md)) | Phase 0 | Phase 1 재평가 |
|---|---|---|---|
| G1 | **원칙 I SSOT** — 규약 본문을 이 문서에 복제하지 않고 링크로 지목한다 | PASS | PASS — 배치·에러·전환 규칙을 원문 링크로 처리. 세션 계약을 재정의하지 않고 `anonymous-auth-session`을 지목한다 |
| G2 | **원칙 II 의존 방향** — `feature`·`data` → `domain`, 역방향 없음 | PASS | PASS — `:feature:splash` → `:core:domain`, `:core:data` → `:core:domain` |
| G3 | **원칙 II feature 간 의존 금지** — 전환은 `:core:navigation` 계약으로 | PASS | PASS — `MainLauncher` 사용, `OnboardingLauncher` 예정([TBD-P4]) |
| G4 | **원칙 II DI 바인딩 소유** — 구현을 가진 모듈이 자기 `di/`에서 바인딩 | PASS | PASS — `UserRepositoryImpl` 바인딩은 `:core:data/di`. 세션 쪽 바인딩은 `anonymous-auth-session`이 소유한다 |
| G5 | **원칙 II `:core:domain`은 Android 비의존** | PASS | PASS — `SplashEntry`·`UserProfile`·UseCase 모두 순수 Kotlin |
| G6 | **원칙 IV `[TBD]` 표시** — 근거 없는 빈틈을 지어내지 않는다 | PASS | PASS — 미확정 3건을 [TBD-P2~P4]로 명시 |
| G7 | **원칙 IV 템플릿 선복사** | PASS | PASS — `plan-template.md`를 복사한 뒤 제자리 편집 |
| G8 | **원칙 V 에러 처리** — `MinoDomainException` 매핑, 정상 시나리오에서 CEH 미도달 | PASS | PASS — `Network`/`Auth` 2분기를 `runCatchingDomain`으로 소비([error_handling.md](../../conventions/error_handling.md)). 열거 밖 실패로 재시도 루프가 끊기지 않게 한다(호출자 계약 C-5 → [research.md R-013](./research.md)) |
| G9 | **기술 표준 — 디자인 토큰 단일 접근점** | PASS | PASS — `MinoSnackbar` 재사용, 판정은 Figma 대조로([figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)) |
| G10 | **기술 표준 — 이미지 에셋 배치** | PASS | PASS — feature 소유·WebP·밀도별([component-asset-placement.md](../../conventions/component-asset-placement.md)) |

**정당화가 필요한 위반: 없음.** → 복잡도 추적 표는 비운다.

> G3은 **조건부 PASS**다. `OnboardingLauncher`의 대상 Activity가 아직 없어 계약을 만들 수 없다([TBD-P4]). 계약을 우회해 온보딩 화면을 직접 참조하면 그 시점에 G3이 FAIL로 뒤집힌다 — 우회하지 않는다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/splash-screen/
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물 (/mino-plan)
├── data-model.md        # Phase 1 산출물 (/mino-plan)
├── quickstart.md        # Phase 1 산출물 (/mino-plan)
├── contracts/           # Phase 1 산출물 (/mino-plan)
│   ├── splash-ui.md
│   └── user-repository.md
├── quality/
│   └── spec-checklist.md
└── tasks.md             # /mino-task 산출물 (/mino-plan 이 생성하지 않음)
```

### 소스 코드 (Repository Root 기준)

```text
feature/splash/                                   # 신규 모듈 (진입형)
├── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml                       # MAIN·LAUNCHER intent-filter
    ├── java/team/mino/feature/splash/
    │   ├── SplashActivity.kt                     # public — 유일한 공개 표면
    │   ├── SplashShell.kt                        # internal
    │   └── main/
    │       ├── SplashRoute.kt                    # internal
    │       ├── SplashScreen.kt                   # internal
    │       ├── SplashViewModel.kt                # internal
    │       ├── SplashUiState.kt                  # internal — Intent·SideEffect 포함
    │       └── component/                        # 스피너·브랜드 레이어
    └── res/drawable-{mdpi,xhdpi,xxhdpi}/         # 캐릭터 5종·워드마크·구름 (WebP)

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/UserProfile.kt                          # 신규
├── model/SplashEntry.kt                          # 신규
├── repository/UserRepository.kt                  # 신규
└── usecase/ResolveSplashEntryUseCase.kt          # 신규
    # AnonymousAuthRepository·EnsureAnonymousSessionUseCase·AnonymousSession은
    # anonymous-auth-session 스펙이 소유한다 — 이 계획은 만들지 않고 주입받는다

core/data/src/main/java/team/mino/core/data/
├── network/service/UserApiService.kt             # 신규
├── network/dto/response/UserResponse.kt          # 신규
├── repository/UserRepositoryImpl.kt              # 신규 — [TBD-P2] 확정 후
├── repository/mapper/UserMapper.kt               # 신규
└── repository/di/                                # 바인딩 (이 모듈 소유)

core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/
└── OnboardingLauncher.kt                         # 신규 — [TBD-P4] 확정 후

feature/main/src/main/AndroidManifest.xml         # 변경 — LAUNCHER intent-filter 제거
app/build.gradle.kts                              # 변경 — :feature:splash 의존 추가
settings.gradle.kts                               # 변경 — include(":feature:splash")
```

**구조 결정**: 스플래시를 **진입형 feature 단일 모듈**로 신설한다. 근거와 기각한 대안은 [research.md R-001](./research.md)이 소유하며, 진입형/탭 구분과 공개 범위 규칙은 [feature-module.md §1](../../architecture/feature-module.md)을 따른다.

`:app`은 `:feature:splash`를 의존해 그래프에 편입하기만 하고 화면을 갖지 않는다. 런처 이관에 따라 `:feature:main`의 `MainActivity`는 LAUNCHER 자격을 잃는다 — 두 개가 공존하면 런처 아이콘이 둘 뜬다.

## 미확정 항목 (Open Questions)

| ID | 내용 | 막는 것 | 확정 경로 |
|---|---|---|---|
| ~~TBD-P1~~ | ~~익명 세션 발급 수단·저장 형태~~ | — | **해소(plan 2.0.0)** — `anonymous-auth-session` 스펙이 확정 소유 → [research.md R-010](./research.md) |
| **TBD-P2** | 프로필 미생성 사용자에게 `GET /api/v1/users/me`가 무엇을 반환하는가. `200`·`401`만 정의됨 | `UserRepositoryImpl`, FR-003 판정 | 백엔드 합의 |
| ~~TBD-P3~~ | ~~Figma 토스트가 `MinoSnackbar` 컴포넌트셋과 동일한가~~ | — | **해소(plan 2.1.0)** — `Snackbar/Snackbar` 인스턴스로 확인, 실측값은 [research.md R-006](./research.md) |
| **TBD-P4** | 온보딩/프로필 설정 feature 부재 → `OnboardingLauncher` 대상 없음 | FR-003 전환 배선 | 온보딩 feature 머지 |

**선행 의존**: `anonymous-auth-session`(#176)이 `develop`에 머지되어야 `EnsureAnonymousSessionUseCase`와 `MinoDomainException.Auth`를 주입·참조할 수 있다. 미머지 상태에서는 Fake로 대체해 `:feature:splash`를 먼저 구현할 수 있다.

**착수 가능 범위**: 위가 열린 상태에서도 `:core:domain`(모델·`UserRepository`·`ResolveSplashEntryUseCase` + JVM 테스트)과 `:feature:splash`(화면·상태·타이밍)는 전부 구현할 수 있다. 막히는 것은 `UserRepositoryImpl` 하나와 온보딩 전환 1줄이다.

## 호출자 계약 대조 (`anonymous-auth-session` C-1~C-8)

세션 확보를 소비하는 진입 화면이 지켜야 하는 조건과 이 스펙의 대응이다.

| # | 호출자 계약 | 이 스펙의 대응 | 판정 |
|---|---|---|---|
| C-1 | 첫 Mino 서버 요청보다 먼저 호출 | FR-002 "첫 서버 요청보다 먼저·구분되는 단계로 확보" | 충족 |
| C-2 | 정상 반환 전 전환 금지 | FR-010, SC-006 | 충족 |
| C-3 | 실패하면 화면에 머물고 조작 없이 재호출 | FR-010, UX-001 | 충족 |
| C-4 | 재시도 횟수 상한 없음 | spec §4 가정 | 충족 |
| C-5 | 재시도 루프를 도메인 예외 수신에만 종속시키지 않음 | **plan 2.0.0에서 신규 반영** → [research.md R-013](./research.md) | 충족 |
| C-6 | `Network`와 `Auth`를 서로 다른 안내로 | FR-008 / FR-009 두 토스트 | 충족 |
| C-7 | 지연은 진행 표시, 임계 초과는 `Auth` 안내로 합류 | FR-006(3초 스피너) / FR-007(13초 → 일시적 오류) | 충족 |
| C-8 | 정상 속도에선 세션 확보 표현 미노출 | UX-005, TS-006 | 충족 |

C-7이 요구하는 "임계 시간은 진입 화면 스펙이 정한다"에 해당하는 값이 spec의 **3초·13초**다.

## 스웨거 대조 결과

지목된 [스웨거](https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml)(`0.1.0-draft`)에서 스플래시에 닿는 것은 다음과 같다.

| 엔드포인트 | 스플래시와의 관계 |
|---|---|
| `GET /api/v1/users/me` | **사용한다.** 프로필 존재 여부 판정 (FR-003·FR-004) |
| `POST /api/v1/users` | **사용하지 않는다.** `deviceId`+`nickname` 필수이고 개인방까지 생성한다. 닉네임은 스플래시 시점에 없으며 `deviceId` 전제는 PRD 5.0.0이 폐기했다 → 온보딩 소관 |
| 세션·토큰 발급 | **이 스웨거에 없는 것이 정상이다.** 발급 주체가 백엔드가 아니라 인증 제공자(Firebase)이며, 서버는 Bearer로 실려 온 ID 토큰을 검증할 뿐이다 → [research.md R-010](./research.md) |

공통 규약(성공 `{ "data": ... }` 래퍼, 에러 `{ "errorCode", "message" }`)은 `:core:data`의 기존 네트워크 계층 방식을 따른다 — [core/data README](../../../core/data/README.md)가 단일 출처다.

## 복잡도 추적 (Complexity Tracking)

> **헌장 준수 확인에서 정당화가 필요한 위반이 있는 경우에만 작성**

해당 없음 — Constitution Check 10개 게이트 전부 PASS.
