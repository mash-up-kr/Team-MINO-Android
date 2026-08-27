# 작업 목록: 방 상세 (Room Detail)

**대상 스펙 경로**: `docs/specs/room-detail`

**기준 plan 버전**: 1.0.0

**최초 작성일**: 2026-08-27

**최종 수정일**: 2026-08-27

**사전 조건**: plan.md, spec.md, research.md, data-model.md, contracts/(`room-detail-main-contract.md`·`place-repository.md`·`entry-dependencies.md`), quickstart.md

**테스트**: 이 저장소에 확립된 자동 테스트 컨벤션이 없다(헌법 「검증 장치의 한계」, [plan.md 기술 컨텍스트](./plan.md)). 이 tasks.md는 테스트 작업을 포함하지 않는다.

**구성 방식**: spec.md의 유저 플로우 4개를 사용자 스토리로 매핑한다 — US1 진입·지도·바텀시트, US2 정렬·필터·뷰 전환, US3 장소 액션(공유/삭제), US4 멤버·방 관리.

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. 한 번 부여한 ID는 바꾸지 않는다.
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: 이 작업이 속한 사용자 스토리(US1~US4)

---

## ⚠️ 착수 전 확인 — 순서 의존 (가용성, 헌법 위반 아님)

이 tasks.md의 상당수 작업이 `feature/154-room-list/task-docs`(PR #228, 아직 `feature/154-room-list/base`에 미병합)가 만든 타입·컴포넌트를 재사용 대상으로 참조한다:

- `feature/room/main/model/BottomSheetLevel.kt`
- `core/domain/model/MapMarkerSortOption.kt`·`PlaceCategoryFilter.kt`·`RoomMemberSummary.kt`·`RoomThumbnail.kt`
- `feature/room/main/screen/RoomListRoute.kt`·`feature/room/main/vm/RoomListViewModel.kt`(T012~T017이 직접 수정 대상으로 삼음)

이 브랜치(`feature/154-room-list/room-detail-task`)가 분기한 시점의 `feature/154-room-list/base`에는 위 파일들이 아직 없다(design-system `MinoMenu`·`MinoChip`, domain `Room`·`RoomRepository`·`RoomFormLauncher`는 이미 있음). **T004 이후 작업을 시작하기 전에 PR #228이 base에 병합되어 있어야 한다** — 그렇지 않으면 컴파일 자체가 안 된다. 이는 레이어 경계 위반이 아니라 다른 PR의 진행 상태에 대한 의존이다([plan.md 헌법 준수 확인 게이트 V](./plan.md)와 같은 성격).

---

## Phase 1: 셋업 — 신규 domain·navigation 타입

**목적**: 어떤 사용자 스토리보다도 먼저 필요한, 이 spec이 최초로 정의하는 타입.

- [ ] T001 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/Place.kt`에 `Place` 데이터 클래스 작성([data-model.md §1](./data-model.md))
- [ ] T002 [P] `core/domain/src/main/kotlin/team/mino/core/domain/repository/PlaceRepository.kt`에 `observePlaces`·`sharePlaces`·`deletePlace` 인터페이스 작성([contracts/place-repository.md](./contracts/place-repository.md))
- [ ] T003 [P] `core/navigation/src/main/java/team/mino/core/navigation/screen/ImmersiveRoute.kt`에 빈 마커 인터페이스 작성([contracts/entry-dependencies.md](./contracts/entry-dependencies.md))

---

## Phase 2: 기반 작업 (모든 사용자 스토리가 공통으로 씀)

**목적**: 화면 골격·네비게이션 배선·데이터 레이어. 어떤 사용자 스토리 작업도 이 단계 없이는 시작할 수 없다.

### 화면 골격 (`:feature:room/detail/`)

- [ ] T004 `feature/room/src/main/java/team/mino/feature/room/RoomNavigation.kt`(기존 파일 병합)에 `internal data class RoomDetailMain(val roomId: String) : Route, ImmersiveRoute` 추가([contracts/room-detail-main-contract.md](./contracts/room-detail-main-contract.md), T003 의존)
- [ ] T005 [P] `feature/room/src/main/java/team/mino/feature/room/detail/model/PlaceViewType.kt`에 `enum class PlaceViewType { LIST, CARD }` 작성([data-model.md §2](./data-model.md))
- [ ] T006 [P] `feature/room/src/main/java/team/mino/feature/room/detail/vm/RoomDetailUiState.kt`에 `RoomDetailUiState`·`LeaveDialogState` 작성([contracts/room-detail-main-contract.md](./contracts/room-detail-main-contract.md), T001·T005 의존)
- [ ] T007 [P] `feature/room/src/main/java/team/mino/feature/room/detail/vm/RoomDetailIntent.kt`에 `RoomDetailIntent` sealed interface 작성(전체 하위 타입, [contracts/room-detail-main-contract.md](./contracts/room-detail-main-contract.md))
- [ ] T008 [P] `feature/room/src/main/java/team/mino/feature/room/detail/vm/RoomDetailSideEffect.kt`에 `RoomDetailSideEffect` sealed interface 작성([contracts/room-detail-main-contract.md](./contracts/room-detail-main-contract.md))
- [ ] T009 `feature/room/src/main/java/team/mino/feature/room/detail/vm/RoomDetailViewModel.kt` 스켈레톤 작성 — `RoomRepository`·`PlaceRepository`·`RoomFormLauncher` 주입, 초기 상태 노출만(T002, T006~T008 의존)
- [ ] T010 `feature/room/src/main/java/team/mino/feature/room/detail/screen/RoomDetailRoute.kt`·`RoomDetailScreen.kt` 스켈레톤 작성(T009 의존)
- [ ] T011 `RoomNavigation.kt`의 `roomGraph()`에 `screen<RoomDetailMain> { entry -> RoomDetailRoute(...) }` 등록(T004, T010 의존)

### room-list ↔ room-detail 연결 배선 (D2 — Activity Launcher 스텁 제거)

> room-list plan 2.0.0([room-list/research.md D13·D14](../room-list/research.md))이 D5(`RoomDetailLauncher` 기반 Activity 전환)를 폐기했다. `feature/154-room-list/task-docs`(PR #228)의 T028 메모가 이 정리를 room-detail의 tasks.md 몫으로 위임해 뒀다([PR #248 리뷰 스레드](https://github.com/mash-up-kr/Team-MINO-Android/pull/248) 참고). 아래 T012~T017이 그 정리 작업이다.

- [ ] T012 `feature/room/src/main/java/team/mino/feature/room/main/screen/RoomListRoute.kt`의 `NavigateToRoomDetail` 처리를 `navController.navigate(RoomDetailMain(effect.roomId))` 직접 호출로 교체 — 기존 `viewModel.roomDetailLauncher.launch(activity) { putExtra(EXTRA_ROOM_DETAIL_ROOM_ID, effect.roomId) }` 제거(T011 의존)
- [ ] T013 `feature/room/src/main/java/team/mino/feature/room/main/vm/RoomListViewModel.kt`에서 `roomDetailLauncher: RoomDetailLauncher` 생성자 주입 제거(T012 의존)
- [ ] T014 `core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/RoomDetailLauncher.kt` 삭제(T013 의존)
- [ ] T015 `core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/ExtraTag.kt`에서 `EXTRA_ROOM_DETAIL_ROOM_ID` 상수 제거(T012 의존)
- [ ] T016 `feature/room/src/main/java/team/mino/feature/room/di/RoomLauncherStubModule.kt`에서 `RoomDetailLauncherStub`·`bindRoomDetailLauncher` 제거(`RoomFormLauncher` 스텁 바인딩은 유지 — `:feature:roomform` 미구현 상태 그대로)(T014 의존)
- [ ] T017 `feature/room/src/test/java/team/mino/feature/room/fake/FakeLaunchers.kt`·`RoomListViewModelTest.kt`에서 `RoomDetailLauncher` 관련 fake·주입 제거(T013 의존)
- [ ] T018 `feature/main/src/main/java/team/mino/feature/main/MainShell.kt`에 `ImmersiveRoute` 판정 기반 `bottomBar` 슬롯 조건부 렌더링 추가([contracts/entry-dependencies.md](./contracts/entry-dependencies.md), T004 의존)

### 데이터 레이어 (`:core:data`) — 백엔드 draft 갭은 [research.md D12](./research.md)와 동일 패턴

- [ ] T019 [P] `core/data/src/main/java/team/mino/core/data/network/dto/response/PlaceResponse.kt` 작성 — draft 응답 필드 기준, spec이 요구하는 필드 중 draft에 없는 것은 T021에서 목데이터로 채움
- [ ] T020 [P] `core/data/src/main/java/team/mino/core/data/network/service/PlaceApiService.kt` 작성 — Ktor `HttpClient` 호출(T019 의존)
- [ ] T021 `core/data/src/main/java/team/mino/core/data/datasource/PlaceRemoteDataSource.kt`(+`Impl`) 작성 — `PlaceApiService` 호출 위임(T020 의존)
- [ ] T022 [P] `core/data/src/main/java/team/mino/core/data/datasource/di/PlaceDataSourceModule.kt` — `@Binds @Singleton`(T021 의존)
- [ ] T023 `core/data/src/main/java/team/mino/core/data/repository/mapper/PlaceMapper.kt` 작성 — `PlaceResponse.toDomain(): Place`, draft에 없는 필드는 임시 목데이터/플레이스홀더(T001, T019 의존)
- [ ] T024 `core/data/src/main/java/team/mino/core/data/repository/PlaceRepositoryImpl.kt` 작성 — `observePlaces`·`sharePlaces`·`deletePlace` 구현. `sharePlaces`·`deletePlace`의 서버 요청 스키마는 [TBD]([contracts/place-repository.md](./contracts/place-repository.md))라 임시 목처리(T002, T021, T023 의존)
- [ ] T025 [P] `core/data/src/main/java/team/mino/core/data/repository/di/PlaceRepositoryModule.kt` — `@Binds @Singleton`(T024 의존)

**체크포인트**: `:app:assembleQaDebug` 통과, 방 리스트 카드 클릭 시 방 상세(빈 화면이어도)로 nested Route 전환됨, 바텀 네비게이션 숨겨짐.

---

## Phase 3: 사용자 스토리 1 — 진입 & 지도·바텀시트 탐색

**목표**: 방 카드 선택 시 그 방 장소만 표시된 지도와 `Half` 시트로 진입하고, `Peek`/`Half`/`Full` 3단 드래그와 `[X]` 복귀가 동작한다.

**독립 테스트**: [quickstart.md 시나리오 1~2](./quickstart.md) — 정렬·필터·장소 액션·멤버 관리 없이도 진입·시트 전환·복귀만으로 검증 가능.

### 사용자 스토리 1 구현

- [ ] T026 [P] [US1] `feature/room/src/main/java/team/mino/feature/room/detail/component/RoomDetailMap.kt` 작성 — `:core:map` `MinoMap` 래핑, 해당 방 장소만 마커 오버레이(T001 의존)
- [ ] T027 [US1] `RoomDetailViewModel`에 `OnScreenEntered` 처리 추가 — `RoomRepository.getRoom(roomId)` 단건 조회 + `PlaceRepository.observePlaces(roomId)` 구독(T009, T002 의존)
- [ ] T028 [US1] `RoomDetailViewModel`에 `OnSheetDraggedUp`/`OnSheetDraggedDown` 처리 추가 — [분기 규칙](./contracts/room-detail-main-contract.md)대로 `PEEK`/`HALF`/`FULL` 전이(T009 의존)
- [ ] T029 [US1] `feature/room/src/main/java/team/mino/feature/room/detail/component/RoomDetailBottomSheet.kt` 작성 — `Peek`/`Half`/`Full` 렌더 분기, 헤더(방 제목·설명·장소 수 인디케이터 `{N}개`/`999+개`·멤버 아바타, FR-001)(T026 의존)
- [ ] T030 [US1] `RoomDetailScreen`에 더보기[⋮] 위치 분기 조립 — `Peek`은 상단, 그 외는 하단(FR-003, TS-010·TS-011)(T029 의존)
- [ ] T031 [US1] `RoomDetailViewModel`에 `OnCloseClick` 처리 추가 — `NavigateBack` SideEffect 발행(T009 의존)
- [ ] T032 [US1] `RoomDetailRoute`에 `NavigateBack` 처리 연결 — `navController.popBackStackIfResumed(entry)` 호출(FR-004, T031, T011 의존)
- [ ] T033 [US1] 장소 0개 상태 빈 화면 처리 — 마커 없음, 시트에 빈 상태 표시(EC-001)(T029 의존)

**체크포인트**: 이 시점에서 사용자 스토리 1은 완전히 동작하고 독립적으로 테스트 가능해야 합니다.

---

## Phase 4: 사용자 스토리 2 — 정렬·필터·뷰 전환

**목표**: 정렬 드롭다운·카테고리 칩으로 장소 목록을 재정렬·필터링하고, 리스트형/카드형 뷰를 전환한다.

**독립 테스트**: [quickstart.md 시나리오 3](./quickstart.md) — US1이 만든 목록 위에서 정렬·필터·뷰 전환만 검증.

### 사용자 스토리 2 구현

- [ ] T034 [P] [US2] `RoomDetailViewModel`에 `OnSortSelected` 처리 추가 — `MapMarkerSortOption` 기준 `places` 재정렬(FR-005, T027 의존)
- [ ] T035 [P] [US2] `RoomDetailViewModel`에 `OnCategoryFilterSelected` 처리 추가 — `PlaceCategoryFilter` 기준 필터링, 해당 카테고리 없으면 빈 목록(FR-006, EC-003, T027 의존)
- [ ] T036 [US2] `RoomDetailBottomSheet`에 정렬 드롭다운 조립 — `MinoMenu` 재사용, 펼침 표시 순서 `꾹 Pick`/`전체`/`최신순`/`거리순`/`코멘트순`([research.md D4·D13](./research.md), T029, T034 의존)
- [ ] T037 [US2] `RoomDetailBottomSheet`에 카테고리 칩 조립 — `MinoChip` 재사용, `전체`/`카페`/`음식점` 3종 고정(T029, T035 의존)
- [ ] T038 [P] [US2] `RoomDetailViewModel`에 `OnViewTypeSelected` 처리 추가 — `PlaceViewType` 토글(FR-007, T009 의존)
- [ ] T039 [P] [US2] `feature/room/src/main/java/team/mino/feature/room/detail/component/PlaceCardList.kt` 작성 — 리스트형 장소 카드(T001 의존)
- [ ] T040 [P] [US2] `feature/room/src/main/java/team/mino/feature/room/detail/component/PlaceCardGrid.kt` 작성 — 카드형 장소 카드(T001 의존)
- [ ] T041 [US2] `RoomDetailScreen`에 뷰 토글(좌측=리스트형/우측=카드형) + `PlaceCardList`/`PlaceCardGrid` 조립(T038, T039, T040 의존)

**체크포인트**: 이 시점에서 사용자 스토리 1과 2 모두 독립적으로 동작해야 합니다.

---

## Phase 5: 사용자 스토리 3 — 장소 액션 (다른 방에 공유 / 삭제)

**목표**: 장소 카드 더보기에서 다른 방에 공유하거나 삭제한다.

**독립 테스트**: [quickstart.md 시나리오 4](./quickstart.md) — 방 선택 시트·삭제 확인 모달의 UI 골격까지 검증(실제 API는 [TBD]).

### 사용자 스토리 3 구현

- [ ] T042 [P] [US3] `feature/room/src/main/java/team/mino/feature/room/detail/component/PlaceActionMenu.kt` 작성 — 장소 카드 더보기, "다른 방에 공유"·"장소 삭제" 2항목만 고정(`MinoMenu`, EC-007)
- [ ] T043 [US3] `RoomDetailViewModel`에 `OnPlaceMoreClick` 처리 추가(T009 의존)
- [ ] T044 [P] [US3] `feature/room/src/main/java/team/mino/feature/room/detail/component/RoomSelectSheet.kt` 작성 — `Full` 676dp 고정, 슬라이드 영역 416dp, 방 다중 선택. `RoomRepository.observeMyRooms()` 재사용해 이미 저장된 방은 체크+비활성(EC-004)
- [ ] T045 [US3] `RoomDetailViewModel`에 `OnShareToOtherRoomClick`/`OnRoomSelectConfirm`/`OnRoomSelectDismiss` 처리 추가 — `PlaceRepository.sharePlaces` 호출(서버 스키마 [TBD], [contracts/place-repository.md](./contracts/place-repository.md)), `ShowShareCompleteToast` 발행(FR-009, UX-002, T024 의존)
- [ ] T046 [P] [US3] `feature/room/src/main/java/team/mino/feature/room/detail/component/PlaceDeleteConfirmDialog.kt` 작성 — UX-001 문구(`이 장소를 삭제할까요?`/`장소에 등록된 사진과 댓글이 모두 삭제되며, 다시 되돌릴 수 없어요.`) 그대로 고정
- [ ] T047 [US3] `RoomDetailViewModel`에 `OnPlaceDeleteClick`/`OnPlaceDeleteConfirm`/`OnPlaceDeleteCancel` 처리 추가 — `PlaceRepository.deletePlace` 호출, `places` 즉시 갱신(FR-010, SC-003, T024 의존)
- [ ] T048 [US3] `RoomDetailScreen`에 `PlaceActionMenu`·`RoomSelectSheet`·`PlaceDeleteConfirmDialog` 조립(T042, T044, T046 의존)

**체크포인트**: 이 시점에서 사용자 스토리 1~3이 함께 동작해야 합니다.

---

## Phase 6: 사용자 스토리 4 — 멤버 & 방 관리

**목표**: 친구 초대, 방 편집(방장 전용), 나가기/위임을 호출한다.

**독립 테스트**: [quickstart.md 시나리오 5](./quickstart.md) — 시트/모달 UI 골격과 화면 전환까지 검증(초대·나가기 실제 API는 [TBD], 방 편집은 `:feature:roomform` 구현 선행 필요).

### 사용자 스토리 4 구현

- [ ] T049 [P] [US4] `feature/room/src/main/java/team/mino/feature/room/detail/component/RoomMoreMenu.kt` 작성 — 화면 더보기[⋮], `isOwner`·`isPersonalRoom` 분기로 방 편집(방장 전용)/나가기 노출 결정(`MinoMenu`, [분기 규칙](./contracts/room-detail-main-contract.md))
- [ ] T050 [US4] `RoomDetailViewModel`에 `OnMoreMenuClick`/`OnMoreMenuDismiss` 처리 추가(T009 의존)
- [ ] T051 [P] [US4] `feature/room/src/main/java/team/mino/feature/room/detail/component/RoomInviteSheet.kt` 작성 — 424dp 고정, 참여자 목록 스크롤 288dp. 참여자 목록 타입·초대 링크 발급은 [TBD]([data-model.md §4](./data-model.md))라 UI 골격만
- [ ] T052 [US4] `RoomDetailViewModel`에 `OnInviteClick`/`OnInviteSheetDismiss` 처리 추가(T009 의존)
- [ ] T053 [US4] `RoomDetailViewModel`에 `OnEditRoomClick` 처리 추가 — `NavigateToRoomForm` SideEffect 발행(편집 모드 extra 키는 [TBD], [research.md D9](./research.md))(T009 의존)
- [ ] T054 [US4] `RoomDetailRoute`에 `NavigateToRoomForm` 처리 연결 — `roomFormLauncher.launch(activity, resultLauncher = editRoomResultLauncher)` 호출, 완료 결과 수신 시 `ShowEditCompleteSnackbar`(FR-012, T053 의존)
- [ ] T055 [P] [US4] `feature/room/src/main/java/team/mino/feature/room/detail/component/RoomLeaveConfirmDialog.kt` 작성 — [SYS-007] Flow A(일반 멤버 확인 모달)
- [ ] T056 [P] [US4] `feature/room/src/main/java/team/mino/feature/room/detail/component/RoomOwnerLeaveDialog.kt` 작성 — [SYS-007] Flow B(방장 확인+위임 모달)
- [ ] T057 [US4] `RoomDetailViewModel`에 `OnLeaveClick`/`OnLeaveConfirm`/`OnLeaveCancel`·`OnOwnerDelegateSelected`/`OnOwnerDelegateConfirm` 처리 추가 — [나가기 플로우 분기 규칙](./contracts/room-detail-main-contract.md)대로 `leaveDialogState` 전이(실제 나가기·위임 API는 [TBD], [research.md D12](./research.md)), 완료 시 `NavigateToRoomList` 발행(T009 의존)
- [ ] T058 [US4] `RoomDetailRoute`에 `NavigateToRoomList` 처리 연결 — `navController.popBackStackIfResumed(entry)`(T057, T011 의존)
- [ ] T059 [US4] `RoomDetailScreen`에 `RoomMoreMenu`·`RoomInviteSheet`·`RoomLeaveConfirmDialog`·`RoomOwnerLeaveDialog` 조립(T049, T051, T055, T056 의존)

**체크포인트**: 이제 모든 사용자 스토리가 독립적으로 동작해야 합니다.

---

## Phase 7: 마무리 및 공통 관심사

**목적**: 여러 사용자 스토리에 걸친 검증·정리.

- [ ] T060 [SCR-005] 방 상세 화면을 [Figma 004 annotation](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2400-270425&m=dev) 기준 실기기/에뮬레이터로 대조(`docs/conventions/figma-design-fidelity.md`)
- [ ] T061 [quickstart.md](./quickstart.md) 검증 시나리오 1~5 수동 실행 및 결과 기록
- [ ] T062 `:app:assembleQaDebug`·`ktlintCheck` 통과 확인

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **셋업(Phase 1)**: `feature/154-room-list/base`에 PR #228이 병합되어 있어야 시작 가능(위 "착수 전 확인" 참고). 그 외 의존성 없음.
- **기반 작업(Phase 2)**: 셋업 완료에 의존. T012~T017(런처 정리)은 T011(nested Route 등록) 완료 후 진행 — 정리와 신설이 동시에 컴파일 불가 상태를 만들지 않기 위함.
- **사용자 스토리(Phase 3 이후)**: 각 작업이 실제로 쓰는 기반 산출물에만 의존한다. Phase 2 전체 완료를 기다리지 않아도 되는 작업은 아래 "병렬 처리 기회" 참고.
- **마무리(Phase 7)**: US1~US4 완료에 의존.

### 사용자 스토리 간 의존성

- **US1**: T001~T011, T026~T033이 쓰는 기반만 있으면 시작 가능 — 다른 스토리 의존 없음.
- **US2**: US1이 만든 `RoomDetailBottomSheet`·`RoomDetailViewModel` 위에 얹는다(T029, T009 의존) — US1과 동시 진행 시 같은 파일(`RoomDetailBottomSheet.kt`) 충돌 가능성 있어 US1의 T029 완료 후 T036·T037을 얹는 편이 충돌이 적다.
- **US3**: T009·T024(데이터 레이어)만 있으면 독립적으로 진행 가능.
- **US4**: T009 및 `RoomFormLauncher`(이미 room-list가 스텁으로 바인딩)만 있으면 독립적으로 진행 가능. 단 T054의 실제 값 채움 검증은 `:feature:roomform` 구현 선행 필요(quickstart.md 기대 결과).

### 병렬 처리 기회

- [P]로 표시된 Phase 1 작업(T001~T003)은 모두 병렬 실행 가능.
- [P]로 표시된 Phase 2 작업(T005~T008, T019, T020, T022, T025)은 각자의 의존 작업만 끝나면 병렬 실행 가능.
- US2·US3·US4는 각자 쓰는 기반 산출물(T009, T024)이 준비되면 서로 다른 담당자가 병렬로 진행 가능 — 단 US2는 위 이유로 US1의 T029 완료를 기다리는 편이 안전하다.

---

## 병렬 실행 예시: 사용자 스토리 3

```bash
# US3의 서로 다른 파일 컴포넌트를 함께 작성:
Task: "PlaceActionMenu.kt 작성 — 장소 카드 더보기 메뉴"
Task: "RoomSelectSheet.kt 작성 — 다른 방에 공유 시트"
Task: "PlaceDeleteConfirmDialog.kt 작성 — 삭제 확인 모달"
```

---

## 구현 전략

### MVP 우선 (사용자 스토리 1만)

1. Phase 1~2: 셋업 + 기반 작업 완료(PR #228 base 병합 선행)
2. Phase 3: US1 완료
3. **중단하고 검증**: [quickstart.md 시나리오 1~2](./quickstart.md)로 독립 테스트
4. 준비되면 배포/데모

### 점진적 전달

1. 셋업 + 기반 작업 완료
2. US1 추가 → 독립 검증 → 데모(MVP)
3. US2 추가 → 독립 검증(US1과 함께)
4. US3 추가 → 독립 검증(방 선택 시트·삭제 모달 UI 골격까지)
5. US4 추가 → 독립 검증(초대·나가기는 UI 골격까지, 방 편집은 `:feature:roomform` 대기)

---

## 참고 사항

- 피해야 할 것: 모호한 작업, 동일 파일 충돌, 독립성을 깨뜨리는 스토리 간 의존성
- 커밋 단위는 [`docs/conventions/commit-message.md`](../../docs/conventions/commit-message.md)의 쪼개기 원칙을 따른다
- `RoomFormLauncher` 편집 모드의 extra 키·result 스키마([research.md D9](./research.md))·[SYS-003]·[SYS-006]·[SYS-007]의 실제 데이터 계약(research.md D10~D12)은 각각 `:feature:roomform`·해당 시스템 spec이 아직 없어 T045·T053·T057에서 임시 목데이터로 남는다 — 그 spec들이 생기면 이 tasks.md가 아니라 그쪽 작업 목록이 실제 계약 반영을 담당한다.
- `ImmersiveRoute`(T003·T018)는 room-detail만을 위한 타입이 아니라 이후 몰입 화면을 만드는 모든 feature가 따라야 하는 공용 계약이다 — plan.md가 이미 ADR 승격을 제안해 뒀다([plan.md 헌법 준수 확인 게이트 III](./plan.md)).
