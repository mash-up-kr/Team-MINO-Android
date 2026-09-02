# 리서치: 장소 상세 & 코멘트 (Place Detail & Comments)

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [plan.md](./plan.md)

각 항목은 어느 plan 버전에서 결정되었는지를 함께 적는다. 뒤집힌 결정은 지우지 않고 취소선과 재검토 표시를 남긴다.

---

## ~~D1. 장소 상세는 진입형 feature 모듈 `:feature:placedetail`이다~~ — 재검토됨(plan 2.0.0)

> **D17로 대체되었다.** 아래 본문은 당시의 판단 근거를 남기기 위한 것이며 현행 설계가 아니다.

- **Decision**: 장소 상세([SCR-006])를 자체 Activity를 갖는 **진입형 feature 신규 모듈**로 만든다([feature-module.md 1장](../../architecture/feature-module.md)). 모듈 이름은 `:feature:placedetail`, 패키지는 `team.mino.feature.placedetail`이다.
- **Rationale**: 사용자가 이번 대화에서 직접 정했다. 검토 과정은 이랬다 — 먼저 `feature/154-room-list/base` 브랜치의 room-list·room-detail 설계를 확인해, 그 둘이 `:feature:room` 단일 모듈 안의 형제 Route이고 Activity가 없는 **탭 feature**임을 확인했다. 이어서 장소 상세를 (A) 같은 그래프의 세 번째 Route로 두는 안과 (B) 방 상세 화면의 상태로 두어 지도를 유지한 채 시트만 교체하는 안을 비교했다. spec 3.0.0의 여러 요구사항이 (B)를 가리켰으나, 사용자가 진입형 Activity를 선택했다.
- **선택의 이점**: 방 상세(이슈 #161)가 아직 머지되지 않았고 `:feature:room` 자체가 이 워크트리에 존재하지 않는다. 진입형은 그 모듈의 `RoomDetailUiState`·`Intent`·`SideEffect` 계약을 건드리지 않으므로 #161과의 순서 조율 없이 진행할 수 있다.
- **감수하는 비용**(측정값이 아니라 구조에서 오는 예상):
  - 방 상세 → 장소 상세 전환에서 `MapView`가 새로 만들어진다. 타일 리로드와 카메라 점프가 보일 수 있고, FR-002·TS-002가 요구하는 "마커가 선택 핀으로 바뀌며 카메라가 그 장소로 이동"이 같은 지도 위의 연속 전이가 아니라 화면 교체가 된다.
  - [현재 위치]·[저장된 방] 지도 오버레이 컨트롤을 방 상세와 장소 상세가 각각 갖는다.
  - FR-009([나가기] → 지금 보고 있는 방의 방 상세)를 Activity 전환으로 따로 배선해야 한다 → D2.
- **Alternatives considered**:
  - (A) `:feature:room`의 세 번째 Route(`PlaceDetailMain`) — 기각(사용자 결정). 지도 재생성 문제는 (B)와 달리 해결하지 못하면서 #161 의존만 생긴다.
  - (B) 방 상세 화면의 상태(`RoomDetailUiState.selectedPlace`)로 두고 지도 유지 — 기각(사용자 결정). FR-002(같은 지도 위 전이)·FR-023(지도 오버레이 버튼)·FR-009(경로 무관 복귀가 상태 초기화만으로 성립)·FR-025(전환이 상태 갱신)가 이 안을 가리켰고, FR-020(바텀바 숨김)도 `RoomDetailMain`의 `ImmersiveRoute`로 이미 해결돼 있었다. 대신 홈·알림 탭 진입 시 `RoomDetailMain(roomId, placeId)` 인자 확장이 필요하고, 시트 앵커 집합이 방 상세(`Peek`/`Half` 256/`Full`)와 장소 상세(`Half` 369/`Full`)로 갈려 한 시트 호스트가 둘을 모두 알아야 하며, 한 화면에 정렬·필터·뷰토글과 코멘트·캐러셀·입력이 몰려 ViewModel 분리 설계가 따로 필요했다.
- **(plan 1.0.0에서 결정)**

## ~~D2. 진입 계약은 `PlaceDetailLauncher` + `pinId`, [나가기]는 이번 범위에서 `finish()`까지만~~ — 재검토됨(plan 2.0.0)

> **D18로 대체되었다.** 아래 본문은 당시의 판단 근거를 남기기 위한 것이며 현행 설계가 아니다.

- **Decision**: `:core:navigation`에 `PlaceDetailLauncher : ActivityLauncher`와 `EXTRA_PLACE_DETAIL_PIN_ID`를 신설한다([feature-navigation.md 1장](../../architecture/feature-navigation.md)). 진입점 네 곳이 모두 `pinId`를 실어 이 Launcher로 연다. **[나가기] (FR-009)는 `finish()`로 호출자에게 돌아가는 데까지만 구현하고**, "지금 보고 있는 방의 [SCR-005] 방 상세 `Half`로 보낸다"는 목적지 배선은 `[TBD]`로 남긴다.
- **Rationale**: 사용자 결정이다. 방 상세 화면이 이 저장소에 아직 없어(이슈 #161 미머지) 목적지가 실존하지 않는다. 없는 화면을 향한 배선을 지금 만들면 검증할 수 없는 코드가 남는다.
- **미해결로 남는 것**: [SCR-005] 방 상세·지도 마커에서 진입한 경우에는 호출자가 곧 목적지라 `finish()`가 우연히 FR-009를 만족한다. 그러나 **[SCR-003] 홈 카드와 [SCR-007] 알림에서 진입하면 호출자가 목적지가 아니므로 FR-009와 어긋난다.** 이 갭은 #161 머지 이후 별도 개정에서 닫는다.
- **Alternatives considered**:
  - `setResult`로 "나갈 때 보고 있던 방"(`EXTRA_PLACE_DETAIL_RESULT_ROOM_ID`)을 돌려주고 호출자가 방 상세로 이동 — 사용자가 기각. 진입형 규약에 맞고 #161과 무관하게 계약을 닫을 수 있었으나, 그 결과를 소비할 호출자가 아직 없어 계약만 떠 있게 된다.
  - 장소 상세가 `RoomDetailLauncher`를 주입받아 직접 이동 — 기각. 방 상세는 탭 feature라 Activity Launcher가 아니고(`MainLauncher` 경유가 필요하다), #161 머지 전에는 계약 자체를 쓸 수 없다.
- **(plan 1.0.0에서 결정)**

## ~~D3. 바텀 네비게이션 숨김(FR-020)은 구현할 것이 없다~~ — 재검토됨(plan 2.0.0)

> **D19로 대체되었다.** 아래 본문은 당시의 판단 근거를 남기기 위한 것이며 현행 설계가 아니다.

- **Decision**: FR-020을 위해 `ImmersiveRoute` 같은 마커 인터페이스를 쓰지 않는다. 별도 구현 없이 충족된다.
- **Rationale**: 바텀 네비게이션은 `:feature:main`의 셸(`MainShell`)이 그리는 chrome이고, 진입형 Activity는 그 셸 밖에서 자기 `PlaceDetailShell`로 뜬다. 화면 위에 바텀바가 존재하지 않으므로 "감춘다"는 동작 자체가 없다.
- **room-detail과 갈리는 지점**: room-detail은 탭 셸 **안**의 Route라 `MainShell`이 바텀바를 조건부로 지워야 했고, 그래서 `:core:navigation`에 `ImmersiveRoute` 마커를 신설하며 "이후 몰입 화면(예: [SCR-006] 장소 상세)이 따라야 하는 패턴"이라고 적었다(`origin/feature/154-room-list/base`의 `docs/specs/room-detail/research.md` D3). **장소 상세가 진입형이 되면서 그 예시는 성립하지 않는다.** `ImmersiveRoute`는 여전히 room-detail에 필요하지만 이 feature의 소비 대상은 아니다.
- **Alternatives considered**: `PlaceDetailMain`이 `ImmersiveRoute`를 구현 — 기각. 판정할 셸이 없어 아무 효과가 없는 선언이 된다.
- **(plan 1.0.0에서 결정)**

## D4. 화면의 식별자는 `pinId`이며, 「지금 보고 있는 방」은 별도 상태가 아니다

- **Decision**: 장소 상세의 진입 인자와 모든 서버 호출의 키를 **`pinId` 하나**로 둔다. spec이 3.0.0에서 도입한 「지금 보고 있는 방」(FR-027)을 `UiState`의 독립 필드로 두지 않고, `pinId`가 가리키는 핀의 `roomId`로 읽는다.
- **Rationale**: 서버가 장소를 **핀 = (장소, 방) 쌍** 단위로 모델링한다 — `GET /api/v1/pins/{pinId}` 응답이 `id`(핀)·`roomId`·`place`(장소)를 함께 담고, 같은 장소가 두 방에 있으면 핀이 둘이다. 그래서 "어느 방의 눈으로 보는가"가 곧 "어느 핀을 여는가"이며, FR-027이 규정한 초기값 결정 규칙(진입 경로가 특정한 방, 알림만 최초 저장 방)은 **진입점이 어느 `pinId`를 싣느냐**로 자연히 해결된다. 클라이언트가 방을 따로 고를 필요가 없다.
- **파급**: FR-025(저장된 방 전환)는 "상태 갱신"이 아니라 **다른 `pinId`로 화면을 다시 여는 것**이 된다. FR-025가 요구하는 전면 초기화(스크롤·캐러셀·초안)와도 맞는다. 다만 그 `pinId`를 알 수 없어 이번 범위에서 빠진다 → D10.
- **Alternatives considered**: `placeId` + `roomId` 두 인자를 싣고 화면이 조합 — 기각. 서버 조회 키가 결국 `pinId`라 두 값에서 핀을 역으로 찾는 단계가 추가된다(그 조회 API도 없다).
- **(plan 1.0.0에서 결정)**

## D5. 시트 2단(`Half`/`Full`)과 헤더 확장·축소는 화면 상태다

- **Decision**: `Half`(369dp 고정)/`Full` 2단은 `PlaceDetailUiState.sheetLevel: PlaceSheetLevel`로, 확장형·축소형 헤더 전환은 `headerMode: PlaceHeaderMode`로 둔다. 둘 다 Route가 아니다.
- **Rationale**: [spec.md FR-001·FR-008](./spec.md)이 두 단계와 두 헤더를 같은 화면의 밀도 변화로 규정한다. 뒤로가기 스택에 남길 목적지가 아니다. room-list·room-detail이 `BottomSheetLevel`을 화면 상태로 둔 것과 같은 논리이나, **그 타입을 재사용하지 않는다** — `:feature:room`은 다른 모듈이고 이 워크트리에 존재하지도 않으며, 단계 집합도 다르다(그쪽은 `Peek`/`Half`/`Full` 3단, 여기는 2단이고 `Peek`이 없다는 것이 [spec.md §4](./spec.md)의 명시적 가정이다).
- **headerMode를 sheetLevel에서 파생시키지 않는 이유**: 축소형 헤더는 `Full`이라는 사실이 아니라 **콘텐츠 스크롤 위치**가 결정한다(FR-008). `Full`이어도 최상단이면 확장형이고, 콘텐츠가 화면보다 짧으면 스크롤이 없어 항상 확장형이다(EC-007).
- **접기 판정은 화면이 한다(5.0.0 추가)**: 헤더가 스크롤 축 밖에 서므로 접히는 순간 그 높이 차만큼 스크롤 범위가 줄어든다. 콘텐츠가 뷰포트보다 조금만 길면 그 감소가 스크롤 위치를 최상단으로 되돌리고, 최상단이 다시 확장형을 불러 왕복이 된다(실기기에서 「대표 이미지 있음 + 코멘트 0건」의 덜컹거림). 그래서 **남은 스크롤 여유가 확장형 헤더 높이보다 작으면 접지 않고**, 펴는 조건은 최상단 하나로 둔다(FR-008 · EC-007 · TS-055). 두 헤더의 높이와 스크롤 범위를 함께 아는 것은 화면이므로 판정도 화면이 하고 ViewModel은 결과만 싣는다.
- **Alternatives considered**: `Half`/`Full`을 각각 Route로 — 기각. 드래그 한 번마다 백스택이 쌓여 시트 높이 변화가 화면 전환으로 둔갑한다.
- **(plan 1.0.0에서 결정)**

## D6. 코멘트 삭제 권한은 서버의 `canDelete`를 그대로 따른다

- **Decision**: [⋮] 노출 여부(FR-015)를 클라이언트가 "작성자 id == 내 id"로 판정하지 않고, 코멘트 응답의 `canDelete: boolean`을 그대로 쓴다.
- **Rationale**: `GET /api/v1/pins/{pinId}/comments`가 항목마다 `canDelete`를 `required`로 내려준다. 권한 판정의 출처가 서버 하나로 모이므로 헌법 원칙 I에 맞고, 방장 예외 같은 규칙이 나중에 바뀌어도 클라이언트가 따라간다. spec FR-015가 규정한 "본인 코멘트만, 방장도 예외 없음"과 현재 서버 동작이 일치한다.
- **Alternatives considered**: 내 프로필 id와 `author.id` 비교 — 기각. 같은 판정을 두 곳에 두게 되고, 내 사용자 id를 이 화면이 따로 조회해야 한다.
- **(plan 1.0.0에서 결정)**

## D7. 「경과일 초기화 확인」(FR-026)은 `POST /pins/{pinId}/accesses`이며 실패를 삼킨다

- **Decision**: 화면 진입 시 `PlaceRepository.recordAccess(pinId)`를 1회 호출한다. 실패는 잡아서 버리고 사용자에게 알리지 않으며 재시도하지 않는다. **`Full` 승격이나 스크롤이 아니라 화면이 열리는 시점** 하나에 묶는다.
- **Rationale**: 서버에 대응 엔드포인트가 있다(`POST /api/v1/pins/{pinId}/accesses` — "홈 카드 덱의 묵힘 계산과 클릭수 집계의 원천. append-only 로그."). [spec.md EC-022](./spec.md)가 "기록 실패는 화면 동작에 영향을 주지 않는다"를 명시했고, EC-023이 "열 때마다 새로 기록하고 짧은 간격이라도 횟수를 줄이지 않는다"를 규정해 디바운스를 금지한다.
- **범위 축소**: FR-026은 "방 전환으로 화면이 갱신될 때에도 다시 기록"을 요구하지만, 방 전환 자체가 이번 범위에서 빠지므로(D10) 진입 시 1회만 구현한다.
- **`:core:analytics`를 쓰지 않는 이유**: 이 기록은 분석 이벤트가 아니라 `꾹 Pick` 순위 판정값을 바꾸는 **도메인 동작**이다. `AnalyticsTracker`가 아니라 Repository를 통한다.
- **(plan 1.0.0에서 결정)**

## D8. 신규 도메인 모델과 Repository 2종

- **Decision**: `:core:domain`에 `PlaceDetail`·`PlaceComment`·`PlaceLabel`을 두고, `PlaceRepository`(핀 상세 조회·접근 기록·복제)와 `PlaceCommentRepository`(코멘트 조회·작성·삭제)로 나눈다.
- **Rationale**: [core/domain README](../../../core/domain/README.md)의 배치 규칙대로 비즈니스 개념은 `model/`, 원격 계약은 `repository/`에 둔다. 두 Repository로 나눈 것은 생애가 다르기 때문이다 — 핀 상세는 진입 시 1회 조회이고, 코멘트는 조회·작성·삭제가 반복되며 페이지네이션 상태를 갖는다. 한 인터페이스에 합치면 코멘트 쪽 변경이 핀 계약을 흔든다.
- **`Place`라는 이름을 쓰지 않는 이유**: room-detail plan이 `:core:domain`에 `Place`(지도 마커·장소 카드용)를 신설할 예정이다(`origin/feature/154-room-list/base`의 `docs/specs/room-detail/data-model.md`). 그 타입은 목록 렌더링에 필요한 필드만 갖고 이 화면이 쓰는 대표 이미지·원문 링크·등록자를 담지 않는다. **두 이슈가 서로 다른 브랜치에서 같은 이름을 만들면 머지 시점에 충돌한다.** 이름을 `PlaceDetail`로 갈라 두면 두 타입이 공존할 수 있고, 머지 후 공통 부분을 뽑을지는 그때 판단한다.
- **Alternatives considered**: 하나의 `PlaceRepository`에 코멘트까지 — 기각(위). room-detail의 `Place`를 기다렸다 재사용 — 기각. 미머지 브랜치의 타입에 이 이슈의 진행을 묶는다.
- **(plan 1.0.0에서 결정)**

## D9. 방 목록은 `RoomSummary`를 재사용하고 `hasPlace` 한 필드만 늘린다

- **Decision**: [다른방에 공유] 시트(FR-018)와 [저장된 방] 시트(FR-024)가 쓰는 방 목록에 새 타입을 만들지 않고 기존 `RoomSummary`를 쓴다. `hasPlace: Boolean?` 필드를 추가하고, `RoomRepository.getRooms(placeId: String? = null)`로 확장한다.
- **Rationale**: `RoomSummary`는 이미 `name`·`color`·`placeCount`·`thumbnailImageUrls`를 갖고 있어 두 시트의 카드 구성(FR-024의 `썸네일 · 방 이름 · 장소 N개`)을 그대로 덮는다. 서버도 같은 엔드포인트 하나로 답한다 — `GET /api/v1/rooms?showHasPlaceId={placeId}`가 방 목록에 `hasPlace`를 얹어 준다. 새 타입을 만들면 같은 응답을 두 모델로 매핑하게 되어 헌법 원칙 I에 어긋난다.
- **`hasPlace`가 nullable인 이유**: 서버가 `?showHasPlaceId=`를 지정했을 때만 내려준다. 기존 호출자(`:feature:sharereceiver`의 방 선택)는 이 값을 요청하지 않으므로 `null`이며, "판정하지 않았음"과 "저장돼 있지 않음"을 구분해야 한다. `RoomSummary`의 KDoc이 이미 "`placeCount`는 지금 저장하려는 장소가 이미 있는지를 뜻하지 않는다"고 경고해 둔 자리에 정확히 대응하는 필드다.
- **기존 호출자 영향**: `getRooms()`의 기본 인자를 `null`로 두어 `GetRoomPickerRoomsUseCase`와 `:feature:sharereceiver`는 고치지 않아도 된다.
- **(plan 1.0.0에서 결정)**

## ~~D10. 저장된 방 전환(FR-023·FR-024·FR-025)은 이번 범위에서 보류한다~~ — 재검토됨(plan 2.0.0)

> **D20로 대체되었다.** 아래 본문은 당시의 판단 근거를 남기기 위한 것이며 현행 설계가 아니다.

- **Decision**: 유저 플로우 7(저장된 방 전환)을 구현하지 않는다. [저장된 방] 버튼은 화면에 두되 **항상 비활성**으로 노출한다. 「저장된 방 시트」와 전환 로직은 만들지 않는다.
- **Rationale**: 사용자 결정이다. 서버에 **전환 대상 방의 `pinId`를 알아낼 방법이 없다.** `GET /api/v1/rooms?showHasPlaceId={placeId}`는 `roomId`와 `hasPlace`만 주고 그 방에 있는 핀의 id를 주지 않으며, `GET /api/v1/pins/{pinId}`는 자기 자신의 핀 하나만 답한다. D4가 정리한 대로 이 화면은 `pinId`로 열리므로, 옮겨 갈 핀을 특정하지 못하면 전환이 성립하지 않는다.
- **서버 협의 항목**: `?showHasPlaceId=` 응답에 그 방의 `pinId`를 함께 실어 달라고 요청한다. [contracts/place-api.md](./contracts/place-api.md) §4에 적었다.
- **Alternatives considered**: `GET /api/v1/pins?roomId=B`로 그 방의 핀을 전부 받아 `place.id`가 같은 것을 찾는 우회 — 사용자가 기각. 방의 핀이 많으면 전환 한 번에 불필요한 전송이 붙고, 그 엔드포인트의 쿼리 파라미터가 OpenAPI 문서에 선언되어 있지 않아(`"parameters": []`) 계약 근거도 약하다.
- **spec과의 관계**: FR-023~FR-025와 TS-042~TS-049, EC-024~EC-027이 이번 구현에서 검증 대상이 아니다. spec을 고치지 않으며, 구현 보류 사실은 [plan.md](./plan.md) 요약과 완료 보고가 나른다.
- **(plan 1.0.0에서 결정)**

## D11. 코멘트는 역방향 페이징으로 싣는다

- **Decision**: `GET /api/v1/pins/{pinId}/comments`의 `page 0`(최신)을 목록 **맨 아래**에 놓고, 위로 스크롤할 때 다음 페이지(더 오래된 것)를 목록 **앞**에 붙인다. 화면의 나열 순서는 [spec.md FR-010](./spec.md)이 정한 오름차순(오래된 것이 위)을 그대로 지킨다.
- **Rationale**: 사용자 결정이다. 서버가 "최신 페이지부터 가져오며, 각 페이지 안에서는 오래된 코멘트가 먼저 온다"는 역방향 페이징이라 화면의 배치와 방향이 반대다. 채팅 타임라인이 쓰는 방식과 같아 사용자에게는 자연스럽고, 입력창이 목록 마지막 아래에 놓인다는 spec §4 가정과도 맞는다.
- **파급**: 새 코멘트 등록(FR-014)은 목록 맨 아래에 덧붙이면 되고, 페이지 경계와 무관하다. UX-007(등록 직후 추가 스크롤 없이 보임)도 맨 아래가 곧 입력창 위라 자연히 성립한다.
- **Alternatives considered**: `pageSize`를 상한(100)으로 잡아 한 번에 받고 페이징을 두지 않음 — 기각. 코멘트가 100건을 넘으면 오래된 것이 잘려 FR-010과 어긋난다. 서버에 오름차순 정방향 페이징 옵션 요청 — 기각(당장 진행이 막힌다). 다만 협의 항목으로는 [contracts/comment-api.md](./contracts/comment-api.md) §5에 남겼다.
- **(plan 1.0.0에서 결정)**

## ~~D12. 장소분류 라벨(FR-005)은 기본값으로 고정하고 서버 협의를 세운다~~ — 재검토됨(plan 2.0.0)

> **D21로 대체되었다.** 아래 본문은 당시의 판단 근거를 남기기 위한 것이며 현행 설계가 아니다.

- **Decision**: 헤더의 장소분류 라벨을 서버에서 받지 않고 `PlaceLabel.WORTH_VISITING`(`가볼 만한 곳`)으로 표시한다. 핀 상세 응답에 `labelGroup`을 추가해 달라고 서버팀에 요청한다.
- **Rationale**: 사용자 결정이다. `labelGroup`(4종 enum)은 `GET /api/v1/rooms/{roomId}/cards` 응답에만 있고 `GET /api/v1/pins/{pinId}`에는 없다. 진입점 넷 중 홈 카드만 그 값을 알고, 지도 마커·방 상세·알림은 알 길이 없다.
- **spec과의 관계**: [spec.md EC-005](./spec.md)가 "상위 세 라벨에 걸리지 않은 장소에는 기본값 `가볼 만한 곳`이 붙어 있어 라벨 자리가 비는 경우는 없다"를 규정하므로, 기본값 표시는 **spec 위반이 아니다.** 다만 FR-005의 취지("[SCR-003] 홈에서 부여된 값을 그대로 표시")는 서버가 값을 줄 때까지 실질적으로 작동하지 않는다.
- **모델은 미리 둔다**: `PlaceLabel` enum 4종을 `:core:domain`에 지금 정의하고 `PlaceDetail.label`을 그 타입으로 둔다. 서버가 필드를 추가하면 Mapper 한 곳만 고치면 되고, 화면은 바뀌지 않는다.
- **Alternatives considered**: 홈 카드 진입 시에만 `labelGroup`을 Intent extra로 전달 — 기각(사용자 선택). 같은 장소가 진입 경로에 따라 다른 라벨을 보이게 된다. 라벨을 아예 표시하지 않음 — 기각. FR-003의 헤더 구성(라벨 자리)이 무너진다.
- **(plan 1.0.0에서 결정)**

## D13. [SYS-003] 방 선택 시트는 이 모듈의 내부 컴포넌트로 두되 내부 규칙은 `[TBD]`다

- **Decision**: [다른방에 공유] (FR-018)가 여는 방 선택 시트를 `:feature:placedetail`의 `main/component/RoomShareSheet.kt`로 만든다. 시트의 시각 표현·높이(676dp)·체크·비활성 규칙은 [spec.md §3.2](./spec.md)가 [SYS-003] 소관이라고 못박았으므로, 이 plan은 **호출·복귀와 서버 계약까지만** 확정하고 시트 내부 규칙은 `[TBD]`로 둔다.
- **Rationale**: [SYS-003]을 정의하는 spec이 아직 없다. 없는 문서를 기다리면 FR-018이 통째로 막히고, 이 plan이 시트 내부를 정하면 spec이 위임한 소유권을 침범한다. 서버 계약(`POST /api/v1/pins/{pinId}/duplicate`, `roomIds` 복수)은 명확하므로 그쪽은 닫는다.
- **`:feature:sharereceiver`의 시트를 재사용하지 않는 이유**: 겉모습이 비슷해 보이지만 CTA 문구(`저장하기` vs `공유하기`), 높이 단계(2단 `Peek`/`Full` vs 단일 `Full` 676dp), 이미 저장된 방 처리(전면 선택 가능 vs 체크·비활성)가 모두 다르다. PRD가 「방 선택 시트」 정의에서 이 갈림을 명시한다. 공용 승격은 두 번째 소비자가 실제로 같은 규칙을 요구할 때 판단할 일이며([component-asset-placement.md](../../conventions/component-asset-placement.md)), 지금은 feature 안에 둔다.
- **`hasPlace`로 체크·비활성을 판정한다**: D9의 `RoomSummary.hasPlace`가 그대로 근거가 된다. FR-022(모든 방에 이미 저장된 경우)도 이 값이 전부 `true`인 상태로 자연히 표현된다.
- **카드는 `:core:design-system`의 `MinoRoomCheckBoxCard`를 쓴다.** 체크박스 달린 방 카드가 이미 디자인 시스템에 있으므로 feature가 다시 만들지 않는다. 새로 만드는 것은 시트 골격(높이·상단 장소 카드·CTA 영역)뿐이며, 그마저도 `[TBD]`인 [SYS-003] 규칙에 걸려 있다.
- **(plan 1.0.0에서 결정)**
- **(plan 2.0.0 보정)** 결정 자체는 유효하다. 모듈이 사라지면서 파일 자리만 옮겨간다 — `:feature:room`의 `placedetail/component/RoomShareSheet.kt`다([D17](#d17-장소-상세를-featureroom에-편입하고-지도를-한-벌만-둔다)). 시트 내부 규칙이 `[TBD]`로 남는 것은 그대로다.

## D14. 에러 처리는 프로젝트 공통 규약을 따르고 화면은 문구를 만들지 않는다

- **Decision**: Repository는 `MinoDomainException`을 던지고, ViewModel은 `launchSafely`로 소비하며, 화면은 `CollectDomainError`로 공통 스낵바에 붙인다([error_handling.md](../../conventions/error_handling.md)). 장소 상세가 자체 에러 문구를 만들지 않는다.
- **Rationale**: [spec.md §4](./spec.md)가 "코멘트 조회·작성·삭제에는 네트워크 연결이 필요하며, 오프라인 및 요청 실패 시의 화면 처리는 프로젝트 공통 에러 처리 규약을 따른다"를 가정으로 못박았다. 문구 매퍼를 feature에 다시 두지 않는다.
- **예외 하나**: 「경과일 초기화 확인」 실패는 이 경로를 타지 않는다(D7). 사용자에게 보이지 않아야 하므로 Repository 호출부에서 잡아 버린다.
- **`POST /pins/{pinId}/duplicate`의 409**: 서버가 "대상 방 중 하나라도 같은 장소가 있으면 409로 전체 거절"한다. 시트가 이미 저장된 방을 비활성으로 막으므로(D13) 정상 흐름에서는 발생하지 않지만, 다른 기기에서 먼저 저장된 경우 등 경합에서 나올 수 있다. 공통 에러 경로로 흘리고 별도 분기를 두지 않는다.
- **(plan 1.0.0에서 결정)**

---

## ~~D15. UI 라운드의 방 정보는 인자 없는 `getRooms()`로 받고, 마커는 두 조회가 끝난 뒤 그린다~~ — 재검토됨(plan 2.0.0)

> **D24로 대체되었다.** 아래 본문은 당시의 판단 근거를 남기기 위한 것이며 현행 설계가 아니다.

- **Decision**: 마커 색상(FR-002)과 [다른방에 공유] 시트 목록(FR-018)을 **이미 구현돼 있는 `GetRoomPickerRoomsUseCase`**(내부적으로 인자 없는 `RoomRepository.getRooms()` 호출)로 받는다. Fake 방 목록을 따로 두지 않는다. 마커는 핀 상세와 방 목록이 **모두** 도착한 뒤에 그린다.
- **Rationale**: D9가 미룬 것은 `getRooms(placeId)` **확장**(→ `hasPlace`)이지 방 목록 조회 자체가 아니었다. `RoomSummary`는 이미 `color`·`placeCount`·`thumbnailImageUrls`를 담고 있어 두 용처를 그대로 덮으며, `:core:data` 변경이 0이라 "API 연결 없이 UI만"이라는 이번 라운드의 제약을 깨지 않는다. 대안으로 검토한 feature 로컬 Fake는 `RoomRepository`가 이미 `:core:data`에서 바인딩돼 있어 덮어쓰면 Hilt 중복 바인딩이 나고, 피하려면 새 추상화를 하나 더 세워야 한다 — 버릴 코드를 위해 구조를 늘리는 셈이라 기각했다.
- **마커 렌더 시점**: `roomColor`가 `null`인 동안 어떤 색으로 그릴지는 spec에 근거가 없다. 없는 기본색을 발명하는 대신 **그리지 않는다.** 두 조회가 병렬이고 그동안 시트도 로딩 상태라 체감 지연이 없으며, 방 목록 조회가 실패하면 마커 없이 시트만 그리고 오류는 공통 경로로 흘린다.
- **감수하는 것**: 방 목록만 서버에 의존하므로 이번 라운드의 검증이 완전 오프라인은 아니다. 핀 상세·코멘트는 Fake라 화면 자체는 그려지고, 조회가 실패하면 마커 색과 공유 시트만 빈다.
- **Alternatives considered**: feature 로컬 `FakeRoomSource` 인터페이스 신설 — 기각(위). `PlaceRepository`에 방 목록 조회를 얹기 — 기각. plan·contracts에 없는 계약을 task 단계에서 만들게 되고, 핀 계약과 방 계약이 한 인터페이스에 섞인다.
- **(plan 1.1.0에서 결정)**

## ~~D16. 지도 위 버튼 행은 이번 라운드에 렌더링까지 한다~~ — 재검토됨(plan 2.0.0)

> **D25로 대체되었다.** 아래 본문은 당시의 판단 근거를 남기기 위한 것이며 현행 설계가 아니다.

- **Decision**: `PlaceMapControls`로 [현재 위치]와 그 왼쪽 [저장된 방]을 한 행에 배치하고 `Full`에서 함께 숨긴다. **[현재 위치]의 동작(카메라 이동·위치 권한)은 구현하지 않는다** — 렌더링과 배치까지다.
- **Rationale**: FR-023이 [현재 위치]의 존재를 명시하고 [spec.md §4](./spec.md) 가정이 "[저장된 방]은 [현재 위치]와 함께 `Full`에서 숨는다"로 그 존재를 전제하므로, 버튼이 없으면 [저장된 방]의 배치 기준 자체가 사라진다. 반면 그 **동작**은 [spec.md §3.2](./spec.md)가 [SYS-004] 소관으로 위임했고 [SYS-004] 구현이 이 저장소에 없다. 존재는 이 spec의 요구, 동작은 남의 소관이라 그 경계에서 끊는다.
- **감수하는 것**: 눌러도 아무 일이 없는 버튼이 화면에 남는다. [SYS-004]가 생기면 그 동작만 붙이면 된다.
- **Alternatives considered**: [현재 위치]를 아예 그리지 않음 — 기각. FR-023과 §4 가정이 모두 그 존재를 전제하고, [저장된 방]의 위치를 정할 기준이 없어진다. 동작까지 이번에 구현 — 기각. 위치 권한 요청 흐름은 [SYS-004] Flow A의 소관이라 이 spec이 정의할 근거가 없다.
- **(plan 1.1.0에서 결정)**

---

## D17. 장소 상세를 `:feature:room`에 편입하고 지도를 한 벌만 둔다

- **Decision**: `:feature:placedetail` 모듈을 해체하고 화면을 `:feature:room`의 세 번째 패키지 `placedetail/`로 옮긴다. 장소 상세는 별도 Route도 Activity도 아니고, 방 리스트 Route(`RoomMain`) 하나가 그리는 **세 번째 시트 분기**다. 지도(`RoomListMap`)는 리스트·방 상세·장소 상세가 한 컴포지션에서 공유한다.
- **Rationale**: [D1](#d1-장소-상세는-진입형-feature-모듈-featureplacedetail이다--재검토됨plan-200)의 근거가 두 갈래였고 **둘 다 소멸했다.**
  - "사용자가 진입형으로 결정했다" → 사용자가 이번 개정에서 편입으로 다시 결정했다.
  - "#161이 미머지라 `:feature:room` 계약을 건드리지 않고 독립 진행할 수 있다" → **#161이 머지됐다**(`2537a6a3`). 이 이점은 더 존재하지 않는다.
- **머지된 room-detail이 실증한 것**: [ADR 2026-08-29](../../adr/2026-08-29-entry-feature-for-cross-tab-immersive-screen.md)가 기각한 (B)안은 "방 상세 화면의 상태로 두고 지도 유지"였는데, 실제로 머지된 room-detail은 **그보다 더 멀리 갔다** — 방 리스트와 방 상세를 Route로도 나누지 않고 `RoomListUiState.selectedRoomId` 하나로 갈랐다. 그 근거가 `RoomNavigation.kt` KDoc에 적혀 있다: "지도(`RoomListMap`)를 하나의 컴포지션에서 계속 살려 두어야 리스트↔상세 전환에서 카메라가 리셋되지 않는다." ADR이 "측정한 값이 아니라 예상"이라고 적어 둔 지도 재생성 비용을, room-detail 구현이 **회피해야 할 실제 결함으로 확인한 것**이다.
- **새 패턴을 만들지 않는다**: `RoomListScreen`은 이미 `detailContent: (@Composable BoxScope.() -> Unit)?` 슬롯을 갖고 있고 `RoomDetailScreen`·`RoomDetailRoute`는 이미 `BoxScope` 확장이다. 장소 상세는 그 슬롯의 **세 번째 분기**로 들어갈 뿐, 새 메커니즘이 없다.
- **spec이 저장 탭을 귀착지로 못박는다**: TS-007(알림 진입)·TS-037(홈 진입) 모두 [나가기]가 "알림 탭이 아니라"·"홈 탭이 아니라" **A방의 방 상세 `Half`로** 가라고 규정한다. 어느 진입점으로 들어와도 사용자가 남는 자리는 저장 탭이다. 편입 구조에서 이것은 `selectedPinId = null` 한 줄이다 — 방 상세 시트가 그 아래에 그대로 살아 있기 때문이다.
- **함께 닫히는 요구사항**: FR-002(같은 지도 위 마커→선택 핀 전이), FR-023(지도 오버레이 버튼 한 벌), FR-025(방 전환이 화면 교체가 아니라 상태 갱신)가 모두 구조에서 따라온다.
- **감수하는 비용**:
  - `:feature:room` 모듈이 커진다(방 리스트 + 방 상세 + 장소 상세). 셋 다 "저장 탭의 지도 위 시트"라는 한 관심사라 응집도는 유지된다.
  - 홈·알림 탭 진입에 탭 간 인자 전달이 필요하다 → [D18](#d18-탭-간-진입은-공유-요청-홀더로-배선하고-placedetaillauncher를-폐기한다).
  - [ADR 2026-08-29](../../adr/2026-08-29-entry-feature-for-cross-tab-immersive-screen.md)를 대체하는 새 ADR이 필요하다. 그 ADR 자신이 `Proposed` 상태로 "#161이 머지되어 두 패턴이 같은 코드베이스에 놓이기 전에는 이 기준이 실제로 두 화면을 모순 없이 설명하는지 확인할 수 없다. 확인되면 `Accepted`로 올리고, **어긋나면 두 결정을 화해시키는 새 ADR로 대체한다**"고 적어 둔 그 경로다. **완료 보고에서 승격을 제안한다**(이 스킬이 ADR을 직접 쓰지 않는다).
- **Alternatives considered**: 진입형 Activity 유지, `:feature:main`이 슬롯을 주입하는 절충안 — 둘 다 기각(사용자 결정). 절충안은 장소 상세도 [현재 위치] 버튼·카메라 이동을 쓰므로 `RoomListViewModel`의 `mapCenter`·`mapCenterRequestId`·`onCurrentLocationClick`까지 `:feature:main`을 거쳐 드릴링해야 해서, 모듈 경계를 지키려다 셸 결합이 더 깊어진다. 그 밖의 후보와 기각 이력은 [D1](#d1-장소-상세는-진입형-feature-모듈-featureplacedetail이다--재검토됨plan-200)을 참조한다.
- **(plan 2.0.0에서 결정)**

## D18. 탭 간 진입은 공유 요청 홀더로 배선하고 `PlaceDetailLauncher`를 폐기한다

- **Decision**: `:core:navigation`에 `@ActivityRetainedScoped class PlaceDetailRequestHolder`를 두고, 여는 값은 `pinId` 하나다. 홈 카드가 `pinId`를 올리면 `:feature:main`이 홀더에 싣고 저장 탭으로 전환하며, `RoomListViewModel`이 그것을 읽어 소비(읽고 나면 `null`로 비움)한다. `PlaceDetailLauncher`와 `EXTRA_PLACE_DETAIL_PIN_ID`는 삭제한다.
- **Rationale**: Route 인자 확장(`RoomGraph(pinId)`)은 탭 전환 구조와 정면 충돌한다. `MainTabNavigation.navigateToTab`이 `popUpTo(...) { saveState = true }` + `restoreState = true`로 떠난 탭의 백스택을 저장·복원하는데, 복원된 항목은 **저장 당시의 인자를 그대로 들고 되살아나** 새 `pinId`가 반영되지 않는다. `restoreState`를 끄면 저장 탭을 떠났다 돌아올 때마다 방 목록·시트 단계가 초기화된다 — 탭 상태 보존을 포기하는 대가가 진입 인자 하나보다 크다.
- **새 패턴이 아니다**: 이 저장소는 셸↔탭 사이의 간접화 장치를 이미 둘 갖고 있다 — `LocalBottomNavVisibility`(`:core:common:ui`, 방 상세가 바텀바를 숨길 때)와 `ImmersiveRouteRegistry`(`:core:navigation`, 셸이 몰입 Route를 판정할 때). 둘 다 "탭 모듈과 셸이 서로의 구체 타입을 모른 채 공유 상태로 합의한다"는 같은 형태이며, 이 홀더가 그 세 번째다.
- **`pinId` 하나면 충분하다**: 핀 상세 응답(`GET /api/v1/pins/{pinId}`)이 `roomId`를 함께 준다. 「지금 보고 있는 방」(FR-027)은 그 값으로 해석되므로 요청이 방을 따로 실어 나를 필요가 없다.
- **`:feature:home`은 바뀌지 않는다**: 홈은 이미 `HomeSideEffect.NavigateToPlaceDetail(pinId)` → `onNavigateToPlaceDetail(pinId)` 콜백까지만 알고 있고, 그 콜백을 받는 `MainNavHost`·`MainActivity` 쪽만 목적지를 Activity에서 홀더로 바꾼다.
- **[SCR-007] 알림 탭**: 화면 자체가 아직 placeholder라 이번 범위 밖이다. 생길 때 같은 홀더를 그대로 쓴다 — 알림이 방을 특정하지 않는다는 EC-001의 조건도 `pinId`→`roomId` 해석으로 자동 충족된다.
- **Alternatives considered**: `RoomGraph` Route 인자 확장 — 기각(위 근거). `setResult`로 방 id를 돌려주는 [D2](#d2-진입-계약은-placedetaillauncher--pinid-나가기는-이번-범위에서-finish까지만--재검토됨plan-200)의 후보들은 Activity가 사라지면서 함께 성립하지 않는다.
- **(plan 2.0.0에서 결정)**

## D19. 바텀 네비게이션 숨김(FR-020)은 `LocalBottomNavVisibility`로 처리한다

- **Decision**: 장소 상세가 열려 있는 동안 `LocalBottomNavVisibility`를 `false`로 둔다. `ImmersiveRoute` 마커는 쓰지 않는다.
- **Rationale**: [D3](#d3-바텀-네비게이션-숨김fr-020은-구현할-것이-없다--재검토됨plan-200)은 "탭 셸 밖에서 뜨므로 바텀바가 애초에 없다"였는데, 편입되면 탭 셸 **안**이라 그 전제가 무너진다. 그렇다고 `ImmersiveRoute`도 쓸 수 없다 — 그것은 **목적지 단위** 마커인데 장소 상세는 `RoomMain`이라는 같은 목적지 안의 로컬 상태이기 때문이다. 방 상세가 정확히 같은 이유로 `LocalBottomNavVisibility`를 쓰고 있고(`RoomListRoute` KDoc: "ImmersiveRoute는 목적지 단위 마커라 이 화면엔 못 쓴다"), 장소 상세는 그 판정식에 조건 하나가 느는 것으로 끝난다.
- **구현 위치**: 이미 `RoomListRoute`가 `DisposableEffect(isDetailMode, state.isNudgeSheetVisible)`로 이 값을 쓰고 있다. `selectedPinId != null`을 그 식에 더한다 — 새 `DisposableEffect`를 만들면 두 곳이 같은 값을 다투게 된다.
- **(plan 2.0.0에서 결정)**

## D20. 저장된 방 전환(FR-023·FR-024·FR-025)을 `matchedPinId`로 구현한다

- **Decision**: 보류를 해제하고 이번 범위에서 구현한다. `GET /api/v1/rooms?showHasPlaceId={placeId}`가 방마다 돌려주는 `matchedPinId`가 전환 대상 핀이며, [저장된 방] 시트에서 B방을 고르면 `selectedPinId = matchedPinId(B)`로 갱신한다.
- **Rationale**: [D10](#d10-저장된-방-전환fr-023fr-024fr-025은-이번-범위에서-보류한다--재검토됨plan-200)의 보류 사유는 "서버가 `roomId`·`hasPlace`만 주고 **대상 방의 `pinId`를 주지 않아** 전환할 핀을 특정할 수 없다"였다. **2026-09-01 조회한 OpenAPI에 `matchedPinId`가 있다** — 파라미터 설명이 "지정하면 각 방에 hasPlace와 matchedPinId를 함께 반환한다"로 바뀌었다. 차단 사유가 서버 쪽에서 해소됐다.
- **전환이 상태 갱신으로 끝난다**: 편입 구조([D17](#d17-장소-상세를-featureroom에-편입하고-지도를-한-벌만-둔다))에서 「지금 보고 있는 방」은 `selectedPinId` 안에 내포되므로([D4](#d4-화면의-식별자는-pinid이며-지금-보고-있는-방은-별도-상태가-아니다)), 방 전환은 핀 교체 하나다. 선택 핀 색(FR-002)과 [나가기] 목적지(FR-009)가 그 한 값에서 함께 따라온다.
- **[저장된 방] 버튼 노출 조건**: 같은 응답의 `hasPlace == true`인 방이 2개 이상일 때만 그린다(FR-023, TS-040·TS-041).
- **코멘트 초안은 버린다**: FR-025가 정한 그대로다. 편입 구조에서도 핀이 바뀌면 `PlaceDetailViewModel`이 `pinId` key로 새로 만들어지므로 초안이 자연히 사라진다.
- **(plan 2.0.0에서 결정)**

## D21. 장소분류 라벨을 걷어내고 헤더에 등록자 닉네임을 둔다

- **Decision**: `PlaceLabel` enum과 `PlaceDetail.label` 필드를 **삭제한다.** 헤더 첫 줄은 등록자 아바타 + 등록자 닉네임 + [나가기]다. 닉네임은 이미 있는 `PlaceDetail.registrant.nickname`이 공급한다.
- **Rationale**: [D12](#d12-장소분류-라벨fr-005은-기본값으로-고정하고-서버-협의를-세운다--재검토됨plan-200)는 "서버가 `labelGroup`을 핀 상세에 안 준다"를 서버 협의 항목으로 세우고 기본값 `가볼 만한 곳`으로 버티는 결정이었다. **spec 4.0.0이 요구사항 자체를 제거했다** — FR-005가 "장소분류 라벨·저장 경과일·카테고리는 장소 상세 어디에도 노출하지 않으며, 홈 카드로 진입한 경우에도 라벨을 넘겨받지 않는다"로 재정의됐고, 근거인 PRD 11.0.0 §1이 라벨 노출 화면을 홈 카드 하나로 좁혔다. 협의할 것이 없어졌고 타입도 필요 없다.
- **서버 대조**: `GET /api/v1/pins/{pinId}`의 `createdBy`가 `nickname`(2~15자)과 `avatar.color`를 준다 — 새 FR-005가 요구하는 값이 이미 응답에 있다. **대응 API 있음.** 서버 협의 항목 하나가 사라진다.
- **말줄임**: 닉네임이 표시 폭을 넘으면 한 줄 유지 + `...`(FR-005, TS-009). 장소명·주소와 같은 규칙이라 헤더 안에서 넘침 처리가 갈리지 않는다.
- **`PlaceLabel.kt` 삭제 범위**: 이 타입의 소비자는 장소 상세뿐이다. 홈 카드는 `PlaceCard`가 자기 라벨 표현을 따로 갖고 있어 영향을 받지 않는다.
- **(plan 2.0.0에서 결정)**

## D22. 코멘트 작성 시각(FR-028)은 도메인이 시각을, 화면이 표기를 갖는다

- **Decision**: `PlaceComment`에 `createdAt: Instant`를 더한다. `방금`·`N시간 전`·`N일 전`·`NNNN년 NN월 NN일`로 끊는 **구간 판정과 문구 조립은 도메인이 아니라 feature의 UI 매핑이 한다.**
- **Rationale**: 서버가 이미 `createdAt`(`format: date-time`)을 목록·작성 응답 모두에 주고 있어 도메인이 값을 받는 데는 아무 걸림이 없다 — [D6](#d6-코멘트-삭제-권한은-서버의-candelete를-그대로-따른다) 시점에 "표기하지 않기로 했으니 담지 않는다"고 뺐던 필드를 되살리는 것뿐이다. 구간 문구를 도메인에 두지 않는 것은 `core/domain/README.md` §5의 규칙 그대로다 — 문자열 리소스는 feature가 소유한다.
- **판정 시점**: 목록을 그리는 시점에 한 번만 판정하고 실시간으로 다시 계산하지 않는다(spec EC-028). 그래서 `Instant` → 표기 변환은 컴포지션 안의 순수 함수이지 흐르는 상태가 아니다.
- **음수 경과 시간**: 기기 시각이 서버보다 앞서 경과가 음수로 나와도 1시간 미만 구간으로 흡수해 `방금`을 쓴다(spec EC-029). `-1시간 전` 같은 표기가 새어 나가지 않게 하는 하한이다.
- **어느 시계로 재는지는 이 문서가 정하지 않는다**: spec §3.2가 명시적으로 위임하지 않은 채 남긴 지점이라 설계가 지어내지 않는다. 구현은 기기 시각과 서버 `createdAt`의 차로 재되, 시간대는 `Instant` 비교라 영향을 받지 않는다.
- **(plan 2.0.0에서 결정)**

## D23. Fake Repository를 걷어내고 `:core:data`에 실구현을 둔다

- **Decision**: `:feature:placedetail`의 `fake/` 패키지와 `PlaceDetailFakeDataModule`을 삭제하고, `:core:data`에 `PlaceRepositoryImpl`·`PlaceCommentRepositoryImpl`과 그 아래 Service·DTO·DataSource·Mapper를 둔다. 바인딩은 구현을 가진 `:core:data`가 소유한다([ADR 2026-08-02](../../adr/2026-08-02-di-binding-ownership.md)).
- **Rationale**: [D15](#d15-ui-라운드의-방-정보는-인자-없는-getrooms로-받고-마커는-두-조회가-끝난-뒤-그린다--재검토됨plan-200)가 세운 "API 없이 UI만"이라는 라운드 경계를 이번 이슈(#270)가 닫는다. Repository 인터페이스 시그니처는 [contracts/place-repository.md](./contracts/place-repository.md)가 확정한 그대로라 ViewModel과 화면은 바인딩 교체만으로 실 데이터를 받는다.
- **`ApiService` 배치**: 서버 태그 단위로 소유한다([ADR 2026-08-28](../../adr/2026-08-28-api-service-owned-per-server-tag.md)) — `pin` 태그는 기존 `PinApiService`에 병합하고, `comment` 태그는 `CommentApiService`를 새로 만든다. 응답 봉투(`data`) 벗기기는 `ApiService`가 한다([ADR 2026-08-27](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)).
- **마커 색 공백은 사라진다**: [D15](#d15-ui-라운드의-방-정보는-인자-없는-getrooms로-받고-마커는-두-조회가-끝난-뒤-그린다--재검토됨plan-200)가 "두 조회가 끝나기 전엔 핀을 안 그린다"로 막아 둔 자리는, 편입 구조에서 방 목록을 **이미 `RoomListViewModel`이 들고 있어** 조회를 기다릴 일이 없다.
- **(plan 2.0.0에서 결정)**

## D24. 방 목록은 `RoomSummary`에 `hasPlace`·`matchedPinId` 두 필드를 늘려 재사용한다

- **Decision**: [D9](#d9-방-목록은-roomsummary를-재사용하고-hasplace-한-필드만-늘린다)가 정한 `hasPlace`에 더해 `matchedPinId: String?`를 함께 늘린다. `RoomRepository.getRooms(placeId: String? = null)`로 확장하고, `placeId`가 `null`이면 두 필드는 `null`이다.
- **Rationale**: 서버가 두 값을 같은 파라미터(`showHasPlaceId`) 하나로 함께 내려주므로 도메인에서 갈라 둘 이유가 없다. `null` 허용은 "물어보지 않았다"와 "저장돼 있지 않다"를 구분하기 위한 것이다 — 인자 없이 부른 호출자(방 리스트 탭·공유 시트)가 `false`를 사실로 오해하지 않게 한다.
- **기존 호출자 영향**: `getRooms()`는 기본 인자라 시그니처가 호환된다. `RoomRepositoryImpl`과 `RoomApiService.listRooms`에 쿼리 파라미터가 하나 는다.
- **(plan 2.0.0에서 결정)**

## D25. 지도와 지도 위 컨트롤을 한 벌로 합친다

- **Decision**: `PlaceDetailMap`을 삭제하고 `RoomListMap`이 선택 핀을 함께 그린다. [현재 위치] 버튼은 `RoomListViewModel`의 `OnCurrentLocationClick`으로 직접 연결하고, [저장된 방] 버튼은 장소 상세가 열려 있을 때만 그 옆에 선다.
- **Rationale**: [D16](#d16-지도-위-버튼-행은-이번-라운드에-렌더링까지-한다)은 지도가 두 벌이던 구조에서 "렌더링만 하고 동작은 [SYS-004] 소관"으로 끊은 결정이었다. 지도가 한 벌이 되면 카메라를 실제로 움직이는 주체가 `RoomListViewModel` 하나로 정해지므로 동작까지 닫힌다. 방 상세가 이미 같은 배선을 하고 있다 — `RoomDetailScreen`의 `onCurrentLocationClick`이 `RoomListViewModel`로 올라가는 이유가 그 KDoc에 실기기 결함과 함께 적혀 있다("예전엔 이 버튼이 `RoomDetailViewModel`의 `mapCenter`를 갱신했는데, 그 상태를 읽는 화면이 없어 버튼이 눌려도 지도가 안 움직였다"). 장소 상세가 자기 ViewModel에 카메라 상태를 두면 **그 결함을 그대로 재현한다.**
- **선택 핀의 카메라 이동**: 장소 상세가 열릴 때 그 좌표로 카메라를 옮기는 것도 `RoomListViewModel`의 `mapCenter`·`mapCenterRequestId`를 통한다. `mapCenterRequestId`가 값이 같아도 다시 움직이게 하는 장치라(`RoomListMap` KDoc의 실기기 버그 기록), 같은 장소를 닫았다 다시 열어도 카메라가 맞춰진다.
- **카메라의 중심은 시트를 뺀 영역이다(5.0.0 추가)**: 지도에 `contentPadding`(maps-compose → `GoogleMap.setPadding`)을 실어 **가려진 가장자리를 선언**하면 카메라 타깃이 그 나머지의 중앙에 놓인다 — 좌표를 손으로 밀어 보정하지 않는다(spec FR-002). 값을 정하는 것은 세 시트 중 무엇이 서 있는지 아는 `RoomListScreen`이고, 위쪽은 지도가 상태바 뒤로 들어간 만큼(`mapBleed`), 아래쪽은 지금 선 시트가 가리는 높이다. **장소 상세 시트만 내비게이션 바 자리까지 덮으므로** 그 인셋만큼 빼서 넘긴다 — 지도의 아랫변은 내비게이션 바 위에서 끝난다. 지도 컨트롤의 노출 판정도 같은 값 하나에서 갈린다(가려진 높이를 알 수 없다 = `Full` = 지도가 안 보인다).
- **핀 외형**: `:core:common:ui`의 `RoomMapPin(color, selected)`이 이미 `selected` 인자를 갖고 있어 선택 핀 표현이 그 컴포넌트 안에 있다. `PlaceDetailMap`이 따로 쓰던 `MinoIcons.PinFill` 직접 조립은 사라진다.
- **(plan 2.0.0에서 결정)**

## D26. 「지금」은 주입한 `Clock`에서 오고, 판정 기준 시각을 상태에 싣는다

- **Decision**: `kotlin.time.Clock`을 Hilt로 주입한다. `PlaceDetailViewModel`이 코멘트 목록을 상태로 올릴 때마다 `clock.now()`를 **한 번** 읽어 `PlaceDetailUiState.commentsObservedAt: Instant`에 함께 싣고, 컴포저블의 순수 함수가 `(createdAt, commentsObservedAt)` 두 값으로 구간을 판정한다. 바인딩은 `:feature:room`의 `di/`가 소유하고 `ViewModelComponent`에 설치한다.
- **Rationale**: [D22](#d22-코멘트-작성-시각fr-028은-도메인이-시각을-화면이-표기를-갖는다)가 "구현은 기기 시각과 서버 `createdAt`의 차로 재되"까지만 적고 그 기기 시각을 **어디서 얻는지는 비워 두었다.** 컴포저블 안에서 `Clock.System.now()`를 직접 부르면 값이 컴포지션마다 달라져 EC-028("실시간으로 다시 계산하지 않는다")이 구현에서 지켜지는지 확인할 수단이 없다. 기준 시각을 상태로 끌어올리면 **언제 판정했는가가 상태에 드러나** 규칙이 코드에서 읽힌다.
- **왜 주입인가**: `now()`를 직접 부르면 TS-050~TS-053(네 구간)·TS-054(등록 직후 `방금`)·EC-029(음수 흡수)를 고정할 수 없다 — 테스트가 실행 시각에 매달린다. `Clock`을 주입하면 고정 시각을 넣어 네 구간의 경계(정확히 1시간·24시간·7일)를 재현 가능하게 찍을 수 있다. `@Inject` 생성자는 기본 인자를 무시하므로(Dagger가 모든 파라미터를 그래프에서 찾는다) `clock: Clock = Clock.System` 같은 기본값 방식은 성립하지 않는다.
- **바인딩 위치**: `:feature:room`의 `di/`에 두고 `ViewModelComponent`에 설치한다. `ShareReceiverResourcesModule`이 같은 판단을 이미 문서화해 뒀다 — "이 바인딩을 요구하는 곳이 이 모듈의 ViewModel 하나뿐이기 때문이다. 앱 전역 그래프에 올리면 소유자 없는 공용 바인딩이 feature에서 자라난다." 구현을 가진 모듈이 바인딩을 갖는다는 [ADR 2026-08-02](../../adr/2026-08-02-di-binding-ownership.md)와도 같은 방향이다.
  - **대가**: 두 번째 feature가 `now()`를 요구하면 이 모듈을 공용 자리로 옮겨야 한다. 지금은 프로덕션 코드에 `Clock.System.now()` 호출이 **하나도 없어**(Preview·Test 전용) 공용 자리를 미리 만들 근거가 없다. 옮길 때가 오면 그때 옮긴다.
- **갱신 시점**: 코멘트 목록 상태를 다시 만들 때마다 `commentsObservedAt`을 새로 읽는다 — 최초 조회, 이전 페이지 추가 로드, 등록·삭제 후 반영이 모두 해당한다. TS-054(등록 직후 `방금`)가 이 갱신으로 성립하고, 목록을 그대로 둔 채 시간만 흐르는 동안에는 갱신되지 않아 EC-028이 성립한다.
- **`Instant`는 `kotlin.time.Instant`다.** 코드베이스가 이미 그 타입으로 정착해 있다(`Place.savedAt`·`Room.lastPlaceSavedAt`·`RoomMember.joinedAt`, `PlaceMapper`의 `Instant.parse`). 버전 카탈로그에 `kotlinx-datetime`이 있으나 `:core:common:kotlin`의 `implementation`이라 다른 모듈로 새지 않고 소스에서 쓰는 곳도 없다.
- **`@OptIn(ExperimentalTime::class)`이 필요하다.** Kotlin 2.2.10 stdlib에서 `kotlin.time.Instant`와 `kotlin.time.Clock`이 모두 `@ExperimentalTime`이고, `build-logic`에 전역 opt-in 설정이 없어 **파일마다** 붙여야 한다. 기존 파일이 모두 그렇게 하고 있다(`Place.kt`는 클래스에 `@OptIn`, `PlaceMapper.kt`는 `@file:OptIn`).
- **어느 시계로 재는지(서버 vs 기기)는 여전히 이 문서가 정하지 않는다.** [D22](#d22-코멘트-작성-시각fr-028은-도메인이-시각을-화면이-표기를-갖는다)가 spec §3.2의 위임을 그대로 둔 판단은 유지된다. 이 결정은 그 위임 안에서 **기기 시각을 어떻게 얻는가**만 정한다 — 서버가 기준 시각을 내려주게 되면 `commentsObservedAt`의 공급원만 바뀌고 화면 쪽 판정 함수는 그대로다.
- **(plan 2.1.0에서 결정)**

## NEEDS CLARIFICATION 해소 현황

plan 2.1.0 기준. 취소선이 그어진 결정을 근거로 삼던 줄은 후속 결정으로 갱신했다.

| 항목 | 상태 | 해소 근거 |
|---|---|---|
| 모듈 배치(진입형 vs 탭) | 해소 | D17 — 저장 탭 편입(사용자 결정). ~~D1~~ 대체 |
| [나가기] 목적지 배선 | **해소** | D17 — `selectedPinId = null`로 방 상세가 드러난다. ~~D2~~의 `[TBD]` 해제 |
| 홈·알림 탭에서의 진입 배선 | 해소 | D18 — `PlaceDetailRequestHolder` |
| 바텀 네비게이션 숨김 | 해소 | D19 — `LocalBottomNavVisibility`. ~~D3~~ 대체 |
| 저장된 방 전환의 대상 `pinId` | **해소** | D20 — 서버가 `matchedPinId` 신설. ~~D10~~의 구현 보류 해제 |
| 장소분류 라벨 공급 | **소멸** | D21 — spec 4.0.0이 요구사항을 제거. ~~D12~~의 서버 협의 항목 철회 |
| 등록자 닉네임 공급 | 해소 | D21 — `createdBy.nickname` |
| 코멘트 작성 시각 표기 | 해소 | D22 — 서버 `createdAt` + feature의 구간 매핑 |
| 구간 판정의 기준 시각(「지금」) | **해소** | D26 — 주입한 `Clock`, `commentsObservedAt`으로 상태에 실음. D22가 비워 둔 자리 |
| 코멘트 정렬·페이징 방향 | 해소 | D11 — 역방향 페이징으로 흡수 |
| 코멘트 삭제 권한 판정 | 해소 | D6 — 서버 `canDelete` |
| 방 목록 타입 | 해소 | D24 — `RoomSummary` + `hasPlace`·`matchedPinId`. D9 확장 |
| 방 정보 공급 | 해소 | D23·D25 — `RoomListViewModel`이 이미 든 목록을 공유. ~~D15~~ 대체 |
| 마커 색·카메라 이동 | 해소 | D25 — 지도 한 벌, `RoomListViewModel` 소유 |
| [현재 위치] 버튼 동작 | 해소 | D25 — 동작까지 닫힌다. ~~D16~~ 대체 |
| [SYS-003] 시트 내부 규칙 | **미해소(`[TBD]`)** | D13 — [SYS-003] spec 부재. spec이 소유권을 위임한 상태 그대로 둔다 |

**남은 `[TBD]`는 하나다.** [SYS-003] 방 선택 시트의 내부 규칙이며, spec §3.2가 그 소유권을 [SYS-003] 스펙에 위임해 둔 상태다. 이번 개정이 닫을 수 있는 항목이 아니다.
