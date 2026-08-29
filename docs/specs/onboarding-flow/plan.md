# 구현 계획: 앱 온보딩 플로우 (Onboarding Flow)

**대상 스펙 경로**: `docs/specs/onboarding-flow`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 1.2.0

**최초 작성일**: 2026-08-23

**최종 수정일**: 2026-08-29

**버전**: 2.1.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

> **Figma 노드 표기**: 이 문서의 `NNNN-NNNNN`은 [MU_디자인](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8) 파일의 노드 ID다. 디자인 시스템 라이브러리 노드는 그 자리에 파일을 밝힌다. 표기 규칙은 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md) §5.

## 요약 (Summary)

온보딩을 **진입형 feature 모듈 `:feature:onboarding`** 하나로 만든다. 스플래시가 `OnboardingLauncher`로 열고, 온보딩이 끝나면 스스로 `MainLauncher`로 홈 탭을 연다 — 결과를 돌려주지 않는다.

핵심 설계 판단은 **온보딩이 화면을 두 장만 갖는다**는 것이다. 스텝 4개 중 프로필 설정과 공동방 생성은 `docs/specs/profile`·`docs/specs/group-room-form`이 소유한 화면이고, **두 화면은 이제 코드로 존재한다**(`:feature:profile`·`:feature:roomform`). 두 모듈은 온보딩 호출자를 예고한 진입·결과 계약(`PROFILE_ENTRY_POINT_ONBOARDING`, `EXTRA_ROOM_FORM_ONBOARDING`·`ROOM_FORM_OUTCOME_CREATED`·`ROOM_FORM_OUTCOME_SKIPPED`)까지 **구현해 두었다.** 이 계획은 그 예고된 호출자를 실제로 배선하는 첫 소비자이며, 두 화면을 다시 그리지 않는다. 온보딩이 직접 그리는 것은 **친구 초대**와 **공유 방법 튜토리얼** 둘뿐이다.

두 번째 판단은 **스텝 전이 규칙과 프레임워크 결합을 가르는 것**이다. 전이 규칙은 Activity 스코프 `OnboardingFlowViewModel`이 소유하고(전이 표가 FR-001·FR-003·FR-004의 단일 출처다), `OnboardingActivity`는 `ActivityResultLauncher` 등록과 `Launcher` 호출만 한다. 위임 스텝 동안 온보딩은 빈 릴레이 Route에 머물러 결과를 기다린다.

세 번째 판단은 **완료 표시를 도메인에 여는 것**이다. FR-022가 요구하는 "프로필이 있어도 완료 표시가 없으면 온보딩"은 온보딩 밖(스플래시)에서 소비되는 규칙이므로, `OnboardingProgressRepository`를 `:core:domain`에 두고 진행 상태(마지막 스텝·만든 방 id·완료 표시)를 공유 Preferences DataStore에 저장한다.

### 2.0.0에서 달라진 것

1.0.x는 세 선행 feature가 **아직 없거나 mock이던 시점**의 계획이다. 셋이 서버 연동까지 구현된 지금, 계획이 세 자리에서 뒤집혔다.

| # | 1.0.x의 전제 | 지금 확인된 사실 | 이 계획의 대응 |
|---|---|---|---|
| 1 | 초대 코드는 `GET /api/v1/rooms/{roomId}` 응답의 `inviteCode`에 실려 온다 | **그 필드가 없다.** 서버가 전용 엔드포인트 `POST /api/v1/rooms/{roomId}/invitations`(태그 `invitation`)를 세웠고, 응답은 `code` 하나다 | R-009·R-010 철회. `Room` 모델·DTO·Mapper를 건드리지 않고, `invitation` 태그의 데이터 계층 한 벌을 새로 만든다([R-021](./research.md)·[R-022](./research.md)) |
| 2 | 스플래시는 아직 없다 — 온보딩을 열 호출자가 미정이다(그 계획의 `TBD-P4`) | 스플래시가 **`ProfileLauncher`를 직접 부르고 있다.** 온보딩 스텝 머신을 거치지 않아 FR-001~FR-004가 성립하지 않는다 | `SplashActivity`의 진입 전환을 `OnboardingLauncher`로 바꾼다([R-023](./research.md)) |
| 3 | `MinoTopNavigation`은 신설 소유자가 둘이라 이 계획이 소비만 한다 | **이미 존재한다.** 다만 우측 슬롯이 텍스트 액션만 받아 친구 초대의 우상단 [X](아이콘)를 그릴 축이 없다 | 그 컴포넌트에 아이콘 액션 축을 더한다([R-025](./research.md)) |

