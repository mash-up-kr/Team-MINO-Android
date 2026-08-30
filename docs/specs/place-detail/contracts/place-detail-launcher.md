# 계약: 장소 상세 진입 (Place Detail Launcher)

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [../plan.md](../plan.md)

장소 상세는 진입형 feature다([research.md D1](../research.md)). 다른 feature는 이 모듈을 모른 채 `:core:navigation`의 계약만 주입받아 연다([feature-navigation.md 1장](../../../architecture/feature-navigation.md)).

---

## 1. 전환 계약 — `:core:navigation`

```kotlin
// activity/launcher/PlaceDetailLauncher.kt — 신규
interface PlaceDetailLauncher : ActivityLauncher
```

```kotlin
// activity/launcher/ExtraTag.kt — 기존 파일에 추가
const val EXTRA_PLACE_DETAIL_PIN_ID = "place_detail_pin_id"
```

키 이름은 규약이 정한 `EXTRA_<대상 feature>_<이름>` 형태를 따른다.

**구현 위치**: `:feature:placedetail`의 `di/PlaceDetailLauncherImpl.kt` + `di/PlaceDetailNavigationModule.kt`. 바인딩은 구현을 가진 모듈이 소유한다([ADR](../../../adr/2026-08-02-di-binding-ownership.md)).

## 2. 진입 인자

| extra | 타입 | 필수 | 의미 |
|---|---|---|---|
| `EXTRA_PLACE_DETAIL_PIN_ID` | `String` | **필수** | 열 핀의 id. 이 값 하나가 장소와 「지금 보고 있는 방」을 함께 특정한다([research.md D4](../research.md)) |

**인자가 `pinId` 하나인 이유**: 서버가 핀을 (장소, 방) 쌍으로 다루므로 `roomId`를 따로 실을 필요가 없다. FR-027이 규정한 초기 「지금 보고 있는 방」 결정 규칙은 **호출자가 어느 핀을 지목하느냐**로 이미 결정된다.

| 진입점 | 싣는 `pinId` | 근거 |
|---|---|---|
| [SCR-005] 방 상세 지도 마커 | 그 방 그 장소의 핀 | FR-027 — 그 화면이 보고 있던 방 |
| [SCR-005] 방 상세 목록 항목 | 그 방 그 장소의 핀 | 위와 같음 |
| [SCR-003] 홈 카드 클릭 | 카드가 담고 있는 핀(`data[].id`) | 홈 카드 응답이 핀 단위라 그대로 쓴다 |
| [SCR-007] 알림 | 알림이 지목한 핀 | FR-027이 "최초 저장 방"을 쓰라고 정했고, 알림이 어느 핀을 실을지는 [SCR-007] 소관 |

> `EXTRA_PLACE_DETAIL_PIN_ID`가 없거나 빈 문자열이면 화면을 열 수 없다. 이 경우의 처리는 `[TBD]` — spec에 근거가 없다. `/mino-task`가 방어 코드의 형태를 정한다.

## 3. 결과 반환

**이번 범위에서는 결과를 돌려주지 않는다.** [나가기](FR-009)는 `finish()`로 호출자에게 복귀하는 데까지만 구현한다([research.md D2](../research.md)).

| 상태 | 내용 |
|---|---|
| 지금 | `setResult`를 호출하지 않는다. 호출자는 `resultLauncher` 없이 `launch(activity)`로 연다 |
| `[TBD]` | "지금 보고 있는 방의 [SCR-005] 방 상세 `Half`로 나간다"(FR-009)를 만족하려면 나갈 때의 `roomId`를 돌려주고 호출자가 그 방 상세로 이동해야 한다. 목적지 화면이 아직 없어(#161 미머지) 이번에 정의하지 않는다 |

**미해결로 남는 갭**: [SCR-005] 방 상세·지도 마커에서 진입한 경우에는 호출자가 곧 목적지라 `finish()`가 우연히 FR-009를 만족한다. **[SCR-003] 홈 카드와 [SCR-007] 알림 진입은 호출자가 목적지가 아니므로 FR-009와 어긋난 채 남는다.** 이 사실을 완료 보고와 [plan.md](../plan.md) 요약이 나른다.

## 4. 호출 예시 (전환 시작 측)

```kotlin
@Inject lateinit var placeDetailLauncher: PlaceDetailLauncher
// ...
placeDetailLauncher.launch(this) { putExtra(EXTRA_PLACE_DETAIL_PIN_ID, pinId) }
```

컴포저블에서 직접 호출하지 않는다 — 화면은 `onNavigateToPlaceDetail(pinId)` 콜백만 올려보내고 전환은 Activity가 시작한다([feature-navigation.md 1장](../../../architecture/feature-navigation.md)).

## 5. room-detail 쪽 계약의 빈칸을 채운다

`origin/feature/154-room-list/base`의 `docs/specs/room-detail/contracts/room-detail-main-contract.md`가 아래를 비워 둔 상태다.

```kotlin
data class NavigateToPlaceDetail(val placeId: String) : RoomDetailSideEffect  // 계약은 [TBD](장소 상세 spec 부재)
```

이 문서가 그 빈칸을 채운다. 다만 **파라미터 이름이 `placeId`가 아니라 `pinId`여야 한다** — 방 상세가 목록·마커에서 들고 있는 값은 핀이다. room-detail 쪽 계약 문서의 갱신은 그 spec(#161)의 몫이며, 이 plan은 고치지 않는다([범위 가드](../../../../.claude/skills/mino-plan/SKILL.md)).

## 6. 모듈 등록

```text
settings.gradle.kts        include(":feature:placedetail")
app/build.gradle.kts       implementation(projects.feature.placedetail)
```

`:app`은 그래프 조립만 한다 — 바인딩을 두지 않는다(헌법 원칙 II).
