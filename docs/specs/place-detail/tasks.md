# 작업 목록: 장소 상세 & 코멘트 (Place Detail & Comments)

**대상 스펙 경로**: `docs/specs/place-detail`

**기준 plan 버전**: 2.1.1

**최초 작성일**: 2026-08-29

**최종 수정일**: 2026-09-02

**사전 조건**: [plan.md](./plan.md) (필수), [spec.md](./spec.md) (사용자 스토리), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/)

**테스트**: 자동 테스트 작업을 새로 만들지 않는다. [spec.md](./spec.md)가 요구하지 않았고 검증은 [quickstart.md](./quickstart.md)의 육안 시나리오로 한다. **다만 `:feature:room`의 기존 `RoomListViewModelTest`가 이번 구조 전환의 회귀 방어선이므로 깨지지 않게 유지한다**(T097).

**구성 방식**: 각 스토리를 독립적으로 구현하고 확인할 수 있도록 작업을 사용자 스토리별로 묶는다.

## 이번 라운드의 범위 — 구조 전환 + API 연결 (이슈 #270)

plan 2.0.0이 **구조를 뒤집었다.** 지난 라운드(plan 1.1.0)가 만든 진입형 모듈 `:feature:placedetail`을 해체하고 화면을 저장 탭(`:feature:room`)으로 편입하며, 동시에 Fake를 실 API로 교체하고 spec 4.0.0의 신규 요구를 반영한다.

**plan 2.1.0이 여기에 작업 둘을 더했다**(T099·T100). 2.0.0이 코멘트 작성 시각을 "도메인이 `Instant`를, 화면이 표기를" 갖는 데까지 정하고 **경과를 재는 기준 시각(「지금」)의 공급을 비워 둔 것**을 `Clock` 주입으로 채운다([research.md D26](./research.md)). 기존 작업의 의미는 그대로이고 T090·T091·T059의 설명만 그 결정에 맞춰 다듬었다.

**그래서 이 문서는 두 층으로 나뉜다.**

| 층 | 내용 |
|---|---|
| **Phase 11~15** | 이번 라운드에 할 일. 구조 전환 → spec 4.0.0 → API 연결 → 저장된 방 전환 → 검증 |
| **Phase 16** | 실기기 검수에서 나온 현상을 spec에 되먹인 뒤 고치는 작업 |
| **완료된 UI 라운드 (Phase 1~9)** | plan 1.1.0에서 이미 끝낸 작업의 기록. **체크 상태를 그대로 보존한다** |
| **폐기된 작업** | 완료됐으나 구조 전환으로 산출물이 사라지는 작업 20건. 정리 범위를 함께 적는다 |

**지난 라운드의 산출물 대부분은 살아남는다.** 시트·헤더·캐러셀·코멘트·입력 컴포넌트는 파일 위치만 옮겨간다. 사라지는 것은 그것을 감싸던 껍데기(Activity·Shell·NavHost·Launcher·중복 지도·Fake)다.

**plan 1.1.0이 남긴 미결 셋 중 둘이 해소되고 하나가 소멸했다** — 서버가 `matchedPinId`를 신설했고(FR-023~025 구현 가능), spec 4.0.0이 장소분류 라벨 요구를 없앴으며, 편입으로 FR-009가 닫힌다. 상세는 [plan.md 「요약」](./plan.md).

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. **한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.** 개정을 거쳐 문서 순서와 ID 순서가 어긋나 있다 — 실행 순서는 Phase 순서와 「의존성」 섹션이 말한다.
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: 이 작업이 속한 사용자 스토리 (US1~US7 — [spec.md §1](./spec.md)의 유저 플로우 1~7에 대응)

## 경로 규칙

모바일(Android, 다중 Gradle 모듈). 모든 경로는 저장소 루트 기준이며 [plan.md 「프로젝트 구조」](./plan.md)가 소유한다.

**이식 대상 경로 대응** — Phase 11이 옮기는 파일의 출발지와 도착지다.

```text
feature/placedetail/src/main/java/team/mino/feature/placedetail/main/
  → feature/room/src/main/java/team/mino/feature/room/placedetail/
```

---

## Phase 11: 구조 전환 — 저장 탭 편입

**목적**: 진입형 껍데기를 걷어내고 화면을 저장 탭의 세 번째 시트 분기로 만든다. 지도를 한 벌로 합친다.

**독립 테스트**: [quickstart.md §4·§5](./quickstart.md) — 방 상세 ↔ 장소 상세 전환에서 지도가 재생성되지 않고, 홈 진입 후 [나가기]가 방 상세로 간다.

### 진입 계약 교체

- [X] T071 [P] `core/navigation/src/main/java/team/mino/core/navigation/entry/PlaceDetailRequestHolder.kt` 신설 — `@ActivityRetainedScoped`, `pending: StateFlow<String?>`·`request(pinId)`·`consume()` ([contracts/place-detail-entry.md §3.1](./contracts/place-detail-entry.md)). T082·T084가 쓴다
- [X] T072 [P] `core/navigation/.../activity/launcher/PlaceDetailLauncher.kt` 삭제와 `ExtraTag.kt`에서 `EXTRA_PLACE_DETAIL_PIN_ID` 제거 ([contracts/place-detail-entry.md §5](./contracts/place-detail-entry.md)). T084·T087이 먼저 참조를 끊어야 컴파일이 통과한다

### 화면 이식 (`:feature:placedetail` → `:feature:room/placedetail`)

- [X] T073 [P] `main/model/` 4종(`PlaceSheetLevel`·`PlaceHeaderMode`·`PlaceCommentUiModel`·`RoomPickerItem`)을 `feature/room/.../placedetail/model/`로 이식 — 패키지 선언만 바꾼다. `RoomColorPalette.kt`는 `:feature:room`에 이미 `RoomColorMapping`이 있으므로 **중복을 합친다**
- [X] T074 `main/component/` 이식 — `PlaceDetailSheet`·`PlaceDetailHeader`·`PlaceActionRow`·`PlaceImageCarousel`·`SheetParts`·`PlaceCommentList`·`PlaceCommentItem`·`PlaceCommentEmpty`·`PlaceCommentInput`·`PlaceCommentMenu`·`PlaceMapControls`·`RoomShareSheet`를 `placedetail/component/`로. **`PlaceDetailMap.kt`·`CurrentLocationButton.kt`·`SavedRoomsButton.kt`는 이식하지 않는다** — 지도·컨트롤 단일화([research.md D25](./research.md))와 FR-023 구현(T093)이 대체한다. T073에 의존
- [X] T075 `main/vm/` 4종을 `placedetail/vm/`으로 이식 — `PlaceDetailViewModel`은 `savedStateHandle.toRoute()` 대신 **`@AssistedInject`로 `pinId`를 받는다**(`RoomDetailViewModel`과 같은 형태). `PlaceDetailUiState`에서 **`roomColor` 필드를 제거**한다 ([contracts/place-detail-main-contract.md §1·§3](./contracts/place-detail-main-contract.md)). T073에 의존
- [X] T076 `main/screen/` 3종을 `placedetail/screen/`으로 이식 — **`PlaceDetailScreen`·`PlaceDetailRoute`를 `BoxScope` 확장으로 바꾸고 지도를 그리지 않는다**(`RoomDetailScreen`과 같은 형태, [contracts/place-detail-main-contract.md §7](./contracts/place-detail-main-contract.md)). `Full` 윗변 계산은 유지한다. T074·T075에 의존
- [X] T077 [P] `feature/placedetail/src/main/res/values/strings.xml`의 문자열을 `feature/room/src/main/res/values/`로 이식 — 이름 충돌 여부를 확인한다

### 저장 탭 편입

- [X] T078 [P] `feature/room/.../main/model/MapPinUiModel.kt`에 `selected: Boolean` 추가와 `main/component/RoomListMap.kt`의 `PlacePin`에서 `RoomMapPin(selected = pin.selected)` 반영 — 선택 핀 전환 (FR-002·TS-002, [contracts/place-detail-main-contract.md §2.4](./contracts/place-detail-main-contract.md)). `:core:common:ui`의 `RoomMapPin`이 이미 두 외형을 가지므로 새 컴포넌트가 없다
- [X] T079 `feature/room/.../main/vm/`에 `selectedPinId` 상태와 인텐트 3종(`OnPlaceSelected`·`OnClosePlaceDetailClick`·`OnPlaceDetailRoomSwitched`) 추가 — **`OnClosePlaceDetailClick`이 `selectedPinId = null` 한 줄로 FR-009를 닫는다**(TS-006). `mapPins` 계산에 `selected` 반영, 진입 시 `mapCenter`·`mapCenterRequestId` 갱신 ([contracts/place-detail-main-contract.md §2.1·2.2](./contracts/place-detail-main-contract.md)). T078에 의존
- [X] T080 `feature/room/.../main/screen/RoomListScreen.kt`에 **시트 세 갈래 분기** 추가 — `selectedPinId` 우선, 그다음 `selectedRoomId`, 아니면 리스트. 지도 컨트롤 노출 판정(`isMapControlVisible`)에 장소 상세 시트 단계를 더한다 ([contracts/place-detail-main-contract.md §2.7](./contracts/place-detail-main-contract.md)). T076·T079에 의존
- [X] T081 `feature/room/.../main/screen/RoomListRoute.kt`에 장소 상세 슬롯 호출과 `BackHandler` 확장(`selectedPinId != null || selectedRoomId != null`), `LocalBottomNavVisibility` 판정식에 `selectedPinId == null` 추가 — **기존 `DisposableEffect`를 고치고 새로 만들지 않는다**(FR-020, [research.md D19](./research.md)). 슬롯이 닫히면 그 아래 방 상세 시트가 그대로 드러나는 것이 **FR-009**의 구현이며, 아래로 드래그해 닫는 경우도 같은 경로다(EC-003). T080에 의존
- [X] T082 `RoomListRoute`·`RoomListViewModel`에 `PlaceDetailRequestHolder.pending` 구독·소비 배선 — `pinId`로 핀 상세를 조회해 `roomId`를 해석한 뒤 `selectedRoomId`·`selectedPinId`를 **함께** 세우고 `consume()` ([contracts/place-detail-main-contract.md §2.3](./contracts/place-detail-main-contract.md)). **둘을 함께 세우는 것이 FR-009의 전제다** — 알림은 방을 특정하지 않으므로 최초 저장 방이 [나가기] 목적지가 된다(FR-009·EC-001·TS-007). 조회 실패 시 소비만 하고 아무것도 열지 않는다. T071·T079에 의존
- [X] T083 `feature/room/.../detail/vm/RoomDetailSideEffect.kt`의 `NavigateToPlaceDetail(placeId)`를 `pinId`로 바꾸고, `detail/screen/RoomDetailRoute.kt`에서 **`-> Unit`으로 흘리던 자리를 실제 배선으로 교체** — `onOpenPlaceDetail(pinId)` 콜백을 `RoomListRoute`가 넘긴다 ([contracts/place-detail-entry.md §2](./contracts/place-detail-entry.md)). T079에 의존

