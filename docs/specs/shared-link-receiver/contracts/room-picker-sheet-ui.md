# 계약: 방 선택 시트 UI

**대상 스펙 경로**: `docs/specs/shared-link-receiver`

**명세서**: [spec.md](../spec.md) · **계획**: [plan.md](../plan.md)

시트를 구성하는 컴포넌트의 **소속 모듈과 공개 표면**을 정한다. 상태·의도 슬롯은 [data-model.md §5](../data-model.md)가 소유하고, 배치 판정 규칙은 [`component-asset-placement.md`](../../../conventions/component-asset-placement.md)가 소유한다.

**Figma**: [013-1-1 peek](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2792-176010) · [013-1-2 full_4개](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2792-176059) · [013-1-3 full_4개 이상](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2792-176034)

---

## 1. 배치 판정

`013-1-2`(노드 `2792:176059`)의 노드 트리를 열어 판정했다. **컴포넌트 인스턴스는 `:core:design-system`, 로컬 프레임은 feature**가 소유한다.

| Figma 노드 | 종류 | 소속 모듈 | 현황 |
|---|---|---|---|
| `Card_Room` | 인스턴스 | `:core:design-system` | `:feature:sample`에서 이관 |
| `Room Thumbnail` | 인스턴스 | `:core:design-system` | 이관 중 분리 |
| `Checkbox` | 인스턴스 | `:core:design-system` | 이관 중 분리 |
| `Scroll Bar/Scroll Bar` | 인스턴스 | `:core:design-system` | 신설 |
| `Action Area/Action Area` | 인스턴스 | `:core:design-system` | **이미 있음** (`component/actionarea`) |
| `Frame 198`(시트 컨테이너) · `controller`(핸들) · `Frame 522`(헤더) | 로컬 프레임 | `:feature:sharereceiver` | 신설 |
| 저장 완료 토스트 | 인스턴스 | `:core:design-system` | **이미 있음** (`component/snackbar`) |

이관 대상 파일 목록과 근거는 [research.md R-010](../research.md)이 소유한다.

---

## 2. `:core:design-system` 공개 표면

컴포넌트 API는 Material3 컴포넌트 패턴(`Defaults`·`Colors`·컴포넌트 토큰)을 따른다 — 본문은 [`core/design-system/README.md`](../../../../core/design-system/README.md) §6.1이 소유한다.

### 2.1 `component/roomcard/` — 이관

```
MinoRoomCheckBoxCard(
    title: String,
    placeCountLabel: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnail: @Composable () -> Unit,
    memo: String? = null,
    colors: MinoRoomCheckBoxCardColors = MinoRoomCardDefaults.checkBoxColors(),
)
```

이관하면서 바뀌는 것:

| 변경 | 이유 |
|---|---|
| `coverImageUrl: String?` → `thumbnail: @Composable () -> Unit` 슬롯 | 썸네일이 단일 이미지에서 콜라주·색상 폴백 두 형태를 갖는 컴포넌트가 되었다(§2.2). 카드가 그 분기를 알 필요가 없다 |
| `private fun RoomCheckBox` 제거 | `MinoCheckbox`(§2.3)로 분리해 Figma의 `Checkbox` 인스턴스와 대응시킨다 |
| `private fun RoomCardCover` 제거 | `MinoRoomThumbnail`(§2.2)로 분리한다 |

`MinoRoomCard`(아바타 표시형)·`RoomCardContent`·`MinoRoomCardDefaults`·`RoomCardTokens`도 함께 이관한다. **`MinoRoomCard`는 이 feature가 쓰지 않지만** 같은 Figma 컴포넌트셋의 다른 variant이므로 분리해 두면 소유가 갈린다.

> **호출부 규칙(이 feature 한정)**: `onClick`과 `onCheckedChange` 모두 같은 `ToggleRoom` 의도를 올린다. UX-003이 "카드 영역 어디를 눌러도 선택이 토글된다"로 정하므로 두 동작이 갈리지 않는다.

### 2.2 `component/roomthumbnail/` — 신설

```
MinoRoomThumbnail(
    imageUrls: ImmutableList<String>,
    color: RoomColor,
    modifier: Modifier = Modifier,
)
```

| `imageUrls` | 표현 |
|---|---|
| 비어 있음 | `color`의 대표 색상 배경 + 캐릭터 글리프 |
| 1~4장 | 콜라주 (1·2·3·4장 배치) |
| 5장 이상 | 앞 4장만 |

