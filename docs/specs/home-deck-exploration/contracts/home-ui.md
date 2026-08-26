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
| `SwipeForward` | 우측 영역 좌→우 드래그 완료 | FR-001, TS-001 |
| `SwipeBackward` | 우측 영역 우→좌 드래그 완료 | FR-002, TS-002, EC-001·003 |
| `TransitionSettled` | 전환 애니메이션 종료 | UX-001, TS-007 (R-007) |
| `SelectSort(sort)` | 정렬 칩 탭 | FR-010, TS-020·021 |
| `OpenActionMenu(pinId)` | 카드 `[...]` 탭 | FR-005, TS-008, EC-007 |
| `DismissActionMenu` | 메뉴 바깥 탭 · 스와이프 | EC-004·005 |
| `SaveToAnotherRoom(pinId)` | 메뉴의 `다른 방 저장` 선택 | FR-005, TS-011 |
| `OpenPlaceDetail(pinId)` | 카드 본문 탭 | FR-007, TS-012·013 |
| `OpenRoomSheet` | 방 뱃지 · 캐릭터 탭 | FR-017, TS-025·026 |
| `SelectRoom(roomId)` | 시트에서 방 선택 | FR-018, TS-028, EC-014 |
| `DismissRoomSheet` | 시트 닫기 | FR-017 |
| `DismissGuide` | 가이드 우측 상단 닫기 | FR-019, TS-031 |
| `LocationPermissionResult(granted)` | 권한 다이얼로그 응답 | EC-009 (R-009) |

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

### 4.1 `ResolveNextDeckUseCase` — 전환 규칙의 단일 출처 (R-003)

```kotlin
class ResolveNextDeckUseCase @Inject constructor() {
    operator fun invoke(context: DeckContext): NextDeck
}
```

**규칙** — 아래 순서로 판정한다.

1. `context.currentRoomId`의 세 정렬 중 `exhausted`에 없는 것이 있으면 → `SameRoom(그중 DeckSort 선언 순서상 최우선)` — FR-011, TS-015·016·017·021
2. 없으면 방 목록에서 현재 방 **다음** 방부터 순회하며 `pinCount > 0`인 첫 방 → `NextRoom(roomId)` — FR-012·013, TS-018·019
3. 그런 방도 없으면 → `AllExhausted` — FR-014, TS-024

**순수 함수다.** 부수효과도 I/O도 없다 — 그래서 TS-015~019·021을 JVM 테스트로 그대로 옮길 수 있다.

**호출 시점**: 덱 소진 시, 정렬 칩 선택 시, 위치 권한 거부 시(EC-009), 그리고 고른 덱의 후보가 0장으로 판명됐을 때 다시(EC-013). **방에 들어올 때 미리 계산해 두지 않는다**(FR-011).

### 4.2 `HomeDeckRepository`

```kotlin
interface HomeDeckRepository {
    /** 순회 대상 방 목록. pinCount 포함. */
    suspend fun getRoomSummaries(): List<RoomSummary>

    /** roomId·sort의 덱. 최대 10장으로 잘린다. 후보가 없으면 빈 덱. */
    suspend fun getDeck(roomId: String, sort: DeckSort): Deck

    /** 카드를 넘긴 사실을 알린다(FR 대상 아님 — spec §3.2). 실패해도 화면을 막지 않는다. */
    suspend fun recordCardConsumed(pinId: String)

    /** FR-005. 실패는 MinoDomainException으로 던진다. */
    suspend fun savePinToRoom(pinId: String, roomId: String)
}
```

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
| 방 뱃지 · 방 캐릭터 | `:feature:home` | R-010 |
| 홈 방 시트 | `:feature:home` | R-011 |
| 툴팁 2종 | `:feature:home` | 사용처가 홈뿐 |
| 액션 메뉴 | `:feature:home` | 카드 앵커에 묶인 형태 |

색·치수·타이포는 [`core/design-system/README.md`](../../../../core/design-system/README.md)의 토큰을 단일 접근점으로 하되, **값이 일치하는 토큰이 없으면 Figma 실측값을 그대로 쓴다.** 판정 절차는 [`conventions/figma-design-fidelity.md`](../../../conventions/figma-design-fidelity.md)를 따른다 — 헌법 §기술 표준과 제약.

FR-018이 명시한 치수(400dp 고정 높이·70dp 썸네일)는 spec이 확정한 값이므로 그대로 쓴다.