### 셸·모듈 정리

- [X] T084 `feature/main/.../MainActivity.kt`·`MainNavHost.kt`에서 `placeDetailLauncher` 주입과 `launchPlaceDetail`을 **`holder.request(pinId)` + `navigateToTab(MainTab.SAVED)`로 교체** ([contracts/place-detail-entry.md §3.2](./contracts/place-detail-entry.md)). 탭을 저장으로 옮기는 것이 **FR-009**가 요구하는 귀착지를 만든다 — 홈에서 들어와도 [나가기]는 홈이 아니라 방 상세로 간다(TS-037). T071에 의존
- [X] T085 외부 지도·원문 열기 실행부(`openExternalMap`·`openSourceLink`·`startViewIntent`)를 `PlaceDetailActivity`에서 `feature/main/.../MainActivity.kt`로 이식하고, `OpenExternalMap`·`OpenSourceLink` SideEffect를 `RoomListRoute` 경유로 잇는다 (FR-016·FR-017). `geo:` 우선 순서와 그 근거 주석을 그대로 옮긴다. T076·T084에 의존
- [X] T086 `placedetail/component/PlaceMapControls.kt`의 [현재 위치]를 **`RoomListViewModel`의 `OnCurrentLocationClick`으로 연결** — 장소 상세가 자기 카메라 상태를 들지 않는다([research.md D25](./research.md)). `RoomDetailScreen`이 이미 같은 배선을 하고 있어 그 형태를 따른다. **plan 1.1.0에서 `[TBD]`였던 [현재 위치] 동작이 여기서 닫힌다.** T080에 의존
- [X] T087 `:feature:placedetail` 모듈 삭제 — `settings.gradle.kts`의 `include`, `app/build.gradle.kts`의 `implementation`, `feature/placedetail/` 디렉터리 전체. T072~T086이 모두 끝난 뒤 마지막에 한다

**체크포인트**: [quickstart.md §4](./quickstart.md)의 9행이 통과하고, 특히 **2번(지도가 다시 그려지지 않는다)** 이 확인된다.

---

## Phase 12: spec 4.0.0 반영

**목적**: 헤더에서 장소분류 라벨을 걷어내고 등록자 닉네임을 세우며, 코멘트에 작성 시각을 붙인다. **plan 2.1.0이 시각 표기의 기준 시각(「지금」) 공급을 이 단계에 더했다**(T099·T100, [research.md D26](./research.md)).

**독립 테스트**: [quickstart.md §4-4·4-5·§6](./quickstart.md) — TS-003·TS-008·TS-009·TS-018·TS-050~TS-054, 그리고 §6 13번(EC-028 — 띄워 둔 채로는 표기가 바뀌지 않는다).

- [X] T088 [P] [US1] `core/domain/.../model/PlaceLabel.kt` **삭제**와 `PlaceDetail.kt`의 `label` 필드 제거, `PlaceRepository.kt` KDoc에서 사실이 아니게 된 두 문단 정리 ([research.md D21](./research.md), [contracts/place-repository.md §1](./contracts/place-repository.md)). `PlaceCard`는 자기 라벨 표현을 따로 가지므로 영향받지 않는지 확인한다
- [X] T089 [US1] `placedetail/component/PlaceDetailHeader.kt` 확장형 헤더에서 **라벨 자리를 등록자 닉네임으로 교체** — 아바타 + 닉네임 + [나가기]가 한 줄. 닉네임은 한 줄 유지 + `...` 생략이며 [나가기] 자리를 침범하지 않는다 (FR-005·TS-008·TS-009). **헤더 첫 줄이 「누가 이 장소를 담았는가」만 말하게 한다**(UX-014) — 장소의 성격을 판정한 라벨이 그 옆에 끼어들지 않는다. 축소형 헤더는 장소명 + [나가기]만 남는 것이 그대로다 (UX-005). T074·T088에 의존
- [X] T090 [P] [US4] `core/domain/.../model/PlaceComment.kt`에 `createdAt: kotlin.time.Instant` 추가와 "작성 시각을 담지 않는다"는 KDoc 문단 제거 ([data-model.md §2](./data-model.md)). **`@OptIn(ExperimentalTime::class)`을 함께 붙인다** — Kotlin 2.2.10에서 `kotlin.time.Instant`가 아직 실험적이고 전역 opt-in 설정이 없다. `Place.kt`가 클래스에 붙인 형태를 따른다
- [X] T091 [US4] `placedetail/model/PlaceCommentUiModel.kt`에 `createdAt` 추가와 **경과 시간 → 표기 환산 함수** 신설, `PlaceCommentItem.kt`에서 본문 우측 아래에 배치 — 1시간 미만 `방금`(음수 포함) / 24시간 미만 `N시간 전` / 7일 미만 `N일 전` / 그 이상 `NNNN년 NN월 NN일` (FR-028·EC-028·EC-029, [contracts/place-detail-main-contract.md §6](./contracts/place-detail-main-contract.md)). **환산 함수의 입력은 `(createdAt, commentsObservedAt)` 둘이다** — 함수 안에서 `Clock.System.now()`를 부르지 않는다([§6.1](./contracts/place-detail-main-contract.md)). **상태가 아니라 컴포지션 시점의 순수 함수다** — 실시간 갱신하지 않는다. 문자열은 `:feature:room`의 리소스가 소유한다. **당일 코멘트의 선후가 목록만 보고 읽히는 것**(UX-015)과 **시각을 알아보려 다른 화면으로 나가지 않는 것**(SC-011)이 이 작업의 판정 기준이다. T074·T090·T100에 의존
- [X] T099 [P] [US4] `feature/room/src/main/java/team/mino/feature/room/di/PlaceDetailClockModule.kt` 신설 — `kotlin.time.Clock`을 `@Provides`로 제공하고 `ViewModelComponent`에 설치한다 ([research.md D26](./research.md), [contracts/place-detail-main-contract.md §6.1](./contracts/place-detail-main-contract.md)). 요구하는 곳이 이 모듈의 ViewModel 하나뿐이라 앱 전역 그래프에 올리지 않는다 — `:feature:sharereceiver`의 `ShareReceiverResourcesModule`이 같은 판단을 KDoc으로 남긴 선례이며, 그 KDoc 형태를 따라 이유를 적는다. 모듈과 `@Provides`는 `internal`로 닫는다 ([ADR 2026-08-02](../../adr/2026-08-02-di-binding-ownership.md))
- [X] T100 [US4] `placedetail/vm/`에 **기준 시각 배선** — `PlaceDetailViewModel` 생성자에 `clock: Clock`을 받고, `PlaceDetailUiState`에 `commentsObservedAt: Instant = Instant.DISTANT_PAST`를 추가한다. **코멘트 목록 상태를 다시 만들 때마다 `clock.now()`로 갱신한다** — 최초 조회·이전 페이지 추가 로드·등록 후 반영·삭제 후 반영 네 자리 모두 ([contracts/place-detail-main-contract.md §3·§6.1](./contracts/place-detail-main-contract.md)). 등록 직후 `방금`이 뜨는 것(TS-054)이 이 갱신으로 성립하고, 목록을 둔 채 시간만 흐르는 동안 갱신되지 않는 것이 EC-028이다. `@OptIn(ExperimentalTime::class)` 필요. T075·T090·T099에 의존

**체크포인트**: 헤더 어디에도 라벨이 없고, 코멘트마다 시각이 네 구간으로 갈려 보인다. 화면을 띄워 둔 채로는 표기가 저절로 넘어가지 않는다.

---

## Phase 13: API 연결 — Fake를 실 서버로 교체

**목적**: `:core:data`에 실구현을 두고 Fake를 걷어낸다.

> **착수 전 확인**: [contracts/place-api.md §5](./contracts/place-api.md)의 서버 협의 항목은 **1건으로 줄었다**(아바타 색 enum 불일치). 계약 근거는 2026-09-01T21:46:23+09:00 시점의 문서다.

**독립 테스트**: [quickstart.md §6~§9](./quickstart.md) — 실 데이터로 코멘트·공유·접근 기록이 동작한다.

