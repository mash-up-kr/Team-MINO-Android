# 리서치: 방 상세 (Room Detail)

**대상 spec**: [spec.md](./spec.md) 2.1.3 · **대상 plan**: [plan.md](./plan.md)

이 문서는 room-detail feature 안에서만 유효한 설계 선택을 담는다. 다른 feature에도 구속력을 갖는 결정은 완료 보고에서 ADR 승격을 제안한다.

---

## D1. `RoomDetailMain`은 `:feature:room`의 `detail/` nested Route다 (신규 모듈 아님)

- **Decision**: 방 상세([SCR-005])를 별도 진입형 feature/모듈로 만들지 않고, room-list([SCR-004])와 같은 `:feature:room` 모듈 안에 `detail/` 패키지(`docs/architecture/feature-module.md` 2장 "탭 feature" 패키지 구조)를 두고 `RoomDetailMain` Route로 구현한다.
- **Rationale**: room-list plan 1.2.0의 D5는 방 상세를 Activity 기반 진입형 feature로 모델링했었지만, 사용자가 PR #186·#234 리뷰에서 이 결정을 직접 뒤집었다 — "저장 탭 → 방 리스트 → 바텀시트로 방 상세 진입은 하나의 Activity여야 한다. 방 리스트와 방 상세는 별개 기능이 아니다." 이 결정은 room-list plan 2.0.0([room-list/research.md D13](../room-list/research.md))에 기록돼 있고, 이 plan은 그 위에서 room-detail 쪽 설계를 채운다.
- **Alternatives considered**: 별도 진입형 모듈(`:feature:roomdetail`) 유지 — 기각(사용자 결정으로 번복, 근거는 room-list D13). 이 plan에서 다시 검토하지 않는다.
- **(plan 1.0.0에서 결정 — room-list plan 2.0.0 D13을 그대로 반영)**

## D2. 화면 전환 — `navController.navigate`/`popBackStackIfResumed`, `RoomDetailLauncher`는 두지 않는다

- **Decision**: `RoomListMain`에서 방 카드 선택 시 `navController.navigate(RoomDetailMain(roomId))`로 진입하고([FR-006] room-list 소관), `[X]` 클릭 시 `popBackStackIfResumed(entry)`로 복귀한다([FR-004]). `:core:navigation`에 `RoomDetailLauncher`·`EXTRA_ROOM_DETAIL_ROOM_ID` 같은 크로스 feature 계약을 두지 않는다.
- **Rationale**: `feature-navigation.md` 2장의 표준 feature 내부 Route 전환 패턴을 그대로 따른다. `RoomListMain`이 백스택에 남아 있는 동안 그 `RoomListViewModel`(및 `sheetLevel`)은 NavHost가 그대로 보존하므로, **UX-003(방 상세 ↔ 방 리스트 이동 시 바텀시트 단계 유지)과 EC-007(room-list 쪽 표기)이 별도 result 계약·시작 인자 없이 해결된다** — room-list의 `sheetLevelOverride` 설계(plan 1.2.0)가 이 재검토로 완전히 사라진 이유다.
- **Alternatives considered**: Activity result로 시트 상태를 되돌려 보낸다 — 기각(D1이 이미 Activity 계약 자체를 없앴다). 방 상세 진입/복귀를 별도 SideEffect 없이 room-list ViewModel이 직접 조율 — 기각. NavHost가 이미 백스택 보존으로 상태를 지켜주므로 ViewModel 간 조율 로직을 추가로 둘 이유가 없다.
- **(plan 1.0.0에서 결정)**

## D3. 바텀 네비게이션 숨김은 `:core:navigation`의 `ImmersiveRoute` 마커 인터페이스로 판정

