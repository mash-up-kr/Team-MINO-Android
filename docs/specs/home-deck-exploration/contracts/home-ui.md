# 계약: 홈 탭 화면 표면

**대상 스펙 경로**: `docs/specs/home-deck-exploration`

**계획서**: [plan.md](../plan.md)

이 문서는 `:feature:home`이 **밖으로 여는 것**과 화면 내부의 **Intent·SideEffect 계약**을 정한다. 타입 이름과 시그니처까지가 이 문서의 범위이고, 함수 본문은 구현 단계의 몫이다.

---

## 1. 모듈 공개 표면

홈은 **탭 feature**다 — [`architecture/feature-module.md`](../../../architecture/feature-module.md) §1. Activity·Shell·NavHost를 갖지 않고, 셸(`:feature:main`)이 등록 함수를 호출한다.

`public`인 것은 기존과 같이 **`HomeNavigation.kt` 하나**다. 그 밖(화면·ViewModel·컴포넌트)은 전부 `internal`.

```kotlin
@Serializable
data object HomeGraph : Route            // 기존 유지

fun NavGraphBuilder.homeGraph(
    onNavigateToPlaceDetail: (pinId: String) -> Unit,   // FR-007 → [SCR-006]
    onNavigateToRoomForm: () -> Unit,                   // EC-015 `방 만들기`
    onCreateRoomFromEmpty: () -> Unit,                  // FR-020 빈 상태 CTA
)
```

**기존 `onNavigateToSample`·`onRequestSampleResult`는 제거한다** — 스텁이 남긴 것이고 이 spec의 요구사항에 대응이 없다.

세 콜백 모두 **feature 밖으로 나가는 전환**이라 셸이 배선한다. 홈 안에서 끝나는 전환(방 시트·액션 메뉴·가이드)은 콜백으로 내보내지 않는다 — 전부 `HomeUiState`의 상태다.

> `onCreateRoomFromEmpty`와 `onNavigateToRoomForm`을 나눈 이유: 전자는 [SYS-009] 공동방 생성 **유도**, 후자는 [SYS-001] 생성 **폼**이다. spec이 EC-015와 FR-020에서 서로 다른 흐름으로 지목했다. 셸이 같은 곳으로 배선하더라도 홈이 그 판단을 대신하지 않는다.

---

## 2. Intent 계약

`HomeIntent`는 사용자 조작 하나에 하나씩 대응한다.

| Intent | 발생 | 대응 |
|---|---|---|
| `SwipeForward` | 우측 영역 좌→우 드래그 완료 | FR-001, FR-023, TS-001, TS-035 |
| `SwipeBackward` | 우측 영역 우→좌 드래그 완료 | FR-002, TS-002, EC-001·003 |
| `TransitionSettled` | 전환 애니메이션 종료 | UX-001, TS-007 (R-007) |
| `SelectSort(sort)` | 정렬 칩 탭 | FR-010, TS-020·021 |
| `OpenActionMenu(pinId)` | 카드 `[...]` 탭 | FR-005, TS-008, EC-007 |
| `DismissActionMenu` | 메뉴 바깥 탭 · 스와이프 | EC-004·005 |
| `SaveToAnotherRoom(pinId)` | 메뉴의 `다른 방 저장` 선택 → 「방 선택 시트」를 연다 | FR-005, TS-011 |
| `ToggleSaveTargetRoom(roomId)` | 「방 선택 시트」의 체크박스 탭 | FR-005, TS-011a·011b |
| `ConfirmSaveTargets` | 「방 선택 시트」의 `저장하기` 탭 | FR-005, TS-011a, EC-018 |
| `DismissSavePicker` | 「방 선택 시트」 닫기 | FR-005 |
| `OpenPlaceDetail(pinId)` | 카드 본문 탭 | FR-007, FR-023, TS-012·013·034 |
| `OpenRoomSheet` | 방 뱃지 · 캐릭터 탭 → 「홈 방 시트」를 연다 | FR-017, TS-025·026 |
| `SelectRoom(roomId)` | 「홈 방 시트」에서 방 선택 | FR-018, FR-024, TS-028·028a·028b·028c, EC-014·020·022 |
| `DismissRoomSheet` | 「홈 방 시트」 닫기 | FR-017 |
| `DismissGuide` | 가이드 우측 상단 닫기 | FR-019, TS-031 |
| `LocationPermissionResult(location)` | 권한 다이얼로그 응답 — 허용이면 좌표, 거부면 `null` | EC-009 (R-009·R-013) |