- [X] T054 [P] `core/data/.../network/dto/response/` 에 `PinDetailResponse`·`PlaceResponse`·`CommentResponse`·`CommentPageResponse` 생성
- [X] T055 `core/data/.../network/service/PinApiService.kt`에 `getPinDetail`·`recordAccess`·`duplicatePin` 추가 ([contracts/place-api.md §1·2·4](./contracts/place-api.md))
- [X] T056 [P] `core/data/.../network/service/CommentApiService.kt` 신설 — 코멘트 3종 ([contracts/comment-api.md](./contracts/comment-api.md))
- [X] T057 `core/data/.../datasource/PinRemoteDataSource.kt`(+Impl)에 3종 추가, `CommentRemoteDataSource.kt`(+Impl) 신설
- [X] T058 [P] `core/data/.../repository/mapper/PlaceDetailMapper.kt` 생성 — `PinDetailResponse` → `PlaceDetail`. **`label`을 채우지 않는다** — `PlaceLabel` 타입이 삭제됐다(T088, [research.md D21](./research.md)). `createdBy.avatar.color`는 13색 팔레트로 해석하고 모르는 값은 `null`로 떨어뜨린다([contracts/place-api.md §1.3](./contracts/place-api.md))
- [X] T059 [P] `core/data/.../repository/mapper/PlaceCommentMapper.kt` 생성 — `hasNext` → `hasOlder`, **`createdAt`을 `Instant.parse`로 `kotlin.time.Instant`에 옮긴다**(FR-028, [contracts/comment-api.md §1.3](./contracts/comment-api.md)). `PlaceMapper.kt`가 `savedAt`에 쓰는 것과 같은 형태이며 `@file:OptIn(ExperimentalTime::class)`도 그대로 따른다. 표기 환산은 하지 않는다 — feature 소관이다(T091)
- [X] T060 `core/data/.../repository/PlaceRepositoryImpl.kt`·`PlaceCommentRepositoryImpl.kt` 생성과 `repository/di/` 바인딩 추가 — 바인딩은 구현을 가진 `:core:data`가 소유한다 ([ADR 2026-08-02](../../adr/2026-08-02-di-binding-ownership.md)). T057·T058·T059에 의존
- [X] T061 `RoomSummary`에 `hasPlace: Boolean?`·**`matchedPinId: String?`** 추가와 `RoomRepository.getRooms(placeId: String? = null)` 확장, `RoomApiService.listRooms(showHasPlaceId)`·`RoomSummaryMapper`·`RoomRepositoryImpl` 반영 ([data-model.md §3](./data-model.md), [contracts/place-repository.md §3](./contracts/place-repository.md)). **기본 인자라 기존 호출자(`GetRoomPickerRoomsUseCase`·`:feature:sharereceiver`·`RoomListViewModel`)가 깨지지 않는지 확인한다**
- [X] T062 [US6] `placedetail/vm/PlaceDetailViewModel.kt`에서 `place` 도착 후 `getRooms(placeId = place.placeId)`를 호출해 `savedRooms`를 채우고, 공유 시트의 이미 저장된 방 판정을 `hasPlace`로 바꾼다 (FR-018·FR-022, [contracts/place-detail-main-contract.md §3.1](./contracts/place-detail-main-contract.md)). **고를 방이 하나도 없어도 사용자가 이유와 남은 선택지를 확인하고 빠져나가야 한다**(SC-007) — 체크·비활성 카드가 곧 그 안내다(UX-010). T061·T075에 의존

**체크포인트**: Fake 없이 실 데이터로 화면이 뜬다. `fake/` 패키지는 T087의 모듈 삭제로 이미 사라져 있다.

---

## Phase 14: 사용자 스토리 7 - 저장된 방 전환

**목표**: 여러 방에 저장된 장소에서 보는 방을 바꾸면 핀 색·코멘트·[나가기] 목적지가 함께 따라온다.

**독립 테스트**: [quickstart.md §7](./quickstart.md) — TS-040·TS-041과 유저 플로우 7, 그리고 **SC-008**([저장된 방] 한 번 + 방 카드 한 번, 총 2회 조작으로 다른 방의 코멘트에 닿는다). **plan 1.1.0에서 구현 보류였던 기능이다**([research.md D20](./research.md)).

- [X] T092 [US7] `placedetail/vm/`에 `SavedRoomsSheetUiState` 추가, `isSavedRoomsEnabled`를 **`false` 고정에서 `savedRooms.count { it.hasPlace == true } >= 2`로 교체**, 인텐트 3종(`OnSavedRoomsClick`·`OnSavedRoomSelected`·`OnSavedRoomsSheetDismiss`)과 SideEffect `SwitchRoom(pinId, roomId)` 추가 ([contracts/place-detail-main-contract.md §3.2·3.3·§4·§5](./contracts/place-detail-main-contract.md)). `PlaceDetailIntent` KDoc의 "[저장된 방] 버튼의 Intent가 없다"를 지운다. T062에 의존
- [X] T093 [US7] `placedetail/component/SavedRoomsSheet.kt` 신설 — 그 장소가 저장된 방 목록에서 **지금 보고 있는 방을 제외한다**(FR-024·TS-042·EC-026). 선택 상태로 표시하는 것이 아니라 빼는 것이다. 고르면 `matchedPinId`를 실어 올린다. **눌러도 아무 일이 없는 항목을 두지 않는다**(UX-012) — 남은 카드가 곧 「옮겨 갈 수 있는 방」이다. **치수는 FR-024가 못박은 값을 쓴다** — 시트 442dp 고정(하단 safe area 60dp 포함)·내부 스크롤 312dp 고정·체크박스와 확정 CTA 없음·카드 탭이 곧 확정([contracts/place-detail-main-contract.md §3.3](./contracts/place-detail-main-contract.md)). **`RoomShareSheet`(676dp)를 따르지 않는다** — 그쪽은 [SYS-003] 소관의 다른 시트다. 카드 내부 표현처럼 두 시트가 겹치는 부분만 참고한다. T092에 의존
- [X] T094 [US7] `placedetail/component/PlaceMapControls.kt`에 [저장된 방] 버튼 활성/비활성 배선 — 활성일 때만 시트가 열린다 (FR-023·TS-040·TS-041). **활성 여부 자체가 「이 장소가 여러 방에 있다」를 알린다**(UX-011) — 중복 저장을 알리는 별도 뱃지·문구를 두지 않는다. `Full`에서 컨트롤 행이 함께 숨는 기존 동작은 유지한다 (UX-013). T086·T092에 의존
- [X] T095 [US7] `SwitchRoom` SideEffect를 `RoomListRoute`가 받아 `RoomListIntent.OnPlaceDetailRoomSwitched(pinId, roomId)`로 넘기는 배선 — `selectedPinId`·`selectedRoomId`가 함께 갱신되어 핀 색·코멘트·[나가기] 목적지가 따라온다 (FR-025). **셋이 서로 다른 방을 가리키는 상태가 0건이어야 한다**(SC-009) — 사용자가 지금 어느 방을 보고 있는지 되묻지 않게 하는 조건이다. 코멘트 초안은 ViewModel이 새로 서면서 자연히 사라진다. T079·T092에 의존

**체크포인트**: [quickstart.md §7](./quickstart.md)의 7행이 모두 통과한다 — 특히 7번(전환 후 [나가기]가 B방으로 간다).

---

## Phase 15: 마무리 및 검증

**목적**: 구조 전환이 기존 화면을 깨뜨리지 않았는지 확인하고 게이트를 통과한다.

- [ ] T053 [quickstart.md §4~§9](./quickstart.md)의 기능 시나리오 수행 — 구조 전환·spec 4.0.0·API 연결·저장된 방 전환을 차례로 확인한다
- [ ] T064 실 데이터 기준 확인 — 「경과일 초기화 확인」이 진입마다 1회 나가는지, 비행기 모드에서 실패해도 화면이 정상인지 (FR-026·EC-022·EC-023, [quickstart.md §9](./quickstart.md))
- [ ] T096 **회귀 확인** — [quickstart.md §10](./quickstart.md)의 6건. 특히 4·5번(넛지 팝업 아래 지도가 하얗게 남던 결함, 공동방 생성 직후 지도)은 T081이 `LocalBottomNavVisibility` 판정식을 직접 건드리므로 반드시 본다. T081에 의존
- [ ] T097 품질 게이트 — `./gradlew :app:assembleQaDebug`와 `./gradlew :core:domain:test :core:data:test :feature:room:test`, `./gradlew lintDebug -Dorg.gradle.jvmargs="-XX:-TieredCompilation"` (헌법 「품질 게이트」, [quickstart.md §3](./quickstart.md)). **기존 `RoomListViewModelTest`가 깨지면 구조 전환이 회귀를 만든 것이다**
- [X] T098 잔여 참조 정리 확인 — `placedetail`·`PlaceDetailLauncher`·`EXTRA_PLACE_DETAIL_PIN_ID`·`PlaceLabel`을 참조하는 코드가 저장소에 없다 ([quickstart.md §2·§11](./quickstart.md)). T087·T088에 의존

**체크포인트**: [quickstart.md §11](./quickstart.md)의 완료 판정 4항이 모두 참이다.

---

## Phase 16: 실기기 검수 반영

**목표**: 실기기에서 확인된 현상을 spec과 코드 어느 쪽의 문제인지 가른 뒤, 문서를 먼저 고치고 코드를 뒤따르게 한다.

- [X] T101 **`Half`가 시트의 하한이다** — spec 5.0.0에서 `EC-003`(아래로 드래그 = 나가기)과 `TS-015`(`Half` 유지 **또는 닫힘**)를 정정하고, `FR-001`에 369dp가 하한임을 명시했다. 끌어 닫기는 PRD §1 「3단 바텀시트」(`[SCR-006]: Half 높이 369dp를 유지한다`)와 Figma 주석 12번 어디에도 근거가 없이 스펙이 덧붙인 단계였다. 코드에서는 `PlaceDetailSheet`의 `SheetAnchor.GONE` 앵커와 `onExitRequest` 파라미터를 걷어내 앵커를 `HALF`/`FULL` 둘로 맞췄다 — 앵커가 없으면 `AnchoredDraggableState`가 그 아래 델타를 흘려보내므로 별도의 하한 판정을 두지 않는다. 나가는 길은 [나가기]와 시스템 뒤로가기 둘이며, `RoomListIntent.OnClosePlaceDetailClick` 하나로 모이는 것은 그대로다. **T081 설명의 "아래로 드래그해 닫는 경우도 같은 경로다(EC-003)"는 이 작업으로 폐기된다** — 그 경로 자체가 없어졌다. 함께 고친 문서: [spec.md](./spec.md) `FR-001`·`EC-003`·`TS-015`·§5, [quickstart.md §4](./quickstart.md) 8행, [contracts/place-detail-entry.md §4](./contracts/place-detail-entry.md), [research.md D5](./research.md)
- [X] T102 실기기 확인 — `Half`에서 시트를 아래로 끌어 놓았을 때 369dp에서 멈추고 닫히지 않는지, `Full`→`Half` 복귀와 [나가기]·뒤로가기가 그대로인지 (`FR-001`·`EC-003`·`TS-014`·`TS-015`, [quickstart.md §4](./quickstart.md) 7~9행). T101에 의존
- [X] T103 **헤더는 접고도 스크롤 여유가 남을 때만 접는다** — spec 5.0.0에서 `FR-008`에 조건을 더하고 `EC-007`을 경계 길이까지 넓혔으며 `TS-055`를 신설했다. 「대표 이미지 있음 + 코멘트 0건」의 `Full`처럼 콘텐츠가 뷰포트보다 조금만 길면, 헤더가 접히며 넓어진 스크롤 영역이 스크롤 범위를 지워 위치가 최상단으로 되돌아가고, 최상단이 다시 확장형을 불러 왕복이 된다 — 실기기의 덜컹거림이 이 되먹임이었다. 코드에서는 판정을 `PlaceDetailScreen`으로 모아 **남은 스크롤 여유가 확장형 헤더 실측 높이보다 작으면 접지 않고**, 펴는 조건은 최상단 하나로 남겼다(양쪽에 같은 식을 걸면 좁아진 여유가 곧바로 식을 뒤집는다). 축소형 높이 대신 확장형 높이를 기준으로 쓰는 것은 그 값이 한 번 접혀 봐야 나오는 값이라 첫 판정에 못 쓰기 때문이며, 두 헤더의 높이 차는 확장형 높이보다 늘 작아 안전한 상한이다. 인텐트는 `OnScrollOffsetChange(isAtTop)` → `OnHeaderExpansionChange(isExpanded)`로 바뀌었고 ViewModel은 결과만 싣는다. 함께 고친 문서: [spec.md](./spec.md) `FR-008`·`EC-007`·`TS-055`·§5, [quickstart.md §6](./quickstart.md) 14행, [research.md D5](./research.md)
- [X] T104 실기기 확인 — 대표 이미지가 있고 코멘트가 0건인 장소를 `Full`로 열어 스크롤 영역을 위아래로 움직였을 때 헤더가 확장형에 머물고 화면이 떨리지 않는지, 코멘트가 많은 장소에서는 축소·복원이 예전대로인지 (`FR-008`·`EC-007`·`TS-012`·`TS-013`·`TS-055`, [quickstart.md §6](./quickstart.md) 14행). T103에 의존

