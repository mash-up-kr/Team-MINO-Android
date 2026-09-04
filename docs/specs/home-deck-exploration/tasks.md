# 작업 목록: [SCR-003] 홈 탭

**대상 스펙 경로**: `docs/specs/home-deck-exploration`

**기준 plan 버전**: 3.0.0

**최초 작성일**: 2026-08-28

**최종 수정일**: 2026-09-03

**사전 조건**: [plan.md](./plan.md) 3.0.0 (필수), [spec.md](./spec.md) 4.0.0 (사용자 스토리), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/)

**테스트**: 포함한다. plan 기술 컨텍스트가 `:core:domain` JVM 단위 테스트(전환 규칙)와 `:feature:home` ViewModel 테스트(화면 상태)를 설계에 못 박았고, [quickstart.md](./quickstart.md) §3이 검증 대상을 TS·EC ID로 지목한다.

**구성 방식**: spec §1의 유저 플로우 5개를 사용자 스토리로 삼는다. US3(자동 전환)이 이 기능의 핵심이다.

> ## 3.0.0 개정분 (2026-09-03)
>
> **이 기능은 이미 머지되어 동작한다.** T001~T073이 spec 3.0.0·plan 2.0.0 기준으로 구현을 끝냈고, 이번 개정은 그 위에 **T074~T103**을 얹어 두 가지를 갈아 끼운다.
>
> 1. **spec 4.0.0의 탐색 축 반전** — 「한 방의 세 덱 → 다음 방」이 「한 정렬로 모든 방 → 다음 정렬」로 뒤집혔고, 자동·수동 규칙이 갈렸다(FR-011·012·024·025).
> 2. **시안 개정** — 방 캐릭터가 방 색별 12 variant로, 완료 안내 일러스트·문구가, 툴팁 위치가 바뀌었다.
>
> **기존 작업의 ID와 체크 상태는 손대지 않았다.** T010·T049·T051처럼 이번에 동작이 바뀌는 자리도 그 작업 자체는 *그때 그렇게 만들었다*는 기록이라 그대로 두고, 바꾸는 일을 새 ID로 세웠다. 그래서 **폐기된 작업은 없다** — 지워지는 산출물이 아니라 내용이 바뀌는 산출물이기 때문이다.
>
> **T095는 잠긴 작업이다.** 완료 안내 문구는 spec FR-014·PRD Flow E 개정이 머지된 뒤에만 착수한다([plan.md](./plan.md) §복잡도 추적 #1).

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. **한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.**
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: US1~US5 (spec §1 유저 플로우 1~5에 대응)
- 설명에는 정확한 파일 경로를 포함한다

## 경로 규칙

Android 다중 모듈이다. 모듈 경계와 파일 배치는 [plan.md](./plan.md) §프로젝트 구조를 단일 출처로 따른다.

- `core/domain/src/main/kotlin/team/mino/core/domain/`
- `core/data/src/main/java/team/mino/core/data/`
- `feature/home/src/main/java/team/mino/feature/home/`

---

## Phase 1: 셋업 (공통 인프라)

**목적**: 스텁 상태인 `:feature:home`을 이 기능이 들어갈 수 있는 모양으로 만든다

- [X] T001 `feature/home/build.gradle.kts`에 의존성 추가 — `:core:domain`·`:core:common:ui`·`:core:design-system`·`:core:map`(GeoPoint)·`:core:navigation`
- [X] T002 `feature/home/src/main/java/team/mino/feature/home/main/vm/HomeUiState.kt`의 플레이스홀더 `title: String` 제거 — 이후 T021이 실제 필드를 채운다

**체크포인트**: 모듈이 이 기능의 의존성을 갖고 빌드된다

---

## Phase 2: 기반 작업 (공통 인프라)

**목적**: 다섯 스토리가 공통으로 쓰는 도메인·데이터 계층. **각 작업 줄에 어느 스토리가 쓰는지 적었다.**

### 2-1. 도메인 모델 ([data-model.md](./data-model.md) §1)

- [X] T003 [P] `core/domain/.../model/DeckSort.kt`에 `DeckSort` enum 생성 — **선언 순서가 곧 우선순위**(`GGUK_PICK`·`LATEST`·`NEAREST`). US1·US3이 쓴다
- [X] T004 [P] `core/domain/.../model/PlaceLabel.kt`에 `PlaceLabel` enum 생성 — 서버 `labelGroup`과 1:1(`WORTH_VISITING`·`MANY_SAVES`·`MANY_COMMENTS`·`MANY_VIEWS`). US2가 쓴다
- [X] T005 [P] `core/domain/.../model/PlaceCard.kt`에 `PlaceCard`·`Registrant` 생성 — 저장 경과일 필드를 두지 않는다. US1·US2가 쓴다
- [X] T006 [P] `core/domain/.../model/Deck.kt`에 `Deck`·`DeckKey` 생성. US1·US3이 쓴다
- [X] T007 [P] `core/domain/.../model/RoomSummary.kt`에 `RoomSummary` 생성 — 기존 `Room`을 넓히지 않는 이유는 [data-model.md](./data-model.md) §1.2 참고. US1·US3·US4가 쓴다
- [X] T008 [P] `core/domain/.../model/NextDeck.kt`에 `DeckContext`·`NextDeck` 생성. US3이 쓴다

### 2-2. 전환 규칙 (US3의 심장)

- [X] T009 [P] [US3] `core/domain/src/test/kotlin/.../usecase/ResolveNextDeckUseCaseTest.kt`에 전환 규칙 테스트 작성 — TS-015·TS-016·TS-017·TS-018·TS-019·TS-021·TS-024·EC-009. **구현 전에 작성하고 실패를 확인한다**
- [X] T010 `core/domain/.../usecase/ResolveNextDeckUseCase.kt` 구현 — 판정 순서는 [contracts/home-ui.md](./contracts/home-ui.md) §4.1이 소유한다. 부수효과·I/O 없는 순수 함수 (T003·T006·T007·T008에 의존)

**3.0.0 — 축이 뒤집혀 규칙을 다시 쓴다.** T009·T010이 세운 「같은 방의 남은 덱 → 다음 방」은 폐기된 규칙이다([contracts/home-ui.md](./contracts/home-ui.md) §4.1).

- [X] T074 [P] `core/domain/.../model/NextDeck.kt`의 `NextRoom`에 `sort` 추가 — 자동 전환이 정렬을 유지하므로(FR-012) 「다음 방」과 「그 방의 어느 정렬」이 한 답이다. 종전 `NextRoom(roomId)`는 방이 바뀌면 정렬이 초기화된다는 폐기된 규칙에 기댄 형태였다([data-model.md](./data-model.md) §1.1). US3·US4가 쓴다
- [X] T075 [US3] `core/domain/src/test/kotlin/.../usecase/ResolveNextDeckUseCaseTest.kt` **재작성** — 격자 순회로 판정이 바뀌었다. TS-015·TS-016·TS-017·TS-019·TS-019a·TS-021·TS-024·EC-009. 담당 TS 목록은 [quickstart.md](./quickstart.md) §3.1 첫 표가 소유한다. **구현 전에 실패를 확인한다** (T074에 의존)
- [X] T076 `core/domain/.../usecase/ResolveNextDeckUseCase.kt` **규칙 재작성** — 「아직 소진되지 않은 (정렬, 방) 칸 중 순서상 가장 앞선 것」. 방 순서는 `context.rooms`를 **받은 순서 그대로** 훑고 재배치하지 않는다. 판정은 [contracts/home-ui.md](./contracts/home-ui.md) §4.1 「자동 전환」이 소유한다 (T074·T075에 의존)
- [X] T077 [P] [US4] `core/domain/src/test/kotlin/.../usecase/ResolveRoomEntryDeckUseCaseTest.kt` 신설 — TS-028·TS-028b·TS-028c, EC-020·EC-022, 그리고 **어떤 입력에서도 `NextRoom`을 내지 않는다**(FR-024·SC-008). **구현 전에 실패를 확인한다** (T074에 의존)
- [X] T078 `core/domain/.../usecase/ResolveRoomEntryDeckUseCase.kt` 신설 — 탐색 범위를 `roomId` 하나로 한정한다. 판정은 [contracts/home-ui.md](./contracts/home-ui.md) §4.1 「수동 방 변경」이 소유한다 (T074·T077에 의존)

### 2-3. Repository 계약

- [X] T011 [P] `core/domain/.../repository/HomeDeckRepository.kt` 인터페이스 정의 — 시그니처는 [contracts/home-ui.md](./contracts/home-ui.md) §4.2 그대로. `getDeck`의 `location: GeoPoint?`를 빠뜨리지 않는다(R-013). US1~US4가 쓴다
- [X] T012 [P] `core/domain/.../repository/HomePreferencesRepository.kt` 인터페이스 정의 — [contracts/home-ui.md](./contracts/home-ui.md) §4.3. US1(시작 방)·US5(가이드 이력)가 쓴다

**3.0.0 — 같은 서버 호출에 계약이 둘이던 것을 걷는다**(R-019).

- [X] T079 `core/domain/.../repository/HomeDeckRepository.kt`에서 `recordPlaceOpened`·`savePinToRoom` **제거** — 둘 다 `PlaceRepository`(`recordAccess`·`duplicatePin`)가 이미 소유한 동작이다. 함께 `getRoomSummaries` KDoc에 **순회 순서를 이 함수가 확정한다**는 계약을 적는다([contracts/home-ui.md](./contracts/home-ui.md) §4.2·§4.2.1). US1·US2·US3·US4가 쓴다
- [X] T080 `core/data/.../repository/HomeDeckRepositoryImpl.kt`의 `getRoomSummaries`가 **순회 순서를 확정** — 개인방(`type == personal`) 먼저, 그다음 `createdAt` 오래된 순. 응답 순서에 기대지 않는다(FR-012, R-014). 재료가 `GET /api/v1/rooms` 응답에 다 있는 것은 [contracts/deck-api.md](./contracts/deck-api.md) §1이 확인했다 (T079에 의존)
- [X] T081 [P] `core/data/src/test/.../repository/HomeDeckRepositoryImplTest.kt`에 순회 순서 테스트 — 개인방이 먼저 오고 공동방이 생성 오래된 순으로 이어진다(TS-019a). 응답을 뒤섞어 넣어도 같은 순서가 나오는지 본다

### 2-4. 데이터 계층 ([contracts/deck-api.md](./contracts/deck-api.md) §4)

- [X] T013 [P] `core/data/.../network/dto/response/CardResponse.kt` 생성 — [contracts/deck-api.md](./contracts/deck-api.md) §2.2의 응답 스키마를 그대로 옮긴다
- [X] T014 `core/data/.../datasource/DeckRemoteDataSource.kt` 인터페이스 정의 — `getCards(roomId, sort, lat?, lng?)` (T013에 의존)
- [X] T015 `core/data/.../datasource/DeckMockRemoteDataSourceImpl.kt` 구현 — **[contracts/deck-api.md](./contracts/deck-api.md) §4의 "반드시 재현해야 하는 경우" 5종을 모두 재현한다.** 빠뜨리면 TS-005·TS-014·TS-017·TS-023·EC-013이 검증되지 않는다 (T014에 의존)
- [X] T016 [P] `core/data/.../repository/mapper/DeckMapper.kt` 작성 — `CardResponse` → `PlaceCard`, `labelGroup` → `PlaceLabel`, `RoomResponse` → `RoomSummary` (T005·T007·T013에 의존)
- [X] T017 `core/data/.../repository/HomeDeckRepositoryImpl.kt` 구현 — **10장 절단을 다시 하지 않는다**(서버가 잘라 준다). `sort == NEAREST`인데 좌표가 `null`이면 요청하지 않고 빈 덱 반환 (T011·T014·T016에 의존)
- [X] T018 [P] `core/data/.../datasource/HomePreferencesLocalDataSource.kt`와 구현체 작성 — 기존 `DataStoreModule` 사용. 저장 대상은 마지막 방·가이드 이력 **둘뿐**
- [X] T019 `core/data/.../repository/HomePreferencesRepositoryImpl.kt` 구현 (T012·T018에 의존)
- [X] T020 [P] `core/data/.../datasource/di/DeckDataSourceModule.kt`와 `core/data/.../repository/di/HomeRepositoryModule.kt` 작성 — mock 바인딩은 **실서버 전환 지점 ②**([contracts/deck-api.md](./contracts/deck-api.md) §4)

### 2-5. feature 골격

- [X] T021 `feature/home/.../main/vm/HomeUiState.kt`·`HomeIntent.kt`·`HomeSideEffect.kt` 정의 — 필드·Intent 13종·SideEffect 4종은 [contracts/home-ui.md](./contracts/home-ui.md) §2·§3, [data-model.md](./data-model.md) §3이 소유한다. US1~US5 전부가 쓴다
- [X] T022 [P] `feature/home/.../main/model/HomePhase.kt`·`HomeTooltip.kt` 생성 (T021과 함께 쓰인다)
- [X] T023 `feature/home/.../main/vm/HomeViewModel.kt` 골격 — `MviContainer` 위임, `processIntent` 분기 뼈대만. 각 스토리가 자기 분기를 채운다 (T021에 의존)
- [X] T024 `feature/home/.../HomeNavigation.kt` 갱신 — 콜백을 `onNavigateToPlaceDetail`·`onNavigateToRoomForm`·`onCreateRoomFromEmpty` 3종으로 교체하고 **스텁이 남긴 `onNavigateToSample`·`onRequestSampleResult`를 제거**한다([contracts/home-ui.md](./contracts/home-ui.md) §1). `:feature:main`의 호출부도 함께 고친다
- [X] T025 [P] `feature/home/.../main/model/RoomColorUiModel.kt` 작성 — `RoomColor` → (배경색·캐릭터 에셋) 대응표. 팔레트는 `:core:design-system`이 소유하고 대응만 여기서 한다(R-010). US1·US4가 쓴다
- [X] T026 [P] 방 캐릭터 이미지 에셋을 `feature/home/src/main/res/`에 추가 — 배치 근거는 [`component-asset-placement.md`](../../conventions/component-asset-placement.md)

**체크포인트**: 각 기반 작업이 끝날 때마다 그것을 쓰는 스토리 작업을 시작할 수 있다. 기반 전체를 기다리지 않는다

---

## Phase 3: 사용자 스토리 1 - 카드 덱 스와이프 탐색

**목표**: 홈에 들어오면 현재 방의 덱이 보이고, 우측 영역 스와이프로 카드를 넘기고 되돌릴 수 있다.

**독립 테스트**: 앱을 새로 설치해 홈에 진입 → 개인방 덱이 뜨고, 우측에서 좌→우로 넘기면 다음 카드가, 우→좌로 되돌리면 이전 카드가 온다. 좌측 드래그는 무반응. [quickstart.md](./quickstart.md) §4.2~4.4

### 테스트 ⚠️

- [X] T027 [P] [US1] `feature/home/src/test/.../HomeViewModelDeckTest.kt`에 덱 로드·스와이프·되돌리기 테스트 작성 — TS-004·TS-005·EC-001·EC-003, `isTransitioning` 중 입력 무시(TS-007). **구현 전에 실패를 확인한다**

### 구현

- [X] T028 [P] [US1] `feature/home/.../main/component/HomeTopShell.kt` 작성 — 방 뱃지·방 캐릭터·인사 문구(FR-021). Figma `2598-95698` (T025·T026에 의존)
- [X] T029 [P] [US1] `feature/home/.../main/component/PlaceCardItem.kt` 작성 — 헤더(등록자 아바타 + 라벨 뱃지 + `[...]`)·장소명·주소·대표 이미지 2칸 그리드. **저장 경과일을 표시하지 않는다** (T005에 의존)
- [X] T030 [US1] `feature/home/.../main/component/CardDeck.kt` 작성 — 뒤로 겹쳐 보이는 스택, 전환 애니메이션(UX-001). 제스처는 T031이 얹는다 (T029에 의존)
- [X] T031 [US1] `CardDeck.kt`에 스와이프 제스처 구현 — **시작점 x좌표가 카드 폭 절반 이상일 때만 소비**한다(FR-003, R-005). 탭과 드래그는 같은 `pointerInput`에서 터치 슬롭으로 가른다(R-006)
- [X] T032 [US1] `feature/home/.../main/vm/HomeViewModel.kt`에 덱 로드와 `SwipeForward`·`SwipeBackward`·`TransitionSettled` 처리 구현 — **`SwipeForward`는 서버를 부르지 않는다**(FR-023, TS-035). `isTransitioning` 중 스와이프 Intent는 큐에 쌓지 않고 버린다(R-007) (T011·T023에 의존)
- [X] T033 [US1] `feature/home/.../main/vm/HomeViewModel.kt`에 시작 방 결정 구현 — `HomePreferencesRepository.getLastRoomId()`가 `null`이면 `type == personal`인 방(FR-022, TS-032·TS-033). 방이 바뀔 때마다 `setLastRoomId` (T012·T019에 의존)
- [X] T034 [US1] `feature/home/.../main/screen/HomeScreen.kt`·`HomeRoute.kt` 작성 — Route는 VM·state·sideEffect 연결과 `CollectDomainError`까지, Screen은 stateless. [`feature-module.md`](../../architecture/feature-module.md) §4를 따른다

**3.0.0 — 방 캐릭터가 방 색별 12 variant로 갈린다**(R-015). T026이 넣은 단일 에셋(126×164) 하나로는 표현되지 않는다 — 소품 모양이 variant마다 다르다.

- [X] T082 [P] [US1] 방 캐릭터 에셋 **12종**을 `feature/home/src/main/res/drawable-{m,x,xx}hdpi/`에 추가 — Figma `Home_Avatar`(노드 `4306:63718`)를 `download_assets`로 밀도 3벌 export. variant는 `black`(색 미선택) + 11색이며 **`brown`이 없다**. 절차는 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md) §1.3
- [X] T083 [US1] `feature/home/.../main/component/HomeTopShell.kt`의 `RoomCharacter`가 **방 색을 받아** 에셋을 고르도록 고치고 크기를 **126×172**로 정정 — `RoomColor` → 에셋 대응은 `main/model/RoomColorUiModel.kt`가 갖는다(R-010·R-015). **`RoomColor.BROWN`은 협의 전까지 `black`으로 떨어뜨린다.** 호출부에서 현재 방의 색을 넘기는 배선까지 포함한다 (T082에 의존)

