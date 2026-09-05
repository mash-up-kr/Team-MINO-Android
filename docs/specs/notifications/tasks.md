# 작업 목록: 알림함 (Notifications)

**대상 스펙 경로**: `docs/specs/notifications`

**기준 plan 버전**: 1.2.2

**최초 작성일**: 2026-09-05

**최종 수정일**: 2026-09-05

**사전 조건**: [plan.md](./plan.md) (필수), [spec.md](./spec.md) 7.0.0 (사용자 스토리), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/)

**테스트**: 포함한다. plan §기술 컨텍스트가 "테스트는 각 모듈의 `src/test/`에 붙는다 — `:feature:notifications`는 ViewModel, `:core:domain`은 UseCase가 대상"으로 지목했다. 구현 전 실패 확인(TDD)은 강제하지 않는다.

**구성 방식**: 각 스토리를 독립적으로 구현하고 테스트할 수 있도록 작업을 사용자 스토리별로 묶는다.

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. **한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.** 개정으로 추가되는 작업은 문서에 존재하는(폐기 섹션 포함) 최대 번호 + 1부터 부여하므로, 개정을 거치면 문서 순서와 ID 순서는 어긋날 수 있다. 실행 순서는 Phase 순서와 "의존성 및 실행 순서" 섹션이 말한다.
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: 이 작업이 속한 사용자 스토리 (US1·US2·US3)
- 설명에는 정확한 파일 경로를 포함할 것

## 경로 규칙

plan §프로젝트 구조를 따른다. 저장소 루트 기준 다중 Gradle 모듈이며, 이 feature가 손대는 곳은 넷이다 — 신설 `feature/notifications/`, 확장 `core/domain/`·`core/data/`, 배선 교체 `feature/main/`.

---

## Phase 1: 셋업 (공통 인프라)

**목적**: `:feature:notifications` 모듈을 빌드에 올린다. 이 단계가 끝나야 이후 모든 코드가 컴파일 대상이 된다.

- [X] T001 `settings.gradle.kts`에 `include(":feature:notifications")` 추가 (기존 탭 feature 줄 옆)
- [X] T002 `feature/notifications/build.gradle.kts` 생성 — `alias(libs.plugins.mino.android.feature)` · `namespace = "team.mino.feature.notifications"` · `testImplementation(libs.kotlinx.coroutines.test)`. `feature/home/build.gradle.kts`와 같은 형태다 (plan §프로젝트 구조)
- [X] T003 `feature/notifications/src/main/AndroidManifest.xml` 생성 (다른 탭 feature 모듈과 같은 최소 형태)

**체크포인트**: `./gradlew :feature:notifications:compileDebugKotlin`이 빈 모듈로 통과한다

---

## Phase 2: 기반 작업 (공통 인프라)

**목적**: 세 스토리가 공통으로 쓰는 도메인 모델·조회 계약·데이터 구현. 여기서 나온 산출물을 어느 스토리가 쓰는지 각 줄에 적었다.

### 도메인 모델 (`:core:domain`) — US1·US2·US3가 모두 읽는다

- [X] T004 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/NotificationType.kt` 생성 — 6종 enum. 서버 enum 대응은 [data-model.md §1.2](./data-model.md). `UNKNOWN` 멤버를 두지 않는다(알 수 없는 값은 항목을 버린다)
- [X] T005 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/NotificationTarget.kt` 생성 — `Pin(pinId)` · `Room(roomId)` · `None` sealed ([data-model.md §1.3](./data-model.md)). **`placeId`를 싣지 않는다**
- [X] T006 `core/domain/src/main/kotlin/team/mino/core/domain/model/Notification.kt` 생성 — [data-model.md §1.1](./data-model.md). T004·T005에 의존
- [X] T007 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/NotificationPage.kt` 생성 — `items` + `hasNext` ([data-model.md §1.4](./data-model.md)). T006에 의존
- [X] T008 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/NotificationDestination.kt` 생성 — `PlaceDetail(pinId)` · `RoomDetail(roomId)` · `SaveErrorGuide` sealed ([data-model.md §1.5](./data-model.md)). **`Unreachable`을 두지 않는다**

