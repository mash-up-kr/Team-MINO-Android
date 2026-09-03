# 계약: 크로스 feature 의존성 (`:core:navigation`)

**대상 plan**: [../plan.md](../plan.md) · 근거: [../research.md](../research.md) D3·D9

room-list의 [contracts/navigation-launchers.md](../../room-list/contracts/navigation-launchers.md)와 달리, 이 spec은 크로스 feature **Launcher**를 새로 만들지 않는다 — 방 상세 자체가 `:feature:room` 내부 nested Route이기 때문이다([research.md D1·D2](../research.md)). 이 문서가 다루는 것은 (1) `:core:navigation`에 신설하는 마커 인터페이스, (2) room-list가 이미 선언한 `RoomFormLauncher`의 재사용뿐이다.

## `ImmersiveRoute` — 신규 마커 인터페이스 ([FR-003] 등 몰입 화면)

```kotlin
// :core:navigation — screen/ImmersiveRoute.kt
/**
 * 바텀 네비게이션을 숨겨야 하는 몰입 화면임을 표시하는 빈 마커 인터페이스.
 *
 * 탭 셸(`:feature:main`의 `MainShell`)이 현재 목적지가 이 마커를 구현하는지만 검사해
 * `bottomBar` 슬롯을 조건부로 그린다. 셸이 구체 Route 타입이나 feature 이름을 알 필요가
 * 없도록, 몰입 화면을 만드는 feature는 자신의 Route에 이 마커를 함께 구현한다.
 */
interface ImmersiveRoute
```

**구현 (`:feature:room`)**

```kotlin
@Serializable
internal data class RoomDetailMain(val roomId: String) : Route, ImmersiveRoute
```

**소비 (`:feature:main`)**

```kotlin
// MainShell.kt — bottomBar 슬롯 조건부 렌더링
val backStackEntry by navController.currentBackStackEntryAsState()
val isImmersive = backStackEntry?.destination?.hierarchy
    ?.any { it.route != null && /* 목적지의 Route 인스턴스가 ImmersiveRoute를 구현하는지 판정 */ } == true

MinoScaffold(
    bottomBar = { if (!isImmersive) MainBottomBar(...) },
) { ... }
```

- 정확한 판정 방법(NavDestination에서 Route 구현 여부를 어떻게 안전하게 읽어내는지 — `NavBackStackEntry.toRoute<T>()` 또는 `KClass` 기반 검사)은 구현 세부라 `/mino-task`가 확정한다. 이 계약은 **무엇을 검사해야 하는지**(마커 인터페이스 구현 여부)만 못박는다.
- **다른 feature에도 구속력을 갖는 결정** — `ImmersiveRoute`는 room-detail만을 위한 타입이 아니라 이후 몰입 화면을 만드는 모든 feature가 구현해야 하는 공용 계약이다. **완료 보고에서 ADR 승격을 제안한다**([research.md D3](../research.md)).

## `RoomFormLauncher` — [FR-012] 방 편집 (재사용, 신규 계약 아님)

```kotlin
// :core:navigation — activity/launcher/RoomFormLauncher.kt (room-list가 이미 선언, 변경 없음)
interface RoomFormLauncher : ActivityLauncher
```

**호출 (`:feature:room/detail/`)**

```kotlin
@Inject lateinit var roomFormLauncher: RoomFormLauncher
// ...
roomFormLauncher.launch(activity, resultLauncher = editRoomResultLauncher) {
    // 편집 모드 진입에 필요한 extra(예: EXTRA_ROOM_FORM_EDIT_ROOM_ID) — [TBD]
}
```

> ⚠️ **미구현 의존성**: 구현체는 이 spec 범위 밖 모듈(`:feature:roomform`, 현재 미존재)이 갖는다 — room-list [research.md D6](../../room-list/research.md)이 이미 크로스 feature 의존성으로 선언해 둔 계약과 동일하다. **편집 모드 진입에 필요한 extra 키·완료 결과(result) 스키마는 이 계약에 아직 없다** — `RoomFormLauncher`는 room-list D6이 "생성" 용도로만 확정했고, room-detail이 필요로 하는 "기존 값이 채워진 상태로 진입"([spec.md FR-012])은 `:feature:roomform`의 plan이 만들어질 때 함께 확장돼야 한다. `/mino-task`가 이 작업을 room-list·room-form 작업과 순서를 맞춰 배치해야 한다.

## SYS-003 · SYS-006 · SYS-007 — Activity 계약 없음, 내부 컴포넌트로 대체

방 선택 시트([SYS-003], [FR-009])·초대 시트([SYS-006], [FR-011])·나가기/위임 모달([SYS-007], [FR-013])은 모두 `:feature:room/detail/component/`의 내부 바텀시트·다이얼로그로 구현하며, 이 문서가 정의하는 크로스 feature 계약이 아니다([research.md D10·D11·D12](../research.md)). 세 시스템 전용 spec은 여전히 이 저장소에 없지만, 실제 데이터 계약은 배포된 서버 API 대조로 대부분 확정됐다([research.md D14·D15·D16](../research.md)):

- [SYS-003] 방 선택 후 복제 — `PlaceRepository.duplicatePin(pinId, roomIds)`, 서버 `POST /pins/{pinId}/duplicate`. 시트를 [SCR-006] 장소 상세와 합치면서 중복이던 `RoomPlacesRepository.sharePlaces`를 걷어내고 이쪽으로 모았다 ([place-detail-main-contract.md §3.4.6](../../place-detail/contracts/place-detail-main-contract.md))
- [SYS-006] 초대 링크 생성 — `RoomRepository.createInvitation(roomId)`, 서버 `POST /rooms/{roomId}/invitations` ([contracts/place-repository.md](./place-repository.md) "`RoomRepository` 확장")
- [SYS-007] 나가기·위임 — `RoomRepository.leaveRoom`·`transferOwner`, 서버 `DELETE /rooms/{roomId}/members/me`·`PUT /rooms/{roomId}/owner` (같은 절)

남는 [TBD]는 `Place.commentCount`·`isGgukPick`(서버 미노출 필드)뿐이다 — [data-model.md §4](../data-model.md) 참조. `deletePlace`는 2026-09-03 `DELETE /api/v1/pins/{pinId}` 배포로 해소됐다.
