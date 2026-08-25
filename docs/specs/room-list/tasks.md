# 작업 목록: 방 리스트 탭 (Room List Tab)

**대상 스펙 경로**: `docs/specs/room-list`

**기준 plan 버전**: 1.2.0

**최초 작성일**: 2026-08-21

**최종 수정일**: 2026-08-21

**사전 조건**: [plan.md](./plan.md)(필수), [spec.md](./spec.md)(사용자 스토리), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/), [ADR 2026-08-18](../../adr/2026-08-18-room-card-components-in-design-system.md)

**테스트**: 자동 테스트 작업을 포함하지 않는다. `spec.md`가 테스트를 명시적으로 요청하지 않고 사용자도 TDD를 요청하지 않았다 — plan.md Technical Context가 이 판단을 tasks.md로 위임했으므로 여기서 "미도입"으로 확정한다(근거는 완료 보고 참고). 검증은 [quickstart.md](./quickstart.md)의 수동 시나리오와 최소 게이트(`./gradlew :app:assembleQaDebug`, `docs/constitution.md` 「검증 장치의 한계」)로 한다.

**구성 방식**: `spec.md`의 유저 플로우 4개를 사용자 스토리(US1~US4)로 그대로 매핑해 각 스토리를 독립적으로 구현·검증할 수 있게 묶는다.

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. 한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 완료되지 않은 작업에 의존하지 않음)
- **[Story]**: US1(지도·시트) / US2(방 목록·상세 진입) / US3(공동방 생성) / US4(Nudge·Ghost Card)
- 설명에는 정확한 파일 경로를 포함한다.

## 경로 규칙

- 모바일(Android, 다중 Gradle 모듈) — `docs/architecture/modularization.md` 기준. 아래 경로는 모두 저장소 루트 기준이며, `core/domain/src/main/kotlin/...`, `core/data/src/main/java/...`, `core/navigation/src/main/java/...`, `core/design-system/src/main/java/...`, `feature/room/src/main/java/...`는 [plan.md 「프로젝트 구조」](./plan.md)를 그대로 따른다.

---

## Phase 1: 셋업 (공통 인프라)

**목적**: `:feature:room` 모듈 골격을 만들고 저장소에 등록한다.

- [X] T001 `settings.gradle.kts`에 `include(":feature:room")` 추가, `feature/room/build.gradle.kts` 작성(컨벤션 플러그인·`:core:domain`·`:core:navigation`·`:core:design-system`·`:core:map`·`:core:common:android`·`:core:common:ui` 의존 추가), `feature/main/build.gradle.kts`에 `:feature:room` 의존 추가
- [X] T002 [P] `feature/room/src/main/java/team/mino/feature/room/RoomNavigation.kt`에 `RoomGraph`(public)·`roomGraph()` 등록 함수 스켈레톤 작성(빈 컴포저블), `:feature:main` 그래프에 등록

> **구현 편차 기록(발견: constitution-auditor, Phase 7)**: T002가 만든 `roomGraph()`는 `RoomListRoute`(T027 이후 실제로 존재)로 교체되지 않고 빈 placeholder(`RoomMainPlaceholder`)로 남아 있었다 — US1~US4가 전부 `[X]`로 마킹된 뒤에도 앱 실행 시 화면에 도달할 방법이 없었던 MUST 위반. Phase 7 검증 중 `roomGraph()`가 `RoomListRoute(sheetLevelOverride = null)`를 등록하도록 수정하고 `:app:assembleQaDebug`·`ktlintCheck`로 재확인했다. tasks.md에 "placeholder를 실제 Route로 교체"하는 별도 작업 ID가 없었던 것 자체가 이번 작업 분해의 누락이었다 — 후속 유사 feature의 tasks.md는 이 교체를 명시적 작업으로 넣을 것.
- [X] T003 [P] `core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/ExtraTag.kt`에 `EXTRA_ROOM_DETAIL_ROOM_ID` 상수 추가(기존 파일에 병합, `EXTRA_SAMPLE_*` 패턴 그대로)