- **Decision**: `:core:navigation`에 빈 마커 인터페이스 `ImmersiveRoute`를 신설한다. `RoomDetailMain`이 이 마커를 구현한다. `:feature:main`의 `MainShell`은 `navController.currentBackStackEntryAsState()`의 `destination`이 이 마커를 구현하는 목적지인지만 검사해 `MinoScaffold`의 `bottomBar` 슬롯을 조건부로 그린다.
- **Rationale**: [FR-003] 등 몰입 화면 요구([SYS-005] Flow B "방 상세·장소 상세 등 몰입 화면에서는 감춘다")를 만족하려면 `RoomDetailMain`이 nested Route가 된 이상(D1) `MainShell`이 이 목적지를 알아야 바텀바를 숨길 수 있다. 구체 타입이나 feature 이름을 하드코딩하면 `feature-module.md` 3장이 금지하는 "탭 셸이 하위 feature 화면 구성을 아는" 결합이 되므로, 양쪽이 이미 의존하는 공용 모듈(`:core:navigation`)의 빈 마커 인터페이스로 간접화한다 — `MainShell`은 `ImmersiveRoute` 타입만 알면 되고 `:feature:room` 모듈 자체를 몰라도 된다(Gradle 의존 방향 유지, `:feature:main`은 이미 `:core:navigation`·각 탭 모듈에 의존).
- **Alternatives considered**: `MainShell`이 `RoomDetailMain` 구체 Route를 직접 참조해 분기 — 기각(위 결합 문제 재현). 몰입 화면 여부를 `RoomListUiState`/`RoomDetailUiState`의 필드로 올려 셸에 전달 — 기각. 화면 전환마다 셸-화면 간 별도 통신 경로를 새로 만들어야 해서, 이미 셸이 구독 중인 `currentBackStackEntryAsState()` 목적지 판별보다 무겁다.
- **다른 feature에도 구속력을 갖는 결정**: `ImmersiveRoute`는 `:core:navigation`의 신규 공개 API이자, 이후 몰입 화면(예: [SCR-006] 장소 상세)을 만드는 모든 feature가 따라야 하는 패턴이다 — room-detail 안에서만 유효한 선택이 아니다. **완료 보고에서 ADR 승격을 제안한다**(`mino-plan` SKILL.md 「research.md와 ADR의 경계」).
- **(plan 1.0.0에서 결정 — room-list plan 2.0.0의 [research.md D14](../room-list/research.md)가 이 결정을 참조한다)**

## D4. 정렬 드롭다운·카테고리 칩은 room-list가 정의한 domain enum을 재사용한다

- **Decision**: [FR-005] 정렬 드롭다운(`꾹 Pick`/`전체`/`최신순`/`거리순`/`코멘트순`)은 room-list의 `MapMarkerSortOption`(`:core:domain`)을, [FR-006] 카테고리 칩(`전체`/`카페`/`음식점` 3종 고정)은 room-list의 `PlaceCategoryFilter`(`:core:domain`)를 그대로 재사용한다. 새 enum을 만들지 않는다.
- **Rationale**: `spec.md FR-005`의 근거가 "PRD [SCR-004]/[SCR-005] 공통 정렬 드롭다운 정의"라고 명시한다 — 같은 값 집합을 다루는 같은 개념이라, room-list가 이미 `:core:domain`에 정의해 둔 두 enum([room-list/data-model.md §1](../room-list/data-model.md))을 그대로 쓰는 것이 헌법 원칙 I(SSOT)에 맞는다. `MapMarkerSortOption`의 항목 순서(`ALL, GGUK_PICK, LATEST, NEARBY, MOST_COMMENTED`)와 spec.md가 요구하는 표시 순서(`꾹 Pick`/`전체`/…)가 다르지만, 순서는 값의 나열이 아니라 UI 컴포저블이 렌더링할 때 결정하는 표현 관심사라 enum 자체를 바꿀 이유가 아니다.
- **Alternatives considered**: room-detail 전용 `PlaceSortOption`·`PlaceCategoryFilter`를 새로 만든다 — 기각. 같은 값 집합을 두 곳에 정의하면 항목이 갈릴 위험이 생기고(헌법 원칙 I), room-list 쪽이 항목을 추가·변경할 때 room-detail이 따라가지 못한다.
- **(plan 1.0.0에서 결정)**

## D5. 시트 단계(Peek/Half/Full)는 room-list와 동일하게 화면 상태이며, `BottomSheetLevel` 타입도 재사용한다

