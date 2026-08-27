# 구현 계획: 스플래시 화면

**대상 스펙 경로**: `docs/specs/splash-screen`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 3.0.2

**참고 — PRD**: spec 3.0.2가 PRD 9.0.0을 기준으로 삼는다. `[SCR-001]`·`[SCR-002]`·`[SYS-011]`이 5.0.0과 동일해 이 계획의 설계에 변동이 없다(2026-08-27 대조).

**최초 작성일**: 2026-08-23

**최종 수정일**: 2026-08-27

**버전**: 3.0.1

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

앱의 런처 진입점을 새 진입형 feature `:feature:splash`로 만든다. 브랜드 화면을 최소 3초 노출하는 동안 「비회원 익명 세션」을 확보하고, 프로필 유무에 따라 온보딩 또는 메인 탭으로 전환한다. 세션이 확보되기 전에는 어느 화면으로도 진입시키지 않고, 지연·실패를 스피너와 두 종류의 토스트로 알리며 자동으로 재시도한다.

세션 확보와 온보딩 전환은 이 스펙이 만들지 않는다. `anonymous-auth-session`(#176)의 `EnsureAnonymousSessionUseCase`와 profile 스펙의 `ProfileLauncher`를 주입·호출하며, **이 계획이 #176이 예고한 스플래시 배선을 수행한다** — 그때까지의 임시 자리였던 `MainActivity` 호출을 이관한다. 이 스펙이 새로 만드는 도메인 표면은 프로필 등록 여부 조회와 목적지 결정 둘뿐이다.

**미확정은 없다.** plan 2.1.0의 TBD 3건이 모두 닫혔다 — 토스트 디자인 대조(P3, 2.1.0), 프로필 미생성 응답(P2, 배포 OpenAPI의 `USER_NOT_REGISTERED`), 온보딩 진입 계약(P4, `ProfileLauncher`). 선행 의존도 전부 `develop`에 들어와 있어 대기 없이 착수할 수 있다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin · Jetpack Compose · Hilt (버전은 `gradle/libs.versions.toml`이 단일 출처)

**주요 의존성**: `:core:domain` · `:core:data` · `:core:navigation` · `:core:design-system` · `:core:error-handling` · `:core:common:ui`

**선행 스펙(둘 다 머지 완료)**: [`anonymous-auth-session`](https://github.com/mash-up-kr/Team-MINO-Android/issues/176) — `EnsureAnonymousSessionUseCase`·`MinoDomainException.Auth` / `profile` — `ProfileLauncher`·`PROFILE_ENTRY_POINT_ONBOARDING`. 이 계획은 둘 다 **소비자**다.

**저장소**: 익명 세션은 인증 제공자 SDK가 앱 프라이빗 저장소에 영속화하며 앱이 직접 캐싱하지 않는다(`anonymous-auth-session` 소유). 스플래시는 프로필을 캐시하지 않으며 profile 스펙의 로컬 캐시도 읽지 않는다(→ [research.md R-015](./research.md)).

**참조 API 문서**: [Team MINO API 1.0.0](https://api.gguk.org/api-docs-json) — 2026-08-27T20:19:22+09:00 조회. 오퍼레이션 24개. 이 계획이 쓰는 것은 `GET /api/v1/users/me` 하나다.

**테스트**: `:core:domain`은 JVM 단위 테스트(분기 규칙). 화면 동작은 [quickstart.md](./quickstart.md)의 수동 시나리오. 이 저장소에는 PR을 검증하는 CI가 없다(헌법 §검증 장치의 한계).

**대상 플랫폼**: Android (`minSdk`·`targetSdk`는 build-logic이 단일 출처)

**프로젝트 유형**: mobile-app — 다중 Gradle 모듈

**성능 목표**: 정상 경로에서 앱 실행 후 3초(±0.5초) 내 전환 (SC-001)

**제약 조건**:
- 세션 미확보 상태로 스플래시 밖 화면이 노출되면 안 된다 (SC-006, FR-010)
- 최초 실행의 세션 발급 대기는 3초 노출 안에 흡수되어야 한다
- 재실행은 네트워크 없이 세션을 복원한다 (FR-011)

**규모/범위**: 화면 1개(상태 4종) · 신규 모듈 1개 · 신규 도메인 표면 3개(`ProfileRegistrationRepository`·`ResolveSplashEntryUseCase`·`SplashEntry`) · 기존 모듈 변경 2개(`:feature:main` 매니페스트, `:app` 의존)

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

| # | 게이트 (출처: [constitution.md](../../constitution.md)) | Phase 0 | Phase 1 재평가 |
|---|---|---|---|
| G1 | **원칙 I SSOT** — 규약 본문을 이 문서에 복제하지 않고 링크로 지목한다 | PASS | PASS — 배치·에러·전환 규칙을 원문 링크로 처리. 세션 계약을 재정의하지 않고 `anonymous-auth-session`을 지목한다 |
| G2 | **원칙 II 의존 방향** — `feature`·`data` → `domain`, 역방향 없음 | PASS | PASS — `:feature:splash` → `:core:domain`, `:core:data` → `:core:domain` |
| G3 | **원칙 II feature 간 의존 금지** — 전환은 `:core:navigation` 계약으로 | PASS | PASS — `MainLauncher`·`ProfileLauncher` 모두 기존 계약 사용 |
| G4 | **원칙 II DI 바인딩 소유** — 구현을 가진 모듈이 자기 `di/`에서 바인딩 | PASS | PASS — `ProfileRegistrationRepositoryImpl` 바인딩은 `:core:data/di`. 세션·프로필 값 쪽 바인딩은 각 소유 스펙에 있다 |
| G5 | **원칙 II `:core:domain`은 Android 비의존** | PASS | PASS — `SplashEntry`·계약·UseCase 모두 순수 Kotlin |
| G6 | **원칙 IV `[TBD]` 표시** — 근거 없는 빈틈을 지어내지 않는다 | PASS | PASS — 미확정 0건. 계약의 모든 응답 갈래가 배포 OpenAPI에 근거한다 |
| G7 | **원칙 IV 템플릿 선복사** | PASS | PASS — `plan-template.md`를 복사한 뒤 제자리 편집 |
| G8 | **원칙 V 에러 처리** — `MinoDomainException` 매핑, 정상 시나리오에서 CEH 미도달 | PASS | PASS — `Network`/`Auth` 2분기를 `runCatchingDomain`으로 소비([error_handling.md](../../conventions/error_handling.md)). 열거 밖 실패로 재시도 루프가 끊기지 않게 한다(호출자 계약 C-5 → [research.md R-013](./research.md)) |
| G9 | **기술 표준 — 디자인 토큰 단일 접근점** | PASS | PASS — `MinoSnackbar` 재사용, 판정은 Figma 대조로([figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)) |
| G10 | **기술 표준 — 이미지 에셋 배치** | PASS | PASS — feature 소유·WebP·밀도별([component-asset-placement.md](../../conventions/component-asset-placement.md)) |

**정당화가 필요한 위반: 없음.** → 복잡도 추적 표는 비운다.

> plan 2.1.0에서 조건부였던 G3이 **무조건 PASS로 바뀌었다.** `ProfileLauncher` 계약과 대상 `ProfileActivity`가 `develop`에 있어 우회할 이유가 사라졌다.

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
│   └── profile-registration.md
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
├── model/SplashEntry.kt                          # 신규
├── repository/ProfileRegistrationRepository.kt   # 신규
└── usecase/ResolveSplashEntryUseCase.kt          # 신규
    # AnonymousAuthRepository·EnsureAnonymousSessionUseCase·AnonymousSession → anonymous-auth-session 소유
    # Profile·ProfileRepository                                              → profile 소유
    # 이 계획은 셋 다 만들지 않고 주입받는다

core/data/src/main/java/team/mino/core/data/
├── network/service/UserApiService.kt             # 신규 — GET /api/v1/users/me
├── repository/ProfileRegistrationRepositoryImpl.kt  # 신규 — errorCode 분기
└── repository/di/                                # 바인딩 (이 모듈 소유)
    # 응답 본문을 도메인으로 옮기지 않으므로 DTO·Mapper가 없다

feature/main/src/main/AndroidManifest.xml         # 변경 — LAUNCHER intent-filter 제거
app/build.gradle.kts                              # 변경 — :feature:splash 의존 추가
settings.gradle.kts                               # 변경 — include(":feature:splash")
```

**구조 결정**: 스플래시를 **진입형 feature 단일 모듈**로 신설한다. 근거와 기각한 대안은 [research.md R-001](./research.md)이 소유하며, 진입형/탭 구분과 공개 범위 규칙은 [feature-module.md §1](../../architecture/feature-module.md)을 따른다.

`:app`은 `:feature:splash`를 의존해 그래프에 편입하기만 하고 화면을 갖지 않는다. 런처 이관에 따라 `:feature:main`의 `MainActivity`는 LAUNCHER 자격을 잃는다 — 두 개가 공존하면 런처 아이콘이 둘 뜬다.

## 미확정 항목 (Open Questions)

**없다.** plan 2.1.0의 3건이 모두 닫혔다.

| ID | 내용 | 해소 |
|---|---|---|
| ~~TBD-P2~~ | 프로필 미생성 시 `GET /users/me` 응답 | **plan 3.0.0** — 배포 OpenAPI가 `401`의 `errorCode`에 `USER_NOT_REGISTERED`를 정의 → [research.md R-003](./research.md) |
| ~~TBD-P3~~ | Figma 토스트가 `MinoSnackbar`와 동일한가 | **plan 2.1.0** — `Snackbar/Snackbar` 인스턴스로 확인 → [research.md R-006](./research.md) |
| ~~TBD-P4~~ | 온보딩 진입 계약 대상 없음 | **plan 3.0.0** — `ProfileLauncher`·`ProfileActivity` 머지됨 → [research.md R-008](./research.md) |

**선행 의존도 해소됐다.** `EnsureAnonymousSessionUseCase`·`MinoDomainException.Auth`·`ProfileLauncher`·`ProfileActivity`가 모두 `develop`에 있어 Fake 대체 없이 착수할 수 있다.

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

## 서버 API 대조 결과

**출처**: [Team MINO API 1.0.0](https://api.gguk.org/api-docs-json) · **조회 시점**: 2026-08-27T20:19:22+09:00 · 오퍼레이션 24개

| 엔드포인트 | 이 계획과의 관계 |
|---|---|
| `GET /api/v1/users/me` | **사용한다.** 프로필 등록 여부 판정 (FR-003·FR-004). 응답 갈래는 [contracts/profile-registration.md](./contracts/profile-registration.md)가 문서에서 인용한다 |
| `POST /api/v1/users` | **사용하지 않는다.** "유저 등록 (+ 개인방 자동 생성)"으로 온보딩 소관이며, profile 스펙이 이미 저장 흐름에서 호출한다 |
| 세션·토큰 발급 | **이 문서에 없는 것이 정상이다.** 발급 주체가 인증 제공자(Firebase)이며 서버는 Bearer로 실려 온 ID 토큰을 검증할 뿐이다 |

**대응 API 없음**: 없다.
**spec과 어긋나는 지점**: 없다. `401`의 `errorCode` enum이 FR-003의 판정 근거를 정확히 제공한다.
**미대조로 남은 계약**: 없다.

> plan 2.0.0은 브랜치 스웨거 초안(`Team-MINO-Node@KKardy/GM-111-outline-prd`)을 봤고 거기엔 `errorCode` enum이 없어 TBD-P2로 남겼다. 이번엔 **배포된 문서**를 조회해 닫았다.

## 복잡도 추적 (Complexity Tracking)

> **헌장 준수 확인에서 정당화가 필요한 위반이 있는 경우에만 작성**

해당 없음 — Constitution Check 10개 게이트 전부 PASS.