**체크포인트**: 모듈이 저장소에 등록되고 그래프에 연결됨(빈 화면이라도 진입 가능)

---

## Phase 2: 기반 작업 (선행 필수 — 모든 사용자 스토리를 차단)

**목적**: `:core:domain` 모델·계약, `:core:data` 구현, 디자인 시스템 승격, 화면 상태 골격까지 모든 스토리가 공유하는 인프라를 완성한다.

**⚠️ 중요**: 이 단계가 끝나기 전에는 어떤 사용자 스토리 작업도 시작할 수 없다.

### 도메인 모델 (`:core:domain`) — [data-model.md §1](./data-model.md)

- [X] T004 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/Room.kt`에 `Room` 데이터 클래스 작성
- [X] T005 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/RoomThumbnail.kt`에 `RoomThumbnail` sealed interface(`ColorAndCharacter`/`Collage`) 작성
- [X] T006 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/RoomMemberSummary.kt` 작성
- [X] T007 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/RoomListSortOption.kt` enum 작성
- [X] T008 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/MapMarkerSortOption.kt` enum 작성
- [X] T009 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/PlaceCategoryFilter.kt` enum 작성
- [X] T010 `core/domain/src/main/kotlin/team/mino/core/domain/repository/RoomRepository.kt`에 `observeMyRooms()`·`getRoom()` 인터페이스 작성(T004 의존, [contracts/room-repository.md](./contracts/room-repository.md))

> **구현 편차 기록**: `Room.color`는 `data-model.md`가 지정한 `MinoRoomColor?` 대신 `String?`(원시 색상 식별자)로 구현됨 — `:core:domain`은 JVM-only라 Android/Compose 의존인 `:core:design-system`을 참조할 수 없음(`core/domain/README.md` §8, 헌법 원칙 II). `MinoRoomColor` 매핑은 렌더링하는 feature가 소유해야 한다(ADR 2026-08-14와 일치). `data-model.md` 정정 필요 — 이 스킬 범위 밖이라 수정하지 않음, `/mino-plan` 재개정 대상으로 보고.

### 네비게이션 계약 (`:core:navigation`) — [contracts/navigation-launchers.md](./contracts/navigation-launchers.md)

- [X] T011 [P] `core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/RoomDetailLauncher.kt`에 `RoomDetailLauncher : ActivityLauncher` 인터페이스 작성(T003 의존)
- [X] T012 [P] `core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/RoomFormLauncher.kt`에 `RoomFormLauncher : ActivityLauncher` 인터페이스 작성

### 데이터 레이어 (`:core:data`) — [contracts/room-repository.md §구현 위치](./contracts/room-repository.md), 백엔드 draft 갭은 [research.md D12](./research.md)

- [X] T013 [P] `core/data/src/main/java/team/mino/core/data/network/dto/response/RoomSummaryResponse.kt` 작성 — draft `GET /api/v1/rooms` 응답 필드(`id`·`type`·`name`·`description`·`color`·`ownerId`·`inviteCode`·`createdAt`·`pinCount`·`memberCount`·옵션 `hasPlace`·`users`) 기준
- [X] T014 [P] `core/data/src/main/java/team/mino/core/data/network/service/RoomApiService.kt` 작성 — Ktor `HttpClient`로 `GET /api/v1/rooms` 호출(T013 의존)
- [X] T015 `core/data/src/main/java/team/mino/core/data/datasource/RoomRemoteDataSource.kt`(+`Impl`) 작성 — `RoomApiService` 호출 위임(T014 의존)
- [X] T016 [P] `core/data/src/main/java/team/mino/core/data/datasource/di/RoomDataSourceModule.kt` — `@Binds @Singleton`(T015 의존)
- [X] T017 `core/data/src/main/java/team/mino/core/data/repository/mapper/RoomMapper.kt` 작성 — `RoomSummaryResponse.toDomain(): Room`. draft에 없는 필드(썸네일 콜라주·`visibleAvatarUrls`·`lastPlaceSavedAt`·`commentCount`)는 임시 목데이터/플레이스홀더로 채움(T004~T006, T013 의존, [research.md D12](./research.md))
- [X] T018 `core/data/src/main/java/team/mino/core/data/repository/RoomRepositoryImpl.kt` 작성 — `observeMyRooms()`·`getRoom()` 구현(T010, T015, T017 의존)
- [X] T019 [P] `core/data/src/main/java/team/mino/core/data/repository/di/RoomRepositoryModule.kt` — `@Binds @Singleton`(T018 의존)