- **Decision**: `Peek`/`Half`/`Full`은 `RoomDetailMain`의 `RoomDetailUiState.sheetLevel: BottomSheetLevel` 필드로 모델링한다(별도 Route 아님). `BottomSheetLevel` 타입은 room-list가 이미 정의한 `feature/room/main/model/BottomSheetLevel.kt`를 그대로 import해서 쓰고, `detail/` 아래 새로 정의하지 않는다.
- **Rationale**: room-list의 [D2](../room-list/research.md)와 완전히 같은 논리다 — 세 단계는 같은 화면의 밀도 변화일 뿐 뒤로가기 스택에 남길 목적지가 아니다([spec.md FR-002](./spec.md)). `RoomDetailMain`이 room-list와 같은 `:feature:room` 모듈 안에 있으므로(D1) `BottomSheetLevel`은 모듈 경계를 넘지 않는 재사용이라 중복 정의를 피할 수 있다.
- **Alternatives considered**: `detail/model/`에 `BottomSheetLevel`을 별도로 정의 — 기각. 같은 모듈 안에서 완전히 같은 개념(`Peek`/`Half`/`Full` 3단계)을 두 번 정의하는 것은 헌법 원칙 I 위반이다.
- **(plan 1.0.0에서 결정)**

## D6. 리스트형/카드형 뷰 토글은 신규 UI 전용 enum `PlaceViewType`

- **Decision**: [FR-007] 리스트형/카드형 뷰 토글 상태는 `:core:domain`이 아니라 `feature/room/detail/model/PlaceViewType.kt`(`enum class PlaceViewType { LIST, CARD }`)로 둔다. `RoomDetailUiState.viewType` 필드로 노출한다.
- **Rationale**: 뷰 형태는 비즈니스 개념이 아니라 이 화면이 장소를 어떻게 렌더링할지 결정하는 순수 UI 상태다(`core/domain/README.md` §3 — 비즈니스 개념만 domain). room-list의 `BottomSheetLevel`도 같은 이유로 `:core:domain`이 아니라 화면 모듈에 있다(D5 참고).
- **Alternatives considered**: `:core:domain`에 두어 다른 화면([SCR-006] 장소 상세 등)과 공유 — 기각. 아직 두 번째 소비자가 실존하지 않는 시점에 domain으로 올리면 `core/common/ui/README.md` §5가 경계한 "검증되지 않은 API를 공용 표면으로 굳히는" 실수가 된다([room-list/research.md D10](../room-list/research.md)과 같은 논리).
- **(plan 1.0.0에서 결정)**

## D7. `Place` 도메인 모델 신설 위치

- **Decision**: `:core:domain`에 `model/Place.kt`(`Place` 데이터 클래스)를 새로 둔다. 필드는 장소 카드/리스트 렌더링에 필요한 것으로 한정한다 — 장소 상세([SCR-006]) 화면 자체의 구성(코멘트·이미지 갤러리 등)은 이 spec의 비목표([spec.md §3.2](./spec.md))라 그 범위의 필드는 담지 않는다.
- **Rationale**: `core/domain/README.md` §3 — 비즈니스 개념을 표현하는 순수 Kotlin 타입은 `model/`에 둔다. `Place`는 room-detail뿐 아니라 이후 [SCR-006] 장소 상세·room-list 지도 마커([room-list/spec.md FR-011])도 잠재적으로 다루는 개념이라 `:feature:room` 로컬이 아니라 `:core:domain`이 SSOT여야 한다(room-list의 `Room`을 D3에서 domain에 둔 것과 같은 논리).
- **Alternatives considered**: `:feature:room/detail/model/`에 로컬 모델로 두기 — 기각. room-list 지도 마커가 이미 "장소" 단위 데이터를 다루고 있어([room-list/contracts/room-repository.md](../room-list/contracts/room-repository.md) "카테고리 필터는 지도 마커(장소 단위) 대상") 두 번째 소비자가 이미 잠재해 있다.
- **(plan 1.0.0에서 결정)**

## D8. `PlaceRepository` 신설

