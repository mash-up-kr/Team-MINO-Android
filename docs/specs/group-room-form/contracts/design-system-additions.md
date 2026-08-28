# 계약: 디자인 시스템 신설·확장

**대상 스펙 경로**: `docs/specs/group-room-form` · **부속 문서**: [plan.md](../plan.md)

이 feature가 `:core:design-system`에 요구하는 것. 각 항목의 **소속 모듈 판정 근거**는 [research.md](../research.md) R-006·R-007·R-008이고, 여기서는 API 표면만 정한다.

> 컴포넌트 구현 패턴(`Defaults`·`Colors`·컴포넌트 토큰)은 [`core/design-system/README.md`](../../../../core/design-system/README.md) §6.1, 값이 토큰이냐 실측값이냐의 판정은 [`figma-design-fidelity.md`](../../../conventions/figma-design-fidelity.md) §2가 소유한다.

> **Figma 노드 표기**: 이 문서의 `NNNN-NNNNN`은 [MU_디자인](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8) 파일의 노드 ID다. 디자인 시스템 라이브러리 노드는 [MU_Wanted Design System](https://www.figma.com/design/hkSOCt4kOfyaVWdxybTicF/MU_Wanted-Design-System--Community-) 파일 소속임을 그 자리에 밝힌다. 표기 규칙은 [`figma-design-fidelity.md`](../../../conventions/figma-design-fidelity.md) §5.

**공통 전제**: 이 문서가 정하는 것은 **API 표면과 축**이다. 아래에 적힌 치수는 이미 노드를 열어 읽은 값만이고, 나머지 치수·색은 구현 착수 시 대조해 정한다 — 판정 절차는 [`figma-design-fidelity.md`](../../../conventions/figma-design-fidelity.md) §2·§4가 소유한다.

---

## 1. `MinoTopNavigation` (확장)

`component/topnavigation/`

**판정**: Figma `MU_Wanted Design System (Community)` 라이브러리의 `component_set` `Top Navigation/Top Navigation`. 사용처 개수와 무관하게 디자인 시스템이 소유한다([R-007](../research.md)).

> [!NOTE]
> **이 컴포넌트는 신설이 아니라 확장이었다.** plan 1.0.0이 `[신규]`로 적었으나 2026-08-25 `597ea97`(다른 이슈)로 좌측 뒤로가기 + 중앙 타이틀이 이미 `develop`에 들어와 있었고, 이 feature가 더한 것은 **우측 텍스트 액션 축**이다. 그 판정과 아래 시그니처의 단일 출처는 [tasks.md](../tasks.md)의 T020이다.

**이번에 필요한 축**

| 축 | 값 | 쓰는 곳 |
|---|---|---|
| 좌측 | 뒤로가기 아이콘 버튼 / 없음 | 온보딩이면 없음 (FR-022) |
| 중앙 | 타이틀 텍스트 | 생성 `공동방 만들기` · 편집 `방 편집` (**FR-025**·TS-044) |
| 우측 | 텍스트 액션 / 없음 | 온보딩이면 [건너뛰기]. 편집은 비어 있다 (FR-017·TS-023) |

생성·편집 두 화면의 상단 바 노드는 `2314-95336` · `2542-125957`이다.

**API** — `develop`에 들어가 있는 실제 시그니처다.

```kotlin
@Composable
fun MinoTopNavigation(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,        // null이면 좌측을 그리지 않고 자리는 비워 둔다
    actionLabel: String? = null,              // null이면 우측을 그리지 않고 자리는 비워 둔다
    onActionClick: () -> Unit = {},
)
```

**초안과 갈렸던 두 지점** — 2026-08-25 사용자 결정으로 **현 구현을 따르기로 확정**했다. 이미 `develop`에 들어와 다른 화면이 쓰기 시작한 시그니처를 문서 초안에 맞추자고 흔드는 편이 대가가 크다.

| 초안 | 실제 | 왜 실제를 따르는가 |
|---|---|---|
| `onNavigateBack` | **`onBackClick`** | 이 모듈의 `on<대상>Click` 관례를 따른다. 우측 액션도 같은 관례로 `onActionClick`이다 |
| `colors: MinoTopNavigationColors = …` | **없음** | 현 구현이 `MinoTopNavigationDefaults`를 직접 읽는다. 색을 바깥에서 바꿀 호출자가 아직 없어 파라미터를 열지 않았다 |

**이번에 열지 않는 것**: 컴포넌트셋이 갖고 있을 다른 variant(아이콘 액션·검색 필드 등). 파라미터를 언제 늘리는지는 [`component-asset-placement.md`](../../../conventions/component-asset-placement.md) §3이 소유한다.

---

## 2. `MinoRoomColor` · `MinoRoomColorChip` (신설)

`component/roomcolorchip/`

**판정**: [방 색상 팔레트 ADR](../../../adr/2026-08-14-room-color-palette-in-design-system.md)이 이미 이 모듈·이 패키지·이 이름으로 결정했다. 이번 작업이 그 ADR이 말한 "첫 적용"이다.

**`MinoRoomColor`** — 12항목 enum. 선언 순서는 Figma 칩 그리드 순서를 따른다.

```
enum class MinoRoomColor { Red, RedOrange, Orange, Lime, Green, Cyan, Violet, Pink, Blue, Brown, LightBlue, Purple }
```

ADR이 명시적으로 **넣지 말라고 한 것** — 회색 기본값 · 표시 이름 · 서버 식별자 · 그리드 배치. 미선택은 소비처가 `MinoRoomColor?`의 `null`로 표현한다.

**`MinoRoomColorChip`**

```
@Composable
fun MinoRoomColorChip(
    color: MinoRoomColor,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    colors: MinoRoomColorChipColors = MinoRoomColorChipDefaults.colors(color),
)
```

- 칩은 **자기 한 칸만 안다.** 그리드 배치와 단일 선택 규칙은 호출부(`RoomColorPalette`)가 갖는다.
- 선택 상태는 `Modifier.rippleSingleSelectable`로 접근성 시맨틱에 노출한다(ADR §결과 · [README §6.3](../../../../core/design-system/README.md#63-클릭선택-modifier-유틸)).
- 채움·테두리는 `AtomicColorToken`의 `<색>60`·`<색>40` 슬롯을 참조한다. **brown 2색만 변수가 없어 실측 raw를 쓰고 주석을 남기지 않는다**(ADR §결정 · figma-design-fidelity §2 판정 4번).

**Figma 노드** (명세 보드 `001-1-1`): 칩 12개는 `2314-95320`~`2314-95323` · `2314-95325`~`2314-95328` · `2314-95330`~`2314-95333`이고, 그 사이의 `2314-95319`·`2314-95324`·`2314-95329`는 칩이 아니라 **행 프레임**이다. 칩은 각 70×70, 그리드는 4열×3행이다.

---

## 3. `MinoTextField` — 확장하지 않는다 (확정)

`Textinput/Textfield` 인스턴스(`2314-95310`)의 구조는 세 단이다.

| 단 | 내용 | 대응 파라미터 |
|---|---|---|
| Heading | `방 이름` + 필수 표기 `*` | `label = "방 이름"` · `required = true` |
| Input | placeholder `방 이름을 입력해 주세요.` | `placeholder` |
| Helper | `한글·영문·숫자만 입력 가능해요. (공백 포함 15자 이내)` — 평상시 `label/alternative`, 오류 시 오류 색 | `helperText` · `status` |

`showClearButton`은 **`false`로 넘긴다.** 기본값이 `true`라 그냥 두면 디자인에 없는 X 버튼이 그려진다 — 노드의 Content에는 placeholder 텍스트뿐이다.

**카운터 노드가 없다.** 상한 안내는 카운터가 아니라 helper 문구 안에 문장으로 들어 있다. 현재 `MinoTextField` API로 그대로 그려지므로 디자인 시스템을 건드리지 않는다 — 판정 경위는 [research.md](../research.md) R-015.

**방 이름에 `n/15` 표시를 두지 않는다** — spec 3.0.0의 FR-003이 카운터를 명시적으로 배제하고, TS-045가 그 부재를 검증한다. **15자 상한 차단 자체는 그대로 지킨다** — 16번째 글자가 반영되지 않는 것은 카운터와 무관한 `NameChanged` 처리다(TS-003·EC-002).

`MinoTextArea`는 방 설명을 `maxLength = 30` · `showCounter = true`로 그대로 받는다. FR-005의 `n/30`은 디자인에도 있어 유효하다.

**이 컴포넌트는 `TextFieldState`를 받는다**(`MinoTextField`의 `value: String`과 다르다). 상한도 컴포넌트가 걸므로 호출부가 다시 자르지 않는다 — 그 API 계약은 `MinoTextArea`의 KDoc이 소유한다. state를 누가 드는지는 [contracts/room-form-ui.md](./room-form-ui.md) §4가 정한다. 세는 단위를 grapheme으로 바꾼 경위는 [research.md](../research.md) R-022.

> **편집 보드와의 불일치 — 해소**: 편집 명세 보드 `2542-125922`가 방 이름을 `Textinput/Textfield`(`4170-140432`)로 갈아 끼웠다. 위 표의 세 단과 문자열이 생성 보드 `2314-95310`과 같고 카운터도 없어, **두 보드가 같은 컴포넌트를 지목한다.** `MinoTextField`를 따르는 이 절의 결정이 이제 양쪽 보드의 지지를 받는다 — 대조 결과는 [research.md](../research.md) R-023.
>
> 남은 불일치는 User Flow 보드 `2792-151339` 하나이며, 이 보드는 편집 화면인데 타이틀이 `공동방 만들기`라 FR-025와도 어긋난다. 명세 보드와 User Flow 보드가 어긋나면 명세 보드를 따른다는 [spec.md](../spec.md) §4 가정에 따라, 이 절은 명세 보드를 근거로 삼는다.

---

## 4. 디자인 시스템에 **두지 않는** 것

| 자산 | 어디로 | 근거 |
|---|---|---|
| 확인 모달 3종 | `:feature:roomform/form/component/RoomFormConfirmDialog.kt` | Figma 컴포넌트셋이 아니다 — 로컬 프레임 ([R-006](../research.md)) |
| 미리보기 카드 | `:feature:roomform/form/component/RoomPreviewCard.kt` | 〃 ([R-008](../research.md)) |
| 방 썸네일 이미지 13종 | **`:core:common:ui/src/main/res/drawable-{mdpi,xhdpi,xxhdpi}/`** (WebP) | 실체가 이미지 에셋이고, 디자인 시스템은 이미지 에셋을 받지 않는다 ([R-008](../research.md) · [배치 규약 §1.1](../../../conventions/component-asset-placement.md#11-이미지-에셋)). **두 번째 사용처가 생겨 공용 모듈로 승격됐다** ([R-034](../research.md)) |
| 썸네일 폴백 컴포넌트 | **`:core:common:ui/component/RoomThumbnailFallback.kt`** (public) | 도메인 색상값과 래스터 에셋을 함께 요구해 디자인 시스템에 둘 수 없다 ([R-034](../research.md)) |
| 3×4 칩 그리드 배치 | `:feature:roomform/form/component/RoomColorPalette.kt` | 배치는 화면의 구성이다 (팔레트 ADR §결정) |

### 썸네일 에셋 export

**대상**: 디자인 시스템 라이브러리 파일 `hkSOCt4kOfyaVWdxybTicF`의 컴포넌트셋 `16765-22588`(`Room Thumbnail_Empty`). variant는 **13개**다.

| variant | 개수 | 도메인 값 |
|---|---|---|
| `red` · `red orange` · `orange` · `lime` · `green` · `cyan` · `violet` · `pink` · `blue` · `brown` · `light blue` · `purple` | 12 | 같은 이름의 `RoomColor` |
| `my room` | 1 | **`RoomColor.GRAY`** — 이름은 개인방 관점이지만 실체가 회색이다 ([research.md](../research.md) R-017) |

셰이프는 `radius` 14, 크기 80×80이다.

**절차**: export는 [figma-design-fidelity §1.3](../../../conventions/figma-design-fidelity.md#13-에셋-export--아이콘-svg이미지)이, 포맷·밀도 배치는 [component-asset-placement §1.1](../../../conventions/component-asset-placement.md#11-이미지-에셋)이 소유한다. 이 문서는 **대상 노드와 variant 목록**만 정한다.

**에셋과 폴백은 이미 `develop`에 있다** — 이 계획이 export하거나 배치할 것이 남아 있지 않다([R-034](../research.md)). 아래 절차는 그때 무엇을 근거로 뽑았는지의 기록이다.

`MinoRoomColor` → drawable 매핑은 **`:core:common:ui`의 `RoomThumbnailFallback`이 소유한다.**

**[방 색상 팔레트 ADR](../../../adr/2026-08-14-room-color-palette-in-design-system.md) §결과의 "매핑은 feature가 소유한다"와 어긋나지 않는다.** 그 문장이 가리키는 것은 **서버 색 식별자 ↔ `MinoRoomColor`** 변환이고, 그 매핑은 지금도 feature에 있다(`form/model/RoomColorUiModel.chip`). 여기서 말하는 것은 **`MinoRoomColor` → drawable**이라는 별개의 축으로, ADR이 다루지 않았고 [래스터 이미지 ADR](../../../adr/2026-08-19-raster-image-placement-and-format.md) §결정 1이 `:core:common:ui`로 정해 둔 것이다([R-034](../research.md)).