- [X] T105 **[저장된 방] 버튼은 노출/미노출로 가른다** — **PRD를 12.1.0으로**, spec을 5.0.0으로 함께 개정했다. `FR-023`(비활성으로 노출 → 노출하지 않는다)·`UX-011`·`EC-024`·`TS-040`·`TS-041`과 PRD [SCR-006] Flow A·§1 「저장된 방 시트」·주석 #15 전사·§5를 맞췄다. 비활성 버튼은 왜 못 누르는지 설명할 통로가 없는 자리(`EC-024`가 문구·토스트를 두지 않기로 한 자리)이고, 버튼의 유무 자체가 중복 저장을 알린다는 `UX-011`의 취지는 그대로 산다. 코드에서는 `PlaceDetailUiState.isSavedRoomsEnabled` → **`isSavedRoomsVisible`**, `PlaceMapControls`가 그 값으로 버튼을 **그릴지 말지**를 가르고 `SavedRoomsButton`은 `enabled` 파라미터를 잃었다. [현재 위치] 버튼이 `Alignment.End`로 서 있어 [저장된 방]이 빠져도 행이 흔들리지 않는다. 비활성 프리뷰는 미노출 프리뷰로 대체했다. **T094 설명의 "활성/비활성 배선"은 이 작업으로 폐기된다.** 함께 고친 문서: [spec.md](./spec.md), [quickstart.md §7](./quickstart.md), [contracts/place-detail-main-contract.md §3.1·3.2·§4·§8](./contracts/place-detail-main-contract.md), [contracts/place-api.md](./contracts/place-api.md), [contracts/place-repository.md](./contracts/place-repository.md), [research.md D24](./research.md)
- [X] T106 실기기 확인 — 한 방에만 저장된 장소에서 지도 우측 하단에 [현재 위치]만 서 있는지, 두 방 이상이면 [저장된 방]이 함께 나타나고 눌리는지 (`FR-023`·`EC-024`·`TS-040`·`TS-041`, [quickstart.md §7](./quickstart.md) 1~2행). T105에 의존

- [X] T107 **`Full`은 상태바 영역까지 덮는다** — spec 5.0.0에서 `TS-011`을 보강하고 §4 가정·§5 확정 항목을 더했다. 실기기에서 헤더 첫 줄이 상태바에 붙어 보였고, 원인은 셸(`MinoScaffold`)이 상단 인셋을 소비한 자리에서 시트가 시작하는데 헤더 위 여백이 12dp뿐이었던 것이다. 코드에서는 `PlaceDetailScreen`의 컨테이너 `systemBarBleed`에 **`top`(상태바)을 더해** 시트가 그 자리를 소유하게 하고, `PlaceDetailSheet`의 `Full` 상단 띠를 **상태바 높이 + 16dp**로 바꿨다(Figma `005-2-1 full`: 상태바 54 · 헤더 프레임 54 · 아바타 70). **단계별로 켜고 끄지 않는다** — 단계는 앵커가 정착한 뒤에야 갱신되므로 손을 떼는 순간 시트가 상태바 높이만큼 튄다. `Half`는 시트 높이가 369dp 고정이라 컨테이너가 위로 늘어나도 서는 자리가 그대로다. 시트를 조금 낮춰 세우는 대안은 상태바 아래에 지도 띠를 상시로 남겨 기각했다.
  - **상태바에 가리는 만큼을 시트가 재서 그만큼만 비운다.** 앞선 두 시도가 실기기에서 모두 빗나갔다 — ① 컨테이너를 상태바만큼 위로 끌어올리고(`systemBarBleed(top)`) 시트가 그 높이를 **따로 읽어** 위쪽 띠에 얹었더니 시트 쪽 값만 0으로 나와 헤더가 상태바 뒤로 들어갔고, ② 값을 화면에서 한 번만 읽어 양쪽에 같이 넘겼는데도 그대로였다. 두 번 다 **「이 화면이 놓이는 자리가 이미 상태바만큼 물러나 있다」는 전제**를 코드가 고정값으로 깔고 있었고, 그 전제가 기기에서 성립하지 않으면 끌어올린 만큼 시트가 화면 밖으로 밀려 같은 증상이 남는다. 그래서 **전제를 없앴다** — `systemBarBleed`의 `top`을 걷어내고, 시트가 `onGloballyPositioned`로 자기 윗변의 창 안 위치를 재서 상태바 높이와의 차이(`statusBarOverlap`)만큼만 위쪽 띠에 얹는다. 시트가 상태바 아래에서 시작하면 0, 화면 최상단부터 서면 상태바 높이가 되므로 어느 쪽이든 헤더는 상태바 아래 16dp에 선다. 상태바 높이 자체는 여전히 화면이 한 번 읽어 넘긴다(`statusBarInset`) — 인셋을 읽는 자리가 둘이면 ①의 갈림이 되살아난다
- [X] T108 실기기 확인 — `Full`에서 시트가 상태바까지 덮고 헤더가 가리지 않는지, 올리는 도중 상단에 지도 띠가 비치지 않는지, `Half`의 시트 높이·지도 노출과 지도 컨트롤 위치가 그대로인지 (`FR-001`·`TS-011`, [quickstart.md §4](./quickstart.md) 10행). T107에 의존

- [X] T109 **선택 핀은 시트에 가리지 않은 지도의 중앙에 놓인다** — spec 5.0.0에서 `FR-002`에 중심의 기준을 명시하고 §5 확정 항목을 더했다. `:core:map`의 `MinoMap`에 `contentPadding`을 열고(maps-compose 7.0.0 → `GoogleMap.setPadding`), `RoomListScreen`이 위쪽은 `mapBleed`(상태바 뒤로 들어간 만큼)·아래쪽은 **지금 선 시트가 가리는 높이**를 실어 넘긴다. 그 높이는 세 갈래(장소 상세 → 방 상세 → 리스트)에서 오고, 장소 상세만 내비게이션 바 자리까지 덮으므로 그 인셋을 뺀다. `placeDetailSheetHeightOrNull`을 신설해 다른 두 시트의 같은 헬퍼와 짝을 맞췄고, 지도 컨트롤 노출 판정(`isMapControlVisible`)도 같은 값에서 갈리게 해 우선순위 분기를 하나로 합쳤다. **지도가 한 벌이라 방 상세·방 리스트에도 함께 적용된다**([research.md D25](./research.md)) — 그 두 화면의 spec은 이번 개정에서 손대지 않았다
- [ ] T111 **탭 간 진입도 카메라를 선택 핀으로 옮긴다** — spec 5.0.0에서 `EC-030`·`TS-056`을 신설하고 §5 확정 항목을 더했다. `FR-002`의 카메라 이동은 진입점 넷 전부에 걸리는데, [contracts/place-detail-main-contract.md §2.3](./contracts/place-detail-main-contract.md)의 탭 간 요청 소비가 `selectedRoomId`·`selectedPinId`만 세우고 카메라를 빼먹어 홈 진입에서 마커가 화면 밖에 남았다(같은 계약 §2.2가 "탭 간 요청도 `OnPlaceSelected`로 온다"고 적어 두 문단이 서로 어긋나 있었다). 좌표는 **핀 상세 응답의 `location`**을 쓴다 — `placesByRoomId`에서 찾으면 홈 콜드 진입에서 목록이 비어 있어 못 찾는다. 함께 `RoomListViewModel`의 자동 카메라 이동(`OnScreenEntered`·`OnLocationPermissionResult`)을 `selectedPinId != null`인 동안 건너뛰게 한다([research.md D27](./research.md), 같은 계약 §2.8) — 탭 전환과 장소 상세 열기가 같은 순간이라 둘이 겹치고 나중에 끝난 쪽이 이기기 때문이다. 사용자가 직접 누른 [현재 위치]는 막지 않는다. 함께 고친 문서: [spec.md](./spec.md) `EC-030`·`TS-056`·§5, [quickstart.md §5](./quickstart.md) 1-1·1-2행, [contracts/place-detail-main-contract.md §2.2·§2.3·§2.8](./contracts/place-detail-main-contract.md), [contracts/place-detail-entry.md §3.2](./contracts/place-detail-entry.md), [research.md D27](./research.md), [room-list/contracts/room-list-main-contract.md](../room-list/contracts/room-list-main-contract.md)
- [ ] T112 실기기 확인 — 현재 위치와 멀리 떨어진 장소를 홈 카드 덱에서 열었을 때 카메라가 그 장소로 옮겨가 선택 핀이 시트 위 영역의 중앙에 보이는지, 그 뒤 카메라가 현재 위치로 튀지 않는지, 저장 탭 안 진입(마커·장소 카드)과 [현재 위치] 버튼이 그대로인지 (`FR-002`·`EC-030`·`TS-056`, [quickstart.md §5](./quickstart.md) 1-1·1-2행). T111에 의존