- **Decision**: `:core:domain`에 `repository/PlaceRepository.kt`를 신설한다. `observePlaces(roomId): Flow<List<Place>>`(방 상세 장소 목록 실시간 관찰), `sharePlaces(placeId, targetRoomIds): Unit`([SYS-003] 다른 방에 공유), `deletePlace(placeId): Unit`([FR-010] 장소 삭제) 세 메서드를 갖는다. 상세 시그니처는 [contracts/place-repository.md](./contracts/place-repository.md) 참조.
- **Rationale**: room-list의 `RoomRepository`와 같은 패턴 — 화면이 소비하는 Repository 인터페이스를 UI 계약으로 삼는다([room-list/contracts/room-repository.md](../room-list/contracts/room-repository.md) 서문과 동일 근거, 백엔드 draft 단계). `sharePlaces`·`deletePlace`는 [FR-009]·[FR-010]이 이 화면에서 직접 트리거하는 동작이라 room-detail이 그 계약의 최초 정의자가 되는 것이 자연스럽다.
- **Alternatives considered**: 공유·삭제를 `RoomRepository`에 얹는다 — 기각. `RoomRepository`는 방 단위 데이터를 다루고, 공유·삭제는 장소 단위 동작이라 책임이 섞인다(room-list [contracts/room-repository.md](../room-list/contracts/room-repository.md)도 "카테고리 필터는 place-repository 소관"이라고 이미 경계를 그어 뒀다).
- **(plan 1.0.0에서 결정)**

## D9. [SYS-001] 방 편집은 `RoomFormLauncher`를 편집 모드로 재사용한다

- **Decision**: [FR-012] 더보기→[방 편집] 클릭(방장 전용) 시 room-list가 이미 선언한 `RoomFormLauncher`(`:core:navigation`)를 결과 콜백과 함께 호출한다. 새 Launcher를 만들지 않는다.
- **Rationale**: PRD [SYS-001]이 "생성과 편집이 같은 화면을 공유"한다고 명시하고, room-list [research.md D6](../room-list/research.md)이 이미 이 계약을 크로스 feature 의존성으로 선언해 뒀다. room-detail이 또 다른 편집 전용 Launcher를 만들면 같은 화면을 두 계약이 가리키게 된다(헌법 원칙 I 위반).
- **미구현 의존성**: `RoomFormLauncher`의 실제 구현(`:feature:roomform`)이 아직 없다는 사정은 room-list와 동일하다. 다만 **편집 모드 진입에 필요한 구체 계약(기존 방 값을 어떻게 넘기고, 완료 스낵바 문구(`방 편집이 완료되었어요`, [FR-012])를 room-detail이 어떻게 트리거하는지 — extra 키·result 스키마)은 room-list의 D6이 정의한 생성 전용 계약에는 없다.** 이 확장은 `:feature:roomform`의 plan이 만들어질 때 함께 확정해야 하는 사항이라 `[TBD]`로 남긴다.
- **Alternatives considered**: room-detail이 편집 폼을 로컬로 구현 — 기각(`spec.md §3.2`가 이미 "공동방 생성/편집 폼의 입력 필드·검증 규칙은 [SYS-001] spec이 정의"라고 범위를 나눔).
- **(plan 1.0.0에서 결정)**

## D10. [SYS-003] 다른 방에 공유 시트는 `:feature:room/detail/component/`의 내부 바텀시트다

- **Decision**: [FR-009] [다른 방에 공유] 클릭 시 여는 방 선택 시트(`Peek`(디폴트)/`Full` 2단 드래그,
  [spec.md 유저 플로우 3](./spec.md) — 2.2.0에서 `Full` 676dp 단일 고정 서술을 디자이너 확인 거쳐
  정정)를 `RoomSelectSheet`(`detail/component/`)로 구현한다. `:core:navigation` Launcher 계약을
  두지 않는다.