**두 확인 이벤트는 서로를 건드리지 않는다**(FR-023).

- `SwipeForward` → 덱에서 카드를 덜어낼 뿐 **서버를 부르지 않는다**(TS-035).
- `OpenPlaceDetail` → `NavigateToPlaceDetail` SideEffect를 던지는 것이 전부다. **덱은 그대로 두고**(TS-013), **「경과일 초기화 확인」도 홈이 보내지 않는다**(TS-034) — 이동해 간 [SCR-006]이 기록한다(`docs/specs/place-detail/spec.md` FR-026).
- `SwipeBackward` → 직전 `SwipeForward`만 취소한다. **[SCR-006]이 이미 기록한 초기화는 되돌리지 않는다**(EC-017).

> **홈은 두 확인 이벤트 중 어느 것도 서버로 보내지 않는다**(spec 4.0.0 FR-023). ①은 [SCR-006]이 소유하고 ②는 클라이언트 전용이라, 아래 Repository 계약에 기록 함수가 없다.

**시트가 둘이므로 Intent도 둘로 갈린다**(spec 4.0.0 FR-005·FR-017). 「홈 방 시트」는 방을 **바꾸고**(누르는 것이 곧 확정), 「방 선택 시트」는 장소를 **담는다**(체크박스 복수 선택 + `저장하기`). 한 시트를 두 용도로 돌려 쓰면서 "지금 어느 용도인가"를 상태 플래그로 들고 있으면, 시트를 닫지 않은 채 방을 고른 순간 저장이 방 전환으로 뒤바뀐다.

**`isGuideVisible == true`이면 `DismissGuide`를 뺀 모든 Intent를 버린다**(FR-019, TS-030).
**`isTransitioning == true`이면 `SwipeForward`·`SwipeBackward`를 버린다**(UX-001, TS-007).

---

## 3. SideEffect 계약

| SideEffect | 쓰임 |
|---|---|
| `NavigateToPlaceDetail(pinId)` | FR-007 |
| `NavigateToRoomForm` | EC-015 |
| `RequestLocationPermission` | EC-009 (R-009) |
| `ShowSaveResult(success)` | FR-005의 저장 완료·실패 알림 |

도메인 에러는 SideEffect로 흘리지 않는다 — `DomainErrorEmitter`를 `HomeRoute`가 수집해 스낵바로 띄운다([`architecture/feature-module.md`](../../../architecture/feature-module.md) §4, [`conventions/error_handling.md`](../../../conventions/error_handling.md) §5·§6).

---

## 4. 도메인 계약 (`:core:domain`)

### 4.1 전환 규칙의 단일 출처 — UseCase 둘 (R-003·R-014)

탐색 범위가 다른 두 판정이므로 **함수를 둘로 가른다.** 반환 타입은 같다.

```kotlin
/** 자동 전환 — 탐색 범위는 격자 전체 (FR-011·012·025) */
class ResolveNextDeckUseCase @Inject constructor() {
    operator fun invoke(context: DeckContext): NextDeck
}

/** 수동 방 변경 — 탐색 범위는 고른 방 하나 (FR-024) */
class ResolveRoomEntryDeckUseCase @Inject constructor() {
    operator fun invoke(context: DeckContext, roomId: String): NextDeck
}
```

#### 자동 전환 — `ResolveNextDeckUseCase`