**체크포인트**: 덱이 뜨고 스와이프로 넘기고 되돌릴 수 있다. 정렬 칩·자동 전환은 아직 없다

---

## Phase 4: 사용자 스토리 2 - 카드 액션과 장소 상세 진입

**목표**: 카드의 `[...]`로 다른 방에 담고, 카드 본문을 눌러 상세로 간다. **두 「확인 이벤트」가 서로를 건드리지 않는다.**

**독립 테스트**: 카드 본문을 탭해 상세를 열고 돌아오면 카드와 잔여 수가 그대로다(②가 아님). 동시에 그 장소의 경과일은 초기화돼 다시 들어오면 뒤로 밀려 있다(①). 그냥 넘긴 카드는 순위가 그대로다. [quickstart.md](./quickstart.md) §4.5

### 테스트 ⚠️

- [X] T035 [P] [US2] `feature/home/src/test/.../HomeViewModelConfirmationTest.kt`에 두 확인 이벤트의 독립 테스트 작성 — TS-013(탭은 덱을 안 건드림)·TS-034(탭이 `recordPlaceOpened`를 부름)·TS-035(넘김은 서버를 안 부름)·EC-017(되돌려도 초기화는 취소 안 됨). **FR-023을 지키는 유일한 그물이다**

### 구현