### 조회 계약과 구현 — US1이 목록을 그리는 원천, US2·US3의 진입점

- [X] T009 `core/domain/src/main/kotlin/team/mino/core/domain/repository/NotificationRepository.kt` 생성 — `suspend fun getNotifications(page: Int): NotificationPage` ([contracts/notification-repository.md §1](./contracts/notification-repository.md)). `pageSize`를 인자로 열지 않는다. T007에 의존
- [X] T010 `core/data/src/main/java/team/mino/core/data/network/dto/response/NotificationResponse.kt` 생성 — 응답 항목 7필드와 `payload`의 세 갈래 ([contracts/notification-api.md §1](./contracts/notification-api.md)). 기존 `PaginationResponse`를 재사용한다
- [X] T011 `core/data/src/main/java/team/mino/core/data/network/service/NotificationApiService.kt` 생성 — `GET /api/v1/notifications`, `page`를 문자열로 넘기고 `pageSize`는 보내지 않는다. 기존 `RoomApiService`의 형태와 인증 경로를 따른다 ([contracts/notification-api.md §1](./contracts/notification-api.md)). T010에 의존
- [X] T012 `core/data/src/main/java/team/mino/core/data/repository/mapper/NotificationMapper.kt` 생성 — 응답 → 도메인. **알 수 없는 `type`의 항목을 버리되 목록 전체를 실패시키지 않는다**(`RoomSummaryMapper`의 선례). `payload`를 `NotificationTarget` 세 갈래로 흡수한다. T006·T010에 의존
- [X] T013 `core/data/src/main/java/team/mino/core/data/datasource/NotificationRemoteDataSource.kt`와 `...Impl.kt` 생성. T011에 의존
- [X] T014 `core/data/src/main/java/team/mino/core/data/repository/NotificationRepositoryImpl.kt` 생성 — 실패를 `Result`로 감싸지 않고 던진다([contracts/notification-repository.md §1](./contracts/notification-repository.md)). T009·T012·T013에 의존
- [X] T015 [P] `core/data/.../datasource/di`·`repository/di`에 T013·T014 바인딩 추가 ([contracts/notification-repository.md §4](./contracts/notification-repository.md))
- [X] T016 [P] `core/data/src/test/java/team/mino/core/data/repository/mapper/NotificationMapperTest.kt` 작성 — 알 수 없는 `type` 항목만 버리고 나머지는 남는지, `payload` 세 갈래가 각각 옳게 흡수되는지. T012에 의존

### 도착지 해석 — US2·US3가 쓴다

- [X] T017 `core/domain/src/main/kotlin/team/mino/core/domain/usecase/ResolveNotificationDestinationUseCase.kt` 생성 — **`suspend`가 아니고 저장소를 주입받지 않는 순수 매핑**([contracts/notification-repository.md §2](./contracts/notification-repository.md)). T005·T008에 의존
- [X] T018 [P] `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ResolveNotificationDestinationUseCaseTest.kt` 작성 — 세 갈래가 각각 옳은 도착지를 내는지. 같은 모듈의 `ResolvePushDestinationUseCaseTest`와 같은 형태다. T017에 의존

**체크포인트**: 각 기반 작업이 끝날 때마다 그것을 쓰는 사용자 스토리 작업을 시작할 수 있다

---

## Phase 3: 사용자 스토리 1 - 알림 목록 조회

**목표**: 알림 탭에서 받은 알림을 최신순으로 훑어본다. 목록·빈 상태·오류 세 화면과 20건 단위 이어 붙이기가 여기서 완성된다. (spec 유저 플로우 1 · FR-001~004·006·012·015~019)

**독립 테스트**: 알림 탭에 들어가 최신순 20건이 그려지는지, 끝까지 스크롤하면 다음 20건이 이어 붙는지, 0건이면 `받은 알림이 없어요`가, 조회에 실패하면 재시도 가능한 오류가 뜨는지 본다. 행을 눌렀을 때의 이동은 US2의 몫이라 여기서는 확인하지 않는다.

### 화면 모델과 상태