- [ ] T113 **홈에서 들어온 [나가기]는 홈으로 되돌린다** — **PRD를 13.0.0으로**, spec을 5.0.0으로 함께 개정했다. spec 3.0.0이 "홈 진입만 예외로 두면 규칙이 둘이 된다"는 이유로 기각했던 안인데, 그 기각의 유일한 근거가 PRD 9.0.0의 "진입 경로와 무관하게"였고 **PRD 13.0.0이 그 문구에 홈 예외를 직접 새기면서 근거가 소멸했다**. `FR-009`·`TS-037`을 재정의하고 `TS-057`·`EC-031`·`EC-032`를 신설했다.
  - **예외 조건은 둘의 AND다** — 진입 출처가 홈이고, [저장된 방](FR-025)으로 방을 바꾼 적이 없다. 방을 바꾸면 그 자리에서 소멸하며(`TS-057`) 되돌려도 살아나지 않는다(`EC-032`) — 판정하는 것은 "지금 어느 방인가"가 아니라 "바꾼 적이 있는가"다.
  - **진입 출처는 요청과 함께 실어 보낸다.** 탭 전환이 끝나면 그 사실이 어디에도 남지 않아 저장 탭이 나중에 되물을 수 없다. `PlaceDetailRequestHolder`의 `pending`이 `String?` → `PlaceDetailRequest?`(`pinId` + `PlaceDetailEntryOrigin`)가 되고, 저장 탭은 여는 순간 `returnsToHomeOnClose: Boolean` 하나로 굳혀 둔다 — 이 화면이 출처로 하는 일이 이 분기 하나뿐이라 출처를 그대로 들고 있으면 읽는 쪽마다 조건을 다시 세우게 된다.
  - **홈으로 나갈 때 `selectedRoomId`도 함께 비운다**(`EC-031`). 홈 진입이 방과 핀을 함께 세우므로, 핀만 비우면 사용자가 연 적 없는 방 상세가 저장 탭에 남아 다음 방문 때 튀어나온다. 이 결함은 [나가기] 직후 화면으로는 드러나지 않는다.
  - **탭을 옮기는 것은 셸이 한다.** `RoomListSideEffect.NavigateToHome`을 신설해 `RoomListRoute` → `roomGraph` 콜백 → `MainNavHost` 순으로 올린다 — `:feature:room`은 `MainTab`을 모른다. 상태가 아니라 SideEffect인 것은 구성 변경 때 다시 소비돼 저장 탭에 돌아올 때마다 홈으로 튕기는 것을 막기 위해서다.
  - **`:feature:home`은 바뀌지 않는다.** 덱 위치 보존은 탭 전환의 `saveState`/`restoreState`가 이미 해 준다.
  - 함께 고친 문서: [spec.md](./spec.md) `FR-009`·`TS-037`·`TS-057`·`EC-031`·`EC-032`·§5, [quickstart.md §5](./quickstart.md) 2·2-1~2-4·4행, [contracts/place-detail-entry.md §3.1·§3.2·§4](./contracts/place-detail-entry.md), [contracts/place-detail-main-contract.md §1·§2.1·§2.2·§2.3·§2.6·§2.6.1](./contracts/place-detail-main-contract.md), PRD [SCR-006] Flow A·Flow C·§5
- [ ] T114 실기기 확인 — 홈 카드로 들어가 [나가기]·뒤로가기가 홈으로 되돌리고 덱 위치가 보존되는지, 그 직후 저장 탭을 눌렀을 때 방 리스트가 열리는지(방 상세가 아니라), [저장된 방]으로 방을 바꾼 뒤에는 바뀐 방의 방 상세로 나가는지, 저장 탭 안 진입(마커·장소 카드)과 방 상세 [X]는 그대로인지 (`FR-009`·`TS-006`·`TS-037`·`TS-057`·`EC-031`·`EC-032`, [quickstart.md §5](./quickstart.md) 2·2-1~2-4행). T113에 의존

- [X] T110 실기기 확인 — 장소 상세 진입 시 선택 핀이 시트 위 영역의 중앙에 오는지, 시트 단계를 바꿀 때 지도가 한 번만(정착 시점) 따라 움직이는지, 방 상세·방 리스트에서도 시트 위 영역 기준으로 자리 잡는지 (`FR-002`, [quickstart.md §4](./quickstart.md) 3-1행). T109에 의존

**체크포인트**: [quickstart.md §4](./quickstart.md) 3-1·7~10행과 [§5](./quickstart.md) 1-1·1-2·2·2-1~2-4행, [§6](./quickstart.md) 14행, [§7](./quickstart.md) 1~2행이 통과한다 — 드래그로는 닫히지 않고, [나가기]와 뒤로가기는 저장 탭 안 진입이면 방 상세로·홈 진입이면 홈으로 나가며, 홈에서 들어와도 선택 핀이 시트 위 지도의 한가운데에 서고, 코멘트 0건 화면이 스크롤에 떨리지 않으며, 단일 방 장소에는 [저장된 방] 버튼이 아예 없으며, `Full`이 상태바까지 덮는다.

---

## Phase 17: [SYS-003] 방 선택 시트 결함 반영

**목표**: 「체크된 채 비활성」이 표시로만 있고 입력으로는 없어 이미 저장된 방이 복제 요청에 실리던 것을 막고, 문서가 비워 둔 시트 규칙(단계·비활성 표현·토스트)을 Figma에서 확정한다.

**독립 테스트**: 두 방 중 한 방에만 저장된 장소를 열어 [다른방에 공유] → 이미 저장된 카드를 눌러 보고 → 나머지 방을 골라 공유 (TS-034·TS-058·TS-033).

- [X] T115 **시트 규칙을 Figma에서 확정하고 문서에 새긴다** — spec을 **5.0.0**으로 개정했다. 유저 플로우 6의 Figma 링크에 실제 프레임 3종(`2392-128669` peek · `2542-10516` full 4개 · `2392-128693` full 4개 이상)을 더하고, 1단계의 「`Full`로 올라온다」를 **`Peek`**으로 정정했으며(근거로 삼았던 `2400-268884`는 섹션 제목 텍스트라 단계를 규정하지 않는다), `TS-058`을 신설했다. §5에 확정 항목 3건(시트 단계 · 「체크된 채 비활성」의 시각 표현 · 토스트 문구·노출 시간)을 더했다. §4 가정의 「누르기 전에 중복 상태를 미리 조회하지 않는다」는 `FR-023`의 진입 시 조회와 어긋나 있어 「버튼의 활성 여부가 저장 상태에 좌우되지 않는다」로 정정했다. 계약에는 [§3.4](./contracts/place-detail-main-contract.md)를 신설해 `ShareSheetUiState`·이중 차단·시각 표현·치수·토스트를 모았다. 함께 고친 문서: [spec.md](./spec.md), [contracts/place-detail-main-contract.md §3.4](./contracts/place-detail-main-contract.md)
- [X] T116 `:core:design-system`에 「체크된 채 비활성」 슬롯을 연다 — `MinoCheckbox`의 `enabled = false`가 입력만 막던 것을 **입력 차단 + 43% 불투명도**로 바꾸고(`CheckboxTokens.DISABLED_ALPHA`), `MinoRoomCheckBoxCard`·`RoomCardRow`에 `enabled`를 더해 카드 본문 탭까지 함께 잠근다. **비활성용 색을 새로 두지 않았다** — Figma `2862-175313`의 `Checkbox` 노드가 `opacity 43%`이고 그 안의 색 토큰은 체크 상태와 같아서, `MinoCheckboxColors`에 `enabled` 축이 늘지 않는다. `MinoCheckbox`·`MinoCheckboxColors` KDoc의 「Figma에 비활성 상태의 색이 정의되어 있지 않다」는 사실과 어긋나 함께 고쳤다 — 색이 아니라 불투명도로 정의돼 있었다. [room-picker-sheet-ui.md §2.3](../shared-link-receiver/contracts/room-picker-sheet-ui.md)이 「`enabled = false`는 [SYS-003]의 '체크된 채 비활성' 규칙이 쓸 슬롯」이라며 미리 열어 둔 자리다. `:feature:sharereceiver`는 기본값(`true`)이라 그대로다. T115에 의존
- [X] T117 **이미 저장된 방을 두 곳에서 막는다** (FR-018·FR-022·EC-019·TS-034·TS-058) — `RoomShareSheet`가 `enabled = !room.hasPlace`를 넘겨 카드와 체크박스를 함께 잠그고, `PlaceDetailViewModel.toggleShareRoom`이 `hasPlace`인 방의 토글을 무시한다. **T047이 "T062까지 붙이지 않는다"로 미뤄 둔 작업이며, T062가 `checked` 판정만 옮기고 비활성을 빠뜨린 채 닫혀 있었다.** 그 사이 화면은 체크만 그리고 탭은 열어 두어, 이미 저장된 방이 `selectedRoomIds`에 들어가 `duplicatePin`에 실리고 서버가 `409`로 거절했다([contracts/place-api.md §3](./contracts/place-api.md)). ViewModel 쪽 방어를 함께 두는 것은 UI가 막는다는 전제를 서버 응답으로 확인하지 않기 위해서다. `RoomShareSheet` KDoc의 「`hasPlace`가 이번 라운드에 전부 `false`」도 T062 이후 거짓이 되어 함께 지웠고, 모든 방이 저장된 상태의 프리뷰를 더했다. T116에 의존
- [X] T118 공유 완료 토스트를 두 진입점이 나눠 쓰게 한다 — `ShareCompletedToast.kt`(`:feature:room` 루트)에 문구와 3초 노출을 모으고 `RoomDetailRoute`·`PlaceDetailRoute`가 함께 부른다. 문구는 Figma `2542-125820` 실측인 **`공유가 완료됐습니다.`**로 통일했다(장소 상세의 `공유가 완료되었습니다.`는 근거 없이 스펙이 만든 변형이었다). 장소 상세는 지속 시간을 지정하지 않아 스낵바 기본값 4초로 떠 있었다 — `SnackbarDuration.Short`가 4초라 그 값으로는 3초를 표현할 수 없어, 방 상세가 쓰던 「띄우고 3초 뒤 거둔다」 패턴을 공용 헬퍼로 옮겼다. 문자열 리소스는 화면이 아니라 시트에 속하므로 `placedetail_share_completed` → `roomshare_completed`로 옮겼다. T115에 의존
- [ ] T119 실기기 확인 — 두 방 중 한 방에만 저장된 장소에서 [다른방에 공유]를 열었을 때 이미 저장된 카드의 체크박스만 흐리고 방 이름·썸네일은 온전한지, 그 카드를 눌러도 아무 일이 없는지, 나머지 방을 골라 공유하면 실패 없이 완료 토스트가 3초 뒤 사라지는지, 방 상세에서 연 같은 시트의 토스트도 같은 문구인지 (`FR-018`·`FR-022`·`EC-019`·`TS-033`·`TS-034`·`TS-058`). T117·T118에 의존

