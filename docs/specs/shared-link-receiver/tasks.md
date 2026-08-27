# 작업 목록: 외부 공유 수신 방 선택 바텀시트 (Shared Link Receiver)

**대상 스펙 경로**: `docs/specs/shared-link-receiver`

**기준 plan 버전**: 3.1.0

**최초 작성일**: 2026-08-27

**최종 수정일**: 2026-08-28

**사전 조건**: [plan.md](./plan.md) · [spec.md](./spec.md) · [research.md](./research.md) · [data-model.md](./data-model.md) · [contracts/](./contracts/) · [quickstart.md](./quickstart.md)

**테스트**: 포함한다. [plan.md](./plan.md) §기술 컨텍스트가 UseCase·ViewModel(JUnit + `kotlinx-coroutines-test`)과 워커 재시도 정책(`androidx.work:work-testing` + `ktor-client-mock`)을 검증 수단으로 지목했고, [quickstart.md §5.5](./quickstart.md)가 재시도 판정의 소유자를 단위 테스트로 못박았다.

**구성 방식**: 각 스토리를 독립적으로 구현하고 테스트할 수 있도록 작업을 사용자 스토리별로 묶는다.

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. **한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.** 개정으로 추가되는 작업은 문서에 존재하는(폐기 섹션 포함) 최대 번호 + 1부터 부여하므로, 개정을 거치면 문서 순서와 ID 순서는 어긋날 수 있다. 실행 순서는 Phase 순서와 "의존성 및 실행 순서" 섹션이 말한다.
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: 이 작업이 속한 사용자 스토리 (US1, US2, US3)
- 설명에는 정확한 파일 경로를 포함할 것

## 경로 규칙

저장소 루트 기준 다중 Gradle 모듈이며, 모듈별 소스 루트는 [plan.md](./plan.md) §프로젝트 구조가 소유한다.

| 모듈 | 소스 루트 |
|---|---|
| `:feature:sharereceiver` (신규) | `feature/sharereceiver/src/main/java/team/mino/feature/sharereceiver/` |
| `:core:domain` | `core/domain/src/main/kotlin/team/mino/core/domain/` · 테스트는 `src/test/kotlin/` |
| `:core:data` | `core/data/src/main/java/team/mino/core/data/` · 테스트는 `src/test/java/` |
| `:core:design-system` | `core/design-system/src/main/java/team/mino/core/designsystem/` |
| `:core:common:ui` | `core/common/ui/src/main/java/team/mino/core/common/ui/` · 공용 이미지 에셋은 `src/main/res/drawable-*` |

---

## Phase 1: 셋업 (공통 인프라)

**목적**: 신규 feature 모듈을 세우고, WorkManager를 프로젝트에 처음 들인다.

- [X] T001 `:feature:sharereceiver` 모듈 생성 — `feature/sharereceiver/build.gradle.kts`(`alias(libs.plugins.mino.android.feature)` · `namespace = "team.mino.feature.sharereceiver"` · `testImplementation(libs.kotlinx.coroutines.test)`)와 `settings.gradle.kts`의 `include(":feature:sharereceiver")`
- [X] T002 [P] WorkManager 의존성을 `gradle/libs.versions.toml`에 등록 — `androidx.work:work-runtime-ktx` · `androidx.hilt:hilt-work` · `androidx.hilt:hilt-compiler`(ksp) · `androidx.work:work-testing`. 신규 라이브러리 채택 근거는 [ADR 2026-08-26](../../adr/2026-08-26-workmanager-for-detached-requests.md)
- [X] T003 `core/data/build.gradle.kts`에 work-runtime-ktx·hilt-work(ksp `androidx-hilt-compiler`)와 `testImplementation(libs.androidx.work.testing)` 추가 (T002에 의존)
- [X] T004 `app/build.gradle.kts`에 `implementation(project(":feature:sharereceiver"))`와 work-runtime-ktx·hilt-work 추가 (T001, T002에 의존)
- [X] T005 WorkManager 온디맨드 초기화 — `app/src/main/java/team/mino/MinoApplication.kt`에 `Configuration.Provider`를 구현해 `HiltWorkerFactory`를 넘기고, `app/src/main/AndroidManifest.xml`에서 `androidx.startup.InitializationProvider`의 `WorkManagerInitializer`를 제거 (T004에 의존)
- [X] T006 [P] 투명 테마 정의 — `feature/sharereceiver/src/main/res/values/themes.xml`에 `Theme.Mino.Transparent`(투명 배경 · 창 전환 애니메이션 없음). 속성 요구는 [contracts/share-intent.md §1](./contracts/share-intent.md)

---

## Phase 2: 기반 작업 (공통 인프라)

**목적**: 여러 스토리가 함께 쓰는 도메인 타입·응답 봉투·디자인 시스템 컴포넌트를 놓는다.

