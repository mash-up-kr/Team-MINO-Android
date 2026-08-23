# 구현 계획: 공동방 생성 및 편집 폼 (Group Room Form)

**대상 스펙 경로**: `docs/specs/group-room-form`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 3.1.0

**최초 작성일**: 2026-08-21

**최종 수정일**: 2026-08-23

**버전**: 1.3.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

> **Figma 노드 표기**: 이 문서의 `NNNN-NNNNN`은 [MU_디자인](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8) 파일의 노드 ID다. 디자인 시스템 라이브러리 노드는 [MU_Wanted Design System](https://www.figma.com/design/hkSOCt4kOfyaVWdxybTicF/MU_Wanted-Design-System--Community-) 파일 소속임을 그 자리에 밝힌다. 표기 규칙은 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md) §5.

## 요약 (Summary)

생성과 편집이 하나의 폼을 공유하고(FR-001), 그 폼을 여는 자리가 온보딩·방 리스트 탭·홈 탭·장소 복제 시트·방 상세로 흩어져 있다. 서로 다른 feature에 흩어진 호출자가 한 화면을 열어야 하므로 **진입형 feature 모듈 `:feature:roomform`** 하나를 만들고, 진입 계약 `RoomFormLauncher`를 [`:core:navigation`](../../../core/navigation/README.md)에 둔다.

핵심 설계 판단은 **폼이 도착점을 모른다**는 것이다. FR-011이 진입점별로 다른 도착점을 요구하지만, 그 도착점(온보딩 친구 초대 스텝·방 상세·복제 시트)은 모두 다른 feature의 화면이다. 폼이 도착점을 알면 feature 간 의존이 생겨 [헌법 원칙 II](../../constitution.md)를 어긴다. 그래서 폼은 **무슨 일이 일어났는지**(생성됨·편집됨·건너뜀·이탈)만 Activity 결과로 돌려주고, 어디로 갈지와 완료 스낵바(FR-012·FR-015)는 폼을 연 진입점 feature가 정한다.

데이터는 [swagger 초안](https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml)의 `POST /api/v1/rooms` · `PATCH /api/v1/rooms/{roomId}` · `GET /api/v1/rooms/{roomId}` 계약을 그대로 DTO·Mapper·Repository 체인으로 세우되, **`DataSource` 구현만 인메모리 mock으로 채운다.** 서버가 붙을 때 갈아 끼우는 지점이 `@Binds` 한 줄이 되도록 나머지 레이어는 실제와 동일하게 만든다.

디자인 자산 실사 결과 세 갈래가 갈렸다 — 상단 내비게이션은 Figma 디자인 시스템 컴포넌트셋이라 `:core:design-system`이 신설하고(`MinoTopNavigation`), 대표 색상 칩은 [ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)이 이미 그 모듈로 정해 두었으며, 확인 모달 3종과 미리보기 카드는 디자인 시스템 컴포넌트가 아니라서 `:feature:roomform`이 갖는다. 근거는 [research.md](./research.md) R-006·R-007·R-008.

1.1.0에서 남아 있던 미확정 4건을 Figma 대조로 모두 닫았고(R-015~R-018), spec 3.0.0이 그중 둘을 FR-003·FR-025로 추인했다.

1.2.0은 **비어 있던 설계 공백 두 곳을 메운다.** 방 설명 필드의 편집 상태를 `RoomFormRoute`가 소유하기로 정했고(R-019), 이 feature가 읽지 않는 `Room.type`을 도메인 모델에서 뺐다(R-020). 앞의 결정은 두 입력 필드의 상한을 자르는 주체를 갈라 놓는다 — 방 이름은 ViewModel, 방 설명은 컴포넌트다. 함께 spec 3.1.0이 확정한 자모 허용을 검증 계약에 반영했다(R-021).

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin 2.2.10 / JVM 17. `compileSdk` 36 · `minSdk` 29 · `targetSdk` 36.

**주요 의존성**: Jetpack Compose · Hilt · AndroidX Navigation(type-safe Route) · kotlinx-serialization · kotlinx-collections-immutable. 새 외부 라이브러리는 도입하지 않는다. 모듈이 자동으로 얻는 의존은 `mino.android.feature` 컨벤션 플러그인이 정한다(`build-logic/convention/src/main/kotlin/AndroidFeatureConventionPlugin.kt`).

**저장소**: 이번 범위에서는 **인메모리 mock**이다. DataStore·DB·네트워크를 쓰지 않는다. 저장소 계약과 실서버 전환 지점은 [contracts/room-api-mock.md](./contracts/room-api-mock.md) §3·§4가 소유한다.

**테스트**: JVM 단위 테스트(JUnit4 + `kotlinx-coroutines-test`). 대상 목록은 [contracts/room-repository.md](./contracts/room-repository.md) §2와 [contracts/room-form-ui.md](./contracts/room-form-ui.md) §5가 소유한다. `:feature:roomform`·`:core:domain`의 `build.gradle.kts`에 `testImplementation(libs.kotlinx.coroutines.test)`를 더한다 — feature 컨벤션 플러그인은 `junit`만 붙인다. Compose UI 테스트는 이번 범위에 넣지 않는다.

**대상 플랫폼**: Android (`minSdk` 29).

**프로젝트 유형**: mobile-app — 다중 Gradle 모듈. 이번 작업이 손대는 모듈은 아래 §프로젝트 구조 참조.

**성능 목표**: SC-002가 요구하는 "한 글자도 뒤처지지 않는 반영"은 프레임 예산이 아니라 **동기 상태 갱신**으로 만족시킨다 — 입력·검증·미리보기 반영에 비동기 경계를 두지 않고, `updateState`가 같은 프레임에서 끝난다. 전달 경로에 지연 연산자(`debounce`·`sample` 등)를 두지 않는다 — 메커니즘 자체를 금지하는 것이 아니라 **지연을 넣지 않는다**는 뜻이다.

**제약 조건**:
- 폼은 다른 feature 모듈을 의존하지 않는다. 진입·복귀는 `:core:navigation` 계약 한 겹으로만 이뤄진다([헌법 원칙 II](../../constitution.md)).
- 도착점 feature(온보딩·방 리스트 탭·홈 탭·장소 복제 시트·방 상세)가 **아직 하나도 존재하지 않는다.** FR-011·FR-012·FR-015의 이동·스낵바는 이 범위에서 구현되지 않고, 결과 계약과 임시 검증 진입점까지가 이번 몫이다(§범위 경계).
- 대표 색상 원시값은 `AtomicColorToken`(`internal`)에 있어 feature에서 보이지 않는다 — 팔레트를 쓰는 컴포넌트는 `:core:design-system` 안에서만 만들 수 있다([ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)).
- 서버 계약이 spec과 세 지점에서 어긋난다(방 설명 길이·색상 표현·색상 가짓수). spec을 따르고 어긋남은 서버팀에 제기한다([research.md](./research.md) R-003).

**규모/범위**: 화면 1개(모달 3종 오버레이 포함) · 신규 feature 모듈 1개 · 신규 Repository 1개 · 신규 UseCase 2개 · 디자인 시스템 신규 컴포넌트 2개. 도메인 모델의 목록은 [data-model.md](./data-model.md) §2가 소유한다.

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

기준은 [`docs/constitution.md`](../../constitution.md) 2.1.0이다.

| # | 게이트 | 판정(Phase 0 전) | 판정(Phase 1 후) | 근거 |
|---|---|---|---|---|
| G1 | **원칙 I — SSOT.** 이 계획이 규약 본문을 복제하지 않고 링크로 지목하는가 | PASS | PASS | 모듈 골격·에러 처리·토큰 판정 절차를 본문에 옮기지 않고 소유 문서를 링크한다. 새 규칙을 만들지 않는다 |
| G2 | **원칙 II — 레이어 경계.** feature→feature 의존이 없고, feature가 `:core:data`를 직접 의존하지 않으며, DI 바인딩을 구현 소유 모듈이 갖는가 | PASS | PASS | 진입·복귀는 `RoomFormLauncher` 계약 한 겹. `:feature:roomform`은 `:core:domain`만 안다. `@Binds`는 `:core:data`와 `:feature:roomform`의 각 `di/`가 소유한다([contracts/room-form-launcher.md](./contracts/room-form-launcher.md) §4) |
| G3 | **원칙 II — Android 의존 방향.** `:core:domain`이 Android를 알지 않는가 | PASS | PASS | 도메인 모델·Repository·UseCase 모두 순수 Kotlin. `RoomColor`는 `Color`가 아니라 enum이다([data-model.md](./data-model.md) §2) |
| G4 | **원칙 III — 기록.** 되돌리기 어려운 결정이 ADR 후보로 식별되었는가 | PASS | PASS | R-002(mock 데이터 레이어 전략)·R-006(디자인 시스템 컴포넌트 판정)·**R-022(DS 컴포넌트의 글자 수 단위)**가 다른 feature를 구속한다. 완료 보고에서 ADR 승격을 제안한다 |
| G5 | **원칙 IV — Spec-First.** plan에만 있고 spec에 근거가 없는 요구사항이 없는가 | PASS | PASS | 모든 설계 항목이 FR/UX/EC/TS 번호로 역추적된다. **1.1.0에서 유일하게 spec을 벗어나 있던 지점(FR-003 카운터)이 spec 3.0.0으로 해소돼 이제 어긋남이 0건이다** |
| G6 | **원칙 V — 에러 처리 규약.** 실패가 2단 분류를 따르는가 | PASS | PASS | 편집 진입 로드 실패는 State(주 데이터), 생성·편집 요청 실패는 `DomainErrorEmitter`(액션 일회성). `launchSafely`·`runCatchingDomain`·`onDomainFailure`만 쓴다([error_handling.md](../../conventions/error_handling.md) §5) |
| G7 | **기술 표준 — 디자인 토큰 판정.** 값이 일치하는 토큰이 있으면 토큰, 없으면 실측값 규칙을 따르는가 | PASS | PASS | 판정은 구현 착수 시 노드 대조로 수행한다([figma-design-fidelity.md](../../conventions/figma-design-fidelity.md) §2). 계획은 토큰 신설을 선행 조건으로 삼지 않는다 |
| G8 | **기술 표준 — 컴포넌트·에셋 배치.** 각 UI 자산의 소속 모듈이 판정 규칙대로 정해졌는가 | PASS | PASS | 세 자산을 각각 판정했다 — 근거는 R-006·R-007·R-008. 판정 근거가 Figma 실사다([component-asset-placement.md](../../conventions/component-asset-placement.md) §1.2) |
| G9 | **기술 표준 — 검증 장치의 한계.** "CI가 잡아 줄 것"을 전제하지 않는가 | PASS | PASS | 빌드 확인의 최소선을 `./gradlew :app:assembleQaDebug`로 두고, 경계 위반은 리뷰가 잡는다는 전제로 계획했다([quickstart.md](./quickstart.md) §4) |

**정당화가 필요한 위반**: 없다. §복잡도 추적 표가 비어 있는 이유다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/group-room-form/
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물 (/mino-plan)
├── data-model.md        # Phase 1 산출물 (/mino-plan)
├── quickstart.md        # Phase 1 산출물 (/mino-plan)
├── contracts/           # Phase 1 산출물 (/mino-plan)
│   ├── room-form-launcher.md        # feature 간 진입·결과 계약
│   ├── room-repository.md           # 도메인 계약 (Repository·UseCase)
│   ├── room-api-mock.md             # 서버 계약(swagger)과 mock 구현 계약
│   ├── room-form-ui.md              # 화면 계약 (UiState·Intent·SideEffect)
│   └── design-system-additions.md   # 디자인 시스템 신설·확장 컴포넌트 계약
└── tasks.md             # /mino-task 산출물 (/mino-plan 이 생성하지 않음)
```

### 소스 코드 (Repository Root 기준)

```text
core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/
├── ExtraTag.kt                       # [수정] EXTRA_ROOM_FORM_* 키 추가
└── RoomFormLauncher.kt               # [신규] interface RoomFormLauncher : ActivityLauncher

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/
│   ├── Room.kt                       # [신규] 공동방 도메인 모델
│   ├── RoomColor.kt                  # [신규] 대표 색상 12종 + GRAY
│   ├── RoomDraft.kt                  # [신규] 생성·편집 입력값
│   └── RoomNameValidation.kt         # [신규] 방 이름 판정 결과
├── repository/RoomRepository.kt      # [신규] 조회·생성·편집 계약
└── usecase/
    ├── ValidateRoomNameUseCase.kt    # [신규] FR-002·FR-004·EC-001·EC-005 (길이는 판정하지 않는다)
    └── CreateRoomUseCase.kt          # [신규] FR-006 회색 기본값 적용 + FR-010

core/data/src/main/java/team/mino/core/data/
├── network/dto/
│   ├── request/RoomRequest.kt        # [신규] Create·Update 요청 DTO
│   └── response/RoomResponse.kt      # [신규] Room 응답 DTO
├── datasource/
│   ├── RoomRemoteDataSource.kt       # [신규] 인터페이스 (DTO 반환)
│   ├── RoomMockRemoteDataSourceImpl.kt  # [신규] 인메모리 mock 구현
│   ├── mock/RoomMockStore.kt         # [신규] @Singleton 인메모리 저장소 + 시드
│   └── di/RoomDataSourceModule.kt    # [신규] @Binds
└── repository/
    ├── RoomRepositoryImpl.kt         # [신규]
    ├── mapper/RoomMapper.kt          # [신규] DTO ↔ 도메인
    └── di/RoomRepositoryModule.kt    # [신규] @Binds

core/design-system/src/main/java/team/mino/core/designsystem/component/
├── topnavigation/                    # [신규] MinoTopNavigation — Figma 컴포넌트셋 · FR-025
│   ├── MinoTopNavigation.kt
│   ├── MinoTopNavigationDefaults.kt
│   ├── TopNavigationPreview.kt
│   └── token/TopNavigationTokens.kt
├── textinput/                        # [완료] R-022 — 상한·카운터를 grapheme 기준으로
│   ├── MinoTextArea.kt               # [수정] 카운터·InputTransformation 교체
│   └── MaxGraphemeLengthTransformation.kt  # [신규]
└── roomcolorchip/                    # [신규] ADR 2026-08-14 지정 위치
    ├── MinoRoomColor.kt              # 팔레트 12항목 enum (도메인 규칙 없음)
    ├── MinoRoomColorChip.kt
    ├── MinoRoomColorChipDefaults.kt
    ├── RoomColorChipPreview.kt
    └── token/RoomColorChipTokens.kt

core/design-system/src/main/java/team/mino/core/designsystem/util/
└── text/GraphemeLength.kt            # [신규] grapheme 단위 글자 수 (R-022)

feature/roomform/                     # [신규] 진입형 feature 모듈
├── build.gradle.kts
└── src/main/
    ├── res/drawable-{mdpi,xhdpi,xxhdpi}/   # 방 썸네일 13종(12색 + 회색=`my room`) WebP
    └── java/team/mino/feature/roomform/
        ├── RoomFormActivity.kt       # (public) 진입점 — extra 복원·결과 반환
        ├── RoomFormDestinations.kt   # @Serializable RoomForm Route
        ├── RoomFormShell.kt          # MinoScaffold + navController + TrackScreenViews
        ├── RoomFormNavHost.kt        # screen<RoomForm> 등록
        ├── di/
        │   ├── RoomFormLauncherImpl.kt
        │   └── RoomFormNavigationModule.kt
        └── form/
            ├── screen/  RoomFormRoute.kt · RoomFormScreen.kt
            ├── vm/      RoomFormViewModel · RoomFormUiState · RoomFormIntent · RoomFormSideEffect
            ├── model/   RoomFormMode.kt · RoomFormDialog.kt · RoomColorUiModel.kt
            └── component/
                ├── RoomPreviewCard.kt        # 상단 미리보기 카드 (FR-008)
                ├── RoomColorPalette.kt       # 3×4 칩 그리드 배치 (FR-006)
                └── RoomFormConfirmDialog.kt  # 확인 모달 3종 공통 (UX-008·UX-009)

feature/main/src/main/java/team/mino/feature/main/   # [수정] 임시 검증 진입점 (§범위 경계)
app/build.gradle.kts                                 # [수정] implementation(project(":feature:roomform"))
settings.gradle.kts                                  # [수정] include(":feature:roomform")
```

**구조 결정**: **진입형 feature 모듈 `:feature:roomform` 단일 모듈**이다. 근거는 [`feature-module.md`](../../architecture/feature-module.md) 1장의 구분 기준 — 폼은 탭 셸의 그래프에 편입되는 화면이 아니라 **Activity로 독립 진입**하고, 호출자가 여러 feature에 흩어져 있으며(FR-001의 진입점 8개), 결과를 호출자에게 돌려줘야 한다(FR-011·FR-019). 화면이 하나여도 `XShell`·`XNavHost`를 유지한다 — 진입 인자 복원(`toRoute`)과 화면 조회 로깅이 NavHost에 딸려 오기 때문이다(같은 문서 4장).

### 범위 경계 — 이번 계획이 만들지 않는 것

spec §3.2가 이미 범위 밖으로 둔 것 외에, **도착점 feature가 존재하지 않아** 이번에 완결되지 않는 것을 명시한다.

| spec 항목 | 이번 범위 | 남는 몫 |
|---|---|---|
| FR-011 진입점별 도착점 이동 | 폼이 결과(`created`·`updated`·`skipped`)와 `roomId`를 돌려주는 데까지 | 각 진입점 feature가 결과를 받아 자기 도착점으로 이동 |
| FR-012 `방 생성 완료!` · FR-015 `방 편집이 완료되었어요` | 스낵바를 표출하지 않는다 — 표출 자리가 도착 화면이므로(UX-006) | 도착점 feature |
| FR-019 복제 시트 목록 두 번째 배치 | `roomId` 반환까지 | 장소 복제 시트(PRD [SYS-003]) |
| FR-014 더보기 [편집] 노출 제어 | 편집 진입 계약(`roomId` extra)까지 | 방 상세(PRD [SCR-005]) |
| FR-016 편집 결과의 다른 화면 반영 | 수정된 `Room`을 결과로 돌려주는 데까지 | 방 목록·지도 마커·방 뱃지를 그리는 feature |
| FR-017 건너뛰기 후 튜토리얼 스텝 이동 | `skipped` 결과 반환까지 | 온보딩(PRD [SCR-002]) |

**임시 검증 진입점을 `:feature:main`에 둔다.** 이 모듈은 이미 전환 검증용 배선(`onNavigateToSample`·`onRequestSampleResult`)과 placeholder 탭을 갖고 있어, 같은 자리에 폼 진입·결과 수신·스낵바 표출을 붙이면 위 표의 미구현분을 **한 진입점에 한해** 실제로 눌러 볼 수 있다. 실제 진입점 feature가 생기면 걷어낸다. `:feature:sample`을 쓰지 않는 이유는 그 모듈이 제거 예정이라 새 의존을 더하지 않기 위해서다.

## 복잡도 추적 (Complexity Tracking)

> **헌장 준수 확인에서 정당화가 필요한 위반이 있는 경우에만 작성**

| 위반 사항 | 필요한 이유 | 더 단순한 대안을 기각한 이유 |
|---|---|---|
| **plan 단계에서 프로덕션 코드를 냈다.** `:core:design-system`의 `MinoTextArea` 동작을 고쳤다(R-022). 헌법 원칙 IV의 단계 순서(명세 → 설계 → 구현)와 열린 항목 F가 적었던 "구현 착수 전에 닫는다"를 함께 어긴다 | 2026-08-23 사용자 지시. F는 **어느 쪽을 고를지**의 문제였고(편차 수용 vs 컴포넌트 수정), 고르는 순간 그 결정의 실체가 코드 몇 줄이라 문서로만 남기면 다음 단계가 같은 판단을 다시 해야 했다 | **`/mino-task`로 미루기** — 이 변경은 `:feature:roomform`이 아니라 `:core:design-system`의 것이라 이 feature의 작업 목록에 자연스럽게 들어가지 않는다. 그 모듈의 별도 이슈로 떼는 편이 정석이나, 그러면 F가 이 계획의 열린 항목으로 무기한 남는다 |

**이 위반의 대가**: 이 델타의 코드는 `tasks.md`를 거치지 않았으므로 `/mino-task`가 만들 작업 목록에 **다시 등장하면 안 된다.** 트리에 `[완료]`로 표기한 이유다.

### 1.0.0이 남긴 미확정 4건 — 전부 해소 *(1.1.0)*

| # | 남겼던 것 | 확정 | 근거 |
|---|---|---|---|
| TBD-1 | 방 이름 필드의 `n/15` 카운터를 `MinoTextField` 확장으로 얻을지 | **카운터가 없다.** `MinoTextField`를 확장하지 않는다 | [research.md](./research.md) R-015 |
| TBD-2 | 편집 폼 상단 타이틀 문구 | 생성 `공동방 만들기` · 편집 **`방 편집`** | R-016 |
| TBD-3 | 회색(미선택) 방의 썸네일 에셋 | `Room Thumbnail_Empty`의 **`my room` variant가 곧 회색**이다. 별도 에셋이 없다 | R-017 |
| TBD-4 | 대표 색상의 서버 식별자 문자열 | 소문자 스네이크 식별자로 진행 | R-018 |

### 1.1.0이 남긴 "다른 문서의 몫" 3건 — 2건 해소 *(1.1.1)*

| # | 무엇 | 상태 |
|---|---|---|
| A | FR-003·TS-003이 요구하는 방 이름 카운터가 디자인에 없다 | **해소.** spec 3.0.0이 FR-003에서 카운터 요구를 걷어냈다. 함께 TS-003·TS-018·UX-007·SC-002가 정정되고 TS-045가 신설됐다 |
| B | spec §4 가정("편집 타이틀을 확정하지 않는다")이 낡았다 | **해소.** spec 3.0.0이 **FR-025**(생성 `공동방 만들기` · 편집 `방 편집`)로 승격하고 TS-044를 신설했다. §4 가정에서는 제거됐다 |
| C | 편집 보드(`2542-125922`·`2792-151339`)가 방 이름을 생성 보드(`2314-95301`)와 다른 컴포넌트로 그렸다 | **미해소.** 사실 서술의 소유자는 [contracts/design-system-additions.md](./contracts/design-system-additions.md) §3 말미다. **닫는 조건**: 디자이너에게 두 보드 중 어느 쪽이 의도인지 확인한다 → 편집 보드가 의도면 FR-003 재검토(`/mino-spec`), 생성 보드가 의도면 편집 보드 갱신 요청. 구현 착수 전에 닫는다 |

### 이 계획 밖에서 닫히는 것

열린 것은 **C·D·G 셋이다.** C(디자인 불일치)와 D(서버 계약)는 이 계획이 닫을 수 없고, G는 조건이 올 때까지 닫지 않는다. E·F는 닫힌 이력을 남겨 둔다.

| # | 무엇 | 닫는 조건 |
|---|---|---|
| D | swagger 초안이 spec과 세 지점에서 어긋난다([research.md](./research.md) R-003) | 서버팀이 계약을 확정하면 닫힌다. 확정 표현이 달라도 고칠 곳은 `RoomMapper` 한 파일이다 |
| ~~E~~ | ~~방 이름의 자모 단독 허용 여부~~ | **해소(1.2.0).** spec 3.1.0이 FR-004를 `한글(완성형·자모)`로 고치고 EC-025를 신설했다 → [research.md](./research.md) R-021 |
| ~~F~~ | ~~방 설명의 글자 수 세는 단위가 spec 가정과 어긋난다~~ | **해소(1.3.0).** `MinoTextArea`의 상한·카운터를 grapheme 기준으로 고쳤다 → [research.md](./research.md) R-022 |
| G | `graphemeLength`가 `:core:design-system`의 `internal`이라 feature에서 보이지 않는다 | 방 이름은 FR-004의 허용 문자가 전부 코드 유닛 1개라 지금은 승격이 필요 없다. **FR-004의 허용 문자가 넓어지거나 두 번째 사용처가 생길 때** `:core:common:kotlin`으로 올린다 — 그때 `java.text.BreakIterator`의 JVM/Android 규칙 차이를 함께 판정한다(R-022) |

**규약 충돌은 열린 항목이 아니다.** 1.1.0이 R-008에서 "규약 충돌"로 보고한 것(Figma 컴포넌트셋 vs 이미지 에셋)은 [래스터 이미지 배치·포맷 ADR](../../adr/2026-08-19-raster-image-placement-and-format.md)이 이미 닫아 둔 문제이므로, 그 ADR을 근거로 지목하는 것으로 끝난다.
