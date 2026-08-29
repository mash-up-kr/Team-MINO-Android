# 작업 목록: 장소 상세 & 코멘트 (Place Detail & Comments)

**대상 스펙 경로**: `docs/specs/place-detail`

**기준 plan 버전**: 1.1.0

**최초 작성일**: 2026-08-29

**최종 수정일**: 2026-08-29

**사전 조건**: [plan.md](./plan.md) (필수), [spec.md](./spec.md) (사용자 스토리), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/)

**테스트**: 테스트 작업을 포함하지 않는다. [spec.md](./spec.md)가 자동 테스트를 요청하지 않았고, 이번 라운드가 UI 구현이라 검증은 [quickstart.md](./quickstart.md)의 육안 시나리오와 Compose Preview로 한다.

**구성 방식**: 각 스토리를 독립적으로 구현하고 확인할 수 있도록 작업을 사용자 스토리별로 묶는다.

## 이번 라운드의 범위 — UI 전용

사용자 지시로 **API 연결 없이 UI만 구현한다.** 그 결정이 작업 목록에 만든 갈래는 셋이다.

1. **도메인 모델과 Repository 인터페이스는 만든다**(`:core:domain`). 구현은 `:core:data`가 아니라 `:feature:placedetail` 안의 **Fake**가 채운다. [contracts/place-repository.md](./contracts/place-repository.md)가 확정한 시그니처를 그대로 쓰므로, 나중에 진짜 구현을 붙일 때 ViewModel과 화면을 고치지 않아도 된다.
2. **`:core:data`를 건드리지 않는다.** Service·DTO·DataSource·RepositoryImpl·Mapper는 Phase 10에 모아 두고 이번 라운드에 착수하지 않는다.
3. **`RoomSummary.hasPlace`와 `RoomRepository.getRooms(placeId)` 확장을 미룬다**([research.md D9](./research.md)). 이 확장은 `:core:data`의 기존 구현까지 함께 고쳐야 하므로 Phase 10으로 넘긴다. **다만 방 목록 조회 자체는 미루지 않는다** — 인자 없는 `getRooms()`는 이미 구현돼 동작하고 `RoomSummary.color`·`placeCount`·`thumbnailImageUrls`를 담고 있어, 마커 색상(FR-002)과 공유 시트 목록(FR-018)을 `GetRoomPickerRoomsUseCase` 재사용만으로 채운다([research.md D15](./research.md)). 미뤄지는 것은 **이미 저장된 방 표시**(`hasPlace`) 하나뿐이다 → [미결 사항](#미결-사항) 3번.

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. **한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.**
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: 이 작업이 속한 사용자 스토리 (US1~US6 — [spec.md §1](./spec.md)의 유저 플로우 1~6에 대응)

## 경로 규칙

모바일(Android, 다중 Gradle 모듈). 모든 경로는 저장소 루트 기준이며 [plan.md 「프로젝트 구조」](./plan.md)가 소유한다.

---

## Phase 1: 셋업 (모듈 골격)

**목적**: 신규 진입형 모듈을 빌드에 올리고, 다른 feature가 이 화면을 열 계약을 세운다.

- [X] T001 `settings.gradle.kts`에 `include(":feature:placedetail")` 추가
- [X] T002 `feature/placedetail/build.gradle.kts` 작성 — `mino.android.feature` 계열 컨벤션 플러그인과 `:core:map`·`:core:design-system`·`:core:common:ui`·`:core:common:android`·`:core:navigation`·`:core:domain`·`:core:error-handling` 의존 선언 ([plan.md 「주요 의존성」](./plan.md))
- [X] T003 `feature/placedetail/src/main/AndroidManifest.xml`에 `PlaceDetailActivity` 등록 (exported=false)
- [X] T004 [P] `app/build.gradle.kts`에 `implementation(projects.feature.placedetail)` 추가
- [X] T005 [P] `core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/PlaceDetailLauncher.kt` 신설과 `ExtraTag.kt`에 `EXTRA_PLACE_DETAIL_PIN_ID` 추가 ([contracts/place-detail-launcher.md §1](./contracts/place-detail-launcher.md))

**체크포인트**: `./gradlew :feature:placedetail:assembleQaDebug`가 빈 모듈로 통과한다.

---

## Phase 2: 기반 작업 (모든 스토리 공통)

**목적**: 도메인 타입·Fake 데이터 원천·진입점 골격·MVI 계약을 세운다. 각 작업이 어느 스토리에 쓰이는지 줄에 드러낸다.

### 도메인 모델과 Repository 계약

- [X] T006 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/PlaceLabel.kt` 생성 — 4종 enum, 기본값 `WORTH_VISITING` ([data-model.md §3](./data-model.md)). US1의 헤더 라벨이 쓴다
- [X] T007 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/PlaceDetail.kt` 생성 — `PlaceRegistrant` 중첩 포함 ([data-model.md §1](./data-model.md)). US1·US3·US5·US6 전부가 쓴다
- [X] T008 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/PlaceComment.kt` 생성 — `PlaceCommentAuthor`·`PlaceCommentPage` 포함 ([data-model.md §2](./data-model.md)). US4가 쓴다
- [X] T009 `core/domain/src/main/kotlin/team/mino/core/domain/repository/PlaceRepository.kt` 생성 — `getPlaceDetail`·`recordAccess`·`duplicatePin` 3종. **`recordAccess`가 예외를 던지지 않는다는 규약을 KDoc에 적는다** ([contracts/place-repository.md §1](./contracts/place-repository.md)). T007에 의존. US1·US6가 쓴다
- [X] T010 `core/domain/src/main/kotlin/team/mino/core/domain/repository/PlaceCommentRepository.kt` 생성 — `getComments`·`addComment`·`deleteComment` ([contracts/place-repository.md §2](./contracts/place-repository.md)). T008에 의존. US4가 쓴다

### Fake 데이터 원천 (이번 라운드 한정)

- [X] T011 `feature/placedetail/src/main/java/team/mino/feature/placedetail/fake/FakePlaceDetailData.kt` 생성 — 화면 검증에 필요한 샘플 집합을 한 파일에 모은다: 이미지 3장/1장/0장 장소, 코멘트 0건/3건/200자, `canDelete` 혼재, 원문 링크 있음/없음. Preview와 Fake Repository가 같은 원천을 쓴다. **방 목록은 담지 않는다** — 실제 `getRooms()`를 쓴다([research.md D15](./research.md))
- [X] T012 [P] `feature/placedetail/src/main/java/team/mino/feature/placedetail/fake/FakePlaceRepository.kt` 생성 — `PlaceRepository` 구현. 지연을 흉내 내 로딩 구간을 볼 수 있게 한다. T009·T011에 의존
- [X] T013 [P] `feature/placedetail/src/main/java/team/mino/feature/placedetail/fake/FakePlaceCommentRepository.kt` 생성 — `PlaceCommentRepository` 구현. 페이지 경계를 만들어 역방향 페이징(US4)을 확인할 수 있게 한다. T010·T011에 의존
- [X] T014 `feature/placedetail/src/main/java/team/mino/feature/placedetail/di/PlaceDetailFakeDataModule.kt` 생성 — 두 Fake를 `@Binds`로 바인딩. **Phase 10에서 통째로 삭제되는 모듈이라는 사실을 파일 주석에 적는다**. T012·T013에 의존

### 진입점 골격 (진입형 feature)

- [X] T015 [P] `feature/placedetail/src/main/java/team/mino/feature/placedetail/PlaceDetailDestinations.kt` 생성 — `@Serializable internal data class PlaceDetailMain(val pinId: String) : Route` ([contracts/place-detail-main-contract.md §1](./contracts/place-detail-main-contract.md))
- [X] T016 `feature/placedetail/src/main/java/team/mino/feature/placedetail/PlaceDetailActivity.kt` 생성 — `@AndroidEntryPoint`, `intent.getStringExtra(EXTRA_PLACE_DETAIL_PIN_ID)`로 시작 Route 구성, `PlaceDetailShell` 호스팅. T005·T015에 의존
- [X] T017 `feature/placedetail/src/main/java/team/mino/feature/placedetail/PlaceDetailShell.kt` 생성 — `MinoScaffold` + `navController` + `TrackScreenViews` ([feature-module.md 4장](../../architecture/feature-module.md)). T016에 의존
- [X] T018 `feature/placedetail/src/main/java/team/mino/feature/placedetail/PlaceDetailNavHost.kt` 생성 — `MinoNavHost` + `screen<PlaceDetailMain>`. T015·T017에 의존
- [X] T019 `feature/placedetail/src/main/java/team/mino/feature/placedetail/di/` 에 `PlaceDetailLauncherImpl.kt`·`PlaceDetailNavigationModule.kt` 생성 ([feature-navigation.md 1장](../../architecture/feature-navigation.md)). T005·T016에 의존

### MVI 계약과 화면 골격

- [X] T020 [P] `feature/placedetail/src/main/java/team/mino/feature/placedetail/main/model/` 에 `PlaceSheetLevel.kt`·`PlaceHeaderMode.kt` 생성 ([data-model.md §5](./data-model.md)). US1·US2가 쓴다
- [X] T021 [P] `feature/placedetail/src/main/java/team/mino/feature/placedetail/main/model/PlaceCommentUiModel.kt`·`RoomPickerItem.kt` 생성 ([contracts/place-detail-main-contract.md §2.1·2.2](./contracts/place-detail-main-contract.md)). US4·US6가 쓴다
- [X] T022 `feature/placedetail/src/main/java/team/mino/feature/placedetail/main/vm/` 에 `PlaceDetailUiState.kt`·`PlaceDetailIntent.kt`·`PlaceDetailSideEffect.kt` 생성 — 계약을 그대로 옮긴다 ([contracts/place-detail-main-contract.md §2~4](./contracts/place-detail-main-contract.md)). T020·T021에 의존
- [X] T023 `feature/placedetail/src/main/java/team/mino/feature/placedetail/main/vm/PlaceDetailViewModel.kt` 생성 — `MviContainer` 위임, `savedStateHandle.toRoute<PlaceDetailMain>()`로 `pinId` 복원, 진입 로딩 순서 구현 ([contracts/place-detail-main-contract.md §5](./contracts/place-detail-main-contract.md) — `recordAccess`는 결과를 기다리지 않고, 나머지는 병렬). 방 정보는 **`GetRoomPickerRoomsUseCase`를 주입**받아 `id == place.roomId`인 방에서 `roomColor`를, 목록 전체로 공유 시트를 채운다([research.md D15](./research.md)). 에러는 `launchSafely`로 흘린다 ([error_handling.md](../../conventions/error_handling.md)). 「경과일 초기화 확인」은 진입 경로와 무관하게 열 때마다 기록하며 디바운스하지 않는다 (SC-010·EC-023). T009·T010·T022에 의존
- [X] T024 `feature/placedetail/src/main/java/team/mino/feature/placedetail/main/screen/` 에 `PlaceDetailRoute.kt`(stateful)·`PlaceDetailScreen.kt`(stateless 골격) 생성 — `CollectSideEffect`·`CollectDomainError` 연결 ([contracts/place-detail-main-contract.md §7](./contracts/place-detail-main-contract.md)). T023에 의존

### 디자인 시스템 확장 (구현 중 발견 — plan 1.1.0에 없던 작업)

확장형 헤더(T027)와 지도 버튼 행(T065)이 `:core:design-system`에 없는 자산을 요구해 추가된 작업이다. [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1.2·§3이 이 셋을 모두 `:core:design-system` 소유로 판정한다.

- [X] T066 `core/design-system/.../foundation/icons/` 에 `MyLocation` 아이콘 추가 — Figma `Icon/Normal/My Location`(노드 `4170:130927`). T065를 막고 있다
- [X] T067 `core/design-system/.../component/button/` 에 **원형 아웃라인 아이콘 버튼** 신설 — Figma `Button/Icon/Outlined`(정의 `2400:132990`)는 기존 `MinoIconButton`이 온 `Button/Button`(`16215:37602`)과 **별도 컴포넌트셋**이라 배치 규약 §3이 분리를 요구한다. 40×40 · padding 10 · border 1 `Line/Normal/Neutral` · 모서리 완전 원형 · 아이콘 20. T027을 막고 있다
- [X] T068 `core/design-system/.../component/contentbadge/` 의 `ContentBadgeColor`에 **Light Blue 축 추가** — 같은 컴포넌트셋의 Color variant라 배치 규약 §3이 기존 컴포넌트 확장을 요구한다. 토큰 `AccentForegroundLightBlue`는 이미 존재하므로 컴포넌트가 그 색을 열어 주기만 하면 된다. T027을 막고 있다

**체크포인트**: 각 기반 작업이 끝나는 대로 그것을 쓰는 스토리 작업을 시작할 수 있다. T024까지 끝나면 `adb`로 화면이 뜬다([quickstart.md §2.2](./quickstart.md)).

---

## Phase 3: 사용자 스토리 1 - 장소 상세 진입 및 `Half` 요약 조회

**목표**: 지도 위로 369dp 시트가 올라와 등록자·라벨·장소명·주소·액션 버튼·이미지 상단이 한눈에 보인다.

**독립 테스트**: [quickstart.md §3.1](./quickstart.md) — `adb`로 화면을 띄워 TS-001~TS-005·TS-008~TS-010을 확인한다.

- [X] T025 [P] [US1] `feature/placedetail/src/main/java/team/mino/feature/placedetail/main/component/PlaceDetailMap.kt` 생성 — `MinoMap`(`:core:map`)에 선택 핀 마커 1개, 카메라를 장소 중심으로 (FR-002). 마커 색은 `state.roomColor`이며 **`null`인 동안에는 마커를 그리지 않는다**([research.md D15](./research.md)). T065의 `PlaceMapControls`를 지도 위에 합성한다
- [X] T026 [P] [US1] `.../component/PlaceDetailSheet.kt` 생성 — `Half` 369dp 고정 앵커와 시트 골격 (FR-001·SC-002·UX-001 — 시트가 화면을 다 덮지 않아 지도와 선택 핀이 계속 보인다). `Full` 승격은 US2에서 붙인다
- [X] T027 [US1] `.../component/PlaceDetailHeader.kt` 생성 — 확장형 헤더: 등록자 아바타(`MinoProfileAvatar`, 없으면 기본 아바타 EC-004) + 라벨 + [나가기]가 한 줄, 그 아래 장소명·주소 각 한 줄에 `...` 생략 (FR-003·FR-004). T026에 의존
- [X] T028 [P] [US1] `.../component/PlaceActionRow.kt` 생성 — `장소보기`·`원문보기`·`다른방에 공유` 3종을 한 행에 두고 가로 스크롤, `장소보기`는 강조 스타일 (FR-006·UX-004). 클릭 배선은 US5·US6에서 붙인다
- [X] T029 [US1] `.../component/SavedRoomsButton.kt` 생성 — **항상 비활성**으로 그린다 (FR-023, [contracts/place-detail-main-contract.md §6](./contracts/place-detail-main-contract.md)). 중복 저장 장소에서도 비활성인 것이 spec과 어긋난 상태임을 파일 주석에 남긴다. 배치는 T065가 맡는다. T065에 의존
- [X] T030 [US1] `feature/placedetail/src/main/java/team/mino/feature/placedetail/PlaceDetailActivity.kt`에 `Exit` SideEffect 처리 배선 — `finish()`만 한다 (FR-009, [research.md D2](./research.md)). 시트 드래그다운(EC-003)도 같은 Intent로 흘린다. T024에 의존
- [X] T031 [P] [US1] `.../screen/PlaceDetailScreenPreview.kt` 생성 — 로딩(`place == null`)·기본·긴 장소명/주소·등록자 없음 상태
- [X] T065 [P] [US1] `.../component/CurrentLocationButton.kt`와 `.../component/PlaceMapControls.kt` 생성 — 지도 우측 하단에 [현재 위치]와 그 왼쪽 [저장된 방]을 한 행으로 배치하고 `Full`에서 함께 숨긴다 (FR-023, [spec.md §4](./spec.md) 가정). **[현재 위치]의 동작(카메라 이동·위치 권한)은 [SYS-004] 소관이라 이번 범위에서 구현하지 않고 렌더링과 배치만 한다**([spec.md §3.2](./spec.md), [research.md D16](./research.md))

**체크포인트**: `Half` 요약이 완성되고 [나가기]로 화면이 닫힌다.

---

## Phase 4: 사용자 스토리 2 - `Full` 승격과 스크롤 헤더 전환

**목표**: 시트를 끌어올려 전체 내용을 훑고, 스크롤 위치에 따라 헤더가 축소형으로 바뀌어 장소명이 항상 보인다.

**독립 테스트**: [quickstart.md §3.2](./quickstart.md) — TS-011~TS-015·EC-006·EC-007.

- [X] T032 [US2] `.../component/PlaceDetailSheet.kt`에 `Half`↔`Full` 2단 앵커와 드래그 처리 추가 — `Peek`으로 머무는 중간 상태를 만들지 않는다 (FR-001·TS-015). T026에 의존
- [X] T033 [US2] `.../component/PlaceDetailSheet.kt`에 콘텐츠 단일 스크롤 축 구성 — 헤더 아래 액션 행·캐러셀·코멘트 목록·입력 영역이 하나의 스크롤을 공유한다 ([spec.md §4](./spec.md) 가정, EC-015·SC-001 — 드래그 1회로 코멘트 영역까지 도달). T032에 의존
- [X] T034 [US2] `.../component/PlaceDetailHeader.kt`에 축소형 헤더(장소명 + [나가기])와 상단 고정 추가 (FR-008·UX-002·UX-005). T027에 의존
- [X] T035 [US2] `PlaceDetailViewModel`에 `OnScrollOffsetChange` 처리 추가 — `headerMode`를 **스크롤 위치**로 판정한다. `sheetLevel`에서 파생시키지 않는다 ([research.md D5](./research.md)). 콘텐츠가 화면보다 짧으면 확장형 고정 (EC-007). T023·T033에 의존

**체크포인트**: `Half`↔`Full`과 두 헤더가 모두 동작한다.

---

## Phase 5: 사용자 스토리 3 - 대표 이미지 캐러셀 조회

**목표**: 원문 게시글의 사진을 좌우로 넘겨 본다.

**독립 테스트**: [quickstart.md §3.3](./quickstart.md) — TS-016·TS-017·EC-008~EC-010. Fake 데이터의 3장/1장/0장 장소로 확인한다.

- [X] T036 [P] [US3] `.../component/PlaceImageCarousel.kt` 생성 — 가로 스와이프 순회, 다음 장이 우측 경계에 걸쳐 보이게 배치 (FR-007·UX-003). `MinoAsyncImage` 사용, 로드 실패 시 자리표시자 (EC-010)
- [X] T037 [US3] `.../main/screen/PlaceDetailScreen.kt`에서 이미지 0장일 때 캐러셀 영역 자체를 노출하지 않고 액션 행 아래에 코멘트 영역이 바로 이어지게 처리 (EC-009). T033·T036에 의존

**체크포인트**: 이미지 장수에 관계없이 레이아웃이 무너지지 않는다.

---

## Phase 6: 사용자 스토리 4 - 코멘트 조회·작성·삭제

**목표**: 같은 방 멤버의 코멘트를 읽고, 남기고, 내 것을 지운다.

**독립 테스트**: [quickstart.md §3.4](./quickstart.md) — TS-018~TS-027·EC-011~EC-016. 역방향 페이징은 Fake가 만든 페이지 경계로 확인한다.

- [X] T038 [P] [US4] `.../component/PlaceCommentItem.kt` 생성 — 작성자 아바타·닉네임·본문. 본문은 높이 제한 없이 전문 노출 (FR-010·FR-021). `canDelete`일 때만 우측 [⋮] 노출 (FR-015·UX-008)
- [X] T039 [P] [US4] `.../component/PlaceCommentEmpty.kt` 생성 — 캐릭터 일러스트 + `아직 코멘트가 없어요!` (FR-011)
- [X] T040 [US4] `.../component/PlaceCommentList.kt` 생성 — 오래된 것이 위인 오름차순 나열, 위로 스크롤 시 이전 페이지를 목록 **앞**에 붙이는 역방향 페이징 ([research.md D11](./research.md)). T033·T038·T039에 의존
- [X] T041 [US4] `.../component/PlaceCommentInput.kt` 생성 — `MinoTextArea` + `N/200` 카운터 + [등록]. 201자째를 받지 않아 카운터를 `200/200`으로 고정하고 (EC-011), 공백만이면 [등록] 비활성 (FR-012·FR-013·UX-006·EC-012). 목록 마지막 아래에 놓인다
- [X] T042 [US4] `.../component/PlaceCommentMenu.kt` 생성 — `MinoMenu`로 `댓글 삭제` 한 항목, 확인 절차 없이 즉시 삭제 (FR-015·TS-025). T038에 의존
- [X] T043 [US4] `.../main/vm/PlaceDetailViewModel.kt`에 코멘트 Intent 5종 처리 추가 — `OnCommentDraftChange`·`OnSubmitCommentClick`·`OnDeleteCommentClick`·`OnLoadOlderComments`. 등록은 반환된 코멘트를 목록 맨 아래에 덧붙이고 목록을 다시 조회하지 않는다 (FR-014·UX-007·SC-003), 삭제 후 0건이면 빈 상태로 전환 (EC-014). T023·T040·T041·T042에 의존

**체크포인트**: 코멘트 조회·작성·삭제와 빈 상태 전환이 모두 동작한다(Fake 기준).

---

## Phase 7: 사용자 스토리 5 - 외부 연동 (장소보기 · 원문보기)

**목표**: 길찾기와 원문 확인을 외부 앱으로 넘기고, 돌아왔을 때 보던 상태가 그대로다.

**독립 테스트**: [quickstart.md §3.5](./quickstart.md) — TS-028~TS-031·EC-017·EC-018.

- [X] T044 [P] [US5] `PlaceDetailActivity`에 `OpenExternalMap` SideEffect 처리 추가 — 외부 지도 앱, 없으면 브라우저로 대체해 아무 반응 없이 끝나지 않게 한다 (FR-016·TS-029·SC-004). **어느 앱을 띄우고 질의를 어떻게 구성할지는 [spec.md §3.2](./spec.md)가 비목표로 둔 `[TBD]`** — 착수 시 정하고 근거를 남긴다. T030에 의존
- [X] T045 [P] [US5] `.../PlaceDetailActivity.kt`에 `OpenSourceLink` SideEffect 처리 추가와, `.../main/component/PlaceActionRow.kt`에서 `sourceUrl`이 없을 때 [원문보기] 비활성 처리 (FR-017·EC-017). 외부에서 열리지 않는 링크는 앱이 따로 처리하지 않는다 (EC-018). T028·T030에 의존
- [X] T046 [US5] `.../main/vm/PlaceDetailViewModel.kt`와 `.../main/screen/PlaceDetailScreen.kt`에서 외부 앱 복귀 시 시트 단계·스크롤 위치·캐러셀 페이지·입력 중이던 코멘트가 보존되는지 확인하고 필요한 상태 보존 처리 추가 (UX-009·TS-031·SC-005). T035·T037·T043에 의존

**체크포인트**: 두 외부 이동이 모두 동작하고 복귀 상태가 유지된다.

---

## Phase 8: 사용자 스토리 6 - 다른 방에 공유 호출

**목표**: 보고 있는 장소를 다른 방에도 담되 화면을 잃지 않는다.

**독립 테스트**: [quickstart.md §3.6](./quickstart.md) — TS-032·TS-033·EC-021. 방 목록은 실제 `getRooms()`로 받는다. **TS-034·EC-019(이미 저장된 방 체크·비활성)는 `hasPlace` 미연동으로 이번 라운드에서 검증되지 않는다**([research.md D15](./research.md)).

- [X] T047 [US6] `.../component/RoomShareSheet.kt` 생성 — 상단에 대상 장소 카드(썸네일·장소명·주소), 목록은 `MinoRoomCheckBoxCard` 재사용 (FR-018). **이미 저장된 방의 체크·비활성 표시는 `hasPlace`가 있어야 하므로 T062까지 붙이지 않는다**([research.md D15](./research.md)). **시트 높이·시각 표현은 [SYS-003] 소관이라 `[TBD]`** ([research.md D13](./research.md)) — 착수 시 정한 값과 근거를 남긴다. T021에 의존
- [X] T048 [US6] `.../main/vm/PlaceDetailViewModel.kt`에 공유 Intent 4종 처리 추가 — `OnShareClick`·`OnShareRoomToggle`·`OnShareConfirmClick`·`OnShareSheetDismiss`. 하나도 고르지 않으면 [공유하기] 비활성 (FR-022), 모든 방이 이미 저장된 경우 안내 문구 없이 상태로만 드러낸다 (UX-010·EC-019). 시트를 닫아도 장소 상세는 직전 단계·스크롤을 유지한다 (EC-021). T023·T047에 의존
- [X] T049 [US6] 공유 완료 처리 — `ShowShareCompleted` SideEffect를 `LocalSnackbarHostState`로 `공유가 완료되었습니다.` 표출, 대상 방으로 이동하지 않고 현재 화면 유지 (FR-018·TS-033). T048에 의존

**체크포인트**: 공유 호출과 복귀가 동작한다(Fake 기준, 실제 복제는 Phase 10).

---

## Phase 9: 마무리 및 공통 관심사

**목적**: 여러 스토리에 걸친 정리와 검증.

- [X] T050 [P] `.../screen/PlaceDetailScreenPreview.kt` 보강 — 코멘트 0건/다수, 축소형 헤더, 이미지 0장, 원문 링크 없음, 공유 시트 열림 상태를 모두 덮는다
- [X] T051 [P] `feature/placedetail/src/main/res/`로 문자열·치수 리소스 정리 — 하드코딩된 문구를 옮기고, Figma 실측값과 토큰 사용을 [figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)에 따라 대조한다
- [X] T069 `feature/placedetail/.../component/` 의 lint error 5건 해소 — 프리뷰 함수 3건(`SavedRoomsButton`·`CurrentLocationButton`·`PlaceMapControls`)의 `ComposeModifierMissing`과 `PlaceCommentList.kt:53,65`의 `ComposeModifierReused` — `lint.xml`이 `visibility-threshold=all`이라 private 프리뷰도 `Modifier` 파라미터를 요구한다. T052의 Lint 게이트가 이것을 요구한다
- [X] T052 `./gradlew :app:assembleQaDebug` 통과 확인 (헌법 「품질 게이트」). Lint는 가능한 환경에서 `./gradlew lintDebug -Dorg.gradle.jvmargs="-XX:-TieredCompilation"`로 확인한다
- [X] T070 `feature/main/src/main/java/team/mino/feature/main/MainActivity.kt`의 `onNavigateToPlaceDetail` 자리 표시용 `Toast`를 `PlaceDetailLauncher` 호출로 교체 — [SCR-003] 홈 카드 진입을 실제로 잇는다 ([contracts/place-detail-launcher.md §4](./contracts/place-detail-launcher.md), home spec FR-007). 결과를 받지 않으므로 `resultLauncher`를 넘기지 않는다(같은 계약 §3). plan 2.x에 없던 발견 작업이며, 「미결 사항」 1·10이 이 배선으로 실제로 드러난다. T019에 의존
- [ ] T053 [quickstart.md §3](./quickstart.md)의 검증 시나리오 수행 — §4의 "검증하지 않는 것"은 제외한다

---

## Phase 10: API 연결 — **이번 라운드 범위 밖**

**목적**: Fake를 실제 서버 연동으로 교체한다. 사용자 지시로 **이번 라운드에 착수하지 않는다.** 계약은 이미 확정돼 있으므로([contracts/place-api.md](./contracts/place-api.md)·[contracts/comment-api.md](./contracts/comment-api.md)) `/mino-task`를 다시 돌리지 않고 이 Phase만 이어서 진행하면 된다.

> **착수 전 필수**: [contracts/place-api.md §5](./contracts/place-api.md)의 서버 협의 항목 4건과 [contracts/comment-api.md §5](./contracts/comment-api.md)의 4건이 어떻게 닫혔는지 먼저 확인한다. 계약 근거는 2026-08-28T22:54:07+09:00 시점의 문서다.

- [ ] T054 [P] `core/data/.../network/dto/response/` 에 `PinDetailResponse`·`PlaceResponse`·`CommentResponse`·`CommentPageResponse` 생성
- [ ] T055 `core/data/.../network/service/PinApiService.kt`에 `getPinDetail`·`recordAccess`·`duplicatePin` 추가 ([contracts/place-api.md §1·2·4](./contracts/place-api.md))
- [ ] T056 [P] `core/data/.../network/service/CommentApiService.kt` 신설 — 코멘트 3종 ([contracts/comment-api.md](./contracts/comment-api.md))
- [ ] T057 `core/data/.../datasource/PinRemoteDataSource.kt`(+Impl)에 3종 추가, `CommentRemoteDataSource.kt`(+Impl) 신설
- [ ] T058 [P] `core/data/.../repository/mapper/PlaceDetailMapper.kt` 생성 — `label`은 서버가 주지 않으므로 `WORTH_VISITING`을 채운다 ([research.md D12](./research.md))
- [ ] T059 [P] `core/data/.../repository/mapper/PlaceCommentMapper.kt` 생성 — `hasNext` → `hasOlder`
- [ ] T060 `core/data/.../repository/PlaceRepositoryImpl.kt`·`PlaceCommentRepositoryImpl.kt` 생성과 `repository/di/` 바인딩 추가
- [ ] T061 `RoomSummary.hasPlace` 추가와 `RoomRepository.getRooms(placeId)` 확장, `RoomApiService.listRooms(showHasPlaceId)`·`RoomSummaryMapper`·`RoomRepositoryImpl` 반영 ([research.md D9](./research.md), [contracts/place-repository.md §3](./contracts/place-repository.md)). 기존 호출자(`GetRoomPickerRoomsUseCase`·`:feature:sharereceiver`)가 깨지지 않는지 확인한다
- [ ] T062 [US6] `.../main/vm/PlaceDetailViewModel.kt`에서 공유 시트 방 목록을 Fake에서 `getRooms(placeId)`로 교체하고 이미 저장된 방 판정을 `hasPlace`로 바꾼다. T061에 의존
- [ ] T063 `feature/placedetail/.../di/PlaceDetailFakeDataModule.kt`와 `fake/` 패키지 삭제 — Fake 제거. T060·T062에 의존
- [ ] T064 실제 데이터로 [quickstart.md §3](./quickstart.md) 재수행. 특히 「경과일 초기화 확인」이 진입 시 1회 나가는지, 비행기 모드에서 실패해도 화면이 정상인지 확인한다 (FR-026·EC-022)

---

## 미결 사항

작업으로 만들 근거가 없거나, spec 요구사항이 이번 구현에서 닫히지 않는 지점. `/mino-analyze`가 검증할 대상이다.

1. **FR-009 [나가기] 목적지** — `finish()`로 호출자 복귀까지만 구현한다([research.md D2](./research.md)). [SCR-005] 방 상세·지도 마커 진입은 호출자가 곧 목적지라 우연히 맞지만, **[SCR-003] 홈 카드와 [SCR-007] 알림 진입은 spec과 어긋난 채 남는다.** 방 상세(#161) 머지 후 별도 개정에서 닫는다. TS-006·TS-007·TS-037과 **EC-001**(알림 진입 시 최초 저장 방을 기준으로 잡고 그 방으로 나간다)이 미검증이다.
2. **FR-023·FR-024·FR-025 저장된 방 전환** — 서버가 대상 `pinId`를 주지 않아 구현 보류([research.md D10](./research.md)). [저장된 방] 버튼은 T029가 항상 비활성으로 그린다. **유저 플로우 7 전체와 TS-041~TS-049·EC-024~EC-027·UX-011~UX-013·SC-008·SC-009가 이번 구현의 검증 대상이 아니다.** 다만 **UX-013은 절반이 성립한다** — T065가 버튼 행을 `Full`에서 숨기므로 "방 전환 조작이 콘텐츠를 읽는 동안 끼어들지 않는다"는 성질 자체는 지켜지고, 전환 기능만 없다.
3. **FR-018 이미 저장된 방 판정** — 방 목록 자체는 실제 `getRooms()`로 받지만 `hasPlace`가 없어 **이미 저장된 방을 체크·비활성으로 표시하지 못한다**([research.md D15](./research.md)). 실제 연동은 T061·T062이며, 그때까지 TS-034·EC-019와 **SC-007**(고를 방이 없는 사용자가 막히지 않고 빠져나간다)이 검증되지 않는다 — `hasPlace`가 없어 그 상태 자체를 재현할 수 없다.
4. **FR-005 장소분류 라벨** — 서버가 값을 주지 않아 항상 기본값 `가볼 만한 곳`이다([research.md D12](./research.md)). EC-005 덕에 spec 위반은 아니나 FR-005의 취지("홈에서 부여된 값을 그대로 표시")는 작동하지 않는다.
5. **[현재 위치] 버튼의 동작** — 렌더링은 T065가 하지만 카메라 이동·위치 권한은 [SYS-004] 소관이라 정의된 곳이 없다. 눌러도 아무 일이 없는 상태로 남는다([research.md D16](./research.md)).
6. **외부 지도 앱 선택 정책** — [spec.md §3.2](./spec.md)가 비목표로 둔 `[TBD]`. T044 착수 시 정한다.
7. **[SYS-003] 방 선택 시트 내부 규칙** — 시트 높이(676dp)·카드 구성·비활성 시각 표현이 [SYS-003] spec 부재로 `[TBD]`다([research.md D13](./research.md)). T047 착수 시 정한 값과 근거를 남긴다.
8. **TS-035·EC-020 (시트에서 새 방 만들고 복귀)** — [SYS-001] 호출이 [SYS-003] 시트 소관이라 이번 범위에 없다.
9. **`EXTRA_PLACE_DETAIL_PIN_ID` 누락 시 처리** — [contracts/place-detail-launcher.md §2](./contracts/place-detail-launcher.md)가 `[TBD]`로 둔 방어 코드. T016 착수 시 정한다.
10. **「경과일 초기화 확인」이 홈 진입에서 2회 나간다** — T070으로 [SCR-003] 홈 카드를 실제로 이으면서 드러났다. 홈은 카드 탭에서 `HomeDeckRepository.recordPlaceOpened`를 부르고(home spec FR-007·TS-034), 상세는 열릴 때마다 `PlaceRepository.recordAccess`를 부른다(FR-026). 두 호출이 같은 서버 기록으로 가므로 **카드 한 번 탭에 기록이 2회 쌓인다.** 「진입 경로와 무관하게 기록한다」는 FR-026이 이 기록의 소유자이므로 홈 쪽 호출을 걷어내는 것이 맞으나, home spec FR-007·TS-034가 홈에도 기록을 요구해 spec 개정 없이는 지울 수 없다. **사용자 지시로 이번 라운드는 중복을 둔 채 진행하며**, 걷어낼 코드라는 사실을 `HomeViewModel.openPlaceDetail`의 KDoc에 명시했다.

---

## 구현 작업이 없는 요구사항

아래 셋은 작업 목록에 대응 항목이 없다. **누락이 아니라 설계상 구현할 것이 없는 경우**이므로 근거를 남긴다.

| 요구사항 | 왜 작업이 없는가 | 확인 방법 |
|---|---|---|
| FR-019 (코멘트가 (장소, 방) 단위에 귀속) | 서버가 코멘트를 `pinId` 경로에 매단다. 핀이 곧 (장소, 방)이므로 T010의 계약을 그대로 쓰는 것만으로 성립한다([research.md D4](./research.md)) | TS-024 — 같은 장소의 다른 방 `pinId`로 열면 코멘트가 다르다 |
| FR-020 (바텀 네비게이션 비노출) | 진입형 Activity는 탭 셸 밖에서 뜨므로 바텀바가 애초에 그려지지 않는다. `ImmersiveRoute` 같은 판정 장치가 필요 없다([research.md D3](./research.md)) | TS-010 — 화면에 바텀바가 보이지 않는다 |
| FR-027 (「지금 보고 있는 방」 초기값 결정) | `pinId` 안에 내포된다. 호출자가 어느 핀을 지목하느냐가 곧 어느 방의 눈으로 보는지다([research.md D4](./research.md)). 화면이 방을 따로 고르는 코드가 없다 | TS-002·TS-007 — 마커 색과 코멘트가 그 핀의 방을 따른다 |
| EC-002 (중복 마커 클릭도 다른 마커와 동일하게 `Half`) | 이 화면이 아니라 **호출자**가 지키는 성질이다. 장소 상세는 `pinId` 하나로 열리므로 그 장소가 여러 방에 저장돼 있는지 알지도 못한다([research.md D4](./research.md)) | 호출자 화면(방 상세 지도)이 생길 때 그쪽에서 확인 |
| SC-006 (90%가 첫 시도에 의도한 액션 완료) | 사용자 조사 지표라 코드로 만들 것이 없다 | 출시 후 측정. [quickstart.md §4](./quickstart.md)가 이미 제외로 적어 두었다 |

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **Phase 1 셋업**: 의존성 없음 — 즉시 시작
- **Phase 2 기반**: T001~T003 완료 후. 내부적으로 도메인(T006~T010) → Fake(T011~T014), 진입점(T015~T019), MVI(T020~T024)의 세 갈래가 있고 갈래끼리는 병렬이다
- **Phase 3~8 스토리**: 각 작업이 **실제로 쓰는** 기반 산출물이 나오면 시작한다. Phase 2 전체를 기다리지 않는다
- **Phase 9 마무리**: 목표한 스토리 완료 후
- **Phase 10 API**: 이번 라운드 범위 밖. Phase 9와 무관하게 언제든 착수 가능하나 서버 협의가 선행 조건이다

### 사용자 스토리 간 의존성

- **US1**: 독립. Phase 2가 끝나면 바로 시작. 내부 순서 하나 — T065(버튼 행) → T029(그 안의 [저장된 방]) → T025(지도에 합성)
- **US2**: T026(US1의 시트 골격)과 T027(확장형 헤더)에 의존 — 같은 파일을 이어서 고친다
- **US3**: T033(US2의 스크롤 축)에 의존. 캐러셀 컴포넌트 자체(T036)는 US1·US2와 병렬 가능
- **US4**: T033에 의존. 컴포넌트 3종(T038·T039·T041)은 US1~US3과 병렬 가능
- **US5**: T028(액션 행)·T030(SideEffect 배선)에 의존. T046은 US2~US4의 상태가 다 있어야 확인 가능
- **US6**: T021(UI 모델)에 의존. 나머지는 독립

### 병렬 처리 기회

- Phase 1: T004·T005가 병렬
- Phase 2: 도메인 3종(T006~T008) 병렬 → Fake 2종(T012·T013) 병렬. 진입점 갈래(T015~T019)와 MVI 갈래(T020~T022)는 도메인과 병렬
- Phase 3: T025·T026·T028·T031·T065가 서로 다른 파일이라 병렬 (T029는 T065 뒤)
- 컴포넌트 작업(T036·T038·T039)은 소속 스토리가 달라도 파일이 겹치지 않아 병렬
- **파일 충돌 주의**: `PlaceDetailSheet.kt`(T026→T032→T033), `PlaceDetailHeader.kt`(T027→T034), `PlaceDetailViewModel.kt`(T023→T035→T043→T048), `PlaceDetailActivity.kt`(T016→T030→T044→T045)는 같은 파일을 여러 작업이 이어서 고치므로 병렬이 아니다

---

## 병렬 실행 예시: Phase 2 기반 작업

```bash
# 도메인 모델 3종을 함께 생성:
Task: "core/domain/.../model/PlaceLabel.kt 에 PlaceLabel 4종 enum 생성"
Task: "core/domain/.../model/PlaceDetail.kt 에 PlaceDetail·PlaceRegistrant 생성"
Task: "core/domain/.../model/PlaceComment.kt 에 PlaceComment·Author·Page 생성"

# 도메인이 끝나면 Fake 2종을 함께 생성:
Task: "feature/placedetail/.../fake/FakePlaceRepository.kt 생성"
Task: "feature/placedetail/.../fake/FakePlaceCommentRepository.kt 생성"
```

## 병렬 실행 예시: Phase 3 (US1)

```bash
Task: "feature/placedetail/.../component/PlaceDetailMap.kt 에 MinoMap + 선택 핀 구현"
Task: "feature/placedetail/.../component/PlaceDetailSheet.kt 에 Half 369dp 앵커 구현"
Task: "feature/placedetail/.../component/PlaceActionRow.kt 에 액션 버튼 3종 가로 스크롤 구현"
Task: "feature/placedetail/.../component/PlaceMapControls.kt 에 지도 위 버튼 행 구현"
```

---

## 구현 전략

### MVP 우선 (US1까지)

1. Phase 1 셋업 완료 → 모듈이 빌드에 올라간다
2. Phase 2 기반 중 US1이 쓰는 것(T006·T007·T009·T011·T012·T014~T024) 완료
3. Phase 3 US1 완료
4. **중단하고 검증**: `adb`로 화면을 띄워 [quickstart.md §3.1](./quickstart.md) 수행
5. 이 시점의 산출물이 "지도 위 요약 시트가 뜨고 닫힌다" — 데모 가능한 최소 단위다

### 점진적 전달

1. 셋업 + 기반 → US1 (요약 시트, MVP)
2. US2 추가 (`Full`과 헤더 전환) → 시트가 완성된다
3. US3 추가 (캐러셀) → 콘텐츠가 채워진다
4. US4 추가 (코멘트) → 화면의 본체가 완성된다
5. US5·US6 추가 (외부 연동·공유) → 액션 행이 전부 살아난다
6. Phase 9 마무리 → UI 라운드 종료
7. Phase 10은 서버 협의가 닫힌 뒤 별도로

### 팀 병렬 전략

Phase 2까지 함께 끝낸 뒤:

- 개발자 A: US1 → US2 (시트·헤더 — 같은 파일 계열이라 한 사람이 잇는 편이 낫다)
- 개발자 B: US4 (코멘트 — 컴포넌트가 독립적이고 분량이 가장 크다)
- 개발자 C: US3 + US6 (캐러셀·공유 시트)
- US5는 A의 T030 이후 누구든

---

## 참고 사항

- 피해야 할 것: 모호한 작업, 동일 파일 충돌(위 「파일 충돌 주의」 참고), 스토리 독립성을 깨뜨리는 의존성
- 커밋 단위는 [`docs/conventions/commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다
- Fake는 이번 라운드 한정이다. T063이 지우는 범위(`fake/` 패키지와 `PlaceDetailFakeDataModule.kt`)를 넘어 Fake가 새어 나가지 않게 한다