- [X] T036 [P] [US2] `feature/home/.../main/component/CardActionMenu.kt` 작성 — **`다른 방 저장` 한 항목만**(FR-005). 클릭한 카드 근처에서 열린다(UX-002)
- [X] T037 [US2] `feature/home/.../main/vm/HomeViewModel.kt`에 `OpenActionMenu`·`DismissActionMenu`·`SaveToAnotherRoom` 처리 구현 — 메뉴가 열린 채 스와이프하면 메뉴만 닫는다(EC-004·EC-005) (T021·T023에 의존)
- [X] T038 [US2] `feature/home/.../main/vm/HomeViewModel.kt`에 `OpenPlaceDetail` 처리 구현 — `recordPlaceOpened(pinId)`를 부르고 **결과를 기다리지 않고** `NavigateToPlaceDetail` SideEffect를 던진다(R-012). **덱의 진행 상태를 건드리지 않는다** (T011·T017에 의존)
- [X] T039 [US2] `feature/home/.../main/component/PlaceCardItem.kt`에 라벨 뱃지 표시 연결 — 4종 중 1종(FR-008, TS-014) (T004·T029에 의존)
- [X] T040 [US2] `feature/home/.../main/component/CardDeck.kt`에서 `[...]` 버튼의 클릭 영역이 카드 제스처보다 우선하도록 배선 — EC-007 (T031·T036에 의존)

