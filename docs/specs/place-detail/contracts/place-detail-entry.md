# 계약: 장소 상세 진입 (Place Detail Entry)

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [plan.md](../plan.md)

> **이 파일이 `place-detail-launcher.md`를 대체한다.** 장소 상세가 저장 탭으로 편입되면서([research.md D17](../research.md)) Activity가 사라졌고, `PlaceDetailLauncher`·`EXTRA_PLACE_DETAIL_PIN_ID`도 함께 폐기된다.

---

## 1. 진입점 네 곳과 각자의 경로

spec의 진입점은 넷이고, **저장 탭 안인지 밖인지로 두 갈래**다.

| # | 진입점 | 탭 | 경로 |
|---|---|---|---|
| ① | [SCR-005] 방 상세의 지도 마커 | 저장 | 탭 내부 — `RoomListIntent.OnPlaceSelected(pinId)` |
| ② | [SCR-005] 방 상세 시트의 장소 카드·리스트 | 저장 | 같음 |
| ③ | [SCR-003] 홈 카드 덱의 카드 클릭 | 홈 | 탭 간 — §3 요청 홀더 |
| ④ | [SCR-007] 알림 | 알림 | 탭 간 — 같은 홀더. **화면 미구현이라 이번 범위 밖** |

**어느 경로든 여는 값은 `pinId` 하나다.** 핀 = (장소, 방) 쌍이므로 「지금 보고 있는 방」(FR-027)은 그 안에 이미 정해져 있다([research.md D4](../research.md)).

---

## 2. 탭 내부 진입 (① ②)

`RoomDetailScreen`이 장소를 지목하면 `RoomDetailSideEffect.NavigateToPlaceDetail(pinId)`이 오르고, `RoomListRoute`가 그것을 받아 `RoomListViewModel`에 `selectedPinId`를 세운다.

**`RoomDetailSideEffect.NavigateToPlaceDetail`의 인자를 `placeId`에서 `pinId`로 바꾼다.** 현재 정의는 `data class NavigateToPlaceDetail(val placeId: String)`이고 `RoomDetailRoute`가 `-> Unit`으로 흘려버리고 있다(미배선). 서버 계약의 키가 `pinId`이므로 그 값으로 맞춘다.

```
RoomDetailScreen  ──OnPlaceClick(place)──>  RoomDetailViewModel
                                                  │
                                    NavigateToPlaceDetail(pinId)
                                                  ↓
RoomListRoute  ──OnPlaceSelected(pinId)──>  RoomListViewModel
                                                  │
                                          selectedPinId = pinId
```

지도 마커(①)도 같은 인텐트로 모인다 — 마커는 `RoomListMap`이 그리므로 `RoomListViewModel`에 직접 오른다.

---

## 3. 탭 간 진입 (③ ④) — `PlaceDetailRequestHolder`

### 3.1 계약

`:core:navigation`이 소유한다. 저장 탭과 다른 탭이 서로의 구체 타입을 모른 채 합의하는 자리다.

```kotlin
// :core:navigation — entry/PlaceDetailRequestHolder.kt
@ActivityRetainedScoped
class PlaceDetailRequestHolder @Inject constructor() {
    /** 열어야 할 핀. 소비되면 null로 돌아간다. */
    val pending: StateFlow<String?>

    /** 다른 탭이 장소 상세를 요청한다. */
    fun request(pinId: String)

    /** 저장 탭이 요청을 받아 갔다. */
    fun consume()
}
```

- **스코프는 `ActivityRetainedComponent`다.** 탭 전환은 같은 Activity 안의 일이고, 구성 변경(회전)에도 요청이 살아남아야 한다.
- **`pending`은 소비되면 비워진다.** 비우지 않으면 사용자가 [나가기]로 닫은 장소가 탭을 오갈 때마다 다시 열린다.

### 3.2 흐름

```
HomeScreen ──카드 탭──> HomeViewModel
                            │  HomeSideEffect.NavigateToPlaceDetail(pinId)   ← 기존 그대로
                            ↓
HomeRoute ──onNavigateToPlaceDetail(pinId)──> MainNavHost ──> MainActivity
                                                                  │
                                          ┌───────────────────────┘
                                          │ 1. holder.request(pinId)
                                          │ 2. navController.navigateToTab(MainTab.SAVED)
                                          ↓
RoomListViewModel  ──collect(holder.pending)──> holder.consume() 후
                                                selectedRoomId·selectedPinId·mapCenter를 함께 세운다
```

