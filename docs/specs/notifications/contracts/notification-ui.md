# 계약: 알림 탭 화면 표면

**대상 스펙**: [spec.md](../spec.md) 7.0.0 · **계획**: [plan.md](../plan.md)

`:feature:notifications`가 밖으로 여는 것과, 모듈 안에서 화면과 ViewModel이 주고받는 계약을 정의한다.

---

## 1. 모듈이 밖으로 여는 표면

```
@Serializable data object NotificationGraph : Route          // public
@Serializable internal data object NotificationMain : Route
@Serializable internal data object SaveErrorGuide : Route

fun NavGraphBuilder.notificationGraph(
    onNavigateToPlaceDetail: (pinId: String) -> Unit,
    onNavigateToRoomDetail: (roomId: String) -> Unit,
)
```

**public인 것은 `NotificationGraph`와 `notificationGraph()` 둘뿐이다.** `:feature:home`의 `HomeGraph`/`homeGraph()`와 같은 모양이며, [`feature-navigation.md`](../../../architecture/feature-navigation.md) 3장이 정한 탭 feature의 화면 표면 규칙이다.

### 콜백이 둘인 이유

| 전환 | 처리 | 근거 |
|---|---|---|
| 장소 대상 알림 → 장소 상세 | `onNavigateToPlaceDetail` 콜백 | **저장 탭 안의 화면.** 셸이 `PlaceDetailRequestHolder`에 요청을 남기고 저장 탭으로 옮긴다([research.md D14](../research.md)) |
| 공동방 참가 → 방 상세 | `onNavigateToRoomDetail` 콜백 | 같은 형태. 셸이 `RoomDetailRequestHolder`에 요청을 남기고 저장 탭으로 옮긴다([research.md D10](../research.md)) |
| 저장 오류 알림 → 안내 화면 | **그래프 내부 전환** | FR-011·UX-008 — 알림 탭 안에 머물러야 바텀 네비게이션이 유지된다([research.md D2](../research.md)) |

**둘 다 이 모듈 밖으로 나가지만 Activity 전환이 아니다.** 장소 상세도 방 상세도 저장 탭 그래프 안의 화면이고, 탭 전환이 백스택을 복원하면서 Route 인자를 되살리므로 여는 값이 홀더를 지난다([ADR 2026-09-02](../../../adr/2026-09-02-immersive-map-screen-shares-one-map-in-tab-feature.md)). 이 모듈은 그 사정을 몰라도 되며, 아는 것은 **`pinId`·`roomId`를 콜백으로 올린다**까지다 — 홀더도 탭 목록도 셸의 것이다.

### 셸이 하는 일

`:feature:main`에서 아래를 바꾼다.

- `MainDestinations.kt`의 `Notification` Route를 **지운다.** Route 소유가 알림 모듈로 넘어간다.
- `MainTab.NOTIFICATION`의 `route`를 `NotificationGraph`로 바꾼다.
- `MainNavHost`의 `screen<Notification> { MainTabPlaceholderScreen(...) }`를 `notificationGraph(...)` 호출로 교체한다(아래 배선).
- `MainShell`·`MainNavHost`에 `onRequestRoomDetail: (roomId: String) -> Unit`을 더하고, `MainActivity`가 `roomDetailRequestHolder::request`를 넘긴다.

`MainNavHost`가 채우는 두 람다는 홈이 이미 쓰는 것과 같은 모양이다.

```kotlin
notificationGraph(
    onNavigateToPlaceDetail = { pinId ->
        onRequestPlaceDetail(pinId, PlaceDetailEntryOrigin.NOTIFICATION)
        navController.navigateToTab(MainTab.SAVED)
    },
    onNavigateToRoomDetail = { roomId ->
        onRequestRoomDetail(roomId)
        navController.navigateToTab(MainTab.SAVED)
    },
)
```