**탐색 축은 「한 정렬로 모든 방 → 다음 정렬」이다.** 격자를 「정렬 우선, 그 안에서 방 순서」로 훑어 **아직 소진되지 않은 칸 중 순서상 가장 앞선 것**을 돌려준다.

1. `DeckSort` 선언 순서로 정렬을 훑고, 각 정렬에서 `context.rooms` 순서로 방을 훑는다. `exhausted`에 없고 `pinCount > 0`인 첫 칸이 답이다 — FR-011·012·025, TS-015·016·017·019·019a·021
2. 그 칸의 방이 현재 방과 같으면 → `SameRoom(sort)` / 다르면 → `NextRoom(roomId, sort)`
3. 남은 칸이 없으면 → `AllExhausted` — FR-014, TS-024

**`NextRoom`이 정렬을 함께 실어야 한다.** 종전 시그니처(`NextRoom(roomId)`)는 방이 바뀌면 정렬이 `꾹 Pick`으로 초기화된다는 옛 규칙에 기대 정렬을 생략했다. 자동 전환이 정렬을 유지하게 된 지금(FR-012) 어느 정렬로 가는지가 반환값에 없으면 호출부가 다시 추측해야 한다.

**현재 정렬·현재 방보다 앞선 칸으로 되돌아가는 것이 정상이다.** 사용자가 칩으로 건너뛴 칸은 소진이 아니므로 순회가 다시 데려온다(FR-010, TS-021) — 종전 규칙의 "앞으로 되돌아가지 않는다"는 폐기됐다.

**방 순서는 이 함수가 정하지 않는다.** `context.rooms`가 **이미 「개인방 먼저, 그다음 생성이 오래된 순」으로 정렬된 목록**이라고 보고 받은 순서를 그대로 훑는다(FR-012, R-014). 정렬 책임은 §4.2가 갖는다.

#### 수동 방 변경 — `ResolveRoomEntryDeckUseCase`

**탐색 범위를 `roomId` 하나로 한정한다.**

1. `roomId`의 세 정렬 중 `exhausted`에 없는 것이 있으면 → `SameRoom(그중 DeckSort 선언 순서상 최우선)` — FR-024, TS-028·028a·028b
2. 없으면 → `AllExhausted` — FR-024·FR-014, TS-028c, EC-020

**`NextRoom`을 절대 내지 않는다.** 그것이 곧 "다른 방으로 넘기지 않는다"(FR-024·SC-008)의 코드 표현이다. 저장 장소가 0개인 방은 세 정렬이 모두 후보 0건이므로 1번을 통과하지 못해 자연히 2번으로 떨어진다 — 별도 분기를 두지 않는다(EC-020·022).

#### 둘에 공통인 것

**순수 함수다.** 부수효과도 I/O도 상태도 없다 — 그래서 TS-015~021·028a~c를 JVM 테스트로 그대로 옮길 수 있다.

**호출 시점**: 덱 소진 시, 정렬 칩 선택 시, 위치 권한 거부 시(EC-009), 시트에서 방을 고를 때(수동 쪽), 그리고 고른 덱의 후보가 0장으로 판명됐을 때 다시(EC-013). **방이나 정렬에 들어올 때 미리 계산해 두지 않는다**(FR-011).

**위치 권한 거부는 `가까운순` 칸을 방마다가 아니라 통째로 소진 처리한 뒤 다시 묻는다**(EC-009). 권한은 방별 값이 아니므로 호출부가 `exhausted`에 `가까운순 × 모든 방`을 넣고 판정을 다시 부른다 — UseCase에 권한 개념을 넣지 않는다.

### 4.2 `HomeDeckRepository`