- [X] T019 [P] [US1] `feature/notifications/src/main/java/team/mino/feature/notifications/main/model/NotificationThumbnail.kt` 생성 — `Image(url: String?)` · `SaveError` **두 갈래** ([data-model.md §2.2](./data-model.md))
- [X] T020 [P] [US1] `feature/notifications/.../main/util/ElapsedTimeFormatter.kt` 생성 — FR-003의 네 구간. **목록을 받은 시점에 한 번만 계산한다**([research.md D12](./research.md))
- [X] T021 [P] [US1] `feature/notifications/.../main/util/ElapsedTimeFormatterTest.kt` 작성 — SC-005의 경계값 6개(59분/60분/23시간 59분/24시간/6일 23시간/7일). T020에 의존
- [X] T022 [US1] `feature/notifications/.../main/model/NotificationItemUiModel.kt` 생성 — [data-model.md §2.1](./data-model.md). T019·T020에 의존
- [X] T023 [US1] `feature/notifications/.../main/vm/NotificationUiState.kt` 생성 — `phase`(Loading·Content·Empty·Error) · `isAppending` · `appendError` ([contracts/notification-ui.md §4.2](./contracts/notification-ui.md)). **`Loading`과 `Empty`를 가르는 것이 UX-001의 요구다**(조회가 끝나 0건임이 확정된 뒤에만 빈 상태). T022에 의존
- [X] T024 [P] [US1] `feature/notifications/.../main/vm/NotificationIntent.kt` 생성 — `Load` · `Retry` · `ReachedEnd` · `RetryAppend` · `NotificationClicked(id)` · `SaveErrorGuideBackClicked` ([contracts/notification-ui.md §2](./contracts/notification-ui.md))
- [X] T025 [P] [US1] `feature/notifications/.../main/vm/NotificationSideEffect.kt` 생성 — 네 갈래 ([contracts/notification-ui.md §3](./contracts/notification-ui.md))

### ViewModel

- [X] T026 [US1] `feature/notifications/.../main/vm/NotificationViewModel.kt` 생성 — 첫 페이지 조회와 페이지 이어 붙이기. **`hasNext == false`거나 `isAppending`이면 `ReachedEnd`를 무시한다**(EC-018). 실패는 첫 페이지와 추가 페이지를 갈라 담는다([research.md D11](./research.md)). T009·T023·T024·T025에 의존
- [X] T027 [US1] `feature/notifications/src/test/java/team/mino/feature/notifications/main/vm/NotificationViewModelTest.kt` 작성 — Fake `NotificationRepository`로 최신순 유지·페이지 이어 붙임·`hasNext` 소진·첫 페이지 실패(`Error`)·추가 페이지 실패(`appendError`, 이미 그린 목록 유지)를 덮는다. `:feature:home`의 ViewModel 테스트 방식을 따른다. T026에 의존

### 화면