**체크포인트**: 이미 저장된 방이 복제 요청에 실리지 않고, 두 진입점의 완료 토스트가 같은 문구로 3초 떠 있다가 사라진다.

**이 Phase가 닫지 않은 것은 Phase 18이 이어받는다** — 시트 단계·[새 방 만들기]·두 벌로 갈라진 구현.

---

## Phase 18: [SYS-003] 방 선택 시트를 한 벌로 합친다

**목표**: 방 상세와 장소 상세가 각자 만든 두 시트를 하나로 합쳐, 진입점이 달라도 Figma대로 같게 동작하게 한다.

**독립 테스트**: 같은 장소를 두 진입점(방 상세 장소 카드 [⋮] · 장소 상세 액션 행)에서 열어 시트가 같은지 확인하고, 모든 방에 저장된 장소에서 [새 방 만들기]로 빠져나간다 (place-detail `TS-034`·`TS-035`·`EC-019`·`EC-020`, room-detail `EC-004`).

- [X] T120 **두 벌이 각각 다른 절반씩만 Figma를 따르고 있었다** — 합치기 전의 상태를 문서에 새겼다. `detail/RoomSelectSheet`는 `Peek`/`Full` 2단·[새 방 만들기]·체크+비활성을 갖고도 카드·체크박스를 자체 조립했고, `placedetail/RoomShareSheet`는 디자인 시스템 컴포넌트·딤·스크롤바를 갖고도 676dp 1단에 [새 방 만들기]가 없었다. spec을 고칠 것은 없다 — 두 spec 모두 이미 같은 것을 요구하고 있었고 구현만 갈려 있었다. 개정한 것은 계약이다: [contracts/place-detail-main-contract.md §3.4.3~§3.4.7](./contracts/place-detail-main-contract.md)에 치수·단계 전환·소유 위치·두 진입점·토스트를 모으고, §4·§5에 [새 방 만들기] 인텐트 2종과 SideEffect 1종을 더했다
- [X] T121 **시트의 자리는 `:core:common:ui`가 아니라 `:feature:room` 모듈 루트다** — 두 사용처가 같은 feature 모듈 안에 있어 [`component-asset-placement.md` §1.2](../../conventions/component-asset-placement.md)의 「둘 이상의 feature」를 만족하지 않는다. 그런데 [`feature-module.md`](../../architecture/feature-module.md)의 패키지 구조에는 **한 feature 안에서 두 화면이 공유하는 컴포저블의 자리가 비어 있었다** — `<screen>/component/`는 한 화면의 것이고, 모듈 루트는 그래프 레벨 파일만 두게 돼 있었다. `:feature:main`이 이미 모듈 루트 `component/`를 쓰고 있어 그 선례를 규칙으로 올렸다(「모듈 루트 `component/`」 절 신설). 시트가 받는 UiModel도 같은 자리에 둔다 — 어느 화면의 `model/`에 두면 다른 화면이 남의 화면 패키지를 참조한다
- [X] T122 통합 시트 `component/RoomShareSheet.kt` + `component/RoomShareItem.kt` 신설 — `Peek`(목록 240)/`Full`(416, 방 5개 이상 448) 2단, 헤더에 [새 방 만들기] 행, 이미 담긴 방은 체크+비활성. **시트 높이가 `목록 영역 + 260dp` 하나로 나온다** — Figma 세 프레임의 차이가 목록 영역뿐이라 위아래 고정 영역(헤더 146 · 구분선 12 · 액션 102)이 상수다. 끄는 것은 손잡이만 받고(본문이 받으면 목록 스크롤과 손짓이 갈린다), `Peek`에서 아래로 끌면 닫힘을 올린다. `placedetail/component/SheetParts.kt`도 두 화면이 함께 보게 되어 `component/`로 옮겼다. T121에 의존
- [X] T123 장소 상세를 통합 시트로 갈아 끼우고 [새 방 만들기]를 배선한다 — `OnShareCreateRoomClick` → `OpenCreateRoomForm` → `PlaceDetailRoute`의 `createRoomResultLauncher` → `OnShareRoomFormResult`. 돌아오면 목록을 다시 받아 새 방을 **선택된 상태로** 얹는다(`EC-020`) — 방 생성 화면이 돌려주는 것이 방 id 하나뿐이라 이름·썸네일 없이는 카드를 세울 수 없다. `RoomFormLauncher`를 ViewModel에 주입해 Route가 쓰는 것은 `RoomDetailViewModel.roomFormLauncher`와 같은 형태다(Composable이 Hilt 주입을 못 받는다). T122에 의존
- [X] T124 방 상세를 통합 시트로 갈아 끼우고 **「이미 저장된 방」을 실제로 판정한다**(room-detail `EC-004`) — 목록 출처를 `observeMyRooms()`에서 `GetRoomPickerRoomsUseCase(place.placeId)`로 바꿨다. 그전까지 `alreadySavedRoomIds`가 언제나 빈 집합이었고 그 자리 주석이 「도메인에 판정할 필드가 없다」를 근거로 들고 있었는데, `RoomSummary.hasPlace`가 생기면서 사실이 아니게 됐다. **묻는 키가 장소라 `Place`에 `placeId`를 더했다**(서버 `Pin.place.id`) — 기존 `Place.id`는 `Pin.id`라 방마다 값이 달라 이 질문을 할 수 없다. 선택 상태도 화면 로컬에서 `RoomDetailUiState`로 올렸다(보내는 목록을 만드는 곳과 든 곳이 갈려 있으면 확정 시점에만 넘겨야 한다). 함께 고친 문서: [room-detail/spec.md §5](../room-detail/spec.md), [room-detail/data-model.md §1](../room-detail/data-model.md). T122에 의존
- [X] T125 **같은 엔드포인트를 가리키던 두 갈래를 하나로 모은다** — `RoomPlacesRepository.sharePlaces`를 걷어내고 방 상세도 `PlaceRepository.duplicatePin`을 쓴다. 그 인터페이스 KDoc이 「room-detail이 `PlaceRepository` 쪽으로 갈아타면 이 메서드는 지워질 수 있다」고 예고해 둔 정리이며, 딸려서 `PlaceRemoteDataSource.duplicatePin`·`PlaceApiService.duplicatePin`도 사라졌다(`PinApiService` 쪽 한 벌만 남는다). T124에 의존
- [X] T126 걷어낸 것 정리 — `detail/component/RoomSelectSheet.kt`·`RoomSelectSheetPreview.kt`, `placedetail/component/RoomShareSheet.kt`, `RoomDetailScreen`의 `RoomDetailRoomSelectOverlay`, `RoomDetailUiState.showRoomSelectSheet`·`myRooms`. 문자열도 시트 소유로 옮겼다(`placedetail_share_confirm` → `roomshare_confirm`, `placedetail_room_place_count` → `room_place_count`, 신설 `roomshare_create_room`)
- [ ] T127 실기기 확인 — 두 진입점에서 연 시트가 같은 모양인지(`Peek`으로 올라오고, 위로 끌면 펼쳐지고, `Peek`에서 아래로 끌면 닫히는지), 이미 담긴 방이 양쪽 모두에서 체크+비활성인지, [새 방 만들기]로 만든 방이 시트에 선택된 채 나타나는지, 방 상세의 공유가 예전처럼 되는지 (place-detail `TS-032`~`TS-035`·`TS-058`·`EC-019`~`EC-021`, room-detail `TS-007`·`EC-004`). T123·T124·T125에 의존

**체크포인트**: [SYS-003] 시트가 한 벌이고, 두 진입점이 같은 시트·같은 목록 출처·같은 복제 API를 쓴다.

---

## 완료된 UI 라운드 (plan 1.1.0 기준) — 기록

**아래 Phase 1~9는 지난 라운드에서 이미 끝낸 작업이다.** 체크 상태를 그대로 보존한다.

**경로를 그대로 읽지 않는다.** 여기 적힌 `feature/placedetail/...` 경로는 작업 당시의 것이고, 그 파일들은 Phase 11(T073~T077)이 `feature/room/.../placedetail/`로 옮긴다. 산출물이 삭제되는 작업 20건은 이 목록에 없고 「폐기된 작업」에 있다.

## Phase 1: 셋업 (모듈 골격)

**목적**: 신규 진입형 모듈을 빌드에 올리고, 다른 feature가 이 화면을 열 계약을 세운다.

> **이 단계의 작업(T001~T005)은 전부 폐기되었다.** plan 2.0.0이 모듈을 해체했다 — 「폐기된 작업」 참조.


**체크포인트**: `./gradlew :feature:placedetail:assembleQaDebug`가 빈 모듈로 통과한다.

---

## Phase 2: 기반 작업 (모든 스토리 공통)

**목적**: 도메인 타입·Fake 데이터 원천·진입점 골격·MVI 계약을 세운다. 각 작업이 어느 스토리에 쓰이는지 줄에 드러낸다.

### 도메인 모델과 Repository 계약

- [X] T007 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/PlaceDetail.kt` 생성 — `PlaceRegistrant` 중첩 포함 ([data-model.md §1](./data-model.md)). US1·US3·US5·US6 전부가 쓴다
- [X] T008 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/PlaceComment.kt` 생성 — `PlaceCommentAuthor`·`PlaceCommentPage` 포함 ([data-model.md §2](./data-model.md)). US4가 쓴다
- [X] T009 `core/domain/src/main/kotlin/team/mino/core/domain/repository/PlaceRepository.kt` 생성 — `getPlaceDetail`·`recordAccess`·`duplicatePin` 3종. **`recordAccess`가 예외를 던지지 않는다는 규약을 KDoc에 적는다** ([contracts/place-repository.md §1](./contracts/place-repository.md)). T007에 의존. US1·US6가 쓴다
- [X] T010 `core/domain/src/main/kotlin/team/mino/core/domain/repository/PlaceCommentRepository.kt` 생성 — `getComments`·`addComment`·`deleteComment` ([contracts/place-repository.md §2](./contracts/place-repository.md)). T008에 의존. US4가 쓴다

### Fake 데이터 원천 (이번 라운드 한정)


### 진입점 골격 (진입형 feature)


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