- `onRequestPlaceDetail`은 **이미 `MainNavHost`의 파라미터다** — 홈이 `PlaceDetailEntryOrigin.HOME`으로 쓰고 있고, 알림은 `NOTIFICATION`을 싣는 것만 다르다. 그 값도 이미 정의돼 있다([research.md D14](../research.md)).
- `onRequestRoomDetail`은 **새로 뚫어야 한다.** `RoomDetailRequestHolder`는 `MainActivity`에 주입돼 있지만 푸시 딥링크 소비 경로에서만 쓰이고 `MainShell`·`MainNavHost`로 내려오지 않는다. `onRequestPlaceDetail`과 같은 자리에 `onRequestRoomDetail = roomDetailRequestHolder::request`를 더한다.

**푸시 딥링크의 낙하 지점을 깨뜨리지 않는지 확인한다.** `MainActivity.resolvePendingPushDestination()`이 도착지를 정할 수 없을 때 `MainTab.NOTIFICATION`으로 보내고(push-notification spec EC-009), 그 탭의 `route`가 placeholder `Notification`에서 `NotificationGraph`로 바뀐다. `MainTab`이 route를 들고 `startTab`·`pendingTab`이 그 값을 쓰므로 배선은 그대로 성립하지만, Route 소유를 옮긴 뒤 **콜드(`startTab`)·웜(`pendingTab`) 두 경로 모두** 알림 탭이 열리는지 확인한다([push-deeplink-contract §5](../../push-notification/contracts/push-deeplink-contract.md)).

---

## 2. `NotificationIntent`

사용자 조작 하나가 Intent 하나로 들어와 `processIntent`의 한 분기로 간다.

| Intent | 발생 | 대응 |
|---|---|---|
| `Load` | 화면 최초 진입 | 첫 페이지 조회. **방 목록을 함께 부르지 않는다**([research.md D5](../research.md)) |
| `Retry` | 오류 상태의 재시도 | `Load`와 같되 `phase`를 `Loading`으로 되돌린다 |
| `ReachedEnd` | 목록 끝 도달 | 다음 페이지 요청. `hasNext`가 `false`거나 `isAppending`이면 무시(EC-018) |
| `RetryAppend` | 목록 끝 재시도 표시 탭 | `appendError`를 내리고 같은 페이지를 다시 요청 |
| `NotificationClicked(id)` | 알림 행 탭 | 도착지 해석 후 `SideEffect` 또는 오류 방출 |
| `SaveErrorGuideBackClicked` | 안내 화면 뒤로가기 | `NavigateBack` 방출 |

`ReachedEnd`는 **자동으로 발생한다** — UX-011이 `더 보기` 버튼이나 새로고침을 금지하므로 목록 끝 감지가 곧 Intent다.

`NotificationClicked`가 `id`만 싣는다. 유형·대상은 ViewModel이 들고 있는 도메인 목록에서 찾으며, 화면 모델에는 그 필드가 없다([data-model.md §2.1](../data-model.md)).

### 중복 탭 방어 (EC-011)

`NotificationClicked` 처리 중에는 같은 Intent를 무시한다. spec 5.0.0에서 도착지 해석이 조회 없는 순수 매핑이 되어 처리 구간은 짧아졌지만([contracts/notification-repository.md §2](./notification-repository.md)), 홀더 적재와 탭 전환이 두 번 나가면 여전히 어긋난다.

---

## 3. `NotificationSideEffect`

```
sealed interface NotificationSideEffect
  NavigateToPlaceDetail(pinId: String)
  NavigateToRoomDetail(roomId: String)
  NavigateToSaveErrorGuide
  NavigateBack
```

**모듈 안에서 끝나는 상태 변화는 여기 싣지 않는다.** 목록 갱신·오류 표시·추가 로드는 모두 `NotificationUiState`다 — `:feature:home`이 쓰는 것과 같은 갈림이다.

`NavigateToSaveErrorGuide`가 SideEffect인 이유는 그래프 내부 전환이어도 **`NavController` 조작이라 Route가 해야 하기 때문이다.** ViewModel은 `NavController`를 알지 않는다.