**3.0.0 — `다른 방 저장`이 「홈 방 시트」에서 「방 선택 시트」로 바뀐다**(spec 4.0.0 FR-005, R-017). 홈이 사본을 만들지 않고 [SYS-002]가 구현해 둔 것을 승격해 쓴다.

- [X] T084 [P] `RoomPickerSheet`와 하위 컴포넌트(`RoomPickerList`·`RoomPickerHeader`·`RoomPickerActionArea`)를 `:feature:sharereceiver`에서 `core/common/ui/src/main/java/team/mino/core/common/ui/component/roompicker/`로 **승격** — 공개 표면은 [SYS-002] 계약([`room-picker-sheet-ui.md`](../shared-link-receiver/contracts/room-picker-sheet-ui.md))이 소유하므로 **바꾸지 않는다**. 승격 기준은 [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §2.1
- [X] T085 `:feature:sharereceiver`의 참조를 승격된 컴포넌트로 잇는다 — **두 feature를 건드리므로 홈 작업과 분리된 커밋으로 낸다.** 홈을 되돌릴 때 [SYS-002]가 함께 깨지지 않게 하는 것이 분리의 이유다 (T084에 의존)
- [X] T086 [US2] `feature/home/.../main/vm/HomeIntent.kt`에 `ToggleSaveTargetRoom`·`ConfirmSaveTargets`·`DismissSavePicker` 추가하고 `HomeUiState.kt`에 `savePicker: SavePickerState?` 추가 — 시트가 둘이므로 상태도 둘이다([contracts/home-ui.md](./contracts/home-ui.md) §2, [data-model.md](./data-model.md) §3)
- [X] T087 [US2] `feature/home/.../main/vm/HomeViewModel.kt`의 `다른 방 저장` 경로를 「방 선택 시트」로 교체 — **`pendingSavePinId` 플래그를 제거**하고, 확정 시 `PlaceRepository.duplicatePin(pinId, roomIds)`를 부른다. 선택이 비면 확정을 막는다(EC-018) (T079·T084·T086에 의존)
- [X] T088 [US2] `feature/home/.../main/screen/HomeScreen.kt`에 「방 선택 시트」 배선 — 「홈 방 시트」와 **다른 시트로 함께 존재**한다. 저장이 방 전환으로 뒤바뀌지 않는지가 검증 지점이다(FR-005·FR-017) (T084·T086에 의존)
- [X] T089 [US2] `feature/home/.../main/vm/HomeViewModel.kt`의 상세 진입이 `PlaceRepository.recordAccess(pinId)`를 부르도록 정정 — T038이 부르던 `recordPlaceOpened`가 사라진다(R-019). 결과를 기다리지 않는 것과 덱을 건드리지 않는 것은 그대로다 (T079에 의존)
- [X] T090 [P] [US2] `feature/home/src/test/.../HomeViewModelConfirmationTest.kt` 갱신 — `recordAccess` 호출로 정정(TS-034), 복수 선택 저장이 `duplicatePin`에 목록을 그대로 넘김(TS-011a), 이미 저장된 방도 고를 수 있음(TS-011b), 선택이 비면 확정 비활성(EC-018)

**체크포인트**: 액션 메뉴와 상세 진입이 동작하고, 두 확인 이벤트가 독립으로 검증된다

---

## Phase 5: 사용자 스토리 3 - 정렬·방 자동 전환과 소진 완료 ⭐

**목표**: 덱을 소진할 때마다 시스템이 다음에 볼 덱을 스스로 정해 이어 붙이고, 볼 것이 떨어지면 완료를 알린다. **이 스펙의 핵심이다.**

**독립 테스트**: 한 덱을 끝까지 넘기면 같은 방의 남은 덱 중 우선순위 최고로 전환되고 칩 표시가 따라 옮겨진다. 세 덱을 다 소진해야 다음 방으로 가고, 그때 칩이 `꾹 Pick`으로 돌아간다. [quickstart.md](./quickstart.md) §4.6

### 테스트 ⚠️

> T009(전환 규칙 JVM 테스트)가 Phase 2에 있다. TS-015·TS-016·TS-017·TS-018·TS-019·TS-021·TS-024와 EC-009가 거기서 검증된다.

- [X] T041 [P] [US3] `feature/home/src/test/.../HomeViewModelTransitionTest.kt`에 화면 상태 쪽 테스트 작성 — 예고 툴팁 덱당 1회(spec §4 가정), 칩 표시와 실제 덱의 일치(UX-004), `가까운순`에 좌표가 없으면 빈 덱 처리(EC-009)

### 구현

- [X] T042 [P] [US3] `feature/home/.../main/component/SortChipRow.kt` 작성 — 3종을 왼쪽부터 `꾹 Pick`·`최신순`·`가까운순` 순으로, 현재 덱의 정렬을 선택 표시(FR-009)
- [X] T043 [P] [US3] `feature/home/.../main/component/HomeTooltipOverlay.kt` 작성 — 툴팁 2종, 3초 후 페이드아웃. **조작을 막지 않는다**(UX-003)
- [X] T044 [P] [US3] `feature/home/.../main/component/AllExhaustedContent.kt` 작성 — 상단은 그대로 두고 카드 자리에만 일러스트와 `꾹 눌러둔 장소를 모두 둘러봤어요`. **CTA 버튼을 두지 않는다**(FR-014). Figma `3388-199413`
- [X] T045 [P] [US3] `feature/home/.../main/component/EmptyContent.kt` 작성 — 빈 상태 안내와 `[공동방 만들기]` CTA(FR-020, EC-011)
- [X] T046 [US3] `feature/home/.../main/vm/HomeViewModel.kt`에 소진 감지와 전환 배선 구현 — 잔여 0이면 `ResolveNextDeckUseCase`를 호출하고 결과대로 `SameRoom`·`NextRoom`·`AllExhausted` 처리. **전환 시점마다 다시 판정한다**(FR-011) (T010·T032에 의존)
- [X] T047 [US3] `feature/home/.../main/vm/HomeViewModel.kt`에서 고른 덱의 후보가 0장으로 판명되면 그 덱도 소진으로 보고 규칙을 다시 적용 — EC-013. **빈 덱을 노출하지 않는다** (T046에 의존)
- [X] T048 [US3] `feature/home/.../main/vm/HomeViewModel.kt`에 `SelectSort` 처리 구현 — 칩 직접 선택 시 즉시 전환하되, 건너뛴 덱은 방을 넘기기 전에 다시 온다(FR-010, TS-020·TS-021) (T046에 의존)
- [X] T049 [US3] `feature/home/.../main/vm/HomeViewModel.kt`에서 방 전환 시 정렬을 `꾹 Pick`으로 초기화하고 방 전환 툴팁 3초 노출(FR-012·FR-016), 되돌리기 이력 초기화(EC-003) (T043·T046에 의존)
- [X] T050 [US3] `feature/home/.../main/vm/HomeViewModel.kt`에 예고 툴팁 판정 구현 — 잔여 2장 이하가 되면 **실제로 다음에 올** 덱·방을 가리킨다. 예고 대상이 없으면 노출하지 않고(TS-023), 덱당 1회만 띄운다 (T043·T046에 의존)
- [X] T051 [US3] `feature/home/.../main/vm/HomeViewModel.kt`에 `가까운순` 전환 시 위치 권한 흐름 배선 — `RequestLocationPermission` SideEffect를 던지고 `LocationPermissionResult(location)`로 받는다. 거부(`null`)면 좌표 없이 `getDeck`을 불러 빈 덱을 받고 소진으로 흡수한다(EC-009, R-009·R-013) (T017·T046에 의존)

**3.0.0 — 자동 전환이 정렬을 유지한다.** T049가 세운 「방이 바뀌면 `꾹 Pick`으로 초기화」와 T051의 방 단위 권한 처리가 폐기된 규칙이다(spec 4.0.0 FR-012·EC-009).

- [X] T091 [P] [US3] `feature/home/src/test/.../HomeViewModelTransitionTest.kt` 갱신 — 자동 방 전환이 정렬을 유지(TS-015), 모든 방을 확인해야 정렬이 넘어가고 첫 방부터 다시 훑음(TS-016), 정렬 전환 자체에는 툴팁이 없음(TS-018), 권한 거부가 `가까운순`을 모든 방에 대해 소진 처리(EC-009), 장소 있는 방이 하나뿐일 때(EC-019)
- [X] T092 [US3] `feature/home/.../main/vm/HomeViewModel.kt`의 자동 방 전환에서 **정렬 초기화를 걷는다** — `NextRoom(roomId, sort)`가 실어 온 정렬을 그대로 쓴다. 방 전환 툴팁과 되돌리기 이력 초기화는 유지한다(FR-012·FR-016, EC-003) (T076·T074에 의존)
- [X] T093 [US3] `feature/home/.../main/vm/HomeViewModel.kt`의 권한 거부 처리를 **정렬 단위로** 정정 — 거부 시 `가까운순 × 모든 방`을 소진 집합에 넣고 판정을 다시 부른다. **방마다 다시 묻지 않는다**(EC-009). 좌표 없는 `getDeck`을 방마다 부르던 흐름이 사라진다 (T076에 의존)
- [X] T094 [US3] 예고 툴팁 문구를 **2갈래로** 정정 — 같은 정렬의 다음 방이면 그 방을, 정렬이 넘어갈 차례면 `곧 {정렬 이름}으로 이동해요!`. `feature/home/src/main/res/values/strings.xml`의 `home_tooltip_next_sort_*` 3종을 이 문구로 바꾼다(FR-015, TS-022·TS-022a) (T076에 의존)
- [X] T095 [US3] 완료 안내에서 정렬 칩을 **`꾹 Pick` 선택 상태로** 표시 — 남은 칸이 없어 도달한 화면이므로 칩이 마지막으로 보던 정렬에 머물지 않는다(FR-014) (T092에 의존)
- [X] T096 [P] [US3] 완료 안내 일러스트를 **209×209**로 재export — Figma 노드 `5073:101117`, `feature/home/src/main/res/drawable-{m,x,xx}hdpi/home_all_exhausted_illustration.webp` 교체(R-018)
- [ ] T097 [US3] ⚠️ **잠긴 작업** — 완료 안내 문구를 `모든 장소를 다 봤어요!`로 교체(`home_all_exhausted_message`). **spec FR-014와 PRD [SCR-003] Flow E 개정이 머지된 뒤에 착수한다.** 지금 착수하면 구현이 명세를 앞지른다([plan.md](./plan.md) §복잡도 추적 #1, R-018)
- [X] T098 [US3] `feature/home/.../main/component/HomeTooltipOverlay.kt`의 툴팁 위치를 **`position = Right`·`align = Center`** 로 교체하고, 조립부 오프셋을 시안값으로 맞춘다 — 화살표가 오른쪽 변 세로 중앙에서 캐릭터를 가리키고 툴팁 본문이 캐릭터 왼쪽에 놓인다(R-016, [contracts/home-ui.md](./contracts/home-ui.md) §5). **구현 중 정정**: `MinoTooltip.position`은 "말풍선이 놓이는 방향"이라 화살표는 그 반대편에 붙는다(`MinoTooltip.kt` KDoc) — 요구한 시각 결과(화살표가 오른쪽 변)를 실제로 내려면 코드값은 `position = Left`여야 해서 그렇게 구현했다. `contracts/home-ui.md` §5·`research.md` R-016의 "Right" 표기는 오기로 보인다(문서 정정은 이 스킬 범위 밖)

**체크포인트**: 사용자가 스와이프만으로 한 방의 세 덱을 훑고 다음 방까지 넘어간다. SC-001·SC-002·SC-003·SC-004가 성립한다

---

## Phase 6: 사용자 스토리 4 - 방 직접 변경

**목표**: 상단 방 뱃지나 캐릭터를 눌러 다른 방으로 문맥을 옮긴다.

**독립 테스트**: 방 뱃지와 캐릭터 어느 쪽을 눌러도 같은 시트가 열리고, 방 카드를 누르면 즉시 그 방의 `꾹 Pick` 덱으로 바뀐다. [quickstart.md](./quickstart.md) §4.7

### 테스트 ⚠️

- [X] T052 [P] [US4] `feature/home/src/test/.../HomeViewModelRoomSheetTest.kt`에 시트 테스트 작성 — 현재 방 재선택 시 덱을 다시 구성하지 않는다(EC-014)

### 구현

- [X] T053 [P] [US4] `feature/home/.../main/component/HomeRoomSheet.kt` 작성 — **400dp 고정 높이·3열 그리드·70dp 썸네일 + 방 이름**, 첫 칸은 `방 만들기`. **체크박스도 확정 버튼도 없다**(FR-018). Figma `2809-139468` (T007·T025에 의존)
- [X] T054 [US4] `feature/home/.../main/component/HomeTopShell.kt`의 방 뱃지·캐릭터에 클릭을 붙여 `OpenRoomSheet` 발행(FR-017, TS-025·TS-026) (T028에 의존)
- [X] T055 [US4] `feature/home/.../main/vm/HomeViewModel.kt`에 `SelectRoom`·`DismissRoomSheet` 처리 구현 — 선택이 곧 확정이고, 현재 방을 다시 고르면 시트만 닫는다(EC-014). `방 만들기`는 `NavigateToRoomForm`(EC-015) (T049·T053에 의존)

**3.0.0 — 수동 변경만 정렬을 되감고, 탐색 범위를 고른 방으로 한정한다**(spec 4.0.0 FR-024). **현재 구현은 장소 0개인 방을 고르면 다른 방으로 튕긴다** — `openDeck`이 세 덱을 소진 처리한 뒤 자동 전환에 넘기기 때문이다.

- [X] T099 [P] [US4] `feature/home/src/test/.../HomeViewModelRoomSheetTest.kt` 갱신 — 수동 변경이 정렬을 `꾹 Pick`으로 되감음(TS-028a), `꾹 Pick`이 소진된 방은 그 방의 남은 덱을 엶(TS-028b), **장소 0개인 방을 고르면 그 방을 단 채 완료 안내**(TS-028c·EC-020), 소진한 방을 다시 골라도 덱이 되살아나지 않음(EC-021)
- [X] T100 [US4] `feature/home/.../main/vm/HomeViewModel.kt`의 `selectRoom`이 **`ResolveRoomEntryDeckUseCase`를 쓰도록** 교체 — 정렬을 `꾹 Pick`으로 되감고, 그 칸이 소진이면 그 방의 남은 덱 중 최고 순위를 열고, 세 칸 모두 소진이면 **그 방의 뱃지·캐릭터를 단 채** `AllExhausted`로 간다. **`advance()`로 넘기지 않는다** — 그것이 다른 방으로 튕기던 원인이다(FR-024·SC-008) (T078·T092에 의존)
- [X] T101 [US4] `feature/home/.../main/component/HomeRoomSheet.kt`에서 **장소 0개인 방도 고를 수 있는지** 확인 — 체크·비활성으로 막지 않는다(EC-022). 막고 있다면 걷는다 (T100에 의존)

**체크포인트**: 방을 직접 바꿀 수 있고 SC-007이 성립한다

---

## Phase 7: 사용자 스토리 5 - 홈 최초 진입 가이드

**목표**: 앱 생애에 한 번, 홈을 처음 연 사용자에게 조작 방법을 알려준다.

**독립 테스트**: 새로 설치하고 홈에 진입 → 딤과 안내 문구 2개. 딤 상태에서 아무 조작도 먹지 않고, 닫은 뒤 앱을 껐다 켜도 다시 뜨지 않는다. [quickstart.md](./quickstart.md) §4.1

### 테스트 ⚠️

- [X] T056 [P] [US5] `feature/home/src/test/.../HomeViewModelGuideTest.kt`에 가이드 테스트 작성 — `isGuideVisible` 중 `DismissGuide` 외 Intent를 전부 버린다(TS-030), 닫은 이력이 있으면 노출하지 않는다(TS-031)

### 구현

- [X] T057 [P] [US5] `feature/home/.../main/component/HomeGuideOverlay.kt` 작성 — 화면 전체 딤, 카드 위 손 아이콘 + `좌우로 스와이프하며 카드를 탐색해 보세요.`, 상단을 가리키는 `방 뱃지와 토끼를 클릭하면 방을 변경할 수 있어요`, 우측 상단 닫기. **문구 2개다**(FR-019). Figma `4334-216197`
- [X] T058 [US5] `feature/home/.../main/vm/HomeViewModel.kt`에 가이드 노출 판정과 `DismissGuide` 처리 구현 — `isGuideDismissed()`로 판정하고 닫으면 `dismissGuide()`로 영속 저장 (T012·T019에 의존)
- [X] T059 [US5] `feature/home/.../main/vm/HomeViewModel.kt`에 가이드 중 조작 차단 배선 — `isGuideVisible == true`면 `DismissGuide`를 뺀 모든 Intent를 버린다(TS-030) (T058에 의존)
- [X] T060 [US5] `feature/home/.../main/vm/HomeViewModel.kt`에서 볼 카드가 없어도 가이드를 먼저 노출하고, 닫은 뒤 빈 상태를 보여주도록 배선 — EC-016. 가이드는 `phase`와 직교한다 (T045·T058에 의존)

**체크포인트**: 다섯 스토리가 모두 독립적으로 동작한다

---

## Phase 8: 마무리 및 공통 관심사

- [X] T061 [P] Figma 대조 — 상단 셸·카드·정렬 칩·시트·툴팁·가이드의 색·치수·타이포를 원본과 맞춘다. 값이 일치하는 토큰이 있으면 토큰, 없으면 실측값. 절차는 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md)
- [X] T062 [P] 정렬 칩이 Figma 디자인 시스템 컴포넌트로 존재하는지 확인하고, 그렇다면 `:core:design-system`으로 옮긴다 — [`component-asset-placement.md`](../../conventions/component-asset-placement.md)
- [X] T063 도메인 에러 경로 확인 — `다른 방 저장` 409(중복)를 포함한 실패가 `HomeRoute`의 `CollectDomainError`로 스낵바에 뜨는지. [`error_handling.md`](../../conventions/error_handling.md) §5·§6
- [X] T064 `./gradlew :app:assembleQaDebug` 통과 확인 — 헌법이 정한 빌드 확인의 최소선
- [ ] T065 [quickstart.md](./quickstart.md) §4의 수동 시나리오 8절 전부 실행

### 실서버 전환 (`/cards` 배포 후 — 2026-08-29)

`GET /api/v1/rooms/{roomId}/cards`가 배포되어 [contracts/deck-api.md](./contracts/deck-api.md) §4의 전환 지점 셋을 닫은 작업이다. 인터페이스(`DeckRemoteDataSource`)·Mapper·Repository·화면은 바뀌지 않았다.

- [X] T066 `core/data/.../network/service/DeckApiService.kt` 신설 — `GET api/v1/rooms/{roomId}/cards`, `sort`·`lat`·`lng` 질의, `MinoResponse<List<CardResponse>>` 봉투 벗기기 (계약 §2.1·2.2)
- [X] T067 `core/data/.../datasource/DeckRemoteDataSourceImpl.kt` 신설 — 전환 지점 ①. 도메인 `DeckSort` → 서버 문자열(`ggukPick`·`latest`·`nearby`) 대응을 이 파일이 소유한다. T066에 의존
- [X] T068 `core/data/.../datasource/di/DeckDataSourceModule.kt`의 `@Binds` 인자를 mock → 실구현으로 교체 — 전환 지점 ②. T067에 의존
- [X] T069 `core/data/.../network/dto/response/CardResponse.kt` 대조 — 전환 지점 ③. 배포 스키마와 필드가 일치해 변경이 없으나, 스키마에 `required`가 없어 **모든 필드에 기본값을 뒀다**. 한 필드가 빠졌다고 덱 전체가 직렬화 실패로 떨어지지 않게 하는 방어이며, `DeckMapper`가 이미 세운 「카드 한 장 때문에 덱이 실패하지 않는다」를 응답을 읽는 자리까지 이은 것이다
- [X] T070 mock 제거 — `DeckMockRemoteDataSourceImpl.kt`·`datasource/mock/DeckMockStore.kt`와 그 테스트를 삭제. T068에 의존
- [X] T071 `core/data/src/test/.../network/DeckApiFixtures.kt`·`DeckApiServiceTest.kt`·`datasource/DeckRemoteDataSourceImplTest.kt` 작성 — **mock이 재현하던 계약 §4의 「반드시 재현해야 하는 경우」를 픽스처로 이관한다.** 실서버는 짧은 덱·0건 정렬·전부 `worthVisiting`을 마음대로 만들어 주지 않으므로, 이관하지 않으면 TS-005·TS-014·TS-017·TS-023·EC-013을 검사할 자리가 사라진다. T066·T067에 의존
- [X] T073 등록자 아바타를 실제 서버 표현으로 정정 — 실기기에서 덱 조회가 `JsonConvertException: Field 'id' is required ... CardAvatarResponse`로 죽었다. `/cards` 문서만 아바타를 `{ id: integer }`로 적어 두었고 실제 응답에는 그 필드가 없다. `CardAvatarResponse`를 지우고 프로필과 같은 `AvatarResponse`(`{ color }`)를 쓰며, 색→아바타 대응표는 `ProfileMapper`가 계속 단독으로 소유한다(`toProfileAvatarOrNull` 공유). 도메인 `Registrant.avatarId: Int?`도 문서 오류에서 나온 모델이라 `avatar: ProfileAvatar?`로 바꿨다. 프로필과 달리 기본 아바타로 메우지 않는다 — 「고르지 않음」을 그대로 싣고 대체 표시는 feature가 정한다
- [ ] T072 실서버로 [quickstart.md](./quickstart.md) §4 재수행 — 특히 **저장한 장소가 그 방의 덱에 실제로 뜨는지**, 정렬 3종 전환, 위치 권한 거부 시 `가까운순`이 빈 덱→소진으로 흡수되는지(EC-009)를 본다

### 3.0.0 개정 마무리

- [X] T102 [P] 시안 재대조 — [quickstart.md](./quickstart.md) §4.9의 4항목. **방 캐릭터 12종**·**툴팁 위치**·**완료 안내 일러스트**는 바뀐 값이고, **「홈 방 시트」 썸네일(노드 `4306:63731`)은 변경 없음 확인만** 한다. 절차는 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md) §4 (T083·T096·T098에 의존)
- [ ] T103 spec 4.0.0 기준으로 [quickstart.md](./quickstart.md) §4 재수행 — 특히 **§4.6(축이 뒤집힌 자동 전환)**·**§4.7(수동 변경과 빈 방)**·**§4.7a(`다른 방 저장` 시트 교체)**. T097이 잠긴 동안 §4.9의 3번은 **미검증**으로 남긴다 (T100·T092에 의존)
- [ ] T104 `./gradlew :app:assembleQaDebug`와 `:core:domain:test`·`:feature:home:testDebugUnitTest`·`:core:data:test` 통과 확인 — 도메인 계약이 바뀌어 기존 테스트가 함께 깨지는 자리를 여기서 잡는다