**⚠️ 중요**: 각 작업이 어떤 사용자 스토리 작업에 쓰이는지 아래 각 줄에 적었다. 실행 순서는 단계가 아니라 그 의존 관계가 정한다([의존성 및 실행 순서](#의존성-및-실행-순서)).

- [X] T007 [P] 응답 봉투 DTO `MinoResponse<T>` 생성 — `core/data/src/main/java/team/mino/core/data/network/dto/response/MinoResponse.kt`. `internal`이며 `ApiService` 밖으로 나가지 않는다([ADR 2026-08-27](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)). **US1의 T020이 쓴다**
- [X] T008 [P] 도메인 모델 `RoomType`·`RoomSummary` 생성 — `core/domain/src/main/kotlin/team/mino/core/domain/model/RoomType.kt` · `RoomSummary.kt`. 필드와 검증 규칙은 [data-model.md §1.1~1.2](./data-model.md). **US1의 T022~T024, US2의 T043이 쓴다**
- [X] T009 [P] 도메인 모델 `SharedPlaceSaveRequest` 생성 — `core/domain/src/main/kotlin/team/mino/core/domain/model/SharedPlaceSaveRequest.kt`([data-model.md §1.3](./data-model.md)). **US1의 T025·T029가 쓴다**
- [X] T010 [P] `WorkManagerModule` 생성 — `core/data/src/main/java/team/mino/core/data/work/di/WorkManagerModule.kt`에서 `WorkManager` 인스턴스를 제공한다. **US1의 T029가 쓴다** (T003, T005에 의존)
- [X] T059 [P] `AnonymousAuthRepository`에 세션 조회 함수 추가 — `core/domain/src/main/kotlin/team/mino/core/domain/repository/AnonymousAuthRepository.kt`에 `suspend fun currentSession(): AnonymousSession?`. 기존 `ensureSession()`은 손대지 않는다([data-model.md §2.3](./data-model.md), [research.md R-020](./research.md)). **US1의 T032가 쓴다**
- [X] T060 `AnonymousAuthRepositoryImpl`에 `currentSession()` 구현 — `core/data/src/main/java/team/mino/core/data/repository/AnonymousAuthRepositoryImpl.kt`. `ensureSession()`의 잠금 밖 빠른 경로와 같은 `AnonymousAuthProvider.currentUserId()` 호출을 쓰되 **발급(`signInAnonymously()`)으로 넘어가지 않는다.** **US1의 T032가 쓴다** (T059에 의존)
- [X] T011 [P] `MinoCheckbox` 신설 — `core/design-system/src/main/java/team/mino/core/designsystem/component/checkbox/`에 `MinoCheckbox.kt`·`MinoCheckboxDefaults.kt`·`token/CheckboxTokens.kt`·`CheckboxPreview.kt`. 구현은 `MinoRoomCheckBoxCard`의 `private fun RoomCheckBox`를 그대로 옮기고 `role = Role.Checkbox`를 유지한다([contracts/room-picker-sheet-ui.md §2.3](./contracts/room-picker-sheet-ui.md)). **US1의 T038이 쓴다**
- [X] T012 [P] `MinoRoomThumbnail` 신설 — `core/design-system/.../component/roomthumbnail/`에 `MinoRoomThumbnail.kt`·`MinoRoomThumbnailDefaults.kt`·`token/RoomThumbnailTokens.kt`·`RoomThumbnailPreview.kt`. **콜라주 배치(1·2·3·4장)만 소유하고 색·캐릭터를 알지 않는다** — 빈 목록이면 `fallback: @Composable () -> Unit` 슬롯을 그린다([contracts/room-picker-sheet-ui.md §2.2](./contracts/room-picker-sheet-ui.md)). **US1의 T038이 쓴다**
- [X] T013 방 카드 일가를 `:core:design-system`으로 이관 — `feature/sample/src/main/java/team/mino/feature/sample/main/component/`의 `MinoRoomCard.kt`·`MinoRoomCheckBoxCard.kt`·`RoomCardContent.kt`·`MinoRoomCardDefaults.kt`·`RoomCardPreview.kt`·`token/RoomCardTokens.kt`를 `core/design-system/.../component/roomcard/`로 옮기고 **원본을 삭제**한다. 옮기면서 `coverImageUrl: String?`을 `thumbnail: @Composable () -> Unit` 슬롯으로 바꾸고 `private fun RoomCheckBox`·`RoomCardCover`를 제거해 T011·T012로 대체한다([research.md R-010](./research.md), [contracts/room-picker-sheet-ui.md §2.1](./contracts/room-picker-sheet-ui.md)). **US1의 T038이 쓴다** (T011, T012에 의존)
- [X] T014 [P] `MinoScrollBar` 신설 — `core/design-system/.../component/scrollbar/`에 `MinoScrollBar.kt`·`MinoScrollBarDefaults.kt`·`token/ScrollBarTokens.kt`·`ScrollBarPreview.kt`([contracts/room-picker-sheet-ui.md §2.4](./contracts/room-picker-sheet-ui.md)). **US2의 T045가 쓴다**
- [X] T058 [P] 썸네일 폴백을 `:core:common:ui`로 승격 — 한 번에 닫아야 빌드가 깨지지 않는 리팩터다([`component-asset-placement.md`](../../conventions/component-asset-placement.md) §2.2·§2.3). **US1의 T038이 쓴다**
  1. `feature/roomform/src/main/res/drawable-{mdpi,xhdpi,xxhdpi}/room_thumbnail_*.webp` 13종을 밀도 3벌 **모두** `core/common/ui/src/main/res/drawable-*/`로 옮긴다
  2. `core/common/ui/src/main/java/team/mino/core/common/ui/component/RoomThumbnailFallback.kt`를 신설한다 — `color: MinoRoomColor?`를 받아 대응 이미지 한 장을 그리고, `null`은 회색 방이다([contracts/room-picker-sheet-ui.md §2.2.1](./contracts/room-picker-sheet-ui.md))
  3. `feature/roomform/.../form/component/RoomPreviewCard.kt`를 새 컴포넌트 호출로 바꾸고 `feature/roomform/.../form/model/RoomColorUiModel.kt`의 `thumbnailRes`를 삭제한다. 도메인 → 팔레트 매핑은 같은 파일의 기존 `RoomColor.chip`을 그대로 쓴다

**체크포인트**: 각 기반 작업이 끝날 때마다 그것을 쓰는 사용자 스토리 작업을 시작할 수 있다.

---

## Phase 3: 사용자 스토리 1 - 외부 앱에서 공유한 게시물을 방에 저장한다

**목표**: OS 공유 시트에서 꾹을 고르면 **앱의 화면을 전면으로 끌어올리지 않고** 딤 배경 위에 방 선택 시트가 곧바로 뜨고, 방을 골라 `[저장하기]`를 누르면 선택한 방 전체에 대한 저장 요청 **한 건**이 접수된 뒤 토스트를 남기고 외부 앱으로 물러난다. 저장할 방이 없으면 같은 시트에 안내를 노출한다.

**독립 테스트**: [quickstart.md §3](./quickstart.md)의 adb `ACTION_SEND` 주입으로 시트를 띄우고 §4.1·§4.2·§4.4·§4.5(TS-001~TS-009, TS-022~TS-028, EC-013)를 통과시킨다. **앱을 띄워둔 채로도 한 번 밟는다** — `force-stop` 전제만 밟으면 TS-027·TS-028이 검증되지 않는다. 시트는 `Peek` 한 단계만 있어도 이 스토리가 성립한다 — 드래그 승격은 US2가 더한다.

### 사용자 스토리 1 테스트 ⚠️

> **참고: 이 테스트들을 먼저 작성하고, 구현 전에 실패하는지 반드시 확인한다.**

- [X] T015 [P] [US1] `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ExtractSharedUrlUseCaseTest.kt` — 문구+URL 1개(TS-007) · URL 여러 개면 첫 번째(EC-003) · URL 없음이면 `null`(EC-002)
- [X] T016 [P] [US1] `core/domain/src/test/kotlin/team/mino/core/domain/usecase/GetRoomPickerRoomsUseCaseTest.kt` — `PERSONAL`이 최상단으로 오고 나머지는 서버 순서를 유지한다(FR-005)
- [X] T017 [P] [US1] `feature/sharereceiver/src/test/java/team/mino/feature/sharereceiver/picker/vm/ShareReceiverViewModelTest.kt` + `fake/` — `RoomRepository`·`SharedPlaceRepository`·`AnonymousAuthRepository` Fake로 세운다. 토글로 `selectedRoomIds`가 갱신되고(FR-007), 선택 0개면 저장이 비활성이며(FR-009), 조회 실패·세션 없음이 모두 빈 목록으로 수렴하고([research.md R-006](./research.md)), `Save`가 `scheduleSave()`를 **한 번** 부른 뒤 `SavedAndFinish`를 낸다
- [X] T070 [P] [US1] 같은 `ShareReceiverViewModelTest.kt`에 `SharedUrlReplaced` 케이스 추가 — 링크가 새 URL로 갈리고 `selectedRoomIds`가 비워지되 `rooms`는 그대로 유지된다(EC-013, [research.md R-024](./research.md))

### 사용자 스토리 1 구현 — 도메인·데이터

- [X] T018 [US1] `ExtractSharedUrlUseCase` 구현 — `core/domain/src/main/kotlin/team/mino/core/domain/usecase/ExtractSharedUrlUseCase.kt`([data-model.md §3](./data-model.md), [research.md R-011](./research.md))
- [X] T019 [P] [US1] `RoomSummaryResponse` DTO 생성 — `core/data/src/main/java/team/mino/core/data/network/dto/response/RoomSummaryResponse.kt`. 필드 대응은 [contracts/room-list-api.md §1.1](./contracts/room-list-api.md). **썸네일 필드명은 plan 3.0.0에서 바뀌었다 — T065가 고친다**
- [X] T020 [US1] `RoomApiService` 신설 — `core/data/src/main/java/team/mino/core/data/network/service/RoomApiService.kt`에 `suspend fun listRooms(): List<RoomSummaryResponse>`. `MinoResponse<List<RoomSummaryResponse>>.data`로 봉투를 벗긴다 (T007, T019에 의존)
- [X] T021 [US1] `RoomListRemoteDataSource`(+`Impl`)와 `core/data/src/main/java/team/mino/core/data/datasource/di/RoomListDataSourceModule.kt` 생성. **기존 `RoomRemoteDataSource`·`RoomDataSourceModule`은 건드리지 않는다**([research.md R-015](./research.md)) (T020에 의존)
- [X] T022 [US1] `RoomSummaryMapper` 생성 — `core/data/src/main/java/team/mino/core/data/repository/mapper/RoomSummaryMapper.kt`. `description` null → `""`, `type` 미상 → `GROUP`, 썸네일 5장 이상이면 앞 4장([data-model.md §1.2](./data-model.md)). **색상 키 필터는 plan 3.0.0에서 생겼다 — T066이 고친다** (T008, T019에 의존)
- [X] T023 [US1] `RoomRepository`에 `suspend fun getRooms(): List<RoomSummary>` 추가 — `core/domain/.../repository/RoomRepository.kt` — 하고 `core/data/.../repository/RoomRepositoryImpl.kt`가 두 DataSource를 함께 주입받게 한다(목록만 실서버, 나머지 셋은 mock 유지) (T021, T022에 의존)
- [X] T024 [US1] `GetRoomPickerRoomsUseCase` 구현 — `core/domain/.../usecase/GetRoomPickerRoomsUseCase.kt`. 개인방 최상단 고정만 하고 그 밖의 정렬을 하지 않는다 (T023에 의존)
- [X] T025 [US1] `SharedPlaceRepository` 인터페이스 신설 — `core/domain/.../repository/SharedPlaceRepository.kt`에 `fun scheduleSave(request: SharedPlaceSaveRequest)`. **`suspend`가 아니며 전송용 함수를 두지 않는다**([data-model.md §2.2](./data-model.md), [research.md R-017](./research.md)) (T009에 의존)
- [X] T026 [P] [US1] `PinCreateRequest` DTO와 `PinApiService` 신설 — `core/data/.../network/dto/request/PinCreateRequest.kt` · `core/data/.../network/service/PinApiService.kt`(`suspend fun createPin(roomId: String, request: PinCreateRequest)`). `202` 본문을 읽지 않는다([contracts/shared-place-save-api.md §1·§4](./contracts/shared-place-save-api.md)). **엔드포인트와 시그니처는 plan 3.0.0에서 바뀌었다 — T061·T062가 고친다**
- [X] T027 [US1] `PinRemoteDataSource`(+`Impl`)와 `core/data/.../datasource/di/PinDataSourceModule.kt` 생성. mock 구현을 두지 않는다([research.md R-013](./research.md)) (T026에 의존)
- [X] T028 [US1] `SharedPlaceSaveWorker` 신설 — `core/data/src/main/java/team/mino/core/data/work/SharedPlaceSaveWorker.kt`. `@HiltWorker` + `@AssistedInject`로 `PinRemoteDataSource`를 받고, `inputData`의 `url`·`roomId`로 요청 한 건을 보낸 뒤 성공을 확정한다. 입력 키는 [data-model.md §4.1](./data-model.md). **재시도·실패 판정은 US3의 T049가 더한다. 입력이 방 하나에서 배열로 바뀐 것은 plan 3.0.0이며 T063이 고친다** (T027에 의존)
- [X] T029 [US1] `SharedPlaceRepositoryImpl`와 `core/data/.../repository/di/SharedPlaceRepositoryModule.kt` 생성 — `roomIds`를 방 단위로 쪼개 `SharedPlaceSaveWorker`를 **N개** 예약한다([research.md R-014](./research.md)). **plan 3.0.0이 이 설계를 뒤집었다(R-021) — T064가 워커 하나로 고친다** (T010, T025, T028에 의존)
- [X] T061 [US1] `PinCreateRequest`에 `roomIds` 추가 — `core/data/src/main/java/team/mino/core/data/network/dto/request/PinCreateRequest.kt`. 저장 대상 방이 경로에서 본문으로 옮겨간다([contracts/shared-place-save-api.md §1.1](./contracts/shared-place-save-api.md), [research.md R-021](./research.md))
- [X] T062 [US1] `PinApiService`의 엔드포인트·시그니처 변경 — `core/data/src/main/java/team/mino/core/data/network/service/PinApiService.kt`. `roomId` 파라미터를 지우고 `client.post("api/v1/rooms/pins")`로 부른다. 종전 `api/v1/rooms/{roomId}/pins`는 **서버에서 삭제됐다**([plan.md §서버 계약 상태](./plan.md)) (T061에 의존)
- [X] T063 [US1] `SharedPlaceSaveWorker`의 입력을 배열로 — `core/data/src/main/java/team/mino/core/data/work/SharedPlaceSaveWorker.kt`. `inputData`의 `roomId` 한 건을 `roomIds` 배열로 바꾸고 요청 한 건을 보낸다. 입력 키는 [data-model.md §4.1](./data-model.md) (T062에 의존)
- [X] T064 [US1] `SharedPlaceRepositoryImpl`이 워커를 **하나만** 예약하도록 — `core/data/src/main/java/team/mino/core/data/repository/SharedPlaceRepositoryImpl.kt`. `roomIds.map { }` 분해를 걷어내고 방 전체를 한 `inputData`에 싣는다. 방 단위 분해는 이제 **서버가** 한다([research.md R-021](./research.md)) (T063에 의존)
- [X] T065 [P] [US1] `RoomSummaryResponse`의 썸네일 필드명을 `thumbnailList`로 — `core/data/src/main/java/team/mino/core/data/network/dto/response/RoomSummaryResponse.kt`. 요청했던 `thumbnailImageUrls`가 아니라 이 이름으로 배포됐다([research.md R-022](./research.md), [contracts/room-list-api.md §1.1](./contracts/room-list-api.md))
- [X] T066 [US1] `RoomSummaryMapper`가 색상 키를 버린다 — `core/data/src/main/java/team/mino/core/data/repository/mapper/RoomSummaryMapper.kt`. `thumbnailList`는 이미지 URL 목록이거나 **색상 키 하나**다. `http://`·`https://`로 시작하지 않는 원소를 걸러낸 뒤 앞 4장을 취한다 — 걸러져 빈 배열이 되면 기존 폴백 경로로 수렴한다([research.md R-022](./research.md)) (T065에 의존)

### 사용자 스토리 1 구현 — UI

- [X] T030 [P] [US1] MVI 슬롯 생성 — `feature/sharereceiver/src/main/java/team/mino/feature/sharereceiver/picker/vm/`에 `ShareReceiverUiState.kt`·`ShareReceiverIntent.kt`·`ShareReceiverSideEffect.kt`. 슬롯 정의는 [data-model.md §5.1·§5.4·§5.5](./data-model.md). `sheetStep`과 `ChangeStep`은 US2의 T042가 더한다
- [X] T031 [US1] `RoomPickerItem` UiModel과 `RoomSummary` → `RoomPickerItem` 변환 — `feature/sharereceiver/.../picker/model/RoomPickerItem.kt`. 빈 `description`은 `null`로 접고, `placeCountLabel` 포맷을 UI가 정하며, 도메인 `RoomColor`를 `MinoRoomColor?`로 옮긴다(`GRAY` → `null`). 이 매핑을 feature가 소유하는 근거는 [방 색상 팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)([data-model.md §5.2](./data-model.md)) (T008에 의존)
- [X] T032 [US1] `ShareReceiverViewModel` 구현 — `feature/sharereceiver/.../picker/vm/ShareReceiverViewModel.kt`. `AnonymousAuthRepository.currentSession()`으로 세션을 확인한 **뒤에** 방 목록을 조회하고(`ensureSession()`을 부르지 않는다 — [research.md R-012](./research.md)·[R-020](./research.md)), 실패·세션 없음은 빈 목록으로 수렴시키며, `Save`에서 `SharedPlaceRepository.scheduleSave()`를 호출한다 (T024, T025, T030, T031, T059, T060에 의존)
- [X] T033 [US1] `ShareReceiverActivity`와 `feature/sharereceiver/src/main/AndroidManifest.xml` 작성 — `ACTION_SEND`/`text/plain` intent-filter, `exported=true`, `Theme.Mino.Transparent`, `excludeFromRecents`, `launchMode=singleTask`. `EXTRA_TEXT`에서 URL을 뽑아 없으면 아무 요청도 보내지 않고 즉시 `finish()`한다([contracts/share-intent.md](./contracts/share-intent.md)). **`taskAffinity`가 빠져 있다 — plan 3.1.0의 T067이 고친다** (T006, T018에 의존)
- [X] T034 [US1] `ShareReceiverRoute` 작성 — `feature/sharereceiver/.../picker/screen/ShareReceiverRoute.kt`. 상태 수집·SideEffect 소비를 맡고, `Shell`을 두지 않으므로 화면 조회 로깅은 `AnalyticsTracker`를 직접 호출한다([research.md R-008](./research.md)) (T032에 의존)
- [X] T035 [US1] `ShareReceiverScreen` 작성 — `feature/sharereceiver/.../picker/screen/ShareReceiverScreen.kt`. 시트·헤더·목록·빈 목록·액션 영역을 조립하는 상태 없는 Composable (T036~T040에 의존)
- [X] T036 [US1] `RoomPickerSheet` 작성 — `feature/sharereceiver/.../picker/component/RoomPickerSheet.kt`. 딤 배경 `Box`(탭하면 닫힘 — EC-001) 위에 `Peek` 436dp 고정 높이 컨테이너와 핸들을 그린다. **2단 드래그는 US2의 T043이 더한다**([contracts/room-picker-sheet-ui.md §3.1](./contracts/room-picker-sheet-ui.md))
- [X] T037 [P] [US1] `RoomPickerHeader` 작성 — `feature/sharereceiver/.../picker/component/RoomPickerHeader.kt`와 `feature/sharereceiver/src/main/res/values/strings.xml`에 `게시물 저장`·`장소를 저장할 방을 선택해주세요.`(FR-004)
- [X] T038 [US1] `RoomPickerList` 작성 — `feature/sharereceiver/.../picker/component/RoomPickerList.kt`. `LazyColumn` + `MinoRoomCheckBoxCard`이며 `onClick`·`onCheckedChange` 모두 같은 `ToggleRoom`을 올린다(UX-003). 카드의 `thumbnail` 슬롯에 `MinoRoomThumbnail`을, 그 `fallback` 슬롯에 `RoomThumbnailFallback(item.color)`를 넘겨 두 모듈로 갈린 썸네일을 여기서 잇는다 (T011, T012, T013, T031, T058에 의존)
- [X] T039 [P] [US1] `RoomPickerEmpty` 작성 — `feature/sharereceiver/.../picker/component/RoomPickerEmpty.kt`와 안내 문구 리소스(FR-013, UX-011). 문구·시각 표현은 미결 M-03
- [X] T040 [US1] 하단 액션 영역 배선 — `MinoActionArea`로 `저장하기` 버튼을 놓고 `selectedRoomIds`가 비면 비활성으로 둔다(FR-009, FR-010, UX-002) (T030에 의존)
- [X] T041 [US1] 저장 완료 피드백과 종료 — `SavedAndFinish`를 받아 시트를 걷고 `MinoSnackbar`(체크 아이콘, 화면 하단 40dp)를 **3초** 띄운 뒤 `finish()`한다. `MinoSnackbar`가 지속 시간을 갖지 않으므로 Activity가 3초를 센다. `Finish`는 토스트 없이 즉시 종료한다([contracts/room-picker-sheet-ui.md §4](./contracts/room-picker-sheet-ui.md)) (T033, T034에 의존)
- [X] T067 [US1] 매니페스트에 `android:taskAffinity=""` 추가 — `feature/sharereceiver/src/main/AndroidManifest.xml`. 이 한 줄이 없으면 기본 affinity가 `applicationId`라 앱이 실행 중일 때 꾹의 태스크가 통째로 전면으로 올라온다(FR-003, TS-027, TS-028 — [contracts/share-intent.md §1](./contracts/share-intent.md), [research.md R-023](./research.md))
- [X] T068 [US1] `SharedUrlReplaced` 슬롯과 ViewModel 처리 — `feature/sharereceiver/.../picker/vm/ShareReceiverIntent.kt`에 `SharedUrlReplaced(url)`를 더하고, `ShareReceiverViewModel.kt`가 `savedStateHandle[KEY_SHARED_URL]`을 덮고 `selectedRoomIds`를 비운다. **`rooms`는 다시 조회하지 않는다** — 두 번째 공유에서만 SC-001·UX-009가 깨진다([data-model.md §5.4·§6](./data-model.md)) (T030, T032에 의존)
- [X] T069 [US1] `ShareReceiverActivity.onNewIntent` 배선 — `feature/sharereceiver/src/main/java/team/mino/feature/sharereceiver/ShareReceiverActivity.kt`. 새 인텐트에서 URL을 뽑아 `setIntent()`로 태스크 레코드를 갈아끼운 뒤 시트에 넘기고, **URL이 없으면 무시해 떠 있는 시트를 유지하며**, 저장 완료 토스트 단계였다면 되돌린다. `recreate()`를 쓰지 않는 이유는 [research.md R-024](./research.md)([contracts/share-intent.md §2.3](./contracts/share-intent.md)) (T067, T068에 의존)

**체크포인트**: 이 시점에서 US1은 완전히 동작하고 독립적으로 테스트 가능하다 — 공유 수신 → `Peek` 시트 → 선택 → 저장 접수 → 토스트 → 복귀.

---

## Phase 4: 사용자 스토리 2 - 시트를 끌어올려 방을 훑고 고른다

**목표**: 방이 많은 사용자가 시트를 `Full`로 끌어올려 목록 전체를 훑고, 스크롤한 뒤에도 헤더와 `[저장하기]`에 바로 닿으며, 단계를 오가도 선택이 유지된다.

**독립 테스트**: [quickstart.md §4.3](./quickstart.md)의 TS-011~TS-014·TS-020·TS-021과 §4.2의 TS-015·TS-016. 높이는 Layout Inspector로 실측한다.

- [X] T042 [US2] `SheetStep` enum과 단계 슬롯 추가 — `feature/sharereceiver/.../picker/model/SheetStep.kt`를 만들고 `ShareReceiverUiState`에 `sheetStep`, `ShareReceiverIntent`에 `ChangeStep(step)`을 더해 ViewModel이 갱신한다. **이 타입은 dp를 알지 않는다**([data-model.md §5.3](./data-model.md))
- [X] T043 [US2] `RoomPickerSheet`에 `AnchoredDraggable` 2단 앵커 적용 — `Peek` 436dp / `Full` 612dp(방 4개 이하) / `Full` 644dp(방 5개 이상). 방 개수와 무관하게 단계 구성은 같다(EC-005, TS-020). 근거는 [research.md R-007](./research.md)·[contracts/room-picker-sheet-ui.md §3.1](./contracts/room-picker-sheet-ui.md) (T036, T042에 의존)
- [X] T044 [US2] 헤더·액션 영역 고정과 목록 스크롤 분리 — `RoomPickerList`의 `LazyColumn`이 시트 높이에서 헤더 94dp와 액션 영역 102dp를 뺀 공간만 차지하게 한다(UX-004, [contracts/room-picker-sheet-ui.md §3.2](./contracts/room-picker-sheet-ui.md)) (T038, T043에 의존)
- [X] T045 [US2] `MinoScrollBar`를 `RoomPickerList` 오른쪽 끝에 결합 — Figma `013-1-2` 기준 `x=366`·`width=9`(UX-005) (T014, T044에 의존)

**체크포인트**: 이 시점에서 US1과 US2가 모두 독립적으로 동작한다.

---

## Phase 5: 사용자 스토리 3 - 저장에 실패한 사실을 알림함에서 확인한다

**목표**: 저장이 방마다 독립적으로 확정되어, 한 방의 실패가 다른 방을 되돌리지 않고 재시도할 값어치가 있는 실패만 재시도된다. **방 단위로 갈라 처리하는 주체는 plan 3.0.0에서 클라이언트에서 서버로 옮겨졌다**([research.md R-021](./research.md)) — 클라이언트가 지키는 것은 요청 한 건의 재시도 판정이다.

**이 스토리에서 클라이언트가 만드는 것은 여기까지다.** `장소를 저장하지 못했어요.`·`이미 저장해둔 곳이에요` 알림 자체는 `202` 이후 서버가 만들고(FR-014·FR-015), 알림함 화면은 spec §3.2가 [SCR-007]로 넘긴 비목표다([contracts/shared-place-save-api.md §3](./contracts/shared-place-save-api.md)).

**독립 테스트**: [quickstart.md §5](./quickstart.md) — §5.1~§5.3(Activity·프로세스 종료 후 생존), §5.4(방을 여러 개 골라도 요청 1건에 `roomIds`가 모두 실린다, TS-019), §5.5(4xx 무재시도 단위 테스트).

### 사용자 스토리 3 테스트 ⚠️

- [X] T046 [P] [US3] `core/data/src/test/java/team/mino/core/data/work/SharedPlaceSaveWorkerRetryTest.kt` — `TestListenableWorkerBuilder` + `ktor-client-mock`으로 4xx는 `Result.failure()`, 5xx·네트워크 오류는 `Result.retry()`임을 검증([quickstart.md §5.5](./quickstart.md))
- [X] T047 [P] [US3] 같은 테스트 파일에 도메인 예외가 아닌 예외의 전파 검증 추가 — 워커가 삼키지 않고 던진다([research.md R-016](./research.md))
- [X] T048 [P] [US3] `core/data/src/test/java/team/mino/core/data/repository/SharedPlaceRepositoryImplTest.kt` — `roomIds` N개가 워커 N건으로 예약되고 각 `inputData`의 `roomId`가 서로 다름을 검증(TS-019, [research.md R-014](./research.md)). **plan 3.0.0이 이 기대를 뒤집었다 — T071이 고친다**
- [X] T071 [P] [US3] `SharedPlaceRepositoryImplTest` 갱신 — `core/data/src/test/java/team/mino/core/data/repository/SharedPlaceRepositoryImplTest.kt`. `roomIds` N개가 워커 **1건**으로 예약되고 그 `inputData`에 `roomIds`가 모두 실림을 검증한다([research.md R-021](./research.md))

### 사용자 스토리 3 구현

- [X] T049 [US3] `SharedPlaceSaveWorker`에 재시도 판정 추가 — `MinoDomainException.Network`와 5xx `Http`는 `Result.retry()`, 4xx `Http`는 `Result.failure()`, 그 밖의 예외는 잡지 않고 전파한다([research.md R-005](./research.md)·[R-016](./research.md), [contracts/shared-place-save-api.md §1.2](./contracts/shared-place-save-api.md)) (T028에 의존)
- [X] T050 [US3] `SharedPlaceRepositoryImpl`의 `OneTimeWorkRequest`에 `NetworkType.CONNECTED` 제약과 지수 백오프 지정 — 오프라인에서는 실행되지 않고 대기한다(EC-009) (T029에 의존)

**체크포인트**: 이제 모든 사용자 스토리가 독립적으로 동작한다.

---

## Phase 6: 마무리 및 공통 관심사

- [X] T051 [P] 이관 잔재 확인 — [quickstart.md §6](./quickstart.md)의 grep 두 건(`feature/sample/`의 방 카드 참조, `core/design-system/`의 `team.mino.feature.sample` 참조)이 **모두 비어 있는지** 확인
- [X] T052 [P] 이관된 Preview 확인 — `core/design-system/src/main/java/team/mino/core/designsystem/component/roomcard/RoomCardPreview.kt`에서 `MinoRoomCheckBoxCard`가 체크/미체크 × 메모 있음/없음 네 조합으로 렌더되는지 Android Studio Preview로 확인
- [X] T053 [P] `core/data/README.md` §3 디렉토리 구조에 신규 패키지 `network/service/`·`work/` 반영
- [X] T054 빌드 게이트 — `./gradlew :app:assembleQaDebug` 성공(헌법 §품질 게이트)
- [ ] T055 Lint 게이트 — `./gradlew lintDebug -Dorg.gradle.jvmargs="-XX:-TieredCompilation"` 위반 0건 또는 문서화된 예외([quickstart.md §2](./quickstart.md))
- [ ] T056 실기기 검증 — [quickstart.md §4·§5](./quickstart.md)의 시나리오를 수행하고 §7 완료 판정 표를 채운다. **`force-stop` 없이 앱을 띄워둔 채 공유하는 경로(TS-027·TS-028)와 시트가 떠 있는 동안의 재공유(EC-013)를 반드시 포함하고, §4.1의 `dumpsys` 태스크 분리 확인을 함께 남긴다**
- [ ] T057 디자인 대조 — `feature/sharereceiver/src/main/java/team/mino/feature/sharereceiver/picker/`의 시트·헤더·목록과 Figma `013-1-1`~`013-1-3`·`013-2`의 높이·문구·카드 구성을 대조한다([`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md))

---

## 미결 사항

작업으로 만들지 않고 남긴 것들이다. 각 항목은 확정되기 전까지 해당 작업의 선행 조건이다.

**개정 — plan 3.1.0 반영 (2026-08-28)**: 기준 plan이 2.2.0에 머물러 있어 **두 개정이 한꺼번에 밀려 있었다.** plan 3.0.0(저장 계약이 방 단위 → `roomIds` 배열 · 썸네일 필드가 `thumbnailList`로 배포)과 plan 3.1.0(태스크 분리 · `onNewIntent`)이다. 대응 코드가 이미 들어간 작업들이라 **기존 `[X]`를 되돌리지 않고** 재작업을 새 ID(T061~T071)로 세웠다 — 그 시점의 설계로 코드가 들어갔다는 사실은 기록이고, 지금 해야 할 일은 따로 세는 것이 맞다. 각 원본 작업 줄에 어느 번호가 고치는지 적었다.

**해소됨 — M-04 (2026-08-28)**: 서버가 썸네일 필드를 붙였다. 이름이 요청한 `thumbnailImageUrls`가 아니라 `thumbnailList`이고 값에 색상 키가 섞여 온다([research.md R-022](./research.md)). T065·T066이 이를 받는다.

**해소됨 — M-01 (2026-08-27)**: `MinoRoomThumbnail`이 `:core:domain`의 `RoomColor`를 받게 적혀 있어 `:core:design-system` → `:core:domain` 역행을 요구했고, 폴백의 캐릭터 에셋도 이미지 에셋을 받지 않는 그 모듈에 들어갈 수 없었다. **폴백을 슬롯으로 밀어내고 `:core:common:ui`가 갖는 것으로 확정**했다 — 콜라주는 디자인 시스템, 폴백 컴포넌트·에셋은 `:core:common:ui`, 도메인 → 팔레트 매핑은 feature. [contracts/room-picker-sheet-ui.md §1·§2.2](./contracts/room-picker-sheet-ui.md)와 [data-model.md §5.2](./data-model.md)를 고쳤고, T012·T031·T038을 갱신하고 T058을 더했다.

| ID | 내용 | 막히는 작업 | 소유 |
|---|---|---|---|
| M-02 | EC-002(URL 없음)가 요구하는 `장소를 저장하지 못했어요.` 알림이 남지 않는다. 클라이언트는 URL 없이 호출할 엔드포인트가 없어 아무 요청도 보내지 않고 종료하므로([contracts/share-intent.md §2.2](./contracts/share-intent.md)), 서버가 이 사례를 인지할 방법이 없다. **PRD 6.0.0 [SYS-002] Flow A·B가 이 알림을 명시적으로 요구하므로**(2026-08-27 확인) 요구사항을 줄이는 대신 서버에 접수 경로를 요청해 닫는다 | 없음(T033은 조용한 종료까지 구현) | 서버 협의 |
| M-03 | 방 0개 안내의 정확한 문구와 시각 표현이 디자인 미확정이다(spec §4 가정) | T039 | 디자인 확정 |


---

## 의존성 및 실행 순서

### 단계 간 의존성

- **셋업 (Phase 1)**: 의존성 없음 — 즉시 시작 가능. 단 T003·T004·T005는 T002의 카탈로그 등록에 걸린다
- **기반 작업 (Phase 2)**: T007~T009·T011·T012·T014·T058·T059는 셋업과 무관하게 시작 가능하고, T010만 T003·T005에, T060은 T059에 걸린다. T058은 `:feature:roomform`을 함께 고치므로 그 모듈을 건드리는 다른 작업과 겹치지 않게 잡는다
- **사용자 스토리 (Phase 3~5)**: 각 작업이 실제로 쓰는 기반 작업에만 의존한다. 기반 단계 전체의 완료를 기다리지 않는다
- **마무리 (Phase 6)**: T051~T053은 대응 구현이 끝나는 대로, T054~T057은 목표한 모든 스토리 완료 후. **T054~T057은 T061~T071이 모두 닫힌 뒤에 다시 돌린다** — 이미 통과 표시된 T054(빌드)도 저장 계약이 바뀌면 다시 판정해야 한다

### 사용자 스토리 간 의존성

- **US1**: 다른 스토리에 의존하지 않는다. Phase 1~2가 공급되면 착수 가능
- **US2**: US1의 `RoomPickerSheet`(T036)·`RoomPickerList`(T038)·MVI 슬롯(T030) 위에 얹힌다. 시트를 두 벌 만들지 않으므로 US1과 파일을 공유하며, **이 셋이 끝난 뒤 시작한다**
- **US3**: US1의 워커(T028·T063)와 예약(T029·T064) 위에 얹힌다. US2와는 무관해 병렬로 진행 가능

### 각 사용자 스토리 내부

- 테스트를 먼저 작성하고 실패를 확인한 뒤 구현한다
- 데이터 흐름 순서: DTO → `ApiService` → `DataSource` → `Mapper` → `RepositoryImpl` → `UseCase`
- UI 순서: MVI 슬롯 → UiModel → ViewModel → Activity·Route → Screen·컴포넌트

### 병렬 처리 기회

- Phase 1의 T002·T006
- Phase 2의 T007·T008·T009·T010·T011·T012·T014·T058·T059 (T013은 T011·T012를, T060은 T059를 기다린다)
- US1의 테스트 3건(T015·T016·T017)
- US1의 데이터 경로 두 갈래 — 방 목록(T019~T024)과 저장(T026~T029)은 서로 닿지 않아 두 사람이 나눠 가질 수 있다
- US1의 UI 컴포넌트 T037·T039는 서로 다른 파일이라 병렬
- **재작업 두 갈래** — 저장 계약(T061~T064)과 썸네일 필드(T065·T066)는 닿는 파일이 겹치지 않아 병렬. 태스크 분리·`onNewIntent`(T067~T069)는 `:feature:sharereceiver`만 건드려 데이터 레이어 재작업과도 병렬
- US3의 테스트 3건(T046·T047·T048)
- Phase 6의 T051·T052·T053

---

## 병렬 실행 예시: 사용자 스토리 1

```bash
# 1) 테스트 3건을 함께 작성한다 (구현 전, 실패 확인)
Task: "ExtractSharedUrlUseCaseTest 작성 — 첫 URL만 추출·URL 없음은 null"
Task: "GetRoomPickerRoomsUseCaseTest 작성 — 개인방 최상단 고정"
Task: "ShareReceiverViewModelTest 작성 — 토글·빈 목록 수렴·저장 예약"

# 2) 데이터 경로 두 갈래를 나눠 진행한다
Task: "RoomSummaryResponse DTO + RoomApiService.listRooms() 구현"
Task: "PinCreateRequest DTO + PinApiService.createPin() 구현"

# 3) 서로 다른 파일의 UI 컴포넌트를 함께 만든다
Task: "RoomPickerHeader 작성 + 헤더 문구 리소스"
Task: "RoomPickerEmpty 작성 + 빈 목록 안내 리소스"
```

---

## 구현 전략

### MVP 우선 (US1만)

1. Phase 1 셋업을 끝낸다 — 모듈·WorkManager·투명 테마
2. Phase 2에서 US1이 쓰는 것(T007~T013, T058~T060)을 공급한다
3. Phase 3(US1)을 끝낸다
4. **중단하고 검증**: adb로 공유를 주입해 `Peek` 시트에서 저장까지 관통시킨다([quickstart.md §3·§4.1·§4.2](./quickstart.md))
5. 이 시점의 산출물은 방 3개 이하 사용자에게 완결된 기능이다

### 밀린 재작업 먼저 (T061~T071)

새로 착수하는 경우가 아니라 **이미 들어간 코드를 plan 3.1.0에 맞추는 것**이 지금의 실제 상태다.

1. 저장 계약 배열화 — T061 → T062 → T063 → T064 → T071 (한 갈래로 이어진다)
2. 썸네일 필드 — T065 → T066 (위와 병렬)
3. 태스크 분리·재공유 — T067 → T068 → T069 → T070 (위 둘과 병렬)
4. **중단하고 검증**: T054 재실행 후 T056으로 TS-027·TS-028·EC-013까지 관통시킨다

### 점진적 전달

1. US1 완료 → 독립 검증 (MVP)
2. US2 추가 → 방 5개 이상 사용자까지 커버 → 독립 검증
3. US3 추가 → 부분 실패·오프라인 재시도까지 확정 → 독립 검증

### 팀 병렬 전략

1. 셋업(Phase 1)은 한 사람이 먼저 끝낸다 — 모듈이 없으면 아무도 시작할 수 없다
2. 이후 갈래를 나눈다
   - 개발자 A: 디자인 시스템·폴백 승격(T011~T014, T058) → US1 UI(T030~T041)
   - 개발자 B: 데이터·도메인(T007~T010, T059·T060, T018~T029) → US3(T046~T050)
3. US2(T042~T045)는 A의 US1 UI가 닫힌 뒤 A가 이어서 진행한다

---

## 참고 사항

- 피해야 할 것: 모호한 작업, 동일 파일 충돌, 독립성을 깨뜨리는 스토리 간 의존성
- 커밋 단위는 [`docs/conventions/commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다
- 이 feature는 저장소에서 **처음으로 실제 서버 응답을 파싱한다.** `network/service/`·`work/`는 신규 패키지이며, 작성 규칙은 [`core/data/README.md`](../../../core/data/README.md) §4·§5·§8이 소유한다
