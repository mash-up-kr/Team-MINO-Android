# 계약: `CardDeck` 컴포넌트 API

**대상 스펙 경로**: `docs/specs/home-card-deck` · **계획서**: [plan.md](../plan.md)

카드덱이 **호출자(홈 화면 셸)에게 노출하는 표면**이다. 이 계약이 spec §3.2의 "목록을 입력으로 받고, 신호를 밖으로 보낸다"를 실체화한다.

---

## 1. 컴포넌트 시그니처

```
@Composable
fun CardDeck(
    cards: List<PlaceCard>,
    state: CardDeckState = rememberCardDeckState(),
    onCardConfirmed: (pinId: String) -> Unit,
    onLoadMore: () -> Unit,
    onSaveToOtherRoom: (pinId: String) -> Unit,
    modifier: Modifier = Modifier,
)
```

| 파라미터 | 방향 | 책임 | 근거 |
|---|---|---|---|
| `cards` | 입력 | 덱을 채울 목록. 최대 10장만 사용, 0개면 덱을 그리지 않음 | FR-006 |
| `state` | 입출력 | 덱 진행 상태. 호출자가 보유·관찰할 수 있게 호이스팅 | [research.md](../research.md) D1 |
| `onCardConfirmed` | 출력 | 카드를 **넘겨 확인**한 시점에 발생. 호출자가 `recordAccess`를 호출 | FR-001, D7 |
| `onLoadMore` | 출력 | `장소 더 보기` 클릭. 호출자가 새 목록을 가져와 `cards`로 다시 넣음 | FR-007·008 |
| `onSaveToOtherRoom` | 출력 | `다른 방 저장` 선택. 호출자가 「홈 방 시트」를 연다 | FR-004, D8 |

**카드덱이 하지 않는 것**: 네트워크 호출, 「홈 방 시트」 표시, 정렬·방 전환, 빈 상태·완료 화면 안내. 전부 spec §3.2 비목표다.

---

## 2. 상태 홀더

```
@Composable
fun rememberCardDeckState(): CardDeckState

@Stable
class CardDeckState {
    val currentCard: PlaceCard?
    val remainingCount: Int
    val canUndo: Boolean
    val isAnimating: Boolean
    fun hidePlace(pinId: String)
}
```

- 프로퍼티 정의와 전이 규칙은 [data-model.md](../data-model.md) §3.1이 소유한다. 여기서 다시 쓰지 않는다.
- `hidePlace`만 공개 함수다 — 스와이프·되돌리기는 제스처로만 일어나므로 외부에서 호출할 이유가 없다.

---

## 3. 동작 계약

| ID | 계약 | 대응 |
|---|---|---|
| C-01 | 화면 **우측 영역**의 좌→우 드래그가 임계값을 넘으면 카드를 넘기고 `onCardConfirmed(pinId)`를 1회 발생시킨다 | FR-001, TS-001 |
| C-02 | 우측 영역의 우→좌 드래그는 직전 1장을 복구한다. 복구 시 `onCardConfirmed`를 **취소하지 않는다**(이미 나간 신호는 되돌리지 않음) | FR-002, TS-002 |
| C-03 | **좌측 영역**의 드래그는 전환·복구 어느 쪽에도 반영하지 않는다 | FR-003, TS-003 |
| C-04 | 임계값 미만에서 손을 떼면 카드가 원위치하고 아무 신호도 나가지 않는다 | EC-002 |
| C-05 | 되돌릴 카드가 없으면 우→좌 드래그는 무동작이다 | EC-001, TS-002 |
| C-06 | 전환 애니메이션 중 추가 스와이프 입력을 무시한다 | UX-001, TS-009 |
| C-07 | `cards`가 10개를 넘으면 앞에서 10장만, 10개 미만이면 있는 만큼, 0개면 덱을 그리지 않는다 | FR-006, TS-004~006 |
| C-08 | 잔여 카드가 2장 이하가 되면 `장소 더 보기` Floating Button을 노출한다 | FR-007, TS-007 |
| C-09 | `장소 더 보기` 클릭 시 `onLoadMore()`를 발생시킨다. 덱은 스스로 목록을 가져오지 않는다 | FR-007·008 |
| C-10 | 새 `cards`가 주입되면 진행 상태를 초기화하고 덱을 다시 구성한다 | FR-008, TS-008 |
| C-11 | 카드 `[...]` 클릭 시 `다른 방 저장`·`장소 가리기` 두 항목 메뉴를 **그 카드 근처에** 연다 | FR-004, UX-002, TS-010·011 |
| C-12 | `장소 가리기`는 현재 덱에서만 제거한다. 새 목록에 다시 들어오면 정상 노출된다 | FR-005, TS-012·013 |
| C-13 | `다른 방 저장` 선택 시 메뉴를 닫고 `onSaveToOtherRoom(pinId)`만 발생시킨다. **덱 진행 상태는 변하지 않는다** | FR-004, TS-014 |
| C-14 | 각 카드에 장소분류 라벨 1종을 표시한다 | FR-009, TS-015·016 |
| C-15 | 액션 메뉴가 열린 상태의 스와이프는 메뉴를 닫고 카드 전환에 반영하지 않는다 | EC-005 |
| C-16 | 메뉴 바깥 탭은 아무 액션 없이 메뉴만 닫는다 | EC-007 |
| C-17 | 덱이 비어도 `장소 더 보기` 버튼 노출은 유지한다. 빈 상태 안내는 하지 않는다 | EC-003·006 |

---

## 4. 카드 한 장 — `HomeCard` 재사용

`:feature:sample`에서 `:feature:home`으로 이동한다([research.md](../research.md) D3). **시그니처를 바꾸지 않는다.**

```
HomeCard(category, title, address, imageCount = 2, avatarImageUrl, onMoreClick)
```

| `HomeCard` 파라미터 | `PlaceCard` 매핑 |
|---|---|
| `category` | `label` → `HomeCardCategory` 1:1 변환 |
| `title` | `placeName` |
| `address` | `address` |
| `imageCount` | 고정 `2` (카드가 2칸 그리드) |
| `avatarImageUrl` | `registrant?.avatarUrl` |
| `onMoreClick` | 덱이 액션 메뉴를 여는 콜백 |

카드 내부 시각 스타일은 spec §3.2 비목표이므로 손대지 않는다.