- PRD 「방 썸네일」의 두 형태를 그대로 옮긴다.
- 서버가 이미지를 내려주기 전까지는 항상 빈 목록이 들어와 색상 폴백이 그려진다([research.md R-003](../research.md)).
- `RoomColor`는 이미 `:core:domain`에 있고 팔레트는 `:core:design-system`이 소유한다(방 색상 팔레트 ADR).

### 2.3 `component/checkbox/` — 신설

```
MinoCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: MinoCheckboxColors = MinoCheckboxDefaults.colors(),
)
```

- 이관 전 `MinoRoomCheckBoxCard` 안의 `private fun RoomCheckBox` 구현(`surface` + `rippleSingleSelectable` + `MinoIcons.Check`)을 그대로 옮긴다. 그 코드의 주석 "디자인 시스템에 Checkbox 컴포넌트가 아직 없어 여기서 최소 형태로 그린다"가 이 분리를 예고하고 있다.
- `role = Role.Checkbox`를 유지한다 — 접근성 서비스가 선택 상태를 읽는 근거다.
- `enabled = false`는 이 feature가 쓰지 않는다. [SYS-003]·[SCR-003]의 '체크된 채 비활성' 규칙이 쓸 슬롯이라 API에 미리 연다.

### 2.4 `component/scrollbar/` — 신설

```
MinoScrollBar(
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
)
```

- Figma `013-1-2`에서 목록 오른쪽 끝(`x=366`, `width=9`)에 붙는다.
- UX-005의 스크롤 어포던스는 "마지막 카드가 잘려 보이는 것"이 주된 수단이고, 스크롤 바는 보조다.

---

## 3. `:feature:sharereceiver` 소유 컴포넌트

모두 로컬 프레임이므로 feature가 갖는다. 두 번째 사용처가 생기면 [`component-asset-placement.md`](../../../conventions/component-asset-placement.md) §2.1의 승격 기준으로 `:core:common:ui`로 옮긴다.

| 컴포넌트 | 역할 | 근거 |
|---|---|---|
| `RoomPickerSheet` | 딤 배경 + `AnchoredDraggable` 2단 높이 컨테이너 + 핸들 | FR-003, FR-008, UX-001, [research.md R-007](../research.md) |
| `RoomPickerHeader` | `게시물 저장` / `장소를 저장할 방을 선택해주세요.` | FR-004 |
| `RoomPickerList` | `LazyColumn` + `MinoRoomCheckBoxCard` + `MinoScrollBar` | FR-005, FR-007, UX-004 |
| `RoomPickerEmpty` | 저장할 방이 없다는 안내 | FR-013, UX-011 |

### 3.1 높이 계약

`RoomPickerSheet`의 앵커는 고정 dp다. 방 개수와 무관하게 단계 구성은 같다(EC-005).

| 단계 | 높이 | 조건 |
|---|---|---|
| `Peek` | 436dp | 진입 기본값. 카드 2개 온전 + 3번째 잘림 |
| `Full` | 612dp | 방 4개 이하 |
| `Full` | 644dp | 방 5개 이상. 카드 4개 온전 + 5번째 잘림 |

- 하단 액션 영역(102dp)을 **포함한** 값이다.
- 헤더 94dp · 카드 104dp/개는 Figma 실측이며, 값이 일치하는 토큰이 있으면 토큰으로 접근한다 — 판정 절차는 [`figma-design-fidelity.md`](../../../conventions/figma-design-fidelity.md)를 따른다.

### 3.2 고정 영역

헤더와 하단 액션 영역은 목록 밖에 있어 스크롤에 따라 움직이지 않는다(UX-004). `LazyColumn`은 시트 높이에서 헤더·액션 영역을 뺀 공간만 차지한다.

---

## 4. 토스트

저장 완료 피드백은 기존 `MinoSnackbar`를 쓴다.

| 항목 | 값 | 근거 |
|---|---|---|
| 문구 | `저장이 완료됐습니다.` | FR-010 |
| 아이콘 | 체크 | UX-006 |
| 위치 | 화면 하단에서 40dp | UX-006 |
| 종료 | 사용자 조작 없이 사라진 뒤 `finish()` | FR-011 |

- **시트가 닫힌 뒤에 뜬다.** 시트 안이 아니라 화면 하단이므로, Activity가 시트를 걷어내고 토스트만 남긴 상태로 잠시 유지한다.
- 이 토스트는 저장 성공을 보장하지 않는다 — "요청을 접수했다"는 피드백이다(spec §4 가정). 실제 성패는 알림함으로 전달된다.