---

## 커버리지 대조표

spec **4.0.0**의 요구사항이 어느 작업으로 닫히는지 적는다. 줄마다 ID를 흩뿌리는 대신 여기서 한 번에 대조한다. `/mino-analyze`가 이 표를 기준으로 검증한다.

### 기능 요구사항 (FR)

**굵은 ID가 3.0.0에서 새로 세운 작업이다.** `종전 → 3.0.0`으로 적힌 줄은 규칙이 바뀐 자리이고, 종전 작업은 그때의 규칙을 닫은 기록으로 남는다.

| FR | 작업 |
|---|---|
| FR-001 좌→우 넘김 | T031·T032 |
| FR-002 우→좌 되돌리기 | T031·T032 |
| FR-003 좌측 영역 무시 | T031 |
| FR-004 최대 10장 | T015·T017 |
| FR-005 액션 메뉴 1항목 · **「방 선택 시트」 복수 선택** | T036·T037 → **T084~T088·T090** |
| *FR-006 결번* | — |
| FR-007 카드 클릭 → 상세 | T038 → **T089** |
| FR-008 장소분류 라벨 | T004·T039 |
| FR-009 정렬 칩 3종 | T042 |
| FR-010 칩 직접 선택 · 건너뛴 칸 복귀 | T048 → **T075·T076** |
| FR-011 **남은 (정렬, 방) 칸 중 최선두** | T010·T046·T047 → **T074·T075·T076** |
| FR-012 **정렬 유지 + 다음 방 · 순회 순서** | T010·T049 → **T076·T080·T081·T092** |
| FR-013 장소 0개 방 건너뜀 *(자동 전환 한정)* | T010 → **T076** |
| FR-014 완료 안내 *(남은 칸 유무 · 칩 `꾹 Pick`)* | T044·T046 → **T095·T096·T097** |
| FR-015 예고 툴팁 *(다음 방 / 다음 정렬 2갈래)* | T050 → **T094** |
| FR-016 방 전환 툴팁 *(위치 · 정렬 전환 제외)* | T049 → **T098** |
| FR-017 시트 열기 | T054 |
| FR-018 「홈 방 시트」 구성 | T053·T055 → **T101** |
| FR-019 최초 진입 가이드 | T057·T058·T059 |
| FR-020 빈 상태 | T045·T060 |
| FR-021 상단 셸 *(방 색별 캐릭터 12종)* | T028 → **T082·T083** |
| FR-022 시작 방 | T033 |
| FR-023 두 확인 이벤트 독립 | T032·T038 → **T089** |
| **FR-024 수동 방 변경 한정 규칙** | **T074·T077·T078·T099·T100·T101** |
| **FR-025 자동 정렬 전환** | **T075·T076·T091·T094** |

