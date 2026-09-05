# 리서치: [SCR-003] 홈 탭

**대상 스펙 경로**: `docs/specs/home-deck-exploration`

**계획서**: [plan.md](./plan.md)

> 이 문서는 **누적**한다. 결정이 뒤집히면 항목을 지우지 않고 취소선과 `재검토됨(plan X.Y.Z)`을 붙인 뒤 새 항목을 덧붙인다.

---

## R-001. 정렬 3종 후보 조회의 원천 *(plan 1.0.0)*

**Decision**: 정렬 3종(`꾹 Pick`·`최신순`·`가까운순`)의 후보 조회를 **`DeckRemoteDataSource` 인터페이스 뒤의 mock 구현**으로 세운다. 실서버가 붙으면 `DeckDataSourceModule`의 `@Binds` 인자 타입 하나만 갈아 끼운다.

**Rationale**: 이 spec의 핵심(FR-011·012의 자동 전환)이 정렬별 후보 집합을 전제로 서 있으므로, 서버를 기다리면 스펙 전체가 멈춘다.

> **근거 갱신(plan 2.0.0)** — 결정은 그대로지만 **전제가 바뀌었다.** 1.0.0 시점에는 대응 API가 설계조차 없었으나, 지금은 `GET /api/v1/rooms/{roomId}/cards`가 서버 PR [Node#94](https://github.com/mash-up-kr/Team-MINO-Node/pull/94)로 **설계·리뷰까지 끝나고 배포만 남았다.** 그래서 mock을 쓰는 이유가 "만들 것이 없어서"에서 **"만들어졌지만 아직 닿지 않아서"** 로 바뀌었고, 그 결과 **mock의 계약을 지어내지 않고 확정된 실제 계약에 맞춘다.** 조회 시점과 대조 결과는 [`contracts/deck-api.md`](./contracts/deck-api.md) §1·§3이 소유한다.

같은 상황을 이 저장소가 이미 한 번 통과했다 — `group-room-form`이 `RoomRemoteDataSource`를 두고 `RoomMockRemoteDataSourceImpl`을 바인딩했으며, 계약을 `contracts/room-api-mock.md`가 소유하고 교체 지점을 그 문서에 적어 두었다. 같은 형태를 따르면 실서버 전환 비용이 `@Binds` 한 줄로 고정되고, feature·domain 레이어는 mock 여부를 모른 채 완성된다.

**Alternatives considered**:
- *클라이언트가 직접 순위를 산출* — spec §3.2가 순위 산출 로직을 **명시적으로 비목표**로 밀어냈다. `꾹 Pick`의 경과일 상위 30% 판정은 방 전체 장소의 분포를 알아야 해서 클라이언트가 가진 데이터로 재현할 수 없다.
- *`GET /api/v1/pins` 전체 조회 후 클라이언트 정렬* — `최신순`(14일 기준)은 흉내 낼 수 있으나 `꾹 Pick`은 위와 같은 이유로 불가하고, `가까운순`(3km 반경)은 좌표가 있어 가능하지만 세 정렬 중 둘만 되는 절름발이 구현이 된다. 무엇보다 §3.2 비목표를 어긴다.
- *서버 구현을 기다림* — 스펙의 핵심 플로우가 통째로 막힌다. 아래 R-002와 함께 서버팀 협의 항목으로 올린다.

---

## R-002. 장소분류 라벨의 원천 *(plan 1.0.0)*

**Decision**: 라벨 4종(`친구들이 많이 본 곳`·`이야기 많은 곳`·`여럿이 저장한 곳`·`가볼 만한 곳`)을 **R-001의 같은 mock DataSource가 카드와 함께 공급**한다. 도메인 모델은 라벨을 `PlaceLabel` enum으로 갖고, feature는 표시만 한다.

**Rationale**: FR-008이 카드마다 라벨 1종 표시를 요구하는데 배포된 OpenAPI 응답 어디에도 대응 필드가 없다(근거는 [`contracts/deck-api.md`](./contracts/deck-api.md) §3).

> **근거 갱신(plan 2.0.0)** — R-001과 같다. 서버 PR Node#94가 `labelGroup` 필드를 확정했고 값도 `worthVisiting` · `manySaves` · `manyComments` · `manyViews` 4종으로 정해졌다. **mock이 내리는 값을 이 enum에 맞춘다.** `place.category`가 있으나 이는 `카페`/`음식점` 축이라 라벨 4종과 다른 것이다 — 둘을 섞으면 잘못된 값이 화면에 뜬다. spec §3.2가 라벨 **판정 로직**을 비목표로 두었으므로 클라이언트가 계산할 대상도 아니다. 값의 원천이 서버뿐인데 서버에 없으니 R-001과 같은 mock 경로로 함께 내린다.

**Alternatives considered**:
- *`place.category`를 라벨로 전용* — 값 체계가 다르다. 4종 라벨 중 어느 것으로도 매핑되지 않는다.
- *라벨을 화면에서 생략* — FR-008을 지우는 것이라 spec 개정이 선행돼야 한다. 설계가 혼자 정할 수 없다.

---

## R-003. 덱 전환 규칙을 어느 레이어에 둘 것인가 *(plan 1.0.0)*

**Decision**: 전환 규칙을 **`:core:domain`의 `ResolveNextDeckUseCase`** 에 둔다. ViewModel은 현재 문맥(방 목록·현재 방·현재 정렬·덱별 소진 여부)을 넘기고 결과를 받아 상태에 반영하기만 한다.

**Rationale**: FR-011·012·013과 EC-009·EC-013이 서로 물린 하나의 규칙 덩어리다 — 우선순위 고정, 후보 0건 = 소진 동일 취급, 전환 시점 재판정, 위치 권한 거부 시 `가까운순` 소진 처리, 장소 0개 방 건너뛰기. 이 판정은 Android도 Compose도 모르는 순수 계산이고, TS-015~019·TS-021이 요구하는 검증이 정확히 이 판정의 입출력 검증이다. UseCase로 세우면 `:core:domain`의 JVM 단위 테스트로 그 시나리오를 그대로 옮길 수 있다.

ViewModel에 두면 같은 검증에 Android 테스트 환경이 필요해지고, 헌법 원칙 II의 의존 방향(바깥 → 안쪽)이 주는 이점을 버리게 된다.

**Alternatives considered**:
- *ViewModel 내부 private 함수* — 규칙이 화면 상태 관리와 엉켜 TS 7건을 개별 검증하기 어렵다.
- *Repository에 배치* — 전환 판정은 데이터 출처와 무관한 정책이다. Repository는 후보를 가져오는 데까지가 책임이다(`core/domain/README.md`).

---

## R-004. 홈의 진행 상태를 어디에 보관할 것인가 *(plan 1.0.0)*

**Decision**: 현재 방·현재 정렬·덱별 소진 여부·되돌리기 이력은 **`HomeUiState`에만** 둔다. 서버에도 DataStore에도 쓰지 않는다. 예외 둘은 DataStore에 영속 저장한다.

- 홈 사용 가이드를 닫은 이력 (FR-019)
- 마지막으로 보던 방 (FR-022)

**Rationale**: spec §4 가정이 "홈의 진행 상태는 클라이언트 로컬 상태로 관리하며 서버에 영구 저장하지 않는다"를 이미 확정했고, 같은 가정이 "다시 들어올 때 이어지는 것은 방까지"라고 못 박았다.

> **보강(plan 2.0.0)** — spec 3.0.0 FR-022가 마지막으로 보던 방을 **"기기에 영속 저장하며 서버에 올리지도, 홈 진입 시 묻지도 않는다"** 로 못 박았다. 1.0.0에서 이 선택의 근거가 적혀 있지 않았으므로 여기에 남긴다: 서버가 쥐면 **방 목록 응답을 받아야 어느 덱을 부를지 알 수 있어 홈 첫 화면 호출이 직렬로 묶인다.** 앱이 값을 갖고 있으면 `GET /rooms`와 `.../cards`를 동시에 보낼 수 있다. 서버 PR Node#94의 `users.last_viewed_room_id`는 이 확정에 따라 쓰지 않는다. 소진 이력까지 이어 붙이면 재진입 사용자가 볼 카드 없이 완료 화면부터 만난다는 근거도 spec에 적혀 있다. 저장소는 `DataStoreModule`과 `ProfileLocalDataSource`의 선례를 따른다.

**Alternatives considered**:
- *`SavedStateHandle`에 소진 상태 보존* — 프로세스 사망 복원까지 이어 붙이는 셈이라 위 가정과 어긋난다.
- *전부 DataStore* — 같은 이유로 기각. spec이 명시적으로 배제했다.

---

## R-005. 우측 영역 한정 스와이프 인식 *(plan 1.0.0)*

**Decision**: 카드 최상단에서 `pointerInput`으로 드래그를 받되, **제스처 시작점의 x좌표가 카드 폭의 절반 이상일 때만** 드래그를 소비한다. 좌측에서 시작한 제스처는 소비하지 않는다.

**Rationale**: FR-003과 TS-003이 "좌측 영역에서 발생하는 스와이프를 반영하지 않는다"를 요구한다. 판정 기준을 **시작점**으로 잡아야 좌측에서 시작해 우측으로 지나가는 드래그도 일관되게 무시된다 — 현재 위치로 판정하면 손가락이 경계를 넘는 순간 동작이 갈린다.

경계값(카드 폭의 절반)은 Figma `2598-95698`에 명시적 가이드가 없어 **가정으로 둔다.** spec §4가 인식 영역을 「카드 덱」 정의에 위임했고 PRD도 "화면 우측 영역"이라고만 적었다. 구현 후 디자이너 확인 항목으로 남긴다.

**Alternatives considered**:
- *화면 전체에서 인식* — FR-003 위반.
- *현재 위치로 판정* — 경계를 넘는 드래그에서 동작이 튄다.

---

## R-006. 탭·드래그·액션 메뉴 세 조작의 분리 *(plan 1.0.0)*

**Decision**: 터치 슬롭으로 가른다. 탭과 드래그를 **같은 `pointerInput` 안**에서 받아, 드래그가 시작된 포인터는 탭으로 처리하지 않는다. `[...]` 버튼은 카드 위 별도 컴포저블이라 자체 클릭 영역이 제스처보다 우선한다.

**Rationale**: EC-006이 "드래그로 시작한 입력은 탭으로 처리하지 않는다"를, SC-006이 "세 조작이 서로를 오작동시키지 않는다"를, EC-007이 "`[...]` 위 탭은 상세로 가지 않는다"를 요구한다. spec §4 가정도 "터치 슬롭으로 가른다"를 이미 확정했다.

**Alternatives considered**:
- *카드 전체 `clickable` + 별도 드래그 모디파이어* — 두 모디파이어가 같은 포인터를 두고 경합해 EC-006이 깨진다.

---

## R-007. 애니메이션 중 입력 무시 *(plan 1.0.0)*

**Decision**: `HomeUiState`에 `isTransitioning: Boolean`을 두고, 전환이 끝날 때까지 도착한 스와이프 Intent를 ViewModel에서 **버린다**(큐에 쌓지 않는다).

**Rationale**: UX-001과 TS-007이 "끝나기 전 두 번째 입력은 무시되어 카드가 한 장만 넘어간다"를 요구한다. 큐에 쌓으면 손을 뗀 뒤에도 카드가 계속 넘어가 SC-005("카드가 두 장 넘어가는 일이 없다")를 어긴다. 판정을 UI가 아니라 상태로 올려야 TS-007을 ViewModel 테스트로 검증할 수 있다.

**Alternatives considered**:
- *Compose 쪽에서만 차단* — 검증이 Android 테스트로 밀린다.

---

## R-008. 툴팁 2종의 노출 제어 *(plan 1.0.0)*

**Decision**: 툴팁을 `HomeUiState.tooltip: HomeTooltip?`(sealed) 하나로 표현하고, 3초 뒤 `null`로 되돌리는 타이머를 ViewModel이 소유한다. 예고 툴팁은 **덱 식별자별 1회** 노출했음을 상태에 기록해 재노출을 막는다.

**Rationale**: FR-015·016이 둘 다 "3초 노출 후 사라짐"이고 UX-003이 "노출 중 조작을 막지 않는다"를 요구하므로, 툴팁은 화면을 가리지 않는 순수 표시 상태다. spec §4 가정이 예고 툴팁의 재노출 조건을 「덱이 임계값 위로 다시 길어졌다」로 확정했으므로 노출 이력을 덱 단위로 들되, 잔여가 임계값 위로 올라가면 그 덱의 이력을 푼다.

두 툴팁이 동시에 뜰 자리는 없다 — 방 전환은 덱 전환을 동반하고, 전환 직후 잔여 2장 이하면 예고가 뒤이어 뜬다(EC-012). 하나의 nullable 필드로 마지막 것이 이긴다.

**Alternatives considered**:
- *툴팁별 독립 상태 2개* — 동시 노출 조합을 정의해야 하는데 디자인에 근거가 없다.

---

## R-009. `가까운순` 전환 시점의 위치 권한 *(plan 1.0.0)*

**Decision**: 권한 요청 자체는 [SYS-004]의 소관이므로 홈은 **`HomeSideEffect.RequestLocationPermission`** 을 던지고 결과를 Intent로 돌려받는다. 거부되면 `가까운순`을 소진 처리한 상태로 R-003의 UseCase를 다시 돌린다.

**Rationale**: EC-009가 정확히 이 동작을 정의했고, spec §3.2가 "위치 권한 요청 UI와 OS 다이얼로그 처리"를 비목표로 밀어냈다. 홈이 정하는 것은 "요청이 필요한 시점"과 "거부 시 덱 처리"뿐이다.

**Alternatives considered**:
- *홈 진입 시 미리 요청* — spec은 `가까운순` 전환 시점을 명시했다. 미리 물으면 `가까운순`에 닿지 않는 사용자에게도 다이얼로그가 뜬다.

---

## R-010. 방 색상 → 캐릭터·팔레트 대응 *(plan 1.0.0)*

**Decision**: `RoomColor` → (배경색·캐릭터 에셋) 대응표를 **`:feature:home` 안에** 둔다. `:core:design-system`은 색 팔레트만 소유한다.

**Rationale**: [`adr/2026-08-14-room-color-palette-in-design-system.md`](../../adr/2026-08-14-room-color-palette-in-design-system.md)가 "팔레트는 `:core:design-system`이, 이 값과 팔레트의 대응은 feature가 소유한다"를 이미 결정했고 `RoomColor` KDoc이 그 결정을 지목한다. 새로 정할 것이 없다.

**캐릭터 이미지 에셋**의 소속은 [`conventions/component-asset-placement.md`](../../conventions/component-asset-placement.md)를 따른다 — 방 캐릭터는 Figma 디자인 시스템 컴포넌트가 아니고 현재 사용처가 홈뿐이므로 `:feature:home`에 두었다가 승격 기준을 충족하면 옮긴다.

**Alternatives considered**: 없음 — 기존 ADR이 소유한 결정이라 재검토 대상이 아니다.

---

## R-012. 두 「확인 이벤트」를 어떻게 배선하는가 *(plan 2.0.0)*

**Decision**: spec 3.0.0의 FR-023이 요구하는 독립을 **호출 경로 자체를 분리해** 지킨다.

- **① 경과일 초기화 확인** — `OpenPlaceDetail` Intent를 받으면 `HomeDeckRepository.recordPlaceOpened(pinId)`를 부르고 **곧바로 화면 전환 SideEffect를 던진다.** 결과를 기다리지 않으며, 실패해도 화면을 막지 않는다.
  > **spec 4.0.0에서 폐기됐다.** [SCR-006] 장소 상세가 배선되면서 `place-detail` FR-026이 같은 기록을 가져갔고, 두 곳이 같은 엔드포인트를 쳐 카드 한 번 탭에 두 건이 쌓였다. `recordPlaceOpened`를 걷어내 홈은 화면 전환 SideEffect만 던진다 — 이 결정에서 남은 것은 「결과를 기다리지 않는다」가 아니라 **「홈은 부르지 않는다」**다.
- **② 카드 열람 확인** — `SwipeForward` Intent가 `HomeUiState`의 덱만 건드린다. **서버를 부르지 않는다.**

**Rationale**: 1.0.0의 `recordCardConsumed(pinId)`는 이름 그대로 *넘김*을 서버에 알리는 함수였다. spec 3.0.0에서 넘김은 서버와 무관해졌으므로 **이 함수는 이름도 호출 시점도 틀렸다.** 상세 진입 시점으로 옮기고 이름을 `recordPlaceOpened`로 바꾼다.

둘을 한 함수로 묶지 않는 이유는 TS-013·034·035가 각각을 따로 검증하기 때문이다. 묶으면 *"클릭했는데 덱이 넘어갔다"* 와 *"클릭했는데 초기화가 안 됐다"* 를 같은 테스트가 잡게 된다.

**결과를 기다리지 않는 이유**: FR-007이 클릭 즉시 상세로 이동할 것을 요구한다. 기록이 늦거나 실패한다고 화면 전환을 늦추면 사용자가 느끼는 것은 "느린 앱"뿐이고, 초기화는 다음 덱 요청부터 반영되면 충분하다.

**Alternatives considered**:
- *Repository 한 함수가 두 이벤트를 다 받고 내부에서 갈라짐* — 호출부에서 어느 이벤트인지 안 보인다. FR-023의 독립이 코드에서 사라진다.
- *넘김도 서버에 보내되 서버가 무시* — 쓰지 않는 트래픽이고, 서버 계약이 넘김을 받지 않는다.

---

## R-013. `sort=nearby`의 좌표를 어디서 넣는가 *(plan 2.0.0)*

**Decision**: `HomeDeckRepository.getDeck`에 **nullable 좌표를 받는다.**

```kotlin
suspend fun getDeck(roomId: String, sort: DeckSort, location: GeoPoint? = null): Deck
```

`sort == NEAREST`인데 `location`이 `null`이면 Repository가 요청을 보내지 않고 **빈 덱을 돌려준다.** 그러면 EC-009의 "거부되면 `가까운순`을 소진 처리"가 별도 분기 없이 성립한다.

**Rationale**: plan 1.0.0의 `getDeck(roomId, sort)`에는 **좌표 자리가 없어 `가까운순`을 아예 호출할 수 없었다.** 설계 구멍이다. 서버 계약이 `sort=nearby`에 `lat`·`lng`를 필수로 요구하고 없으면 400을 내므로([`contracts/deck-api.md`](./contracts/deck-api.md) §2), 클라이언트가 좌표를 쥐고 있어야 한다.

**빈 덱으로 돌려주는 이유**: 「소진」의 정의가 이미 *"카드를 모두 넘겼거나 애초에 후보가 0건인 상태"* 이고(spec §2.3), `ResolveNextDeckUseCase`가 그 둘을 같게 다룬다(R-003). 권한 거부를 빈 덱으로 표현하면 **전환 규칙에 예외 분기를 만들지 않아도 된다.**

**Alternatives considered**:
- *`getNearbyDeck(roomId, lat, lng)`를 따로 둠* — 정렬 3종이 같은 엔드포인트를 쓰는데 함수만 갈라진다. 호출부(`ResolveNextDeckUseCase` 결과 처리)가 정렬별 분기를 갖게 된다.
- *권한 거부를 예외로 던짐* — EC-009는 오류가 아니라 정상 흐름이다. 예외로 만들면 `try/catch`가 전환 로직에 섞인다.
- *좌표를 Repository가 직접 조회* — 도메인 계약이 위치 제공자에 묶인다. 권한 요청 시점은 [SYS-004] 소관이고 홈은 결과만 받는다(R-009).

---

## R-011. 「홈 방 시트」를 공용으로 올릴 것인가 *(plan 1.0.0)*

**Decision**: `:feature:home` 안에 둔다. 공용 모듈로 올리지 않는다.

**Rationale**: FR-018이 정한 형태(400dp 고정 높이·3열 그리드·70dp 썸네일·첫 칸 `방 만들기`·체크박스 없음)는 [SYS-002]/[SYS-003]의 방 선택 시트(체크박스와 CTA가 있는 다른 시트)와 형태가 다르다. `component-asset-placement.md`의 기본값이 "그것을 쓰는 feature에 두었다가 공유가 실제로 생기면 승격"이므로 지금 올릴 근거가 없다.

**Alternatives considered**:
- *`:core:common:ui`에 선제 배치* — 사용처가 하나인 컴포넌트를 공용으로 올리는 것은 위 규약의 승격 기준에 어긋난다.

---

## R-014. 탐색 축이 뒤집힌 뒤의 전환 규칙 *(plan 3.0.0)*

**Decision**: `ResolveNextDeckUseCase`는 **(정렬, 방) 격자 순회**로 규칙을 바꾸고, 수동 방 변경의 판정은 **별도 UseCase `ResolveRoomEntryDeckUseCase`로 분리**한다. R-003이 정한 "전환 규칙은 `:core:domain`의 순수 UseCase" 라는 레이어 결정은 그대로 유효하다.

- `ResolveNextDeckUseCase(context)` — 자동 전환(FR-011·012·025). 탐색 범위는 **모든 방**이다.
- `ResolveRoomEntryDeckUseCase(context, roomId)` — 수동 변경(FR-024). 탐색 범위는 **그 방 하나**로 한정되고, 남은 칸이 없으면 `AllExhausted`를 돌려준다.

**Rationale**: 두 판정은 규칙이 아니라 **입력이 다르다.** 자동은 격자 전체에서 「남은 칸 중 최선두」를 찾고, 수동은 「고른 방 안의 남은 덱 중 최고 순위」를 찾는다. 한 함수에 `manual: Boolean` 같은 플래그를 넣으면 두 규칙이 함수 안의 분기로 숨어, TS-015~021(자동)과 TS-028a~c(수동)가 같은 함수의 같은 경로를 밟는지 어긋나는지 테스트가 구분하지 못한다.

**둘의 반환 타입은 같다**(`NextDeck`). 수동 경로가 `SameRoom`·`AllExhausted`만 쓰고 `NextRoom`을 절대 내지 않는 것이 곧 "다른 방으로 튕기지 않는다"(FR-024·SC-008)의 코드 표현이다.

**방 순회 순서는 도메인 입력으로 받는다.** FR-012의 「개인방 먼저, 그다음 생성이 오래된 순」은 홈이 정하는 순서이므로 `DeckContext.rooms`를 **이미 정렬된 목록**으로 넘기고, UseCase는 받은 순서를 그대로 훑는다. 정렬 자체는 `:core:data`가 방 목록을 도메인 모델로 옮길 때 수행한다 — 여러 화면이 공유하는 조회 응답의 순서에 홈이 기대지 않는다는 요구(FR-012)가 여기서 지켜진다.

**Alternatives considered**:
- *한 UseCase + 탐색 범위 파라미터(`scope: DeckScope`)* — 반환 타입이 같고 규칙만 갈리니 그럴듯하지만, `scope`가 곧 두 요구사항(FR-011 vs FR-024)의 이름이라 파라미터가 아니라 함수 경계가 맞다. 호출부에서도 "지금 어느 규칙인가"가 인자에 묶여 보이지 않는다.
- *격자를 도메인 모델로 만들어 순회를 그 안에 둠(`DeckGrid`)* — 칸이 최대 3×N이라 자료구조를 세울 만큼 크지 않고, 소진 집합(`Set<DeckKey>`)과 방 목록만 있으면 순회가 한 줄로 끝난다.
- *`exhausted` 집합을 UseCase가 들고 상태화* — R-003의 "부수효과도 상태도 없다"를 깨고, FR-011의 「전환 시점마다 다시 판정」이 흐려진다.

---

## R-015. 방 캐릭터 12 variant의 표현과 에셋 소속 *(plan 3.0.0)*

**Decision**: 방 캐릭터를 **방 대표 색마다 한 장**으로 갈고(Figma `Home_Avatar`, 노드 `4306:63718`), 대응표는 R-010대로 `:feature:home`이 갖는다. 에셋 12종×밀도 3벌은 `:feature:home/res/drawable-*`에 둔다.

**Rationale**: 종전 구현은 색과 무관한 단일 에셋 하나(`home_room_character`, 126×164)였다. 새 시안은 **같은 검은 캐릭터에 방 색 소품(모자·안경·헤드폰·리본 등)이 얹힌 12 variant**이고 크기도 126×**172**로 바뀌었다. 색을 코드로 덧입힐 수 있는 형태가 아니라 — 소품의 모양 자체가 variant마다 다르다 — variant당 한 장이 유일한 표현이다.

**`:core:common:ui`의 `RoomThumbnailFallback`과 다른 에셋군이다.** 그쪽은 방 썸네일 자리를 채우는 **정사각 배경 + 토끼 실루엣**이고, 이쪽은 홈 상단에 걸터앉은 **투명 배경의 큰 캐릭터**다. 둘을 한 컴포넌트로 묶으면 크기·모양·쓰임이 다른 두 그림이 한 API를 공유하게 된다. 승격 기준([`component-asset-placement.md`](../../conventions/component-asset-placement.md) §2.1)은 두 번째 사용처가 생겼을 때이고, 홈 캐릭터는 아직 홈뿐이다.

**팔레트가 어긋난다 — brown이 없다.** `Home_Avatar`와 `Room Thumbnail_HOME`(노드 `4306:63731`) 두 컴포넌트셋 모두 variant가 `black`(=색을 고르지 않은 방) + 11색이고 **`brown`이 없다.** 그런데 PRD 「방」의 12종 팔레트와 `RoomColor`·`MinoRoomColor`에는 `BROWN`이 있고, `:core:common:ui`에는 `room_thumbnail_brown` 에셋도 이미 있다. **이 스펙이 정할 수 없는 어긋남이므로 봉합하지 않는다** — `RoomColor.BROWN`은 `black` variant로 떨어뜨려 두고 협의 항목으로 세운다(§완료 보고).

**Alternatives considered**:
- *단일 에셋에 `ColorFilter`로 색을 입힘* — 소품 모양이 variant마다 다르므로 색만 바꿀 수 없다.
- *`:core:common:ui`로 선제 승격* — 사용처가 홈 하나뿐이라 승격 기준 미충족. R-010이 같은 판단을 이미 내렸다.
- *`RoomThumbnailFallback`에 크기 variant를 추가해 겸용* — 배경 유무·비율·크기가 모두 달라 한 컴포넌트의 파라미터로 표현되지 않는다.

---

## R-016. 방 전환 툴팁의 위치 표현 *(plan 3.0.0)*

**Decision**: `MinoTooltip`의 기존 파라미터로 표현한다 — `position = TooltipPosition.Left`, `align = TooltipAlign.Center`. 새 컴포넌트도 새 파라미터도 만들지 않는다.

**Rationale**: 시안(노드 `2809:143382`)의 툴팁은 본문 158 + 화살표 8 = **166×56**이고 화살표가 **오른쪽 변 세로 중앙**에 붙어 캐릭터를 가리킨다. 종전 구현은 `Bottom`·`End`(화살표가 아래 변)였다. `TooltipAlign.Center`가 세로 배치에서 중앙을 뜻하므로 값 두 개를 바꾸는 것으로 끝난다.

> **`position`은 화살표가 붙는 변이 아니라 말풍선이 놓이는 방향이다**(`MinoTooltip` KDoc: *"앵커를 기준으로 말풍선이 놓이는 방향이다. 화살표는 그 반대편, 즉 앵커를 향하는 변에 붙는다"*). 시안이 요구하는 것은 「말풍선이 캐릭터 **왼쪽**, 화살표는 그 오른쪽 변」이므로 값은 `Left`다. 3.0.0 작성 시점에 이 항목을 화살표 쪽 이름인 `Right`로 적어 두었고 구현이 그것을 바로잡았다 — 되돌리면 툴팁이 캐릭터 오른쪽으로 넘어가 시안과 어긋난다.

**자리는 호출부가 정한다** — 툴팁 컴포저블은 이미 위치를 `modifier`로 받고 있어(`HomeTooltipOverlay` KDoc) 조립부의 오프셋만 시안값으로 맞춘다. 그 오프셋은 실측값이므로 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md) §2의 판정 4번을 따라 주석 없이 리터럴로 쓴다.