- [X] T026 [P] [US1] `.../component/PlaceDetailSheet.kt` 생성 — `Half` 369dp 고정 앵커와 시트 골격 (FR-001·SC-002·UX-001 — 시트가 화면을 다 덮지 않아 지도와 선택 핀이 계속 보인다). `Full` 승격은 US2에서 붙인다
- [X] T027 [US1] `.../component/PlaceDetailHeader.kt` 생성 — 확장형 헤더: 등록자 아바타(`MinoProfileAvatar`, 없으면 기본 아바타 EC-004) + 라벨 + [나가기]가 한 줄, 그 아래 장소명·주소 각 한 줄에 `...` 생략 (FR-003·FR-004). T026에 의존
- [X] T028 [P] [US1] `.../component/PlaceActionRow.kt` 생성 — `장소보기`·`원문보기`·`다른방에 공유` 3종을 한 행에 두고 가로 스크롤, `장소보기`는 강조 스타일 (FR-006·UX-004). 클릭 배선은 US5·US6에서 붙인다
- [X] T031 [P] [US1] `.../screen/PlaceDetailScreenPreview.kt` 생성 — 로딩(`place == null`)·기본·긴 장소명/주소·등록자 없음 상태

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

---

---

## 폐기된 작업

**완료(`[X]`)됐으나 plan 2.0.0의 구조 전환으로 산출물이 사라지는 작업 20건이다.** 이미 코드가 들어갔다는 뜻이므로 지우지 않고 정리 범위를 함께 남긴다. **이 번호들은 재사용하지 않는다.**

### 진입형 모듈 골격 — 모듈째 삭제 (T087이 정리)

| ID | 원래 작업 | 폐기 사유 · 정리 범위 |
|---|---|---|
| T001 | `settings.gradle.kts`에 `include(":feature:placedetail")` | 편입으로 모듈이 사라진다. **정리**: 그 줄을 지운다 |
| T002 | `feature/placedetail/build.gradle.kts` 작성 | 같음. **정리**: 파일 삭제 |
| T003 | `AndroidManifest.xml`에 `PlaceDetailActivity` 등록 | Activity 폐기. **정리**: 파일 삭제 |
| T004 | `app/build.gradle.kts`에 `implementation(projects.feature.placedetail)` | 같음. **정리**: 그 줄을 지운다 |
| T015 | `PlaceDetailDestinations.kt` (`PlaceDetailMain(pinId)` Route) | 별도 Route가 아니라 `RoomMain`의 로컬 상태가 된다. **정리**: 파일 삭제 |
| T016 | `PlaceDetailActivity.kt` | 진입형 폐기. **정리**: 파일 삭제. 단 외부 지도·원문 실행부는 **T085가 `MainActivity`로 이식한다** |
| T017 | `PlaceDetailShell.kt` | 탭 셸(`MainShell`)이 대신한다. **정리**: 파일 삭제 |
| T018 | `PlaceDetailNavHost.kt` | 목적지가 없어졌다. **정리**: 파일 삭제 |
| T019 | `PlaceDetailLauncherImpl.kt`·`PlaceDetailNavigationModule.kt` | Launcher 계약 폐기. **정리**: 파일 삭제 |

### 진입 계약 — 요청 홀더로 교체 (T071·T072·T084가 정리)

| ID | 원래 작업 | 폐기 사유 · 정리 범위 |
|---|---|---|
| T005 | `PlaceDetailLauncher.kt` 신설과 `EXTRA_PLACE_DETAIL_PIN_ID` 추가 | 탭 간 진입을 Route 인자로 나를 수 없어 홀더로 바꾼다([research.md D18](./research.md)). **정리**: `PlaceDetailLauncher.kt` 삭제, `ExtraTag.kt`에서 상수 제거 |
| T070 | `MainActivity`의 `onNavigateToPlaceDetail`을 `PlaceDetailLauncher` 호출로 교체 | 같음. **정리**: `holder.request(pinId)` + 탭 전환으로 교체(T084) |
| T030 | `PlaceDetailActivity`에 `Exit` SideEffect → `finish()` 배선 | [나가기]가 화면 종료가 아니라 `selectedPinId = null`이 된다. **정리**: Activity와 함께 삭제. 대체는 T081 |

### Fake 데이터 — 실 API로 교체 (Phase 13이 정리)

| ID | 원래 작업 | 폐기 사유 · 정리 범위 |
|---|---|---|
| T011 | `FakePlaceDetailData.kt` | 실 API 연결([research.md D23](./research.md)). **정리**: 모듈 삭제로 함께 사라진다. **단 Preview가 이 원천을 쓰고 있으므로**, T073~T076 이식 시 Preview용 샘플을 `placedetail/screen/…Preview.kt` 안으로 옮기거나 `:feature:room`의 기존 Preview 관례에 맞춘다 |
| T012 | `FakePlaceRepository.kt` | 같음. **정리**: 삭제 |
| T013 | `FakePlaceCommentRepository.kt` | 같음. **정리**: 삭제 |
| T014 | `PlaceDetailFakeDataModule.kt` | 같음. **정리**: 삭제. 바인딩은 `:core:data`가 갖는다(T060) |

> **T063(Fake 삭제 작업)은 미착수 상태로 사라졌다.** 모듈째 삭제(T087)에 흡수되어 별도 작업이 필요 없다. 규칙에 따라 미착수 작업은 폐기 기록 없이 지웠으나, 번호는 재사용하지 않는다.

### 지도·컨트롤 이중화 — 한 벌로 통합 (T074·T078·T086이 정리)

| ID | 원래 작업 | 폐기 사유 · 정리 범위 |
|---|---|---|
| T025 | `PlaceDetailMap.kt` (`MinoMap` + 선택 핀) | 지도를 `RoomListMap` 한 벌로 합친다([research.md D25](./research.md)). **정리**: 파일 삭제. 선택 핀은 T078이 `RoomListMap`에 흡수 |
| T065 | `CurrentLocationButton.kt`·`PlaceMapControls.kt` 생성 | `CurrentLocationButton`은 `RoomList`/`RoomDetail`이 이미 각자 갖고 있어 중복이다. **정리**: `CurrentLocationButton.kt` 삭제, `PlaceMapControls.kt`는 T074가 이식하고 T086이 `RoomListViewModel`에 연결 |

### 구현 보류 산출물 — 실제 구현으로 교체 (Phase 14가 정리)

| ID | 원래 작업 | 폐기 사유 · 정리 범위 |
|---|---|---|
| T006 | `PlaceLabel.kt` 생성 (4종 enum) | spec 4.0.0이 장소 상세의 라벨 노출을 없앴다([research.md D21](./research.md)). **정리**: T088이 파일과 `PlaceDetail.label`을 삭제 |
| T029 | `SavedRoomsButton.kt` — 항상 비활성으로 그린다 | 서버가 `matchedPinId`를 신설해 전환을 구현할 수 있게 됐다([research.md D20](./research.md)). **정리**: 파일 삭제. 대체는 T093·T094 |

**폐기가 만든 부채는 없다.** 20건 모두 이번 라운드의 작업(T071~T098)이 정리 범위를 흡수하며, 남는 잔여물은 T098이 확인한다.

---

## 미결 사항

작업으로 만들 근거가 없거나, spec 요구사항이 이번 구현에서 닫히지 않는 지점. `/mino-analyze`가 검증할 대상이다.

**plan 1.1.0의 미결 10건 중 6건이 닫힌다.** 아래는 이번 라운드 이후에도 남는 것과, 이번에 새로 생긴 것이다.

### 이번 라운드에서 닫히는 것 (기록)

| 지난 미결 | 닫는 작업 |
|---|---|
| 1. FR-009 [나가기] 목적지 — 홈·알림 진입이 spec과 어긋남 | **T079·T081·T082·T084** — 상태 전이는 T079(`selectedPinId = null`), 시트 노출은 T081, 진입 시 목적지 확정은 T082·T084다. 편입으로 경로 무관 복귀가 성립하며 EC-001도 `pinId`→`roomId` 해석으로 충족 |
| 2. FR-023~025 저장된 방 전환 — 서버가 `pinId`를 안 줌 | **Phase 14 (T092~T095)** — `matchedPinId` 신설 |
| 3. FR-018 이미 저장된 방 판정 — `hasPlace` 없음 | **T061·T062** |
| 4. FR-005 장소분류 라벨 — 서버가 값을 안 줌 | **T088·T089** — 요구사항 자체가 소멸 |
| 5. [현재 위치] 버튼의 동작 | **T086** — 지도가 한 벌이 되며 `RoomListViewModel`이 소유 |
| 9. `EXTRA_PLACE_DETAIL_PIN_ID` 누락 시 처리 | **T072** — 상수와 함께 사라진다 |

### 남는 것

1. **[SYS-003] 방 선택 시트 내부 규칙** — 시트 높이 단계(`Peek` 500dp / `Full` 676dp / 방 5개 이상 708dp)·카드 구성·비활성 시각 표현이 [SYS-003] spec 부재로 `[TBD]`다([research.md D13](./research.md)). **현재 `RoomShareSheet`는 `676.dp` 단일 앵커(`OPEN`/`GONE`)로 3단 중 하나만 구현돼 있다 — 의도적 축소가 아니라 미구현이다.** `[SYS-003]` spec이 서기 전에는 닫히지 않는다.
   - **이 항목은 「저장된 방 시트」와 무관하다.** 그쪽 치수(442dp·312dp)는 FR-024가 직접 못박았고 이 스펙이 소유한다 — T093은 `RoomShareSheet`를 따라가지 않는다.