### 핵심 UX 규칙 (UX)

| UX | 작업 |
|---|---|
| UX-001 전환 애니메이션·입력 무시 | T030·T032 |
| UX-002 메뉴가 카드 근처에서 열림 | T036 |
| UX-003 툴팁이 조작을 막지 않음 | T043 |
| UX-004 칩 표시와 실제 덱 일치 | T042·T046·T048 → **T091·T092·T095** |

### 성과 기준 (SC)

| SC | 어디서 확인하나 |
|---|---|
| SC-001 스와이프만으로 **한 정렬로 모든 방·다음 정렬** | Phase 5 체크포인트, T103 |
| SC-002 건너뛴 칸도 완료 전에 노출 | T048, T075 |
| SC-003 칩과 덱이 어긋나지 않음 | T041, T042, T091 |
| SC-004 예고와 실제가 일치 | T050, T041, T094 |
| SC-005 두 장 넘어가지 않음 | T027(TS-007), T032 |
| SC-006 세 조작이 충돌하지 않음 | T031·T040, T035 |
| SC-007 마지막 방에서 이어 봄 | T033, T052 |
| **SC-008 고른 방을 벗어나지 않음** | **T077, T099, T100** |

### 엣지 케이스 (EC)

| EC | 작업 | EC | 작업 |
|---|---|---|---|
| EC-001 되돌릴 카드 없음 | T027·T032 | EC-010 완료 후 재순환 안 함 | T044·T046 |
| EC-002 임계값 미만 드래그 | T031 | EC-011 볼 장소가 없음 | T045 |
| EC-003 덱 전환 직후 되돌리기 | T027·T049 | EC-012 1~2장 덱 전환 직후 예고 | T050 |
| EC-004 메뉴 중 스와이프 | T037 | EC-013 전환 대상 칸이 0장 | T047·T076 |
| EC-005 메뉴 바깥 탭 | T037 | EC-014 현재 방 재선택 | T052·T055·T099 |
| EC-006 드래그를 탭으로 안 봄 | T031 | EC-015 `방 만들기` 선택 | T055 |
| EC-007 `[...]` 위 탭 | T040 | EC-016 가이드 후 빈 상태 | T060 |
| EC-009 위치 권한 거부 *(정렬 전체)* | T009·T051 → **T075·T091·T093** | EC-017 되돌려도 초기화 유지 | T035·T038 |
| **EC-018 선택 없으면 확정 비활성** | **T087·T090** | **EC-019 장소 있는 방이 하나뿐** | **T091** |
| **EC-020 고른 방의 세 칸 소진** | **T077·T099·T100** | **EC-021 소진한 방 재선택** | **T099·T100** |
| **EC-022 장소 0개 방도 고를 수 있음** | **T077·T101** | | |