- **Rationale**: 이 시트는 "탐색 중이던 화면 Context를 유지"([PRD SYS-003])해야 하고 높이가 고정된 모달형 바텀시트라, `feature-module.md` 1장이 구분하는 "진입형 feature"(Activity 진입점·독립 플로우)의 특징과 맞지 않는다 — room-list의 자체 3단 바텀시트(`RoomListBottomSheet`)가 `:feature:room` 내부 컴포넌트인 것과 같은 성격이다. 데이터 소스는 `RoomRepository.observeMyRooms()`(room-list가 이미 정의, [room-list/contracts/room-repository.md](../room-list/contracts/room-repository.md))를 그대로 재사용할 수 있다 — 방 목록 자체는 이미 SSOT가 있다.
- **미구현/미확정 사항**: "이미 저장된 방을 체크된 채 비활성으로 표시"하는 규칙은 spec이 확정했지만([spec.md EC-004](./spec.md)), **실제 복제 실행 API(어떤 요청으로 여러 방에 한 번에 저장하는지)는 이 spec 범위 밖([SYS-003] 전용 spec 소관, `spec.md §3.2`)이라 `PlaceRepository.sharePlaces`의 서버 계약은 `[TBD]`로 남긴다**(구현 시 목데이터로 메운다, room-list D12와 같은 패턴). [시트의 [새 방 만들기] 버튼 → SYS-001 재호출](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8) 흐름은 PRD [SYS-003] Flow B에 있으나 `spec.md`의 FR/유저 플로우에는 명시돼 있지 않다 — spec에 없는 요구사항이라 이 plan도 추가하지 않는다(spec 개정이 필요하면 `/mino-spec` 몫).
- **Alternatives considered**: `:core:navigation`에 `RoomSelectLauncher` 계약을 만들고 향후 [SCR-006] 장소 상세도 같은 시트를 쓰게 대비 — 기각. 아직 두 번째 소비자가 이 plan 범위 안에 없고, 그 경우도 Activity가 아니라 컴포넌트 승격(→ `:core:common:ui`) 문제라 Launcher 패턴과 무관하다.
- **(plan 1.0.0에서 결정)**

## D11. [SYS-006] 초대 시트는 `:feature:room/detail/component/`의 내부 바텀시트다

- **Decision**: [FR-011] [친구 +] 클릭 시 여는 초대 바텀시트(424dp 고정, 참여자 목록 스크롤 288dp, [spec.md 유저 플로우 4](./spec.md))를 `RoomInviteSheet`(`detail/component/`)로 구현한다. `:core:navigation` Launcher 계약을 두지 않는다.
- **Rationale**: PRD [SYS-006] Flow B가 이 진입을 "바텀시트"로 명시하고, 이미 방 상세라는 화면을 벗어나지 않는 오버레이라 D10과 같은 논리다.
- **미확정 사항**: 참여자 목록의 데이터 소스(`RoomMemberSummary`는 room-list가 카드용 4개 미리보기로 정의했을 뿐 전체 참여자 목록·역할 정보는 없음)와 **초대 링크 생성·클립보드 복사·OS 공유 시트 연동의 실제 API 계약은 [SYS-006] 전용 spec이 이 저장소에 아직 없어 확정할 근거가 없다.** `RoomInviteSheet`가 소비할 참여자 목록 타입(`RoomMember`? 기존 `RoomMemberSummary` 확장?)과 초대 링크 발급 Repository는 **`[TBD]`로 남긴다.**
- **Alternatives considered**: `:core:navigation`에 `RoomInviteLauncher`(온보딩의 [SYS-006] Flow A "전체 화면"과 공유) — 기각. Flow A(온보딩)는 전체 화면이고 Flow B(방 상세)는 바텀시트로 화면 형태 자체가 달라([PRD SYS-006] "진입점에 따라 화면 형태·문구·후속 동작이 다르다"), 하나의 Launcher/화면으로 통합할 근거가 없다. 이 spec은 Flow B만 다룬다.
- **(plan 1.0.0에서 결정)**

## D12. [SYS-007] 나가기·위임은 `:feature:room/detail/component/`의 내부 확인·위임 모달이다

