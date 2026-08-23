# 계약: `CardDeck` 컴포넌트 API

**대상 스펙 경로**: `docs/specs/home-card-deck` · **계획서**: [plan.md](../plan.md)

카드덱이 **호출자(홈 화면 셸)에게 노출하는 표면**이다. 이 계약이 spec §3.2의 "목록을 입력으로 받고, 신호를 밖으로 보낸다"를 실체화한다.

---

## 1. 컴포넌트 시그니처

```
@Composable
fun CardDeck(
    cards: List<PlaceCard>,
    onCardConfirmed: (pinId: String) -> Unit,
    onLoadMore: () -> Unit,
    onSaveToOtherRoom: (pinId: String) -> Unit,
    modifier: Modifier = Modifier,
    state: CardDeckState = rememberCardDeckState(),
)
```

> **파라미터 순서는 `ComposeParameterOrder`(`lint.xml`에서 **error**)가 강제한다** — 필수 파라미터 → `modifier` → 기본값 있는 파라미터. plan 1.0.0은 `state`를 필수 파라미터 앞에 두어 이 규칙과 충돌했고, 그대로는 컴파일 게이트를 통과할 수 없었다. 규약이 [`compose-lint.md`](../../../conventions/compose-lint.md)로 단일 출처를 갖는 이상 **고쳐야 하는 쪽은 계약 문서다.** (plan 2.0.0 정정)

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
- **C-08·C-18의 "10장 충족" 판정은 공개 표면에 없다.** 상태 홀더가 이미 들고 있는 덱 원본의 크기로 내부에서 판정하며, 호출자가 알아야 할 값이 아니다([research.md](../research.md) D11).

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
| C-08 | **덱이 10장으로 구성된 경우에 한해** 잔여 카드가 2장 이하가 되면 `장소 더 보기` Floating Button을 노출한다. 판정은 **덱 구성 시점의 장수**(`CardDeckState`의 덱 원본 크기)로 하며, 넘김·`장소 가리기`로 줄어든 장수는 쓰지 않는다 | FR-007, TS-007·018 |
| C-09 | `장소 더 보기` 클릭 시 `onLoadMore()`를 발생시킨다. 덱은 스스로 목록을 가져오지 않는다 | FR-007·008 |
| C-10 | 새 `cards`가 주입되면 진행 상태를 초기화하고 덱을 다시 구성한다 | FR-008, TS-008 |
| C-11 | 카드 `[...]` 클릭 시 `다른 방 저장`·`장소 가리기` 두 항목 메뉴를 **그 카드 근처에** 연다 | FR-004, UX-002, TS-010·011 |
| C-12 | `장소 가리기`는 현재 덱에서만 제거한다. 새 목록에 다시 들어오면 정상 노출된다 | FR-005, TS-012·013 |
| C-13 | `다른 방 저장` 선택 시 메뉴를 닫고 `onSaveToOtherRoom(pinId)`만 발생시킨다. **덱 진행 상태는 변하지 않는다** | FR-004, TS-014 |
| C-14 | 각 카드에 장소분류 라벨 1종을 표시한다 | FR-009, TS-015·016 |
| C-15 | 액션 메뉴가 열린 상태의 스와이프는 메뉴를 닫고 카드 전환에 반영하지 않는다 | EC-005 |
| C-16 | 메뉴 바깥 탭은 아무 액션 없이 메뉴만 닫는다 | EC-007 |
| C-17 | 덱이 비어도 `장소 더 보기` 버튼 노출은 유지한다 — 단 **10장으로 구성된 덱이었을 때만**이다. 어느 쪽이든 빈 상태 안내는 하지 않는다 | EC-003·006 |
| C-18 | 덱이 **10장 미만으로 구성**되었으면 잔여 장수와 무관하게 `장소 더 보기` 버튼을 끝까지 노출하지 않는다. 목록을 준 쪽에 더 줄 카드가 없다는 뜻이므로 눌러도 채울 것이 없다 | FR-007, TS-017, EC-003·006 |

---

## 4. 카드 한 장 — `MinoHomeCard` 재사용

`:feature:sample`에서 **`:core:design-system`의 `component/homecard/`**로 옮기고 `MinoHomeCard`로 명명한다([research.md](../research.md) D10 — Figma 디자인 시스템 컴포넌트이므로 사용처 개수와 무관하게 이 모듈 소속이다). **파라미터 구성은 바꾸지 않고**, 이름과 자리만 규약에 맞춘다.

```
MinoHomeCard(category, title, address, modifier, imageCount = 2, avatarImageUrl, onMoreClick)
```

| `MinoHomeCard` 파라미터 | `PlaceCard` 매핑 |
|---|---|
| `category` | `label` → `HomeCardCategory` 1:1 변환 |
| `title` | `placeName` |
| `address` | `address` |
| `imageCount` | 고정 `2` (카드가 2칸 그리드) |
| `avatarImageUrl` | `registrant?.avatarUrl` |
| `onMoreClick` | 덱이 액션 메뉴를 여는 콜백 |

카드 내부 시각 스타일은 spec §3.2 비목표이므로 손대지 않는다.

**변환은 `:feature:home`이 갖는다.** 도메인 `PlaceCategoryLabel` → variant 축 `HomeCardCategory` 매핑을 디자인 시스템 쪽에 두면 공용 모듈이 도메인을 알게 된다. `MinoHomeCard`는 자기 variant 축만 알고, 도메인과의 연결은 카드덱이 책임진다([research.md](../research.md) D10).