```kotlin
interface HomeDeckRepository {
    /**
     * 순회 대상 방 목록. pinCount 포함.
     *
     * **순서를 이 함수가 확정한다** — 개인방 먼저, 그다음 방을 만든 지 오래된 순(FR-012, R-014).
     * 여러 화면이 공유하는 방 목록 조회의 응답 순서에 기대지 않으므로, 구현이 받은 응답을
     * 이 순서로 재배치해 돌려준다. 호출부와 `ResolveNextDeckUseCase`는 받은 순서를 그대로 훑는다.
     */
    suspend fun getRoomSummaries(): List<RoomSummary>

    /**
     * roomId·sort의 덱. 서버가 최대 10장으로 잘라 주므로 받은 것을 그대로 담는다.
     *
     * [location]은 sort가 NEAREST일 때만 쓰인다. NEAREST인데 null이면 요청을 보내지 않고
     * **빈 덱**을 돌려준다 — 위치 권한 거부를 「소진」으로 흡수한다(EC-009, R-013).
     */
    suspend fun getDeck(roomId: String, sort: DeckSort, location: GeoPoint? = null): Deck

    // 「경과일 초기화 확인」을 알리는 함수는 없다 — spec 4.0.0에서 [SCR-006]이 소유한다.
}
```

> **기록 함수가 두 번 바뀌어 끝내 사라졌다.** plan 1.0.0의 `recordCardConsumed`는 *넘김*을 서버에 알리는 함수였는데, spec 3.0.0에서 넘김이 서버와 무관해지면서(「카드 열람 확인」은 클라이언트 전용) 호출 시점을 상세 진입으로 옮기고 `recordPlaceOpened`로 바꿨다. spec 4.0.0에서는 그 기록의 소유가 [SCR-006]으로 넘어가 **함수 자체를 걷어냈다** — 홈과 상세가 같은 `POST /pins/{pinId}/accesses`를 쳐서 카드 한 번 탭에 두 건이 쌓였기 때문이다.

> **`savePinToRoom`도 이 인터페이스에서 빠졌다** — `PlaceRepository`가 이미 소유한 동작이다(R-019). 아래 §4.2.1 참고.

#### 4.2.1 이미 있는 계약을 쓴다 — `PlaceRepository` (R-019)

`다른 방 저장`은 **홈만의 동작이 아니다.** [SCR-006] 장소 상세가 같은 서버 호출을 이미 계약으로 갖고 있으므로 홈은 그것을 쓴다.

```kotlin
// core/domain — PlaceRepository (place-detail spec 소유, 이 spec은 호출만 한다)
suspend fun duplicatePin(pinId: String, roomIds: List<String>)   // FR-005
```

- **`duplicatePin`** — spec 4.0.0 FR-005가 **복수 선택**을 요구하므로 인자는 방 하나가 아니라 `roomIds`다. `roomIds`가 비면 부르지 않는다는 전제도 그쪽 계약이 이미 갖고 있고, 홈에서 그 전제를 지키는 것은 `저장하기` 비활성 규칙(EC-018)이다.
- **`recordAccess`는 홈이 부르지 않는다.** R-019가 처음 세울 때는 「경과일 초기화 확인」도 이 목록에 있었으나, spec 4.0.0이 그 기록의 소유를 [SCR-006] 하나로 넘겼다 — 홈이 부르면 상세도 진입 시 기록하므로 카드 한 번 탭에 두 건이 쌓인다. 「앱 전역에서 일어난다」는 PRD의 규정은 어느 화면에서든 **한 번씩**이라는 뜻이지 여러 화면이 겹쳐 부른다는 뜻이 아니다.

> **`GeoPoint`는 [`core/map`](../../../../core/map/README.md)이 소유한다.** 새로 만들지 않는다.

`Flow`를 흘리지 않고 실패를 `Result`로 감싸지 않는다 — 기존 `RoomRepository`와 같은 규약이다.

### 4.3 `HomePreferencesRepository`

```kotlin
interface HomePreferencesRepository {
    suspend fun getLastRoomId(): String?          // FR-022, TS-033
    suspend fun setLastRoomId(roomId: String)
    suspend fun isGuideDismissed(): Boolean       // FR-019, TS-031
    suspend fun dismissGuide()
}
```