> **구현 편차 기록**: `RoomApiService`는 상대 경로(`api/v1/rooms`)로 호출하므로 `NetworkModule`의 `HttpClient` baseUrl이 실제 서버로 교체되면 그대로 동작한다. 단, `feature/154-room-list/base`가 develop 대비 24커밋 뒤처져 있어(`develop`에 이미 머지된 [PR #208](https://github.com/mash-up-kr/Team-MINO-Android/pull/208) — GitHub 스캐폴딩 제거·`NetworkModule` baseUrl을 `BuildConfig.API_BASE_URL`로 교체) 지금 이 브랜치의 `NetworkModule`은 여전히 `https://api.github.com/`으로 하드코딩돼 있고 `Github*` 파일 8개가 `:core:data`에 남아 있다. room-list 구현과는 충돌 없이 공존하며 별개 이슈(사용자 확인 완료, 나중에 base→develop 머지로 자연 해소)로 남긴다.
> **구현 편차 기록**: `observeMyRooms()`는 서버에 realtime 채널이 없어(`contracts/room-repository.md`도 이 한계를 명시) `Flow { emit(getRooms()) }` 형태의 단발성 방출로 구현됨 — 다른 화면에서 방 정보가 바뀌어도 재구독 없이는 반영되지 않는다(재진입 시에는 반영됨). 실시간 반영이 필요해지면 서버 realtime 지원 이후 재구현 대상.

### 디자인 시스템 승격 (`:core:design-system`) — [ADR 2026-08-18](../../adr/2026-08-18-room-card-components-in-design-system.md), [research.md D4](./research.md)

> ADR이 이미 결정을 확정해 뒀다("이 승격 작업 자체는 room-list의 `/mino-plan`이 아니라 `/mino-task`(구현 단계)가 수행한다"). 아래는 그 실행 작업이다.

- [X] T020 `feature/sample/src/main/java/team/mino/feature/sample/main/component/`의 `MinoRoomCard.kt`·`MinoRoomCardDefaults.kt`·`MinoRoomCheckBoxCard.kt`·`MinoChipRoom.kt`·`MinoHeaderRoom.kt`·`MinoHeaderRoomDefaults.kt`·`RoomCardContent.kt`·`token/RoomCardTokens.kt`·`token/ChipRoomTokens.kt`·`token/HeaderRoomTokens.kt`를 `core/design-system/src/main/java/team/mino/core/designsystem/component/roomcard/`(및 하위 `token/`)로 이동한다. 컴포넌트는 **stateless 그대로 유지**하고(`Room` 등 도메인 모델을 파라미터로 받지 않음, ADR 「결정」), 시그니처를 바꾸지 않는다

> **구현 편차 기록(발견: quality-gate-runner Compose Lint, Phase 7)**: `RoomCardContent.kt`의 trailing lambda 파라미터명이 `placeCountTrailing`으로 `ComposableLambdaParameterNaming` 룰(trailing lambda는 `content`로 명명)을 위반했다. 호출부(`MinoRoomCard`·`MinoRoomCheckBoxCard`) 둘 다 named argument가 아니라 trailing lambda 문법으로 호출하고 있어 호출부 변경 없이 `content`로 리네임했다 — 공개 시그니처(순서·타입)는 그대로다.
- [X] T021 `feature/sample/src/main/java/team/mino/feature/sample/main/component/`의 `RoomCardPreview.kt`·`ChipRoomPreview.kt`·`HeaderRoomPreview.kt`를 삭제하고(ADR 「결과」 — 승격 후 `:feature:sample` 원본은 삭제), 필요하면 `core/design-system` 쪽에 동등한 Preview를 새로 작성한다(T020 의존)

### 화면 상태 골격 (`:feature:room`) — [contracts/room-list-main-contract.md](./contracts/room-list-main-contract.md), [data-model.md §2](./data-model.md)

- [X] T022 `feature/room/src/main/java/team/mino/feature/room/main/model/BottomSheetLevel.kt` enum(`PEEK`/`HALF`/`FULL`) 작성
- [X] T023 `feature/room/src/main/java/team/mino/feature/room/main/vm/RoomListUiState.kt` 작성 — 계약의 모든 필드(T004~T009, T022 의존)
- [X] T024 [P] `feature/room/src/main/java/team/mino/feature/room/main/vm/RoomListIntent.kt` 작성 — 계약의 모든 Intent
- [X] T025 [P] `feature/room/src/main/java/team/mino/feature/room/main/vm/RoomListSideEffect.kt` 작성 — 계약의 모든 SideEffect
- [X] T026 `feature/room/src/main/java/team/mino/feature/room/main/vm/RoomListViewModel.kt` 스켈레톤 작성 — 초기 상태 노출만, Intent 분기는 각 스토리 단계에서 추가(T010, T018, T023~T025 의존)
- [X] T027 `feature/room/src/main/java/team/mino/feature/room/main/screen/RoomListRoute.kt`·`RoomListScreen.kt` 스켈레톤 작성 — `RoomListRoute`는 `sheetLevelOverride: BottomSheetLevel?` 시작 인자를 받는다(`data-model.md §2`, EC-007 대비)(T026 의존)
- [X] T028 `feature/room/src/main/java/team/mino/feature/room/di/RoomLauncherStubModule.kt` — `RoomDetailLauncher`·`RoomFormLauncher`의 **임시 스텁 구현 + `@Binds`** 작성. `:feature:roomdetail`·`:feature:roomform`이 아직 없어 Hilt 그래프 컴파일이 막히는 문제의 임시 조치([research.md D5·D6](./research.md), [quickstart.md 선행 조건](./quickstart.md)) — 두 모듈이 실제로 생기면 이 파일을 지우고 해당 모듈의 `di/`가 바인딩을 넘겨받는다(T011, T012 의존)

**체크포인트**: 기반 준비 완료 — `:app:assembleQaDebug`가 통과해야 한다(빈 화면이라도 그래프 조립 성공). 이제 사용자 스토리를 순서대로 구현한다.

---

## Phase 3: 사용자 스토리 1 — 지도 & 3단 바텀시트 탐색

**목표**: 탭 진입 시 지도와 `Half` 시트가 뜨고, 드래그로 `Peek`/`Half`/`Full`을 전환하며, 정렬 드롭다운·카테고리 칩으로 지도 마커를 필터링한다.

**독립 테스트**: [quickstart.md](./quickstart.md) 1~3번 시나리오 — `RoomDetailLauncher`·`RoomFormLauncher`가 스텁이어도 지도·시트·필터는 이 스토리만으로 완결 검증 가능하다.

### 사용자 스토리 1 구현

- [X] T029 [P] [US1] `feature/room/src/main/java/team/mino/feature/room/main/component/RoomListMap.kt` 작성 — `:core:map` `MinoMap` 래핑, `personalRoom`·`groupRooms` 장소 마커 오버레이
- [X] T030 [US1] `RoomListViewModel`에 초기 `sheetLevel` 결정 로직 추가 — `sheetLevelOverride`가 `null`이면 `HALF`+공동방 수 기준 높이(FR-002), 값이 있으면 그대로 사용(EC-007, T023·T027 의존)
- [X] T031 [US1] `RoomListViewModel`에 `OnScreenEntered` 처리 추가 — `ContextCompat.checkSelfPermission`로 즉시 조회(D8), 이미 허용이면 실제 위치로 `mapCenter` 설정, 미허용이면 `RequestLocationPermission` SideEffect 발행(T026 의존)
- [X] T032 [US1] `RoomListViewModel`에 `OnLocationPermissionResult` 처리 추가 — `granted=false`면 기본 디폴트 좌표로 `mapCenter` 설정(EC-002), `true`면 실제 위치로 설정
- [X] T033 [US1] `RoomListRoute.kt`에 위치 권한 런처 연결 — `RequestLocationPermission` SideEffect 구독 시 `[FINE, COARSE]` 권한 요청, 결과를 `OnLocationPermissionResult`로 전달(T031 의존)
- [X] T034 [US1] `feature/room/src/main/java/team/mino/feature/room/main/component/RoomListBottomSheet.kt` 작성 — `Peek`(88dp)/`Half`(256/360/380dp, 공동방 수 기준)/`Full` 렌더 분기(FR-002)
- [X] T035 [US1] `RoomListViewModel`에 `OnSheetDraggedUp`/`OnSheetDraggedDown` 분기 처리 추가 — [contracts/room-list-main-contract.md 「분기 규칙 — 시트 드래그 전이」](./contracts/room-list-main-contract.md) 표 그대로(T026 의존)
- [X] T036 [US1] `RoomListScreen`에 정렬 드롭다운 조립 — `:core:design-system` `MinoMenu` 재사용, `OnMapSortSelected` 연결([research.md D11](./research.md))
- [X] T037 [US1] `RoomListScreen`에 카테고리 칩 조립 — `:core:design-system` `MinoChip` 재사용, `OnCategoryFilterSelected` 연결
- [X] T038 [US1] `RoomListViewModel`에 `OnMapSortSelected`·`OnCategoryFilterSelected` 처리 추가 — `RoomListMap` 마커 필터링에 반영
- [X] T039 [US1] `RoomListScreen`에 현재 위치 버튼 최소 구현 — `OnCurrentLocationClick` 처리로 `mapCenter` 갱신, `sheetLevel == FULL`일 때 숨김(UX-002, [research.md D10](./research.md))

> **구현 편차 기록(T029)**: spec.md FR-001이 요구하는 지도 마커는 개별 장소(Place) 좌표가 있어야 하는데, `Room` 도메인 모델·`data-model.md` 어디에도 `Place`·좌표 계약이 없다(spec.md §3.2가 Place 자체를 이 spec 범위 밖으로 명시). 사용자 확인 하에 방 ID 해시로 파생한 **임시 목데이터 좌표**(강남역 주변, `RoomListMap.kt`의 `mockMarkerCenter()`)로 마커를 얹었다 — 실제 장소 좌표 계약이 생기면 이 함수를 지우고 교체해야 한다. `OnMapSortSelected`·`OnCategoryFilterSelected`(T038)도 마커 자체에 카테고리·좌표 정보가 없어 상태 갱신 이상의 실질적 필터링은 하지 못한다.
> **구현 편차 기록**: `DefaultMapCenter`(EC-002 기본 디폴트 좌표)는 PRD [SYS-004] Flow A "현재 강남역으로 임시 지정"에 맞춰 강남역 좌표(37.4979, 127.0276)로 설정했다(최초 구현 중 서울시청으로 잘못 넣었던 것을 정정).
> **버그 수정(2026-08-25, 실기기 검증 중 발견 — T031~T033·T039 모두 `[X]` 완료 처리됐음에도 실제로는 동작하지 않았다)**: 세 가지가 함께 있어야 FR-001·UX-002가 실제로 성립하는데 그중 하나라도 빠지면 증상이 "위치가 안 바뀐다"로 동일하게 보여 순서대로 발견했다.
>   1. **`AndroidManifest.xml`에 `ACCESS_FINE_LOCATION`·`ACCESS_COARSE_LOCATION` 선언이 아예 없었다** — 매니페스트에 없는 권한은 런타임에 요청해도 시스템이 다이얼로그를 띄우지 않고 조용히 거부 처리된다(T033이 권한 런처를 연결해놔도 애초에 뜰 수 없었음). `app/src/main/AndroidManifest.xml`에 두 줄 추가로 해결.
>   2. **`currentDeviceLocation()`이 `LocationManager.getLastKnownLocation()`만 조회**해 다른 앱이 최근 위치를 요청한 적 없는 기기에서는 모든 provider가 `null`을 반환하고, 권한을 허용했는데도 조용히 `DefaultMapCenter`로 폴백했다. 캐시가 없으면 활성화된 provider로 `requestSingleUpdate`(최대 10초)를 능동적으로 요청하도록 수정.
>   3. **`RoomListMap`의 `rememberMinoCameraState`가 최초 컴포지션 시점의 `center`만 초기값으로 쓸 뿐, 이후 `mapCenter`가 바뀌어도 카메라를 옮기지 않았다** — 위 두 가지를 고쳐도 이 문제 때문에 지도가 여전히 안 움직였다. `LaunchedEffect(mapCenter) { cameraPositionState.animate(...) }`로 수정. **세 버그 중 실제 증상의 최종 원인은 이것**이었다.
>   T031~T033·T039의 `[X]` 표시는 "코드가 작성됨"을 의미할 뿐 "실기기에서 검증됨"을 보장하지 않는다는 사례로 남긴다 — 후속 유사 작업은 완료 처리 전 실기기·에뮬레이터 수동 검증을 병행할 것.

**체크포인트**: 이 시점에서 지도·3단 시트·필터가 독립적으로 동작하고 검증 가능해야 한다.

---

## Phase 4: 사용자 스토리 2 — 방 목록 조회 및 방 상세 진입 (Full)

**목표**: `Full` 시트에서 개인방 고정 + 공동방 목록을 정렬해 훑고, 방 카드 선택으로 방 상세에 진입한다.

**독립 테스트**: [quickstart.md](./quickstart.md) 4번 시나리오.

### 사용자 스토리 2 구현

- [X] T040 [P] [US2] `RoomListBottomSheet.kt`의 `Full` 분기에 방 카드 목록 렌더 추가 — 개인방(`내 장소`) 최상단 고정 + 공동방, `:core:design-system` `MinoRoomCard`(T020) 사용(FR-004)
- [X] T041 [US2] `feature/room/src/main/java/team/mino/feature/room/main/model/RoomCardUiModel.kt`(또는 `Room.toRoomCardParams()` 확장 함수) 작성 — `MinoRoomCard`는 stateless라 `Room` 도메인 모델을 모른다(ADR 「결정」). `Room` → `title`·`placeCountLabel`·`participantImageUrls`·`coverImageUrl`·`memo` 매핑을 이 feature가 소유(T004, T020 의존)
- [X] T042 [US2] `RoomListScreen`에 정렬 칩(전체/최근 저장 순/코멘트 순) 조립 — `MinoChip` 재사용, `OnRoomListSortSelected` 연결
- [X] T043 [US2] `RoomListViewModel`에 `OnRoomListSortSelected` 처리 추가 — 개인방 고정 유지한 채 공동방만 재정렬(FR-005)
- [X] T044 [US2] `RoomListViewModel`에 `OnRoomCardClick` 처리 추가 — `NavigateToRoomDetail` SideEffect 발행
- [X] T045 [US2] `RoomListRoute.kt`에 `RoomDetailLauncher` 호출 연결 — `roomDetailLauncher.launch(activity) { putExtra(EXTRA_ROOM_DETAIL_ROOM_ID, roomId) }`(T028, T044 의존)

**체크포인트**: 이 시점에서 US1+US2가 함께 독립적으로 동작해야 한다.

---

## Phase 5: 사용자 스토리 3 — 신규 공동방 생성

**목표**: 시트 `[+]`로 공동방 생성 폼을 호출하고, 생성 완료 시 방 상세로 직행한다.

**독립 테스트**: [quickstart.md](./quickstart.md) 5번 시나리오.

### 사용자 스토리 3 구현

- [X] T046 [US3] `RoomListViewModel`에 `OnAddRoomClick` 처리 추가 — `NavigateToRoomForm` SideEffect 발행(FR-007)
- [X] T047 [US3] `RoomListRoute.kt`에 `RoomFormLauncher` 호출 연결 — `resultLauncher`로 결과 수신(T028 의존)
- [X] T048 [US3] `RoomListViewModel`에 `OnRoomFormResult` 처리 추가 — `createdRoomId`가 있으면 `NavigateToRoomDetail`로 체이닝(FR-007, T044 재사용)

> **구현 편차 기록(T047)**: `RoomFormLauncher` 결과 extra 키(`EXTRA_ROOM_FORM_CREATED_ROOM_ID`)는 `:feature:roomform`(미구현)이 확정할 계약이라 `:core:navigation`의 `ExtraTag.kt`에 정식 상수로 올리지 않고, `RoomListRoute.kt` 파일 내부 `private const val`로 임시 정의했다 — roomform 쪽 계약이 확정되면 이 파일만 고치면 되도록 근거 주석을 남겨뒀다.

**체크포인트**: 이 시점에서 US1+US2+US3가 함께 독립적으로 동작해야 한다.

---

## Phase 6: 사용자 스토리 4 — 공동방 생성 유도 (Nudge & Ghost Card)

**목표**: 공동방 0개인 사용자에게 Nudge와 Ghost Card로 첫 공동방 생성을 유도한다.

**독립 테스트**: [quickstart.md](./quickstart.md) 6번 시나리오.

### 사용자 스토리 4 구현

- [X] T049 [P] [US4] `feature/room/src/main/java/team/mino/feature/room/main/component/RoomNudgeSheet.kt` 작성
- [X] T050 [P] [US4] `feature/room/src/main/java/team/mino/feature/room/main/component/RoomGhostCard.kt` 작성
- [X] T051 [US4] `RoomListViewModel`의 `RoomRepository.observeMyRooms()` 구독 리듀서에서 `groupRooms` 갱신마다 `showNudge`·`showGhostCard`를 `groupRooms.isEmpty()` 파생값으로 계산(T018, T026 의존, [contracts/room-list-main-contract.md 「분기 규칙 — Nudge·Ghost Card 노출」](./contracts/room-list-main-contract.md))
- [X] T052 [US4] `RoomListViewModel`에 `OnNudgeCreateClick` 처리 추가 — `NavigateToRoomForm` 재사용(T046과 동일 SideEffect)
- [X] T053 [US4] `RoomListViewModel`에 `OnNudgeDismissClick` 처리 추가 — `showNudge`만 로컬로 `false`로 접고 `groupRooms`는 불변 유지(재진입 시 재계산되도록, TS-014)
- [X] T054 [US4] `RoomListScreen`에 `RoomNudgeSheet`·`RoomGhostCard` 조립 — `showNudge`/`showGhostCard` 바인딩

> **구현 편차 기록(T051)**: `RoomRepository.observeMyRooms()`를 실제로 구독하는 배선(`init { observeMyRooms() }`)이 T018·T026·T040 완료 시점까지 빠져 있었다 — 그때까지는 `personalRoom`·`groupRooms`가 초기값(`null`/빈 리스트)에 머물러 있었을 것이다. `contracts/room-list-main-contract.md` 「재조회」 절이 이미 이 구독을 계약으로 확정해 뒀고 T051 자체가 "구독 리듀서" 작업이라, 이 구독 배선을 T051 범위 안에서 함께 만들었다. 이전 US1·US2 체크포인트(T034·T040 등)가 틀린 건 아니다 — 컴포넌트·분기 로직 자체는 맞게 작성됐고, 실데이터 연결만 뒤늦게 완성됐다.

**체크포인트**: 이제 US1~US4 모두 독립적으로 동작해야 한다.

---

## Phase 7: 마무리 및 공통 관심사

**목적**: 전체 스토리에 걸친 최종 검증.

- [ ] T055 [quickstart.md](./quickstart.md) 1~6번 시나리오 수동 검증 실행(7번은 `room-detail`[이슈 #161] 구현 이후로 보류)
- [X] T056 `./gradlew :app:assembleQaDebug` 통과 확인(최소 게이트, `docs/constitution.md` 「검증 장치의 한계」)
- [X] T057 [P] `ktlintCheck` 통과 확인

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **셋업 (Phase 1)**: 의존성 없음 — 즉시 시작 가능
- **기반 작업 (Phase 2)**: 셋업 완료에 의존 — 모든 사용자 스토리를 차단함
- **US1~US4 (Phase 3~6)**: 모두 기반 작업 완료에 의존
  - US2는 US1과 병렬 가능(공유 파일 `RoomListBottomSheet.kt`·`RoomListViewModel.kt`가 겹치므로 실제로는 US1의 T034·T035 완료 후 US2의 T040을 얹는 편이 충돌이 적다)
  - US3은 독립적(런처 스텁만 있으면 됨)
  - US4는 US1의 `RoomListScreen` 조립(T036~T039) 이후에 컴포넌트를 붙이는 편이 자연스럽다
- **마무리 (Phase 7)**: US1~US4 완료에 의존

### 각 사용자 스토리 내부

- 컴포넌트 작성 → `RoomListViewModel` Intent 분기 추가 → `RoomListRoute`/`RoomListScreen` 조립 순서
- 같은 파일(`RoomListViewModel.kt`·`RoomListScreen.kt`)을 여러 스토리가 이어서 수정하므로, 그 파일에 닿는 작업들은 스토리 간 순차 진행을 권장한다(병렬 시 병합 충돌)

### 병렬 처리 기회

- Phase 2의 도메인 모델(T004~T009), 데이터 레이어의 서로 다른 파일(T013·T014는 [P], 이후 T015~T019는 순차), `RoomListIntent`/`RoomListSideEffect`(T024·T025)는 병렬 가능
- US1의 `RoomListMap.kt`(T029)는 다른 US1 작업과 파일이 겹치지 않아 [P]
- US4의 `RoomNudgeSheet.kt`·`RoomGhostCard.kt`(T049·T050)는 서로 다른 파일이라 [P]

---

## 병렬 실행 예시: Phase 2 도메인 모델

```bash
Task: "core/domain/.../model/Room.kt에 Room 데이터 클래스 작성"
Task: "core/domain/.../model/RoomThumbnail.kt에 RoomThumbnail sealed interface 작성"
Task: "core/domain/.../model/RoomMemberSummary.kt 작성"
Task: "core/domain/.../model/RoomListSortOption.kt enum 작성"
Task: "core/domain/.../model/MapMarkerSortOption.kt enum 작성"
Task: "core/domain/.../model/PlaceCategoryFilter.kt enum 작성"
```

---

## 구현 전략

### MVP 우선 (사용자 스토리 1만)

1. Phase 1~2 완료(빌드 통과가 체크포인트)
2. Phase 3(US1) 완료 → 지도+시트+필터 단독 검증
3. 준비되면 이 상태로 데모 가능(방 카드·생성·Nudge 없이도 탐색 기능만으로 가치 있음)

### 점진적 전달

1. 셋업 + 기반 작업 → 기반 준비 완료(그래프 조립 성공)
2. US1 추가 → 독립 검증 → 데모(MVP)
3. US2 추가 → 독립 검증(방 상세 전환은 스텁 호출까지만 확인 가능)
4. US3 추가 → 독립 검증(폼 전환도 스텁까지만)
5. US4 추가 → 독립 검증 → 전체 완료

---

## 참고 사항

- 피해야 할 것: 모호한 작업, 동일 파일 충돌, 독립성을 깨뜨리는 스토리 간 의존성
- 커밋 단위는 [`docs/conventions/commit-message.md`](../../docs/conventions/commit-message.md)의 쪼개기 원칙을 따른다
- `RoomDetailLauncher`·`RoomFormLauncher`의 실제 화면 전환은 `:feature:roomdetail`(이슈 #161)·`:feature:roomform`이 구현되기 전까지 스텁 호출 확인까지만 가능하다(T028) — 그 모듈들이 생기면 T028의 스텁 바인딩을 제거하고 실제 구현으로 교체하는 후속 작업이 필요하다(이 tasks.md 범위 밖, 해당 feature의 tasks.md가 다룬다)