- [X] T028 [P] [US1] `feature/notifications/.../main/component/NotificationRow.kt` 생성 — 썸네일·유형 문구·대상 이름·경과 시간 네 요소, **행 전체가 클릭 영역**(UX-005), 한 줄 말줄임으로 높이 고정(UX-007), 읽음 여부 표현 없음(FR-016·UX-009 — 건수 배지도 강조도 두지 않는다) ([contracts/notification-ui.md §4.3](./contracts/notification-ui.md)). T019에 의존
- [X] T029 [P] [US1] `feature/notifications/.../main/component/NotificationEmptyContent.kt` 생성 — 스팟 일러스트 + `받은 알림이 없어요` (FR-006)
- [X] T030 [P] [US1] `feature/notifications/.../main/component/NotificationErrorContent.kt` 생성 — 재시도 가능한 오류 상태 (UX-002·EC-001)
- [X] T031 [P] [US1] `feature/notifications/.../main/component/NotificationListFooter.kt` 생성 — 목록 끝의 `isAppending`·`appendError` 표시 (UX-011·UX-012·EC-016)
- [X] T032 [US1] `feature/notifications/.../main/screen/NotificationScreen.kt` 생성 — 상단 `알림` 제목과 네 상태 분기. **목록이 화면 최상단부터 시작한다**(FR-017·UX-010). 목록 끝 도달을 감지해 `ReachedEnd`를 자동 발생시킨다(UX-011). T028~T031에 의존
- [X] T033 [US1] `feature/notifications/.../main/screen/NotificationRoute.kt` 생성 — 상태 구독과 SideEffect 수집. T026·T032에 의존
- [X] T034 [US1] `feature/notifications/src/main/java/team/mino/feature/notifications/NotificationNavigation.kt` 생성 — `NotificationGraph`(public) · `NotificationMain`(internal) · `notificationGraph()` ([contracts/notification-ui.md §1](./contracts/notification-ui.md)). T033에 의존
- [X] T035 [US1] 썸네일·빈 상태·오류 상태에 쓰는 에셋을 `feature/notifications/src/main/res/`에 넣는다 — 배치 판정은 [`component-asset-placement.md`](../../conventions/component-asset-placement.md)를 따르고, 값은 Figma 원본과 대조한다([`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md))

### 셸 배선

- [X] T036 [US1] `feature/main/src/main/java/team/mino/feature/main/MainDestinations.kt`에서 `Notification` Route를 지우고, `MainTab.kt`의 `NOTIFICATION.route`를 `NotificationGraph`로 바꾼다 ([contracts/notification-ui.md §1](./contracts/notification-ui.md)). T034에 의존
- [X] T037 [US1] `feature/main/src/main/java/team/mino/feature/main/MainNavHost.kt`의 `screen<Notification> { MainTabPlaceholderScreen(...) }`을 `notificationGraph(...)` 호출로 교체한다. T036에 의존
- [X] T038 [US1] 푸시 딥링크의 알림 탭 낙하가 **콜드(`startTab`)·웜(`pendingTab`) 두 경로 모두** 성립하는지 확인한다 — `MainActivity.resolvePendingPushDestination()`이 `MainTab.NOTIFICATION`을 돌려주는 갈래 ([contracts/notification-ui.md §1](./contracts/notification-ui.md), [push-deeplink-contract §5](../push-notification/contracts/push-deeplink-contract.md)). T037에 의존

**체크포인트**: 알림 탭이 placeholder를 대체하고, 목록·빈 상태·오류·추가 로드가 모두 동작한다

---

## Phase 4: 사용자 스토리 2 - 알림 클릭 후 대상 화면 이동

**목표**: 알림을 눌러 그 알림이 가리키는 화면으로 곧바로 간다. 장소 대상은 장소 상세로, 공동방 참가는 방 상세로 — 둘 다 저장 탭 안의 화면이라 홀더를 지난다. (spec 유저 플로우 2 · FR-005·020·022)

**독립 테스트**: 장소 대상 알림을 눌러 그 알림이 실어 온 핀의 방 기준으로 장소 상세가 열리는지, 공동방 참가 알림을 눌러 그 방의 방 상세가 열리는지, 두 경우 모두 방을 고르는 단계가 없는지 본다.

- [X] T039 [US2] `NotificationViewModel`에 `NotificationClicked` 처리를 더한다 — 도메인 목록에서 `id`로 찾아 T017을 태우고 SideEffect를 방출한다. **처리 중 같은 Intent를 무시한다**(EC-011, [contracts/notification-ui.md §2](./contracts/notification-ui.md)). T017·T026에 의존
- [X] T040 [US2] `notificationGraph()`에 `onNavigateToPlaceDetail: (pinId) -> Unit`·`onNavigateToRoomDetail: (roomId) -> Unit` 두 콜백을 더하고 `NotificationRoute`가 SideEffect를 그리로 흘리게 한다 ([contracts/notification-ui.md §1·§3](./contracts/notification-ui.md)). T034·T039에 의존
- [X] T041 [US2] `feature/main/.../MainShell.kt`와 `MainNavHost.kt`에 `onRequestRoomDetail: (roomId: String) -> Unit` 파라미터를 더하고, `MainActivity.kt`가 `roomDetailRequestHolder::request`를 넘긴다 — 홀더는 이미 주입돼 있고 푸시 소비 경로에서만 쓰이고 있다 ([contracts/notification-ui.md §1](./contracts/notification-ui.md))
- [X] T042 [US2] `MainNavHost.kt`의 `notificationGraph(...)` 호출에 두 람다를 채운다 — 장소는 `onRequestPlaceDetail(pinId, PlaceDetailEntryOrigin.NOTIFICATION)`, 방은 `onRequestRoomDetail(roomId)`, 둘 다 뒤에 `navigateToTab(MainTab.SAVED)` ([research.md D10·D14](./research.md)). **`NOTIFICATION` origin이 UX-013·UX-016을 집행한다** — 알림에서 왔다고 화면의 소속도 나가기 규칙도 달라지지 않는다. T040·T041에 의존
- [X] T043 [US2] `feature/notifications/src/test/java/team/mino/feature/notifications/main/vm/NotificationViewModelTest.kt`에 클릭 분기 테스트를 더한다 — 세 갈래가 각각 옳은 SideEffect를 내는지, 빠른 두 번 탭이 한 번만 방출되는지(EC-011). T039에 의존

**체크포인트**: 목록의 모든 행이 눌렀을 때 제 갈 곳으로 간다. 장소 상세의 [나가기]가 방 상세로 내려가는지는 `PlaceDetailEntryOrigin.NOTIFICATION`이 이미 정한다

---

## Phase 5: 사용자 스토리 3 - 저장 오류 안내 확인

**목표**: 저장 오류 알림을 눌러 실패 조건 3줄을 읽는다. 이 화면만은 알림 탭 안에 머물러 바텀 네비게이션이 계속 보인다. (spec 유저 플로우 3 · FR-010·011)

**독립 테스트**: 저장 오류 알림을 눌러 `확인해주세요` 안내 화면이 열리고 바텀 네비게이션이 유지되는지, 뒤로가기가 목록으로 되돌리는지, 다른 탭에 갔다 오면 안내가 아니라 목록이 보이는지 본다.

- [X] T044 [US3] `feature/notifications/.../main/screen/SaveErrorGuideScreen.kt` 생성 — 스팟 일러스트 + `확인해주세요` + 고정 3줄. **ViewModel을 갖지 않는다**(EC-013, [contracts/notification-ui.md §4.4](./contracts/notification-ui.md))
- [X] T045 [US3] `NotificationNavigation.kt`에 `SaveErrorGuide`(internal Route)를 더하고 같은 그래프의 두 번째 목적지로 등록한다 — 그래야 바텀 네비게이션이 배선 없이 유지되고(UX-008), 탭을 떠났다 오면 안내가 아니라 목록이 보인다([research.md D2](./research.md)). T034·T044에 의존
- [X] T046 [US3] `NotificationRoute`가 `NavigateToSaveErrorGuide` SideEffect를 받아 `NavController`로 이동시킨다 ([contracts/notification-ui.md §3](./contracts/notification-ui.md)). T039·T045에 의존
- [X] T047 [US3] `feature/notifications/.../main/screen/SaveErrorGuideScreen.kt`의 상단 뒤로가기와 시스템 뒤로가기가 모두 목록으로 되돌리는지 확인한다 (FR-011·EC-014). T046에 의존

**체크포인트**: 세 스토리가 모두 독립적으로 동작한다

---

## Phase 6: 마무리 및 공통 관심사

- [X] T048 [P] **새 문서를 만들지 않는다는 것을 확인한다** — `feature/*`는 README를 두지 않는 관례이고(README는 `core/*`만 갖는다), `CLAUDE.md` 문서 네비게이션 표도 개별 feature 모듈을 싣지 않는다. 따라서 헌법 「새 문서 추가 시 `CLAUDE.md` 표에 한 줄 추가」 조항이 걸리지 않는다([`constitution.md`](../../constitution.md) I). 관례가 바뀌어 README를 두게 되면 그때 같은 변경에서 표를 갱신한다
- [X] T049 품질 게이트 실행 — `./gradlew :feature:notifications:testDebugUnitTest :core:domain:test :core:data:testDebugUnitTest`와 `lintDebug`. Compose Lint 위반 처리는 [`compose-lint.md`](../../conventions/compose-lint.md)를 따른다. 로컬 lint가 JBR에서 죽으면 `-XX:-TieredCompilation`으로 회피한다
- [ ] T050 [quickstart.md](./quickstart.md) 검증 실행 — §5가 나열한 「검증되지 않는 것」은 제외한다. **성능 지표 셋을 번호로 확인한다** — `SC-001`(탭 진입 후 2초 이내 첫 화면) · `SC-002`(스크롤 중 프레임 드랍 없음) · `SC-011`(첫 화면을 위해 20건을 넘는 알림을 기다리지 않음). 나머지 SC는 결과 지표라 별도 측정 대상이 아니다

---

## 미결 사항

작업으로 옮길 근거가 없어 남겨 둔 것들이다. **`/mino-task`는 plan에 없는 작업을 만들지 않는다.**

| 항목 | 성격 | 처리 |
|---|---|---|
| ~~spec UX-006·EC-009·EC-010과 설계가 어긋난다~~ | **해소(spec 7.0.0)** | spec이 설계 쪽으로 맞춰졌다(§5 Q14) — 알림함은 이동 전에 대상의 생사를 되묻지 않고, 그 판정은 도착지 화면의 몫이다. **클라이언트 작업이 없다.** 다만 [SCR-006]·[SCR-005]에 「없는 대상」을 재시도 불가로 구분하는 과제가 넘어갔다([research.md 「인접 spec에 전할 것」](./research.md)) — 그 과제가 닫히기 전까지는 사용자가 재시도 오류 화면에 갇힌다 |
| **FR-014 · FR-019 · UX-014 · UX-015에 클라이언트 작업이 없다** | 커버리지(정당) | 넷 다 알림함이 코드로 할 일이 없다. **FR-014**(위치 권한 미허용자에게 위치 기반 리마인드를 보이지 않음) — 서버가 걸러 내려주므로 클라이언트에 필터가 없다([contracts/notification-api.md §1](./contracts/notification-api.md)). **FR-019**(대표 알림이 목록에 없음) — 서버 `type` enum에 그 값이 없어 구조적으로 실릴 수 없다. **UX-014**(바텀 네비게이션 노출로 탭 안팎을 구분) — 셸의 기존 동작이라 이 모듈이 코드를 더하지 않는다([contracts/notification-ui.md §5](./contracts/notification-ui.md)). **UX-015**(한 번의 저장 조작을 한 사건으로 셈) — FR-021과 동치로 서버 몫이다 |
| ~~plan.md에 낡은 서술 4건~~ | **해소(plan 1.2.2)** | 헌법 게이트 두 곳이 폐기된 결정(D6·D13)을 근거로 들던 것과, 참조 API 메타데이터·기준 spec 버전이 낡았던 것을 모두 정정했다 |
| **FR-021 묶음을 서버가 하는지** | 서버 동작 | spec §4 가정을 신뢰하고 진행하기로 했다(spec §5 Q13). 클라이언트 작업이 없어 T050의 quickstart 확인에만 걸린다 |
| **장소 0개 공동방 알림 행의 플레이스홀더** | 디자인 확인 | 서버가 이미지를 주지 못하면 플레이스홀더가 선다. 실제 데이터에서 흔하면 디자인 확인이 필요하다([quickstart.md §4.2](./quickstart.md)) |

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **셋업 (Phase 1)**: 의존성 없음 — 즉시 시작 가능. T001~T003은 순차다(모듈 등록 → 빌드 스크립트 → 매니페스트)
- **기반 작업 (Phase 2)**: T001~T003 완료에 의존
- **사용자 스토리 (Phase 3~5)**: 각 작업이 **실제로 읽거나 컴파일 대상으로 삼는** 기반 작업에만 의존한다. 기반 단계 전체의 완료를 기다리지 않는다
  - US1의 화면 모델·포맷터(T019~T022)는 도메인 모델(T004~T008)만 있으면 시작 가능 — 데이터 구현(T010~T016)을 기다리지 않는다
  - US2는 T017(도착지 UseCase)과 US1의 T026·T034에 의존한다
  - US3은 US1의 T034에 의존하고 US2와는 독립이다
- **마무리 (Phase 6)**: 목표한 스토리의 완료에 의존

### 사용자 스토리 간 의존성

- **US1**: 기반 산출물이 준비되면 시작 가능 — 다른 스토리에 의존하지 않는다
- **US2**: **US1에 의존한다.** 목록이 없으면 누를 행이 없고, `NotificationViewModel`(T026)과 그래프(T034)를 확장하는 형태다
- **US3**: **US1에 의존한다.** 그래프의 두 번째 목적지를 더하는 형태다. **US2와는 독립** — 두 사람이 나눠 진행할 수 있다

### 각 사용자 스토리 내부

- 모델 → ViewModel → 화면 → 그래프 → 셸 배선 순서
- 테스트는 대상 구현 직후에 붙인다(T021은 T020, T027은 T026, T043은 T039 뒤)
- 다음 스토리로 넘어가기 전에 해당 스토리의 체크포인트를 확인한다

### 병렬 처리 기회

- **Phase 2**: T004·T005는 동시에 가능하고, T007·T008도 T006 이후 동시에 가능하다. 데이터 계열(T010~T016)과 도착지 계열(T017·T018)은 서로 독립이라 두 사람이 나눌 수 있다
- **Phase 3**: T019·T020·T024·T025가 동시에 가능하고, 컴포넌트 넷(T028~T031)도 T019 이후 동시에 가능하다
- **Phase 4·5**: US2와 US3은 US1 완료 후 병렬로 진행 가능하다
- 한 파일을 함께 고치는 작업은 [P]를 붙이지 않았다 — T039·T043은 같은 ViewModel·테스트 파일을 손대므로 순차다

---

## 병렬 실행 예시: 사용자 스토리 1

```bash
# 화면 모델과 Intent/SideEffect를 함께 만든다 (서로 다른 파일):
Task: "feature/notifications/.../main/model/NotificationThumbnail.kt 에 두 갈래 sealed 생성"
Task: "feature/notifications/.../main/util/ElapsedTimeFormatter.kt 에 FR-003 네 구간 포맷터 생성"
Task: "feature/notifications/.../main/vm/NotificationIntent.kt 에 6종 Intent 생성"
Task: "feature/notifications/.../main/vm/NotificationSideEffect.kt 에 4종 SideEffect 생성"

# 화면 컴포넌트 넷을 함께 만든다 (T019 완료 후):
Task: "main/component/NotificationRow.kt 에 알림 행 생성"
Task: "main/component/NotificationEmptyContent.kt 에 빈 상태 생성"
Task: "main/component/NotificationErrorContent.kt 에 오류 상태 생성"
Task: "main/component/NotificationListFooter.kt 에 목록 끝 표시 생성"
```

---

## 구현 전략

### MVP 우선 (US1만)

1. Phase 1 셋업 완료 (T001~T003)
2. Phase 2에서 **US1이 쓰는 것만** 완료 — 도메인 모델(T004~T008)과 조회 계약·구현(T009~T016). T017·T018은 미뤄도 된다
3. Phase 3 완료 → 알림 탭이 placeholder를 대체한다
4. **중단하고 검증**: 목록·빈 상태·오류·추가 로드를 [quickstart.md](./quickstart.md) §1~§4로 확인
5. 이 시점의 알림 탭은 "보는 것"까지 완결이다. 행을 눌러도 아무 일이 없다는 것만 알려진 상태다

### 점진적 전달

1. 셋업 → 기반 작업은 끝나는 것부터 US1에 공급
2. US1 추가 → 독립 테스트 → 데모 (MVP)
3. US2 추가 → 알림이 화면으로 이어진다 → 독립 테스트
4. US3 추가 → 저장 오류 안내가 닫힌다 → 독립 테스트
5. Phase 6으로 게이트를 통과시킨다

### 팀 병렬 전략

1. 함께 Phase 1을 끝낸다
2. Phase 2를 둘로 나눈다 — 개발자 A는 도메인 모델·조회 계약·데이터 구현(T004~T016), 개발자 B는 도착지 UseCase(T017·T018)와 US1 화면 모델(T019~T022)
3. US1 완료 후 US2(개발자 A)와 US3(개발자 B)을 병렬로 진행한다 — 두 스토리는 서로 독립이다

---

## 참고 사항

- 피해야 할 것: 모호한 작업, 동일 파일 충돌, 독립성을 깨뜨리는 스토리 간 의존성
- 커밋 단위는 [`docs/conventions/commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다