- **Decision**: [FR-013] 더보기→[나가기] 클릭 시 여는 확인 모달(일반 멤버, PRD Flow A)·위임 모달(방장, PRD Flow B)을 각각 `RoomLeaveConfirmDialog`·`RoomOwnerLeaveDialog`(`detail/component/`)로 구현한다. 나가기 완료 후 [SCR-004] 방 리스트로 이동은 크로스 feature 전환이 아니라 `popBackStackIfResumed(entry)`(같은 `:feature:room` 그래프 안에서 `RoomDetailMain` → `RoomListMain`으로의 복귀, D2와 동일한 메커니즘)로 처리한다.
- **Rationale**: 확인·위임 모달은 다이얼로그이지 독립 화면이 아니다. 나가기 후 이동 대상이 하필 [SCR-004]인 것도, 방 상세가 애초에 room-list 백스택 위에 쌓인 nested Route이므로(D1) 자연스럽게 pop으로 표현된다 — `[X]`로 나가는 것과 같은 전환 메커니즘이다.
- **미확정 사항**: 실제 나가기·권한 위임 API 계약(멤버 목록 조회, 위임 대상 선택 후 요청 스키마)은 [SYS-007] 전용 spec이 이 저장소에 아직 없어 **`[TBD]`로 남긴다.**
- **Alternatives considered**: 나가기 완료 후 `navController.navigate(RoomListMain, popUpTo(...))`로 명시적 재진입 — 기각. `RoomListMain`이 이미 백스택에 살아있는 상태([D2](#d2-화면-전환--navcontrollernavigatepopbackstackifresumed-roomdetaillauncher는-두지-않는다))라 `popBackStackIfResumed`만으로 충분하고, `navigate`는 불필요한 새 인스턴스를 만든다.
- **(plan 1.0.0에서 결정)**

## D13. 더보기 메뉴·정렬 드롭다운·카테고리 칩은 `:core:design-system`의 `MinoMenu`·`MinoChip`을 그대로 쓴다

- **Decision**: 화면 더보기[⋮] 메뉴([FR-013] 방 편집/나가기), 장소 카드 더보기[...] 메뉴([FR-008] 다른 방에 공유/삭제), 정렬 드롭다운([FR-005]), 카테고리 칩([FR-006])은 모두 `MinoMenu`·`MinoChip`(`:core:design-system`)을 조립해 그린다. `:feature:room/detail/`에 새 드롭다운·메뉴·칩 컴포넌트를 만들지 않는다.
- **Rationale**: room-list [research.md D11](../room-list/research.md)이 이미 같은 결론을 냈다 — `MinoMenu`·`MinoChip`이 이미 항목 리스트·선택 상태를 다루는 범용 API라 그대로 재사용 가능하다. room-detail이 새로 만들면 두 구현이 갈린다(헌법 원칙 I).
- **Alternatives considered**: room-detail 전용 메뉴 컴포넌트를 새로 만든다 — 기각(위 근거, room-list D11과 동일).
- **(plan 1.0.0에서 결정)**

## D14. 배포된 서버 API 대조 — `Pin`이 `Place`를 감싸는 실제 응답 구조, `sharePlaces` 계약 확정

- **Decision**: 배포된 라이브 API(`https://api.gguk.org`, `GET /api-docs-json`, 조회 2026-08-27T20:54:28+09:00)를 대조한 결과, 서버는 `Place`를 단독으로 내려주지 않고 `Pin { id, roomId, place: Place, images: string[], createdBy, createdAt }`이 감싼다(`GET /api/v1/pins?roomId=`·`GET /api/v1/pins/{pinId}`, tag `pin`). `Place`에는 `id·provider·providerPlaceId·name·address·city·district·lat·lng·category·phone·mapUrl·createdAt·updatedAt`이 있고 `images`는 없다(핀으로 이동). `data-model.md`의 `Place.thumbnailUrl`은 `Pin.images.firstOrNull()`에서, `Place.savedAt`은 `Pin.createdAt`에서 온다 — **domain `Place`는 서버 `Place`가 아니라 `Pin`+`Place`를 합쳐 만든다.**
  `PlaceRepository.sharePlaces`의 서버 계약도 확정됐다: `POST /api/v1/pins/{pinId}/duplicate`, body `{ roomIds: string[] (minItems 1) }`, 대상 방 중 하나라도 같은 장소가 이미 저장돼 있으면 `409 DUPLICATE_PIN_IN_ROOM`으로 전체 거절. **호출 식별자는 `placeId`가 아니라 `pinId`다**(엔드포인트가 `/pins/{pinId}/...`) — D8·[contracts/place-repository.md](./contracts/place-repository.md)의 시그니처를 이에 맞춰 개정한다(plan 2.0.0).
  `deletePlace`에 대응하는 `DELETE /api/v1/pins/{pinId}` 같은 엔드포인트는 이 API 문서에 **없다** — D10이 남긴 "복제 실행 API는 TBD"라는 문장은 해소됐지만, 삭제 쪽은 여전히 서버 미구현이라 `[TBD]`로 남는다.
- **Rationale**: `mino-plan` SKILL.md 「서버 API 대조」— 서버 엔드포인트를 쓰는 계약은 추정하지 않고 배포된 문서를 조회해 근거로 삼는다. D8·D10 작성 시점에는 이 저장소의 정적 draft(`docs/swagger.yaml`, room-list D12)만 참고했는데, 그 사이 서버가 실제로 `pin` 태그 5개 엔드포인트를 배포해 draft보다 앞서 있었다.
- **Alternatives considered**: 기존 `Place`·`sharePlaces(placeId, ...)` 시그니처를 그대로 두고 Mapper에서만 흡수 — 기각. `sharePlaces`가 호출하는 실제 엔드포인트가 `pinId`를 요구하므로 Repository 계약 자체가 `pinId`를 받아야 한다(Mapper로 가릴 수 있는 표현 차이가 아니라 계약의 식별자 차이).
- **(plan 2.0.0에서 결정 — D8·D10의 TBD를 부분 해소)**

## D15. `RoomRepository` 확장 — 멤버 목록·나가기·방장 위임 API 확정 ([SYS-007])

- **Decision**: 같은 API 대조에서 `GET /api/v1/rooms/{roomId}/members`(방 멤버 목록, `{userId, nickname, avatar, isOwner, joinedAt}[]`, tag `room`)·`DELETE /api/v1/rooms/{roomId}/members/me`(방 나가기 — 방장+다른 멤버 존재 시 `409 OWNER_TRANSFER_REQUIRED`, 방장+마지막 멤버면 방 자동 삭제)·`PUT /api/v1/rooms/{roomId}/owner`(방장 위임, body `{ nextOwnerId: uuid }`)가 이미 배포돼 있는 것을 확인했다. D12가 "[SYS-007] 나가기·권한 위임 API 계약은 TBD"로 남긴 지점이 해소됐다 — [contracts/room-detail-main-contract.md](./contracts/room-detail-main-contract.md)의 나가기 흐름 분기 규칙(일반 멤버/1인 방장/N인 방장)이 이 세 엔드포인트의 상태 코드(`200`/`409 OWNER_TRANSFER_REQUIRED`)와 그대로 맞아떨어진다 — **설계를 바꿀 필요 없이 API 근거만 채운다.**
  이 세 메서드는 방 단위 동작(API tag도 `room`)이라 D8이 그은 "방 단위는 `RoomRepository`, 장소 단위는 `PlaceRepository`" 경계를 따라 **room-list가 정의한 `RoomRepository`(`:core:domain`)에 추가한다** — 새 Repository를 만들지 않는다.
- **크로스 spec 영향**: `RoomRepository`의 계약 문서(`docs/specs/room-list/contracts/room-repository.md`)는 room-list plan이 소유한다. 이 plan(room-detail)은 그 인터페이스에 메서드를 추가해야 하지만, `mino-plan` SKILL.md 범위 가드가 다른 spec 디렉터리 수정을 막는다 — **room-list 쪽 계약 문서 갱신은 이 plan이 하지 못하고, 완료 보고에서 후속 조치로 남긴다.** `:core:domain`의 실제 인터페이스 파일 자체는 room-detail의 `/mino-task`가 손대되, 계약 문서의 정합성은 room-list 담당자가 별도로 반영해야 한다.
- **Rationale**: PRD [SYS-007]이 요구하는 동작(위임 없이 나가기 시도 시 막힘, 마지막 1인 방장은 방 삭제)이 서버 에러 코드로 이미 표현돼 있어, 클라이언트는 `409 OWNER_TRANSFER_REQUIRED`를 `LeaveDialogState.DelegateOwner`로 분기하면 된다(별도 사전 조회로 멤버 수를 세지 않아도 서버가 상태를 판정해 준다 — 클라이언트 로직 단순화).
- **Alternatives considered**: 멤버 수·방장 여부를 클라이언트가 미리 계산해 분기 — 기각. 서버가 나가기 시도 자체에 대해 `409`로 판정 결과를 내려주므로 클라이언트가 같은 규칙을 중복 구현할 이유가 없다(SSOT는 서버).
- **(plan 2.0.0에서 결정 — D12의 TBD를 해소)**
- **뒤집힘 (2026-08-30)**: "멤버 수를 사전에 세지 않는다" 부분은 실기기 확인 결과 폐기했다 — `409`를 기다리는 동안 공유방 방장에게도 `ConfirmOwnerSingle`("혼자라 삭제된다")의 잘못된 문구가 먼저 보이는 결함으로 드러나, 이미 로드돼 있는 `RoomMemberSummary`로 `OnLeaveClick` 시점에 바로 분기하도록 되돌렸다. `409` 자체(엔드포인트·상태 코드)는 그대로 유효하고 방어선으로 남아 있다. 상세: [failures/2026-08-30-leave-flow-member-count.md](../../failures/2026-08-30-leave-flow-member-count.md).

## D16. 초대 링크 발급 API 확정 ([SYS-006]) — `RoomMember` 신규 도입

- **Decision**: `POST /api/v1/rooms/{roomId}/invitations`(멤버당 1개, 이미 발급했으면 같은 `code` 재반환, `code`는 대문자+숫자 6자 `^[A-Z0-9]{6}$`, 클라이언트가 `gguk.org/r/{code}`로 조립. 개인방은 `403 PERSONAL_ROOM_NOT_ALLOWED`)이 이미 배포돼 있다. D11이 "초대 링크 생성 API·참여자 목록 타입은 TBD"로 남긴 두 지점 중 API는 해소됐다. 참여자 전체 목록은 D15가 확인한 `GET /api/v1/rooms/{roomId}/members` 응답(`{userId, nickname, avatar, isOwner, joinedAt}`)을 그대로 쓸 수 있어, `data-model.md §4`가 가칭으로 남겼던 `RoomMember`를 이 스키마로 확정한다(`RoomMemberSummary`와는 별개 타입 — 후자는 카드 미리보기 4개 한정용).
- **Rationale**: `RoomMember`는 `RoomInviteSheet`(초대 참여자 목록)와 D15의 위임 대상 선택 모달(`OnOwnerDelegateSelected`)이 공유하는 개념이라 — 이미 D15가 같은 `GET /members` 응답을 근거로 삼았으므로 domain 타입도 하나로 합친다(헌법 원칙 I).
- **Alternatives considered**: `RoomMemberSummary`를 확장해 `isOwner`·`joinedAt`을 추가 — 기각. `RoomMemberSummary`는 "최대 4개 아바타 + overflow count"라는 카드 미리보기 전용 축약 표현이라(room-list 계약) 전체 목록과 필드 의미가 다르다. 하나의 타입에 두 용도를 억지로 합치면 카드 미리보기 소비자가 안 쓰는 필드를 받게 된다.
- **(plan 2.0.0에서 결정 — D11의 TBD를 해소)**

---

## NEEDS CLARIFICATION 해소 현황 (plan 2.0.0 기준)

plan 1.0.0이 남겼던 미확정 항목은 D14~D16의 서버 API 대조로 대부분 해소됐다. 남는 진짜 미확정은 다음 두 가지뿐이다.

- **`deletePlace`([FR-010] 장소 삭제) 서버 계약** — 배포된 API에도 `DELETE /pins/{pinId}` 같은 엔드포인트가 없다(D14). 서버 미구현이 확인된 것이지 설계 공백이 아니다.
- **`RoomFormLauncher` 편집 모드의 extra 키·result 스키마**([D9](#d9-sys-001-방-편집은-roomformlauncher를-편집-모드로-재사용한다)) — 서버 쪽 `PATCH /api/v1/rooms/{roomId}`(name·description·color)는 이미 있지만(room-list의 `RoomRepository.updateRoom`이 이미 이 엔드포인트를 쓴다), 이건 서버 API 문제가 아니라 **클라이언트 크로스 feature 계약**(Activity result 스키마) 문제라 `:feature:roomform`의 plan이 아직 없어 계속 확정할 수 없다.
