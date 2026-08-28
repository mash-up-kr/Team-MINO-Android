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