**여는 값은 셋이 한 벌이다** — 방(§4)·핀·카메라. 요청이 싣고 오는 것은 `pinId` 하나지만 나머지 둘은 핀 상세 응답(`roomId`·`location`)이 준다. 셋 중 하나가 빠지면 화면이 반쪽으로 열린다 — 카메라가 빠지면 선택 핀이 화면 밖에 남는다(spec FR-002·TS-056). 상세는 [place-detail-main-contract.md §2.3](./place-detail-main-contract.md).

**`:feature:home`은 바뀌지 않는다.** 홈은 이미 `pinId`를 콜백으로 올려보내는 데까지만 알고 있다. 목적지가 Activity에서 홀더로 바뀌는 것은 그 콜백을 받는 `:feature:main` 쪽이다.

**`:feature:home`과 `:feature:room`은 서로를 모른다.** 양쪽 다 `:core:navigation`에만 의존한다 — [ADR 2026-08-01](../../../adr/2026-08-01-single-module-navigation-contract.md)이 지키려는 방향 그대로다.

### 3.3 왜 Route 인자가 아닌가

`MainTabNavigation.navigateToTab`이 `popUpTo(...) { saveState = true }` + `restoreState = true`로 탭 백스택을 저장·복원한다. 복원된 항목은 **저장 당시의 인자를 들고 되살아나** 새 `pinId`가 반영되지 않는다. `restoreState`를 끄면 탭을 오갈 때마다 방 목록·시트 단계가 초기화된다.

같은 성격의 선례가 이미 둘 있다 — `LocalBottomNavVisibility`(`:core:common:ui`)와 `ImmersiveRouteRegistry`(`:core:navigation`). 상세는 [research.md D18](../research.md).

### 3.4 방은 요청에 싣지 않는다

핀 상세 응답(`GET /api/v1/pins/{pinId}`)이 `roomId`를 함께 준다. 알림이 방을 특정하지 않는다는 EC-001의 조건도 이 해석으로 충족된다 — 서버가 그 핀이 속한 방을 알려주기 때문이다.

---

## 4. 나가기 (FR-009)

**`selectedPinId = null` 하나다.**

```
[나가기] · 시스템 뒤로가기
        ↓
RoomListIntent.OnClosePlaceDetailClick
        ↓
selectedPinId = null   →  selectedRoomId != null 이므로 방 상세 Half가 드러난다
```

**시트 드래그는 이 경로에 없다.** `Half`가 시트의 하한이라 아래로 끌어도 닫히지 않는다(spec FR-001 · EC-003).

진입 경로와 무관하게 사용자가 남는 자리는 「지금 보고 있는 방」의 방 상세다 — spec TS-006(방 상세 진입)·TS-007(알림 진입)·TS-037(홈 진입)이 모두 요구하는 그대로다.

**홈·알림으로 들어온 경우 방 상세가 아직 안 열려 있다.** 그래서 진입 시 `selectedPinId`와 함께 `selectedRoomId`도 세운다 — 값은 핀 상세 응답의 `roomId`다. 상세는 [place-detail-main-contract.md §2](./place-detail-main-contract.md).

**시스템 뒤로가기의 우선순위**: 장소 상세 → 방 상세 → 리스트 → 탭·앱 이탈. `RoomListRoute`의 `BackHandler`가 이미 `selectedRoomId != null`로 잡고 있으므로, 조건을 `selectedPinId != null || selectedRoomId != null`로 넓히고 안에서 갈래를 가른다.

---

## 5. 삭제되는 계약

| 대상 | 위치 | 사유 |
|---|---|---|
| `PlaceDetailLauncher` | `:core:navigation/activity/launcher/PlaceDetailLauncher.kt` | Activity 폐기 |
| `EXTRA_PLACE_DETAIL_PIN_ID` | `:core:navigation/activity/launcher/ExtraTag.kt` | 같음 |
| `PlaceDetailLauncherImpl`·`PlaceDetailNavigationModule` | `:feature:placedetail/di/` | 모듈째 사라진다 |
| `MainActivity.launchPlaceDetail`·`placeDetailLauncher` 주입 | `:feature:main` | §3.2로 대체 |