2. **TS-035·EC-020 (시트에서 새 방 만들고 복귀)** — [SYS-001] 호출이 [SYS-003] 시트 소관이라 이번 범위에 없다.
3. **외부 지도 앱 선택 정책** — [spec.md §3.2](./spec.md)가 비목표로 둔 `[TBD]`다. T044가 `geo:` 우선으로 정하고 근거를 남겼으며, T085가 그 판단을 그대로 이식한다. spec이 이 결정을 승인한 적은 없다.
4. **아바타 색 enum 불일치** — 핀 상세의 `createdBy.avatar.color`에는 enum 제약이 없고 코멘트의 `author.avatar.color`에는 있다([contracts/place-api.md §1.3](./contracts/place-api.md)). **서버팀 협의 항목**이며, T058이 모르는 값을 `null`로 떨어뜨리는 것으로 버틴다.
5. **코멘트 작성 시각의 기준 시계** — 기기 시각과 서버 `createdAt` 중 무엇을 기준으로 경과를 재는지 [spec.md §3.2](./spec.md)가 정의하지 않은 채 위임했고, 그 위임은 그대로 남는다([research.md D22](./research.md)). **plan 2.1.0이 이 항목의 절반은 닫았다** — 기기 시각을 어떻게 얻는지가 주입한 `Clock`으로 정해졌고(T099·T100, [research.md D26](./research.md)), 서버가 기준 시각을 내려주게 되면 `commentsObservedAt`의 공급원만 갈면 되어 화면 쪽 판정 함수(T091)는 손대지 않는다. 남은 위험은 EC-029(음수 흡수)가 덮는다.
6. **「경과일 초기화 확인」이 홈 진입에서 2회 나간다** — plan 1.1.0에서 발견된 그대로 남는다. 홈이 `HomeDeckRepository.recordPlaceOpened`를(home spec FR-007·TS-034), 상세가 `PlaceRepository.recordAccess`를(FR-026) 각각 부른다. **편입해도 두 호출은 그대로라 중복이 유지된다.** FR-026이 이 기록의 소유자이므로 홈 쪽을 걷어내는 것이 맞으나 home spec 개정이 선행되어야 한다. `HomeViewModel.openPlaceDetail`의 KDoc에 명시돼 있다.

### 새로 생긴 것

7. **`restoreState`와 요청 홀더의 상호작용** — T084가 탭 전환과 홀더 적재를 함께 하는데, 저장 탭이 이미 열려 있던 상태로 복원되는 경우 `RoomListViewModel`이 구독을 유지하고 있는지 확인이 필요하다. [quickstart.md §5-4](./quickstart.md)가 이 지점을 본다.
8. **`:feature:room` 모듈 비대화** — 화면 3개(리스트·방 상세·장소 상세)를 한 모듈이 갖는다. plan 2.0.0 「복잡도 추적」이 감수한 비용으로 기록했으나, 이후 화면이 더 붙으면 재검토가 필요하다.

---

## 구현 작업이 없는 요구사항

아래는 작업 목록에 대응 항목이 없다. **누락이 아니라 설계상 구현할 것이 없는 경우**이므로 근거를 남긴다.

| 요구사항 | 왜 작업이 없는가 | 확인 방법 |
|---|---|---|
| FR-019 (코멘트가 (장소, 방) 단위에 귀속) | 서버가 코멘트를 `pinId` 경로에 매단다. 핀이 곧 (장소, 방)이므로 T010의 계약을 그대로 쓰는 것만으로 성립한다([research.md D4](./research.md)) | TS-024 — 같은 장소의 다른 방 `pinId`로 열면 코멘트가 다르다 |
| FR-027 (「지금 보고 있는 방」 초기값 결정) | `pinId` 안에 내포된다([research.md D4](./research.md)). 화면이 방을 따로 고르는 코드가 없다 | TS-002·TS-007 — 마커 색과 코멘트가 그 핀의 방을 따른다 |
| EC-002 (중복 마커 클릭도 다른 마커와 동일하게 `Half`) | **호출자**가 지키는 성질이다. 장소 상세는 `pinId` 하나로 열린다. 편입 후 그 호출자는 `RoomListViewModel`의 `OnPlaceSelected`이며 방을 먼저 고르게 하는 분기가 없다 | [quickstart.md §4-2](./quickstart.md) |
| SC-006 (90%가 첫 시도에 의도한 액션 완료) | 사용자 조사 지표라 코드로 만들 것이 없다 | 출시 후 측정 |

> **FR-020(바텀 네비게이션 비노출)은 더 이상 이 표에 없다.** 진입형일 때는 "바텀바가 애초에 없어서" 구현이 필요 없었으나, 편입 후에는 탭 셸 안이라 **T081이 실제로 숨겨야 한다**([research.md D19](./research.md)).

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **Phase 11 구조 전환**: 먼저 한다. 나머지 전부가 이 위에 선다. 내부 순서는 진입 계약(T071·T072) → 화면 이식(T073~T077) → 저장 탭 편입(T078~T083) → 셸·모듈 정리(T084~T087)
- **Phase 12 spec 4.0.0**: 이식(T074·T075)이 끝나야 시작. 예외는 **T099(DI 모듈 신설)로, 새 파일 하나뿐이라 이식과 무관하게 언제든 할 수 있다.** Phase 13과 병렬 가능 — 건드리는 파일이 겹치지 않는다. 단계 내부 순서는 T090 → T099 → T100 → T091이다(시각 표기 갈래)
- **Phase 13 API 연결**: `:core:data` 작업(T054~T060)은 Phase 11과 **완전히 병렬**이다. T061·T062만 이식 이후다
- **Phase 14 저장된 방**: T062(방 목록 실연동) 이후. Phase 13에 의존하는 유일한 스토리다
- **Phase 15 검증**: 전부 끝난 뒤. 단 T096(회귀)은 T081 직후에 한 번 먼저 보는 편이 낫다

### 임계 경로

```
T071 → T084 ┐
T073 → T074 → T075 → T076 → T080 → T081 → T096
                              ↑
T078 → T079 ──────────────────┘

(별도 갈래) T054~T060 → T061 → T062 → T092 → T093 → T094 → T095

(시각 표기 갈래) T090 ┐
             T099 ┴→ T100 → T091
```

**가장 긴 사슬은 이식 → 편입 → 회귀 확인이다.** API 갈래는 그와 무관하게 먼저 끝낼 수 있다.

### 사용자 스토리 간 의존성

- **US1~US6**: 화면 자체는 지난 라운드에 완성됐다. 이번 라운드에서는 **이식으로 한꺼번에 옮겨지므로** 스토리별 순서가 없다. 예외는 US1의 헤더(T089)와 US4의 코멘트 시각(T090·T099·T100·T091) — spec 4.0.0 반영분과 plan 2.1.0이 더한 기준 시각 배선이다
- **US7 (저장된 방 전환)**: 신규. T062에 의존하며 다른 스토리와 독립이다

### 병렬 처리 기회

- **가장 큰 기회**: Phase 13의 `:core:data` 작업(T054~T060)과 Phase 11의 이식·편입이 **파일이 전혀 겹치지 않아 완전 병렬**이다. 두 사람이 붙으면 라운드가 절반으로 줄어든다
- Phase 11: T071·T072가 병렬, T073·T077·T078이 병렬
- Phase 12: T088·T090·T099가 병렬(도메인 파일 둘과 신규 DI 파일 하나로 서로 겹치지 않는다)
- Phase 13: T054·T056이 병렬, T058·T059가 병렬
- **파일 충돌 주의**: `RoomListRoute.kt`(T081→T082→T085→T095), `RoomListScreen.kt`(T080), `RoomListViewModel`(T079→T082→T086), `PlaceDetailViewModel`(T075→T100→T062→T092), `PlaceDetailHeader.kt`(T089), `PlaceMapControls.kt`(T074→T086→T094)는 같은 파일을 여러 작업이 이어서 고치므로 병렬이 아니다

---

## 병렬 실행 예시: Phase 11 화면 이식

```bash
# 서로 다른 파일 계열이라 함께 옮길 수 있다:
Task: "main/model/ 4종을 feature/room/.../placedetail/model/로 이식하고 RoomColorPalette를 RoomColorMapping에 합친다"
Task: "feature/placedetail/src/main/res/values/strings.xml을 feature/room 리소스로 이식"
Task: "MapPinUiModel에 selected 추가하고 RoomListMap의 PlacePin에 반영"
```

## 병렬 실행 예시: 두 갈래 동시 진행

```bash
# 갈래 A — 구조 전환 (feature 계층)
Task: "Phase 11: :feature:placedetail을 :feature:room으로 편입"

# 갈래 B — API 연결 (:core:data 계층, 파일이 겹치지 않는다)
Task: "core/data/.../dto/response/에 PinDetailResponse·CommentResponse 생성"
Task: "core/data/.../service/CommentApiService.kt 신설"
```

---

## 구현 전략

### 먼저 구조를 세운다 (Phase 11까지)

1. 진입 계약 교체(T071·T072) → 홀더가 생기고 Launcher가 사라진다
2. 화면 이식(T073~T077) → 파일이 `:feature:room` 아래로 옮겨간다
3. 저장 탭 편입(T078~T083) → 세 갈래 분기와 지도 공유가 선다
4. 셸·모듈 정리(T084~T087) → 진입형 흔적이 사라진다
5. **중단하고 검증**: [quickstart.md §4·§5·§10](./quickstart.md) 수행. **여기서 지도가 깜빡이면 편입이 덜 된 것이다**
6. 이 시점의 산출물이 "홈에서 눌러 들어가 저장 탭 방 상세로 나온다" — 이번 라운드의 핵심 성과다

### 점진적 전달

1. Phase 11 → 구조 전환 (핵심)
2. Phase 12 → spec 4.0.0 반영 (헤더 닉네임·코멘트 시각)
3. Phase 13 → 실 데이터로 교체
4. Phase 14 → 저장된 방 전환 (마지막 미구현 유저 플로우가 닫힌다)
5. Phase 15 → 회귀·게이트

### 팀 병렬 전략

- **개발자 A**: Phase 11 전체 (이식·편입 — 한 사람이 잇는 편이 낫다. 파일 충돌이 이 갈래에 몰려 있다)
- **개발자 B**: Phase 13의 T054~T060 (`:core:data` — A와 완전히 독립)
- 둘이 만나는 지점은 T061·T062. 그 뒤 B가 Phase 14를 이어받는다
- Phase 12는 A의 T075 이후 누구든

---

## 참고 사항

- 피해야 할 것: 모호한 작업, 동일 파일 충돌(위 「파일 충돌 주의」 참고), **이식 중 로직을 함께 고치는 것** — 옮기는 커밋과 고치는 커밋을 분리해야 회귀 원인을 좁힐 수 있다
- 커밋 단위는 [`docs/conventions/commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다. 이식(T073~T077)은 파일 이동만 담은 커밋으로 두면 diff가 읽힌다
- **T087(모듈 삭제)은 반드시 마지막이다.** 먼저 지우면 이식이 끝나지 않은 파일이 함께 사라진다
- 폐기된 20건의 번호(T001~T006·T011~T019·T025·T029·T030·T065·T070)와 삭제된 T063은 **재사용하지 않는다**