함께 닫힌 것이 둘이다. **개인방 생성**은 서버가 `POST /api/v1/users`에서 함께 처리한다고 명시했고 프로필이 그 API를 이미 부르고 있어 클라이언트가 만들 것이 없다([R-027](./research.md), 열린 항목 G 종료). **방 데이터 레이어의 mock**도 실제 서버 연동으로 대체됐다(열린 항목 H 종료).

새로 여는 것은 하나다 — **스플래시의 진입 판정이 완료 표시를 보지 않는다.** spec 1.2.0이 이 판정의 소유자를 온보딩 spec(FR-021·FR-022)으로 못박았으므로, 1.0.x가 "스플래시 계획이 정한다"로 미뤄 두었던 열린 항목 B를 이 계획이 닫는다([R-024](./research.md)).

설계 근거는 [research.md](./research.md), 데이터·상태는 [data-model.md](./data-model.md), 계약 표면은 [contracts/](./contracts/), 검증 절차와 **이번 범위가 확인할 수 없는 것**은 [quickstart.md](./quickstart.md)에 있다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin · Jetpack Compose. 버전은 `gradle/libs.versions.toml`이 단일 출처다.

**주요 의존성**: Hilt · AndroidX Navigation(type-safe Route) · Compose Foundation `HorizontalPager` · Ktor(기존 `HttpClient`) · `androidx-datastore-preferences`. **버전 카탈로그에 새 항목을 추가하지 않는다** — 모듈이 자동으로 얻는 의존은 `mino.android.feature` 컨벤션 플러그인이 정한다.

**저장소**: 온보딩 진행 상태는 기존 공유 `DataStore<Preferences>`(`core/data/storage/DataStoreModule`, 파일 `mino_preferences`)에 3개 키. 새 DataStore 파일을 만들지 않는다([research.md R-007](./research.md)).

**외부 계약**: 이번 범위가 새로 호출하는 엔드포인트는 **`POST /api/v1/rooms/{roomId}/invitations` 하나**다(초대 코드 발급). 그 밖의 세 엔드포인트(`POST /api/v1/users`·`POST /api/v1/rooms`·`GET /api/v1/users/me`)는 이미 구현된 세 feature가 부르며 온보딩은 결과만 받는다. 대조 결과와 어긋남은 [contracts/invite-link.md §1](./contracts/invite-link.md)이 소유한다. 앱 밖으로 여는 표면은 `:core:navigation`의 `OnboardingLauncher` 하나다.

**참조 API 문서**: <https://api.gguk.org/api-docs-json> — `Team MINO API 1.0.0`, 오퍼레이션 25개. 조회 시점 **2026-08-29T01:09:27+09:00**. 이 조회가 [contracts/invite-link.md §1](./contracts/invite-link.md)의 유일한 재현 근거다.

**선행 스펙과 그 구현 상태**

| 스펙 | 모듈 | 이 계획과의 관계 |
|---|---|---|
| `docs/specs/splash-screen` | `:feature:splash` **구현됨** | 온보딩을 여는 호출자. 진입 전환과 진입 판정 두 곳을 이 계획이 고친다([R-023](./research.md)·[R-024](./research.md)) |
| `docs/specs/profile` | `:feature:profile` **구현됨** | 프로필 스텝. 진입 인자·결과 계약을 그대로 소비한다 |
| `docs/specs/group-room-form` | `:feature:roomform` **구현됨** | 공동방 스텝. 온보딩 4갈래 결과 계약을 그대로 소비한다 |