`NavigateToRoomDetail`은 `NotificationDestination.RoomDetail`에서, `NavigateToPlaceDetail`은 `PlaceDetail`에서 나온다([data-model.md §1.5](../data-model.md)). 둘 다 Route가 §1의 콜백으로 흘린다.

---

## 4. 화면 구성

### 4.1 `NotificationRoute` (`NotificationMain`)

- ViewModel의 상태를 구독하고 SideEffect를 수집해 콜백·`NavController`로 흘린다.
- `DomainErrorEmitter`를 수집해 목록 조회 실패를 알린다. **EC-009·EC-010(대상 소멸)은 여기서 다루지 않는다** — spec 7.0.0 UX-006이 그 판정을 도착지 화면의 몫으로 옮겼고, 알림함은 이동 전에 되묻지 않는다.

### 4.2 `NotificationScreen`

| 상태 | 그리는 것 | 근거 |
|---|---|---|
| `Loading` | 빈 상태 문구를 **그리지 않는다** | UX-001 |
| `Content` | 상단 `알림` 제목 + 행 목록. 끝에 `isAppending`·`appendError` 표시 | FR-002·UX-011·UX-012 |
| `Empty` | 스팟 일러스트 + `받은 알림이 없어요` | FR-006 |
| `Error` | 재시도 가능한 오류 상태 | UX-002·EC-001 |

목록은 **화면 최상단부터 시작한다.** 권한 유도 배너 자리를 두지 않는다(FR-017·UX-010).

### 4.3 알림 행

썸네일 · 유형 문구 · 대상 이름 · 경과 시간 네 요소(FR-002). **행 전체가 클릭 영역이다**(UX-005).

- 유형 문구와 대상 이름은 각각 한 줄, 넘치면 말줄임. 모든 행의 높이가 같다(UX-007).
- 읽음 여부에 따른 표현 차이를 두지 않는다 — 배경·색·굵기·점 어느 것도(FR-016·TS-032).
- 썸네일은 `NotificationThumbnail` 두 갈래를 `when`으로 남김없이 가른다([data-model.md §2.2](../data-model.md)).
  - `Image` → `MinoAsyncImage`. `url`이 `null`이면 플레이스홀더.
  - `SaveError` → 고정 오류 아이콘
- **`:core:design-system`에 더할 것이 없다.** 방 썸네일 컴포넌트를 쓰지 않으므로 `MinoRoomThumbnail`도 건드리지 않는다([research.md D5·D13](../research.md)).

### 4.4 `SaveErrorGuideScreen` (`SaveErrorGuide`)

스팟 일러스트 + `확인해주세요` 제목 + 고정 3줄 안내(FR-010). 상단 뒤로가기가 목록으로 되돌린다(FR-011).

**ViewModel을 갖지 않는다.** 화면에 담기는 값이 전부 고정 문자열이고 상태가 없다 — 어느 저장 오류 알림을 눌렀든 같은 화면이다(EC-013).

시스템 뒤로가기는 별도 처리 없이 목적지 팝으로 동작한다(EC-014).

---

## 5. 바텀 네비게이션

이 모듈은 바텀 네비게이션을 **직접 다루지 않는다.**

- `MainShell`이 항상 그리고 `currentTab()`이 `hierarchy`를 훑어 상위 탭을 찾으므로, `SaveErrorGuide`에서도 `알림` 탭이 선택 상태로 유지된다(FR-011·TS-030).
- 장소 상세·방 상세는 저장 탭의 몰입 화면이라 `ImmersiveRouteRegistry`·`LocalBottomNavVisibility`가 바텀 네비게이션을 감춘다(FR-020·TS-042). 셸의 기존 동작이며 알림 모듈이 관여하지 않는다([research.md D14](../research.md)).
- 탭을 떠났다 돌아올 때의 스크롤 위치 보존은 `navigateToTab`의 `saveState`/`restoreState`가 덮는다(FR-015·TS-011·TS-031·TS-043).

세 가지 모두 **셸의 기존 동작이라 이 모듈이 코드를 더하지 않는다.** [SYS-005]가 소유한다(spec §3.2).