### 테스트 시나리오 (TS)

spec 4.0.0의 **TS 41건**을 아래 테스트 작업과 수동 검증이 나눠 갖는다. 3.0.0에서 갱신·신설된 작업은 굵게 적었다.

| 작업 | 담당 TS |
|---|---|
| ~~T009~~ → **T075** (JVM 자동 전환) | TS-015·TS-016·TS-017·TS-019·**TS-019a**·TS-021·TS-024 |
| **T077** (JVM 수동 변경) | **TS-028·TS-028b·TS-028c** |
| T027 (VM) | TS-004·TS-005·TS-007 |
| T035 → **T090** (VM) | TS-013·TS-034·TS-035·**TS-011a·TS-011b** |
| T041 → **T091** (VM) | TS-018·TS-020·TS-022·**TS-022a**·TS-023 |
| T052 → **T099** (VM) | TS-028·**TS-028a·TS-028b·TS-028c** |
| T056 (VM) | TS-030·TS-031 |
| T065·**T103** (수동) | TS-001·TS-002·TS-003·TS-006·TS-008·TS-011·TS-012·TS-014·TS-025·TS-026·TS-027·TS-029·TS-032·TS-033 |

> 수동으로 미룬 14건은 대부분 **제스처·화면 렌더링**이라 ViewModel 테스트로 잡히지 않는다. 계측 테스트를 도입하면 이 표의 마지막 줄이 줄어든다.