**테스트**: JVM 단위 테스트(JUnit4 + Fake 구현체). 대상 목록은 [quickstart.md §3](./quickstart.md)이 소유한다. Compose UI 테스트는 이 저장소에 선례가 없어 도입하지 않는다.

**대상 플랫폼**: Android. `minSdk`·`targetSdk`는 build-logic이 단일 출처다.

**프로젝트 유형**: mobile-app — 다중 Gradle 모듈. 신규 모듈 1개(`:feature:onboarding`).

**성능 목표**: SC-001(스텝 전환 조작 3회 이내)·SC-005(튜토리얼 어디서든 2회 이내 홈 도달)는 **조작 횟수 목표**이며 프레임 예산이 아니다. 화면 구성이 그 횟수를 넘기지 않는 것으로 만족시키고 별도 계측 인프라를 두지 않는다. SC-007(완주율 90%)은 분석 이벤트가 필요하며 spec §3.2가 비목표로 뒀다.

**제약 조건**:
- 온보딩은 앞으로만 흐른다 — 백스택에 앞 스텝이 남지 않고(FR-006), 루트에서의 시스템 뒤로가기는 앱을 백그라운드로 보낸다(FR-007).
- feature 간 결합은 `:core:navigation` 계약 세 겹(`ProfileLauncher`·`RoomFormLauncher`·`MainLauncher`)과 스플래시가 쓰는 `OnboardingLauncher` 한 겹뿐이다([헌법 원칙 II](../../constitution.md)).
- 시간이 지나 저절로 넘어가는 스텝이 없다(UX-005) — feature 어디에도 지연 후 전환이 없다.
- **두 위임 화면의 뒤로가기 동작이 이 spec과 어긋나 있고, 그대로 구현되어 있다.** `ProfileRoute`·`RoomFormRoute` 모두 온보딩 진입에서 제스처를 삼켜 `무반응`이며, FR-007이 요구하는 `앱을 백그라운드로`가 아니다. 이 계획은 온보딩이 소유한 지점만 만들고 나머지는 보고한다([열린 항목 A](./research.md#열린-항목)).
- **`:core:data`를 여러 feature가 공유한다.** `UserApiService`·`RoomApiService`가 이미 세 feature에 걸려 있으므로, 이번 범위가 그 파일을 넓히면 회귀 확인 대상이 함께 늘어난다([ADR](../../adr/2026-08-28-api-service-owned-per-server-tag.md)).

**규모/범위**: 화면 2개(+빈 릴레이 1) · 신규 feature 모듈 1 · 신규 도메인 모델 2·Repository 2·UseCase 2·인터페이스 1 · 신규 데이터 계층 2벌(로컬 진행 상태 · `invitation` 원격) · 디자인 시스템 컴포넌트 신설 1·확장 1 · 신규 전환 계약 1. 기존 파일 변경은 `settings.gradle.kts` · `:app` build 스크립트 · `MinoScaffold` · `SplashActivity` · `ResolveSplashEntryUseCase`다. **`Room` 모델과 그 DTO·Mapper는 건드리지 않는다**(2.0.0에서 철회).

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

기준은 [`docs/constitution.md`](../../constitution.md) 2.1.0이다.

| # | 게이트 | Phase 0 | Phase 1 후 | 근거 |
|---|---|---|---|---|
| G1 | **원칙 I — SSOT.** 규약 본문을 복제하지 않고 링크로 지목하는가 | PASS | PASS | 모듈 골격·전환·에러·배치·토큰 판정 절차를 본문에 옮기지 않고 소유 문서를 링크한다. **두 위임 화면의 내부 규칙을 다시 쓰지 않은 것**이 이 게이트의 핵심 판정이다 |
| G2 | **원칙 II — feature 간 의존 금지.** 전환이 `:core:navigation` 계약으로만 이뤄지는가 | PASS | PASS | `:feature:onboarding`의 의존에 다른 `:feature:*`가 없다. 결합은 `ProfileLauncher`·`RoomFormLauncher`·`MainLauncher` 세 계약뿐이고, 스플래시가 온보딩을 여는 것도 `OnboardingLauncher` 한 겹이다([contracts/onboarding-launcher.md §4](./contracts/onboarding-launcher.md)) |
| G3 | **원칙 II — 의존 방향·DI 소유.** feature가 `:core:data`를 직접 의존하지 않고, 구현 소유 모듈이 바인딩을 갖는가 | PASS | PASS | feature는 `:core:domain`만 안다. `@Binds`는 `:core:data`의 `di/`(Repository 2·DataSource 2·`InviteLinkBuilder`)와 `:feature:onboarding`의 `di/`(Launcher)가 각각 소유한다 |
| G4 | **원칙 II — `:core:domain`이 Android를 모르는가** | PASS | PASS | `OnboardingStep`·`OnboardingProgress`·Repository 2개·UseCase 2개·`InviteLinkBuilder` 전부 순수 Kotlin. **URL 호스트를 도메인에 두지 않은 것**이 이 게이트가 가른 설계다([ADR](../../adr/2026-08-24-invite-link-assembly-domain-interface.md) · [research.md R-011](./research.md)) |
| G5 | **원칙 III — 기록.** 다른 feature를 구속하는 결정이 ADR 후보로 식별되었는가 | PASS | PASS | 후보 3건이 **모두 ADR로 기록됐다** — [초대 링크 조립 위치](../../adr/2026-08-24-invite-link-assembly-domain-interface.md)(R-011, **서버가 코드 발급을 분리한 뒤에도 유효하다**) · [토스트 소유자](../../adr/2026-08-24-snackbar-host-owned-by-mino-scaffold.md)(R-017) · [앱 진입 화면 판정](../../adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md)(R-024, 승격: plan 2.0.1). 남은 승격 후보는 없다 |
| G6 | **원칙 III — 이미 기록된 결정을 근거 없이 되돌리지 않는가** | PASS | PASS | 2.0.0이 뒤집는 R-009·R-010은 ADR이 아닌 `research.md` 항목이고, 되돌리는 근거가 **서버 문서 조회 결과**다. 두 항목을 지우지 않고 취소선 + `재검토됨(plan 2.0.0)`으로 남겼다. ADR 2건은 뒤집지 않았다 |
| G7 | **원칙 IV — Spec-First.** plan에만 있고 spec에 근거가 없는 요구사항이 없는가 | PASS | PASS | 모든 설계 항목이 FR/UX/EC/TS/SC 번호로 역추적된다. spec이 다른 문서로 넘긴 범위(프로필 입력 규칙·공동방 폼 규칙·초대 링크 형식·홈 구성)를 끌어오지 않았다. **스플래시 두 파일의 변경도 spec 1.2.0 §3.2·§4가 판정 소유권을 이 문서에 준 결과다** — 계획이 스스로 넓힌 범위가 아니다 |
| G8 | **원칙 IV — 빈틈을 지어내지 않는가** | PASS | PASS | 이 계획이 닫을 수 없는 4건을 [열린 항목](./research.md#열린-항목)으로 드러냈고, 각각의 **닫는 주체**를 지목했다. 설계로 봉합한 것이 없다 |
| G9 | **원칙 IV — 템플릿 선복사** | PASS | PASS | 1.0.0이 `plan-template.md`를 `cp`한 뒤 제자리 편집했고, 2.0.0은 그 문서를 이어 편집했다 |
| G10 | **원칙 V — 에러 처리.** 2단 분류를 따르고 정상 시나리오에서 CEH에 닿지 않는가 | PASS | PASS(경계 1건 명시) | 초대 링크 확보 실패는 State + 액션 시점 `DomainErrorEmitter`다. [`error_handling.md`](../../conventions/error_handling.md) §5의 두 통로 중 어느 쪽도 그대로 맞지 않는 **경계 사례**이며, 같은 문서 §8이 "첫 적용 화면 구현 시 결정"으로 열어 둔 자리에서 정했다([research.md R-012](./research.md)). DataStore 실패는 버그로 두어 CEH로 보낸다. 발급 API의 `403`(개인방·비멤버)은 [invite-link.md §5](./contracts/invite-link.md)가 리프 매핑을 든다 |
| G11 | **기술 표준 — 컴포넌트·에셋 배치.** 각 자산의 소속 모듈이 판정 규칙대로 정해졌는가 | PASS | PASS | 5개 자산을 각각 판정했다 — `Pagination/Dots`는 `:core:design-system` 신설, `Top Navigation`은 **기존 컴포넌트의 축 확장**, 이미지는 feature([contracts/design-system-additions.md §1](./contracts/design-system-additions.md)) |
| G12 | **기술 표준 — 디자인 토큰·실측 판정** | PASS | PASS(조건부) | `MinoPaginationDots`와 상단 바 아이콘 액션의 값 판정은 구현 착수 시 노드 대조로 한다([figma-design-fidelity.md](../../conventions/figma-design-fidelity.md) §2). 이 계획은 컴포넌트의 존재·역할·표면까지만 정했고 토큰 신설을 선행 조건으로 삼지 않는다 |
| G13 | **기술 표준 — 검증 장치의 한계.** "CI가 잡아 줄 것"을 전제하지 않는가 | PASS | PASS | 빌드 확인의 최소선을 `./gradlew :app:assembleQaDebug`로 두고, 경계·규약 위반은 리뷰가 잡는다는 전제로 계획했다([quickstart.md §2·§6](./quickstart.md)) |
| G14 | **에이전트 행동 규칙 — 요청 범위를 넘는 파일을 만들지 않는가** | PASS | PASS(주의 2건) | 이 계획은 온보딩 밖 파일 넷의 변경을 포함한다 — `MinoScaffold`(UX-003) · `MinoTopNavigation`(FR-013) · `SplashActivity`(FR-001) · `ResolveSplashEntryUseCase`(FR-022). 넷 다 이 feature의 요구사항 없이는 성립하지 않고, 앞 둘의 소유권은 [ADR](../../adr/2026-08-24-snackbar-host-owned-by-mino-scaffold.md)과 [배치 규약](../../conventions/component-asset-placement.md)이, 뒤 둘은 spec 1.2.0 §4 가정이 정했다. **`:core:data`의 공유 파일(`RoomApiService` 등)은 넓히지 않는다** — `invitation` 태그는 소유자가 없어 새로 만든다 |

**정당화가 필요한 위반: 없음.** → 복잡도 추적 표는 비운다.

**Phase 1 설계 후 재평가(2.0.0)**: 판정이 뒤집힌 게이트가 없다. 1.0.1 대비 실질적으로 달라진 것은 셋이다.

1. **G6이 새로 섰다.** 2.0.0은 앞선 계획의 결정 둘을 뒤집으므로 원칙 III의 "근거 없이 되돌리지 않는다"가 판정 대상이 된다. 되돌린 근거가 서버 문서 조회 결과이고 기각 이력을 남겼으므로 PASS다.
2. **G5의 승격 후보가 기록으로 닫혔다.** 진입 판정의 소유권([R-024](./research.md))은 스플래시와 온보딩 두 feature의 경계를 정하므로 [ADR](../../adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md)로 승격했다(plan 2.0.1). 이후 그 규칙의 소유자는 ADR이며, `research.md`에 남은 본문은 이 계획이 그 결정에 이른 경위다.
3. **G14의 주의가 1건에서 2건으로 늘었다.** 스플래시 두 파일이 변경 목록에 들어왔다 — 1.0.x는 그 feature가 없어 "스플래시 계획의 개정"으로 미뤄 두었던 몫이다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/onboarding-flow/
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물 (/mino-plan)
├── data-model.md        # Phase 1 산출물 (/mino-plan)
├── quickstart.md        # Phase 1 산출물 (/mino-plan)
├── contracts/           # Phase 1 산출물 (/mino-plan)
│   ├── onboarding-launcher.md        # 온보딩 진입 계약 + 소비하는 세 Launcher
│   ├── onboarding-progress.md        # 진행 상태 Repository·재개 지점 UseCase
│   ├── invite-link.md                # swagger 대조 · 초대 링크 확보 경로
│   ├── onboarding-flow-ui.md         # 화면 계약 (UiState·Intent·SideEffect·전이 표)
│   └── design-system-additions.md    # 디자인 시스템·공용 UI 변경과 배치 판정
├── quality/
│   └── spec-checklist.md
└── tasks.md             # /mino-task 산출물 (/mino-plan 이 생성하지 않음)
```

### 소스 코드 (Repository Root 기준)

모바일(Android) 다중 모듈 구조를 그대로 따른다. **신규·변경 파일만** 적는다.

```text
feature/onboarding/                                  # [신규] 진입형 feature
├── build.gradle.kts                                 # alias(mino.android.feature) + namespace만
└── src/main/
    ├── AndroidManifest.xml                          # OnboardingActivity, exported=false (런처 아님)
    ├── res/drawable-{mdpi,xhdpi,xxhdpi}/            # 튜토리얼 예시 이미지 5종 · 친구 초대 일러스트 (WebP)
    ├── res/values/strings.xml                       # 튜토리얼 문구 5 · 친구 초대 문구 · 토스트 문구
    └── java/team/mino/feature/onboarding/
        ├── OnboardingActivity.kt                    # public — 셸 호스팅 · Launcher 호출 · 결과 수신
        ├── OnboardingDestinations.kt                # internal Route 3종 (Relay · Invite(roomId) · Tutorial)
        ├── OnboardingShell.kt                       # MinoScaffold(bottomBar 없음) + navController + TrackScreenViews + 루트 BackHandler
        ├── OnboardingNavHost.kt                     # screen<T> 등록만
        ├── di/
        │   ├── OnboardingLauncherImpl.kt
        │   └── OnboardingNavigationModule.kt
        ├── flow/vm/                                 # 스텝 머신 — 화면이 아니라 플로우의 상태
        │   ├── OnboardingFlowViewModel.kt
        │   ├── OnboardingFlowUiState.kt
        │   ├── OnboardingFlowIntent.kt
        │   └── OnboardingFlowSideEffect.kt
        ├── relay/screen/OnboardingRelayScreen.kt    # 배경만 그리는 빈 화면
        ├── invite/
        │   ├── screen/  InviteRoute.kt · InviteScreen.kt
        │   ├── vm/      InviteViewModel · InviteUiState · InviteIntent · InviteSideEffect
        │   └── component/ 안내 문구·일러스트 조립 조각
        └── tutorial/
            ├── screen/  TutorialRoute.kt · TutorialScreen.kt        # ViewModel 없음 (research R-013)
            ├── model/   TutorialStep.kt                             # 번호·문구·이미지 리소스
            └── component/ 스텝 페이지 조립 조각

core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/
└── OnboardingLauncher.kt                            # [신규] — 스플래시가 프로필을 직접 열던 것을 대체한다
                                                     # ExtraTag.kt는 건드리지 않는다 — 진입 인자가 없다

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/OnboardingStep.kt                          # [신규]
├── model/OnboardingProgress.kt                      # [신규]
├── repository/OnboardingProgressRepository.kt       # [신규]
├── repository/RoomInvitationRepository.kt           # [신규] 초대 코드 발급 (관심사 단위)
├── usecase/ResolveOnboardingStepUseCase.kt          # [신규] 재개 지점 판정 (JVM 테스트)
├── usecase/GetInviteLinkUseCase.kt                  # [신규] 코드 발급 + 링크 조립
├── usecase/ResolveSplashEntryUseCase.kt             # [변경] 완료 표시를 함께 읽는다 (FR-022)
└── invite/InviteLinkBuilder.kt                      # [신규] 인터페이스 — 호스트를 모른다
                                                     # model/Room.kt는 건드리지 않는다 (2.0.0에서 철회)

core/data/src/main/java/team/mino/core/data/
├── datasource/OnboardingProgressLocalDataSource.kt(+Impl)          # [신규] 공유 DataStore 3개 키
├── datasource/InvitationRemoteDataSource.kt(+Impl)                 # [신규] invitation 태그
├── datasource/di/OnboardingDataSourceModule.kt                     # [신규]
├── network/service/InvitationApiService.kt                         # [신규] POST /rooms/{roomId}/invitations
├── network/dto/response/InvitationResponse.kt                      # [신규] { code }
├── repository/OnboardingProgressRepositoryImpl.kt                  # [신규]
├── repository/RoomInvitationRepositoryImpl.kt                      # [신규]
├── repository/di/OnboardingProgressRepositoryModule.kt             # [신규]
├── repository/di/RoomInvitationRepositoryModule.kt                 # [신규]
└── invite/InviteLinkBuilderImpl.kt + di/                           # [신규] flavor 호스트를 아는 구현
                                                     # RoomApiService·RoomResponse·RoomMapper는 건드리지 않는다

core/design-system/src/main/java/team/mino/core/designsystem/component/
├── pagination/                                      # [신규] Figma 컴포넌트셋 — FR-015·FR-016
│   ├── MinoPaginationDots.kt
│   ├── MinoPaginationDotsDefaults.kt
│   ├── PaginationDotsPreview.kt
│   └── token/PaginationDotsTokens.kt
└── topnavigation/MinoTopNavigation.kt               # [변경] 우측 아이콘 액션 축 추가 — FR-013

core/common/ui/src/main/java/team/mino/core/common/ui/scaffold/
└── MinoScaffold.kt                                  # [변경] 스낵바 호스트를 MinoSnackbar로 · 하단 40dp

feature/splash/src/main/java/team/mino/feature/splash/
└── SplashActivity.kt                                # [변경] ProfileLauncher 직접 호출 → OnboardingLauncher

app/build.gradle.kts                                 # [변경] implementation(project(":feature:onboarding"))
settings.gradle.kts                                  # [변경] include(":feature:onboarding")
```

**데이터 흐름 결정**: 온보딩에는 갈래가 둘뿐이다.

1. **진행 상태** — `OnboardingFlowViewModel → OnboardingProgressRepository → LocalDataSource(DataStore)`. 단방향 쓰기와 진입 시 1회 읽기다. `Flow` 구독이 없다 — 진행 상태를 관찰해야 하는 화면이 없다. 같은 저장소를 **스플래시가 읽는다**(`ResolveSplashEntryUseCase`).
2. **초대 링크** — `InviteViewModel → GetInviteLinkUseCase → (RoomInvitationRepository + InviteLinkBuilder)`. 화면은 완성된 문자열만 받고, 서버 응답 형태도 URL 형식도 모른다.

두 갈래 어디에도 조건 분기가 없다. 분기는 오직 **스텝 전이 표**([contracts/onboarding-flow-ui.md §2.4](./contracts/onboarding-flow-ui.md))에만 있고, 그것이 이 feature의 유일한 비즈니스 로직이다.

**구조 결정**: **진입형 feature 모듈 `:feature:onboarding` 단일 모듈**이다. 근거는 [`feature-module.md`](../../architecture/feature-module.md) 1장의 구분 기준 — 온보딩은 탭 셸의 그래프에 편입되는 화면이 아니라 Activity로 독립 진입하고 바텀 네비게이션을 노출하지 않는다(FR-005). 같은 문서가 "온보딩·로그인은 호출자가 하나여도 진입형"이라고 예시로 못박는다. 화면이 둘뿐이어도 `OnboardingShell`·`OnboardingNavHost`를 유지한다 — 진입 인자 복원(`toRoute`)과 화면 조회 로깅이 NavHost에 딸려 오기 때문이다(같은 문서 4장). 이미 머지된 `:feature:splash`·`:feature:profile`·`:feature:roomform` 셋이 모두 같은 골격을 쓰고 있어, 이 모듈은 그 셋을 그대로 본뜬다.

**위임 스텝 동안의 자리**: 프로필·공동방 스텝에서 온보딩 Activity는 살아 있어야 한다(결과 수신·EC-005 복원). 살아 있는 Activity는 무언가를 그려야 하므로 빈 `OnboardingRelay` Route를 둔다([research.md R-005](./research.md)).

### 범위 경계 — 이번 계획이 만들지 않는 것

spec §3.2가 이미 범위 밖으로 둔 것 외에, **다른 작업이 있어야 완결되는 것**을 명시한다. 1.0.x의 여섯 줄 중 셋이 닫혔다.

| spec 항목 | 이번 범위 | 남는 몫 |
|---|---|---|
| FR-002 프로필 저장이 개인방 생성을 촉발 | 저장 결과(`RESULT_OK`)를 받아 다음 스텝을 여는 데까지 | **없다.** 서버가 `POST /api/v1/users`에서 개인방을 함께 만들고 `:feature:profile`이 그 API를 이미 부른다([R-027](./research.md)) |
| FR-021·FR-022 완료 표시로 진입 화면이 갈린다 | 완료 표시 기록 + `ResolveSplashEntryUseCase`가 그것을 함께 읽도록 넓히는 것까지 | **없다.** 1.0.x가 "스플래시 계획의 개정"으로 미뤄 둔 몫을 이 계획이 가져왔다([R-024](./research.md)) |
| FR-008 초대 링크 확보 | 발급 API 호출 → 코드 → 링크 조립까지 전부 | **없다.** 전 flavor에 프로덕션 호스트를 쓰기로 가정으로 닫았다([R-021](./research.md)). dev·qa 호스트 확인은 서버팀 협의로 남지만 착수를 막지 않는다 |
| FR-014 튜토리얼 스텝 5의 예시 이미지 | 스텝 구조와 에셋 슬롯까지 | 디자인 — Figma에 자리표시자만 있다([열린 항목 C](./research.md#열린-항목)) |
| UX-003 토스트 40dp | `MinoScaffold` 변경까지 | **없다.** 소유자는 [ADR](../../adr/2026-08-24-snackbar-host-owned-by-mino-scaffold.md)이 정했고 이 계획이 그 변경을 낸다 |
| FR-007 프로필·공동방 스텝의 시스템 뒤로가기 | **아무것도 하지 않는다** | 두 spec의 개정과 그에 따른 구현 수정([열린 항목 A](./research.md#열린-항목)) |

각 항목의 **닫는 조건**은 [research.md 열린 항목](./research.md#열린-항목)이 소유한다.

**임시 진입점을 두지 않는다.** 1.0.x는 `:feature:splash`가 없어 온보딩 매니페스트에 LAUNCHER intent-filter를 임시로 붙이는 방법을 적었다. 스플래시가 머지된 지금은 필요 없다 — `SplashActivity`의 전환 한 줄을 바꾸면 앱 실행으로 온보딩이 열린다([quickstart.md §4](./quickstart.md)).

## 복잡도 추적 (Complexity Tracking)

> **헌장 준수 확인에서 정당화가 필요한 위반이 있는 경우에만 작성**

해당 없음 — Constitution Check 14개 게이트 전부 PASS.