영속 대상은 **이 둘뿐**이다(R-004). 소진 상태·정렬·되돌리기 이력은 저장하지 않는다.

---

## 5. 컴포넌트 배치

어느 모듈에 두는지는 [`conventions/component-asset-placement.md`](../../../conventions/component-asset-placement.md)를 단일 출처로 따른다.

| 컴포넌트 | 모듈 | 근거 |
|---|---|---|
| 카드 덱 스택 · 장소 카드 | `:feature:home` | 사용처가 홈뿐 |
| 정렬 칩 3종 | `:feature:home` | Figma 디자인 시스템 컴포넌트로 존재하면 `:core:design-system` — **구현 착수 시 확인** |
| 방 뱃지 | `:feature:home` | R-010 |
| **방 캐릭터 + 색상별 에셋 12종** | `:feature:home` | R-015. 사용처가 홈뿐이라 승격 기준 미충족. `:core:common:ui`의 `RoomThumbnailFallback`과 **다른 에셋군**이다 |
| 홈 방 시트 (방 변경) | `:feature:home` | R-011 — 그대로 유지 |
| **방 선택 시트 (`다른 방 저장`)** | `:core:common:ui`로 **승격** | R-017. `:feature:sharereceiver`가 이미 구현했고 홈이 세 번째 사용처다. 홈이 사본을 만들지 않는다 |
| 툴팁 2종 | `:feature:home` | 사용처가 홈뿐. `MinoTooltip`을 `position = Right`·`align = Center`로 쓴다(R-016) |
| 액션 메뉴 | `:feature:home` | 카드 앵커에 묶인 형태 |
| **완료 안내 일러스트** | `:feature:home` | 사용처가 홈뿐. 209×209, 노드 `5073:101117`에서 재export(R-018) |

색·치수·타이포는 [`core/design-system/README.md`](../../../../core/design-system/README.md)의 토큰을 단일 접근점으로 하되, **값이 일치하는 토큰이 없으면 Figma 실측값을 그대로 쓴다.** 판정 절차는 [`conventions/figma-design-fidelity.md`](../../../conventions/figma-design-fidelity.md)를 따른다 — 헌법 §기술 표준과 제약.

FR-018이 명시한 치수(400dp 고정 높이·70dp 썸네일)는 spec이 확정한 값이므로 그대로 쓴다.

**시안 대조로 확인된 값**(plan 3.0.0, 조회 2026-09-03) — 요구사항이 값을 정하지 않은 자리다.

| 대상 | 노드 | 값 |
|---|---|---|
| 방 캐릭터 | `4306:63718` `Home_Avatar` | **126×172** (종전 구현 126×164). variant 12종 = `black` + 11색 |
| 방 전환·예고 툴팁 | `2809:143382` 내 `4221:54341` | 166×56 (본문 158 + 화살표 8). 화살표가 **오른쪽 변 세로 중앙**, 캐릭터 왼쪽에 놓인다 |
| 완료 안내 일러스트 | `5073:101117` | 209×209 |
| 완료 안내 문구 | 같은 노드 | `모든 장소를 다 봤어요!` · `Headline 2/Medium`(SUITE Medium 17 / line-height 1.412) · `Semantic/Label/Neutral` #2E2F33. **문구는 spec FR-014 개정을 선행 조건으로 갖는다**(R-018) |
| 시트 방 썸네일 | `4306:63731` `Room Thumbnail_HOME` | 컴포넌트 80×80, 시트에서 **70dp로 사용**. 기존 `room_thumbnail_*` 에셋과 **동일한 그림이라 교체가 필요하지 않다** |

**시트 썸네일은 바뀌지 않았다.** 「방 변경 시트 내 캐릭터 변경」으로 지목된 노드를 열어 기존 에셋과 대조한 결과 같은 그림이었다. 이 항목은 확인으로 닫히고 작업이 붙지 않는다.

**두 컴포넌트셋 모두 `brown` variant가 없다** — 협의 항목이다(R-015).