> **TS-002a**(연속 되돌리기)는 T027이 이미 검증한다. 표에 별 줄로 세우지 않은 이유는 T027의 되돌리기 테스트가 그 시나리오 그대로이기 때문이다.

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **Phase 1 셋업**: 의존성 없음 — 즉시 시작
- **Phase 2 기반**: T001 완료에 의존
- **Phase 3~7 스토리**: **기반 전체가 아니라 자기가 쓰는 산출물에만** 의존한다
  - US1은 T011·T012·T017·T019·T021·T023·T025가 나오면 시작 가능
  - US2는 여기에 T004가 더 필요하다
  - US3은 **T010(전환 UseCase)** 이 핵심 전제다
  - US4는 T007·T053이, US5는 T012·T019가 전제다
- **Phase 8 마무리**: 대상 스토리들의 완료에 의존

### 사용자 스토리 간 의존성

- **US1**: 다른 스토리에 의존하지 않는다. **MVP는 여기까지다**
- **US2**: US1의 카드 컴포넌트(T029·T031) 위에 얹힌다
- **US3**: US1의 덱 로드(T032) 위에 얹힌다. 칩·툴팁·완료 화면은 독립적으로 만들 수 있다
- **US4**: US1의 상단 셸(T028)과 US3의 방 전환 처리(T049)를 쓴다
- **US5**: 다른 스토리와 직교한다. 딤이 조작을 막는 배선만 US1~US4의 Intent 처리와 닿는다

### 각 스토리 내부

- 테스트를 먼저 작성하고 실패를 확인한다
- 모델 → Repository → ViewModel → Composable 순서
- 컴포넌트(`component/`)는 서로 독립이라 [P]로 병렬 가능
- ViewModel 분기는 같은 파일을 건드리므로 병렬 불가

### 병렬 처리 기회

- **T003~T008** 도메인 모델 6건 전부 병렬
- **T013·T016·T018·T020** 데이터 계층 일부 병렬
- **T028·T029** / **T042~T045** / **T053** / **T057** 컴포넌트는 파일이 달라 병렬
- 각 스토리의 테스트(T027·T035·T041·T052·T056)는 서로 다른 파일이라 병렬
- 인력이 여럿이면 **US1 완료 후 US2·US3·US5를 동시에** 진행할 수 있다

---

## 병렬 실행 예시: Phase 2 도메인 모델

```bash
# 도메인 모델 6건을 함께 생성:
Task: "core/domain/.../model/DeckSort.kt에 DeckSort enum 생성"
Task: "core/domain/.../model/PlaceLabel.kt에 PlaceLabel enum 생성"
Task: "core/domain/.../model/PlaceCard.kt에 PlaceCard·Registrant 생성"
Task: "core/domain/.../model/Deck.kt에 Deck·DeckKey 생성"
Task: "core/domain/.../model/RoomSummary.kt에 RoomSummary 생성"
Task: "core/domain/.../model/NextDeck.kt에 DeckContext·NextDeck 생성"
```

## 병렬 실행 예시: 사용자 스토리 3 컴포넌트

```bash
Task: "feature/home/.../component/SortChipRow.kt 작성"
Task: "feature/home/.../component/HomeTooltipOverlay.kt 작성"
Task: "feature/home/.../component/AllExhaustedContent.kt 작성"
Task: "feature/home/.../component/EmptyContent.kt 작성"
```

---

## 구현 전략

### MVP 우선 (US1만)

1. Phase 1 셋업
2. Phase 2에서 US1이 쓰는 것만 — T003·T005·T006·T007·T011~T021·T023·T024·T025·T026
3. Phase 3 US1 완료
4. **중단하고 검증**: 덱이 뜨고 스와이프가 동작하는지
5. 이 시점에 홈이 "카드를 넘겨 보는 화면"으로는 완성된다

### 점진적 전달

1. 셋업 → 기반은 끝나는 것부터 스토리에 공급
2. **US1** → 덱 탐색 (MVP)
3. **US3** → 자동 전환. **이 스펙의 값어치는 여기서 나온다**
4. **US2** → 카드 액션·상세 진입
5. **US4** → 방 직접 변경
6. **US5** → 최초 진입 가이드

> US3을 US2보다 먼저 두는 것을 권한다. spec이 US3을 핵심으로 지목했고, 자동 전환 없이는 덱 하나를 소진한 사용자가 막힌 화면을 보게 된다.

### 팀 병렬 전략

1. 함께 Phase 1·2를 끝낸다 — 특히 **T010(전환 UseCase)** 을 먼저 낸다
2. 이후:
   - 개발자 A: US1 → US3 (덱과 전환은 같은 ViewModel 분기를 만져 한 사람이 낫다)
   - 개발자 B: US2 → US4
   - 개발자 C: US5 + Phase 8의 Figma 대조

---

## 참고 사항

- 피해야 할 것: 모호한 작업, 같은 파일 동시 수정, 스토리 독립성을 깨는 의존성
- **`HomeViewModel`은 여러 스토리가 함께 만지는 유일한 파일이다.** T032·T037·T038·T046~T051·T055·T058·T059가 모두 여기 들어가므로 병렬 배정 시 충돌에 주의한다
- 커밋 단위는 [`commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다
- **미결 사항 1건 해소(2026-08-29)**: `GET /api/v1/rooms/{roomId}/cards`가 배포되어 T066~T071로 mock을 걷고 실제 구현으로 교체했다. 전환 지점 셋은 [contracts/deck-api.md](./contracts/deck-api.md) §4가 적은 그대로였고 그 밖은 손대지 않았다. 남은 것은 실기기 재검증(T072)뿐이다