**Alternatives considered**:
- *툴팁을 캐릭터의 형제로 넣고 자동 앵커링* — 캐릭터가 화면 프레임의 마지막 자식으로 오른쪽 끝에 걸려 있어(현행 `HomeTopShell` KDoc) 형제 배치로는 시안의 좌측 오프셋이 나오지 않는다.
- *`MinoTooltip`에 앵커 파라미터를 추가* — 사용처가 홈 하나인데 디자인 시스템 컴포넌트의 표면을 넓히는 것이 된다.

---

## R-017. `다른 방 저장`이 여는 시트를 어디서 얻는가 *(plan 3.0.0)*

**Decision**: `:feature:sharereceiver`의 `RoomPickerSheet`(와 그 하위 `RoomPickerList`·`RoomPickerHeader`·`RoomPickerActionArea`)를 **`:core:common:ui`로 승격**해 홈이 재사용한다. 홈에 네 번째 사본을 만들지 않는다.

**Rationale**: spec 4.0.0 FR-005가 `다른 방 저장`의 시트를 「홈 방 시트」에서 **「방 선택 시트」([SYS-002]형)** 로 바꿨다. 그 시트는 [SYS-002] 외부 공유 수신이 이미 구현해 두었고([`shared-link-receiver/contracts/room-picker-sheet-ui.md`](../shared-link-receiver/contracts/room-picker-sheet-ui.md)), `:feature:room`의 장소 상세가 `RoomPickerItem`을 따로 두며 **한 번 복제된 상태**다. 홈이 세 번째 사용처이므로 [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §2.1의 승격 기준을 명확히 넘는다.

**「홈 방 시트」는 그대로 남는다.** 방을 **바꾸는** 그리드 시트(FR-017·018)와 장소를 **담는** 리스트 시트(FR-005)는 PRD가 다른 시트로 갈라 놓았고, R-011의 "홈 방 시트를 공용으로 올리지 않는다"도 그대로 유효하다 — 승격 대상은 방 선택 시트 쪽이다.

**승격은 이 spec의 구현 범위 안이지만 소유는 아니다.** 컴포넌트의 공개 표면은 [SYS-002] 계약이 소유하므로 홈은 그 표면을 **바꾸지 않고** 쓴다. 옮기는 작업 자체가 두 feature를 건드리므로 `tasks.md`에서 독립 작업으로 세운다.

**Alternatives considered**:
- *홈이 자기 리스트 시트를 새로 만듦* — 같은 시트의 네 번째 사본. 중복 처리·CTA 문구·단계별 높이가 [SYS-002] 계약과 조용히 갈라진다.
- *`:feature:home`이 `:feature:sharereceiver`를 의존* — 헌법 II(feature 간 의존 금지) 위반.
- *승격을 나중으로 미루고 일단 복제* — 복제 시점에 이미 세 번째라 미룰 근거가 없고, 되돌리는 비용만 커진다.

---

## R-018. 완료 안내의 문구와 일러스트 *(plan 3.0.0)*

**Decision**: 일러스트는 시안(노드 `5073:101117`, 209×209)으로 갈고, **문구도 시안의 `모든 장소를 다 봤어요!`를 따른다.** 이 문구는 **PRD → spec 개정이 뒤따라야 확정된다** — 그 개정이 이 plan의 선행 조건이다(§복잡도 추적).

**Rationale**: [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md) §3이 "산문 설명과 노드가 어긋나면 노드를 따르고 그 사실을 보고에 적는다"고 정한 대로 노드를 따른다. 디자인이 새로 그려진 화면에서 문구만 옛 값으로 남기면 화면 안에서 일러스트와 문구의 출처가 갈린다.

**그래도 이 항목이 spec보다 앞선 것은 사실이다.** FR-014와 PRD [SCR-003] Flow E는 아직 `꾹 눌러둔 장소를 모두 둘러봤어요`를 값으로 못박고 있다. 헌법 IV(Spec-First)를 지키려면 **구현이 착수되기 전에 그 두 문서가 개정되어야** 하고, 그것이 이 plan이 남기는 유일한 미결 선행 조건이다. 순서를 뒤집지 않기 위해 `tasks.md`의 이 문구 작업은 개정 이후로 잠근다.

**요구사항이 값을 정하지 않은 자리**(일러스트·타이포·색)는 그대로 시안을 따른다 — `Headline 2/Medium`(SUITE Medium 17 / line-height 1.412)과 `Semantic/Label/Neutral`(#2E2F33)은 변수가 붙어 있으므로 §2 판정 2번에 따라 대응 토큰으로 접근한다.

**Alternatives considered**:
- *문구만 spec 값으로 유지* — 검토했고 기각했다. 같은 화면에서 일러스트는 새 시안, 문구는 옛 spec이 되어 어느 쪽도 온전히 따르지 않는 상태가 남는다. 다만 이 선택이 Spec-First를 지키는 쪽이었으므로, 채택한 안에서는 spec 개정을 선행 조건으로 명시해 그 값을 대신 지킨다.
- *두 문구를 모두 두고 플래그로 고름* — 어느 것이 맞는지 정하지 않은 것을 코드가 떠안는다.

---

## R-019. 「경과일 초기화 확인」과 `다른 방 저장`을 어느 계약으로 부르는가 *(plan 3.0.0)*

**Decision**: `HomeDeckRepository`에서 두 함수를 **빼고** `PlaceRepository`의 것을 쓴다.

| 종전 (plan 2.0.0) | 3.0.0 |
|---|---|
| ~~`HomeDeckRepository.recordPlaceOpened(pinId)`~~ | ~~`PlaceRepository.recordAccess(pinId)`~~ → **호출 자체를 걷었다**(재검토됨, spec 4.0.0) |
| `HomeDeckRepository.savePinToRoom(pinId, roomId)` | `PlaceRepository.duplicatePin(pinId, roomIds)` |

> **①은 옮긴 것이 아니라 걷어냈다(재검토됨, spec 4.0.0).** 「같은 서버 호출에 계약이 둘」을 없애려 처음에는
> `PlaceRepository.recordAccess`로 옮기려 했으나, [SCR-006] 장소 상세가 **진입 경로와 무관하게** 이미 기록하므로
> (`place-detail` FR-026) 홈이 부르면 카드 한 번 탭에 두 건이 쌓인다. 「앱 전역에서 일어난다」는 PRD의 규정은
> 어느 화면에서든 **한 번씩**이라는 뜻이지 여러 화면이 겹쳐 부른다는 뜻이 아니다. 그래서 옮기는 대신 홈에서
> **지웠다** — 위 R-012의 갱신 주석과 같은 결론이고, `duplicatePin`만 `PlaceRepository`의 것을 쓴다.

**Rationale**: 둘 다 **홈만의 동작이 아니다.** 「경과일 초기화 확인」은 PRD 「장소 확인 이벤트」 ①이 *"앱 전역에서 일어난다"* 고 못박은 이벤트이고, `다른 방 저장`은 PRD가 *"저장 동작 자체는 [SYS-003] 장소 복제와 같다"* 고 적은 경로다. [SCR-006] 장소 상세가 두 서버 호출을 이미 계약으로 갖고 구현까지 마친 상태라, 홈이 자기 이름의 함수를 따로 두면 **같은 서버 호출에 도메인 계약이 둘**이 된다 — 헌법 I(SSOT) 위반이다.

**plan 2.0.0 시점에는 이 중복이 보이지 않았다.** `place-detail` spec이 그 뒤에 들어오면서 `PlaceRepository`가 생겼고, 홈 쪽 두 함수가 사후적으로 사본이 됐다. 이번 개정이 그것을 걷어낸다.

**복수 선택은 이 교체가 아니면 성립하지 않는다.** spec 4.0.0 FR-005가 방을 여럿 고르게 하므로 인자가 `roomId` 하나에서 `roomIds` 목록으로 늘어야 한다. `duplicatePin`이 이미 그 모양이고, 빈 목록을 부르지 않는다는 전제까지 계약에 적혀 있다.

**`HomeDeckRepository`에 남는 것은 홈만의 것 둘뿐이다** — `getRoomSummaries`(순회 순서를 확정하는 자리)와 `getDeck`(정렬별 덱 조회).

**Alternatives considered**:
- *홈 쪽 함수를 두고 내부에서 `PlaceRepository`에 위임* — 위임 한 겹이 늘 뿐 계약은 여전히 둘이다. 호출부에서 어느 것이 진짜인지 보이지 않는다.
- *`PlaceRepository`의 두 함수를 공용 이름으로 다시 가름* — 소유가 `place-detail` spec에 있어 이 spec이 그 표면을 바꾸지 않는다. 필요해지면 그쪽 개정으로 다룬다.
- *`recordAccess`만 옮기고 저장은 홈에 남김* — 같은 근거가 둘에 똑같이 적용되는데 하나만 지키는 것이 된다.
