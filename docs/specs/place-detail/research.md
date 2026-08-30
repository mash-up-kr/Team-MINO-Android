# 리서치: 장소 상세 & 코멘트 (Place Detail & Comments)

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [plan.md](./plan.md)

각 항목은 어느 plan 버전에서 결정되었는지를 함께 적는다. 뒤집힌 결정은 지우지 않고 취소선과 재검토 표시를 남긴다.

---

## D1. 장소 상세는 진입형 feature 모듈 `:feature:placedetail`이다

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

## D2. 진입 계약은 `PlaceDetailLauncher` + `pinId`, [나가기]는 이번 범위에서 `finish()`까지만

- **Decision**: `:core:navigation`에 `PlaceDetailLauncher : ActivityLauncher`와 `EXTRA_PLACE_DETAIL_PIN_ID`를 신설한다([feature-navigation.md 1장](../../architecture/feature-navigation.md)). 진입점 네 곳이 모두 `pinId`를 실어 이 Launcher로 연다. **[나가기](FR-009)는 `finish()`로 호출자에게 돌아가는 데까지만 구현하고**, "지금 보고 있는 방의 [SCR-005] 방 상세 `Half`로 보낸다"는 목적지 배선은 `[TBD]`로 남긴다.
- **Rationale**: 사용자 결정이다. 방 상세 화면이 이 저장소에 아직 없어(이슈 #161 미머지) 목적지가 실존하지 않는다. 없는 화면을 향한 배선을 지금 만들면 검증할 수 없는 코드가 남는다.
- **미해결로 남는 것**: [SCR-005] 방 상세·지도 마커에서 진입한 경우에는 호출자가 곧 목적지라 `finish()`가 우연히 FR-009를 만족한다. 그러나 **[SCR-003] 홈 카드와 [SCR-007] 알림에서 진입하면 호출자가 목적지가 아니므로 FR-009와 어긋난다.** 이 갭은 #161 머지 이후 별도 개정에서 닫는다.
- **Alternatives considered**:
  - `setResult`로 "나갈 때 보고 있던 방"(`EXTRA_PLACE_DETAIL_RESULT_ROOM_ID`)을 돌려주고 호출자가 방 상세로 이동 — 사용자가 기각. 진입형 규약에 맞고 #161과 무관하게 계약을 닫을 수 있었으나, 그 결과를 소비할 호출자가 아직 없어 계약만 떠 있게 된다.
  - 장소 상세가 `RoomDetailLauncher`를 주입받아 직접 이동 — 기각. 방 상세는 탭 feature라 Activity Launcher가 아니고(`MainLauncher` 경유가 필요하다), #161 머지 전에는 계약 자체를 쓸 수 없다.
- **(plan 1.0.0에서 결정)**

## D3. 바텀 네비게이션 숨김(FR-020)은 구현할 것이 없다

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
- **Alternatives considered**: `Half`/`Full`을 각각 Route로 — 기각. 드래그 한 번마다 백스택이 쌓이고 EC-003(드래그다운 = 나가기)이 성립하지 않는다.
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

## D10. 저장된 방 전환(FR-023·FR-024·FR-025)은 이번 범위에서 보류한다

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

## D12. 장소분류 라벨(FR-005)은 기본값으로 고정하고 서버 협의를 세운다

- **Decision**: 헤더의 장소분류 라벨을 서버에서 받지 않고 `PlaceLabel.WORTH_VISITING`(`가볼 만한 곳`)으로 표시한다. 핀 상세 응답에 `labelGroup`을 추가해 달라고 서버팀에 요청한다.
- **Rationale**: 사용자 결정이다. `labelGroup`(4종 enum)은 `GET /api/v1/rooms/{roomId}/cards` 응답에만 있고 `GET /api/v1/pins/{pinId}`에는 없다. 진입점 넷 중 홈 카드만 그 값을 알고, 지도 마커·방 상세·알림은 알 길이 없다.
- **spec과의 관계**: [spec.md EC-005](./spec.md)가 "상위 세 라벨에 걸리지 않은 장소에는 기본값 `가볼 만한 곳`이 붙어 있어 라벨 자리가 비는 경우는 없다"를 규정하므로, 기본값 표시는 **spec 위반이 아니다.** 다만 FR-005의 취지("[SCR-003] 홈에서 부여된 값을 그대로 표시")는 서버가 값을 줄 때까지 실질적으로 작동하지 않는다.
- **모델은 미리 둔다**: `PlaceLabel` enum 4종을 `:core:domain`에 지금 정의하고 `PlaceDetail.label`을 그 타입으로 둔다. 서버가 필드를 추가하면 Mapper 한 곳만 고치면 되고, 화면은 바뀌지 않는다.
- **Alternatives considered**: 홈 카드 진입 시에만 `labelGroup`을 Intent extra로 전달 — 기각(사용자 선택). 같은 장소가 진입 경로에 따라 다른 라벨을 보이게 된다. 라벨을 아예 표시하지 않음 — 기각. FR-003의 헤더 구성(라벨 자리)이 무너진다.
- **(plan 1.0.0에서 결정)**

## D13. [SYS-003] 방 선택 시트는 이 모듈의 내부 컴포넌트로 두되 내부 규칙은 `[TBD]`다

- **Decision**: [다른방에 공유](FR-018)가 여는 방 선택 시트를 `:feature:placedetail`의 `main/component/RoomShareSheet.kt`로 만든다. 시트의 시각 표현·높이(676dp)·체크·비활성 규칙은 [spec.md §3.2](./spec.md)가 [SYS-003] 소관이라고 못박았으므로, 이 plan은 **호출·복귀와 서버 계약까지만** 확정하고 시트 내부 규칙은 `[TBD]`로 둔다.
- **Rationale**: [SYS-003]을 정의하는 spec이 아직 없다. 없는 문서를 기다리면 FR-018이 통째로 막히고, 이 plan이 시트 내부를 정하면 spec이 위임한 소유권을 침범한다. 서버 계약(`POST /api/v1/pins/{pinId}/duplicate`, `roomIds` 복수)은 명확하므로 그쪽은 닫는다.
- **`:feature:sharereceiver`의 시트를 재사용하지 않는 이유**: 겉모습이 비슷해 보이지만 CTA 문구(`저장하기` vs `공유하기`), 높이 단계(2단 `Peek`/`Full` vs 단일 `Full` 676dp), 이미 저장된 방 처리(전면 선택 가능 vs 체크·비활성)가 모두 다르다. PRD가 「방 선택 시트」 정의에서 이 갈림을 명시한다. 공용 승격은 두 번째 소비자가 실제로 같은 규칙을 요구할 때 판단할 일이며([component-asset-placement.md](../../conventions/component-asset-placement.md)), 지금은 feature 안에 둔다.
- **`hasPlace`로 체크·비활성을 판정한다**: D9의 `RoomSummary.hasPlace`가 그대로 근거가 된다. FR-022(모든 방에 이미 저장된 경우)도 이 값이 전부 `true`인 상태로 자연히 표현된다.
- **카드는 `:core:design-system`의 `MinoRoomCheckBoxCard`를 쓴다.** 체크박스 달린 방 카드가 이미 디자인 시스템에 있으므로 feature가 다시 만들지 않는다. 새로 만드는 것은 시트 골격(높이·상단 장소 카드·CTA 영역)뿐이며, 그마저도 `[TBD]`인 [SYS-003] 규칙에 걸려 있다.
- **(plan 1.0.0에서 결정)**

## D14. 에러 처리는 프로젝트 공통 규약을 따르고 화면은 문구를 만들지 않는다

- **Decision**: Repository는 `MinoDomainException`을 던지고, ViewModel은 `launchSafely`로 소비하며, 화면은 `CollectDomainError`로 공통 스낵바에 붙인다([error_handling.md](../../conventions/error_handling.md)). 장소 상세가 자체 에러 문구를 만들지 않는다.
- **Rationale**: [spec.md §4](./spec.md)가 "코멘트 조회·작성·삭제에는 네트워크 연결이 필요하며, 오프라인 및 요청 실패 시의 화면 처리는 프로젝트 공통 에러 처리 규약을 따른다"를 가정으로 못박았다. 문구 매퍼를 feature에 다시 두지 않는다.
- **예외 하나**: 「경과일 초기화 확인」 실패는 이 경로를 타지 않는다(D7). 사용자에게 보이지 않아야 하므로 Repository 호출부에서 잡아 버린다.
- **`POST /pins/{pinId}/duplicate`의 409**: 서버가 "대상 방 중 하나라도 같은 장소가 있으면 409로 전체 거절"한다. 시트가 이미 저장된 방을 비활성으로 막으므로(D13) 정상 흐름에서는 발생하지 않지만, 다른 기기에서 먼저 저장된 경우 등 경합에서 나올 수 있다. 공통 에러 경로로 흘리고 별도 분기를 두지 않는다.
- **(plan 1.0.0에서 결정)**

---

## D15. UI 라운드의 방 정보는 인자 없는 `getRooms()`로 받고, 마커는 두 조회가 끝난 뒤 그린다

- **Decision**: 마커 색상(FR-002)과 [다른방에 공유] 시트 목록(FR-018)을 **이미 구현돼 있는 `GetRoomPickerRoomsUseCase`**(내부적으로 인자 없는 `RoomRepository.getRooms()` 호출)로 받는다. Fake 방 목록을 따로 두지 않는다. 마커는 핀 상세와 방 목록이 **모두** 도착한 뒤에 그린다.
- **Rationale**: D9가 미룬 것은 `getRooms(placeId)` **확장**(→ `hasPlace`)이지 방 목록 조회 자체가 아니었다. `RoomSummary`는 이미 `color`·`placeCount`·`thumbnailImageUrls`를 담고 있어 두 용처를 그대로 덮으며, `:core:data` 변경이 0이라 "API 연결 없이 UI만"이라는 이번 라운드의 제약을 깨지 않는다. 대안으로 검토한 feature 로컬 Fake는 `RoomRepository`가 이미 `:core:data`에서 바인딩돼 있어 덮어쓰면 Hilt 중복 바인딩이 나고, 피하려면 새 추상화를 하나 더 세워야 한다 — 버릴 코드를 위해 구조를 늘리는 셈이라 기각했다.
- **마커 렌더 시점**: `roomColor`가 `null`인 동안 어떤 색으로 그릴지는 spec에 근거가 없다. 없는 기본색을 발명하는 대신 **그리지 않는다.** 두 조회가 병렬이고 그동안 시트도 로딩 상태라 체감 지연이 없으며, 방 목록 조회가 실패하면 마커 없이 시트만 그리고 오류는 공통 경로로 흘린다.
- **감수하는 것**: 방 목록만 서버에 의존하므로 이번 라운드의 검증이 완전 오프라인은 아니다. 핀 상세·코멘트는 Fake라 화면 자체는 그려지고, 조회가 실패하면 마커 색과 공유 시트만 빈다.
- **Alternatives considered**: feature 로컬 `FakeRoomSource` 인터페이스 신설 — 기각(위). `PlaceRepository`에 방 목록 조회를 얹기 — 기각. plan·contracts에 없는 계약을 task 단계에서 만들게 되고, 핀 계약과 방 계약이 한 인터페이스에 섞인다.
- **(plan 1.1.0에서 결정)**

## D16. 지도 위 버튼 행은 이번 라운드에 렌더링까지 한다

- **Decision**: `PlaceMapControls`로 [현재 위치]와 그 왼쪽 [저장된 방]을 한 행에 배치하고 `Full`에서 함께 숨긴다. **[현재 위치]의 동작(카메라 이동·위치 권한)은 구현하지 않는다** — 렌더링과 배치까지다.
- **Rationale**: FR-023이 [현재 위치]의 존재를 명시하고 [spec.md §4](./spec.md) 가정이 "[저장된 방]은 [현재 위치]와 함께 `Full`에서 숨는다"로 그 존재를 전제하므로, 버튼이 없으면 [저장된 방]의 배치 기준 자체가 사라진다. 반면 그 **동작**은 [spec.md §3.2](./spec.md)가 [SYS-004] 소관으로 위임했고 [SYS-004] 구현이 이 저장소에 없다. 존재는 이 spec의 요구, 동작은 남의 소관이라 그 경계에서 끊는다.
- **감수하는 것**: 눌러도 아무 일이 없는 버튼이 화면에 남는다. [SYS-004]가 생기면 그 동작만 붙이면 된다.
- **Alternatives considered**: [현재 위치]를 아예 그리지 않음 — 기각. FR-023과 §4 가정이 모두 그 존재를 전제하고, [저장된 방]의 위치를 정할 기준이 없어진다. 동작까지 이번에 구현 — 기각. 위치 권한 요청 흐름은 [SYS-004] Flow A의 소관이라 이 spec이 정의할 근거가 없다.
- **(plan 1.1.0에서 결정)**

---

## NEEDS CLARIFICATION 해소 현황

| 항목 | 상태 | 해소 근거 |
|---|---|---|
| 모듈 배치(진입형 vs 탭) | 해소 | D1 — 사용자 결정 |
| [나가기] 목적지 배선 | **미해소(`[TBD]`)** | D2 — 방 상세(#161) 미머지. 홈·알림 진입 경로가 FR-009와 어긋난 채 남는다 |
| 저장된 방 전환의 대상 `pinId` | **미해소(구현 보류)** | D10 — 서버 협의 항목 |
| 장소분류 라벨 공급 | **미해소(기본값 고정)** | D12 — 서버 협의 항목 |
| 코멘트 정렬·페이징 방향 | 해소 | D11 — 역방향 페이징으로 흡수 |
| 코멘트 삭제 권한 판정 | 해소 | D6 — 서버 `canDelete` |
| 방 목록 타입 | 해소 | D9 — `RoomSummary` 재사용 + `hasPlace` |
| UI 라운드의 방 정보 공급 | 해소 | D15 — 인자 없는 `getRooms()` 재사용 |
| `roomColor == null`일 때 마커 색 | 해소 | D15 — 두 조회가 끝나기 전에는 마커를 그리지 않는다 |
| [현재 위치] 버튼 | 해소 | D16 — 렌더링까지, 동작은 [SYS-004] 소관 |
| [SYS-003] 시트 내부 규칙 | **미해소(`[TBD]`)** | D13 — [SYS-003] spec 부재. spec이 소유권을 위임한 상태 그대로 둔다 |
