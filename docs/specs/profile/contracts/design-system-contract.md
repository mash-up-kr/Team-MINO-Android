# 계약: 디자인 시스템 신설 표면 (`:core:design-system`)

이 기능이 `:core:design-system`에 새로 여는 공개 API. 컴포넌트 구조는 [M3 컴포넌트 패턴](../../../adr/2026-07-25-design-system-component-m3-pattern.md)(`Defaults`·`token/`)을 따르고, 값의 토큰·실측 판정은 [figma-design-fidelity.md](../../../conventions/figma-design-fidelity.md)의 절차로 구현 단계에서 한다.

## 1. 프로필 아바타 (`component/profileavatar/`)

```kotlin
enum class MinoProfileAvatar { /* 12항목 */ }

@Composable
fun MinoProfileAvatarImage(
    avatar: MinoProfileAvatar,
    modifier: Modifier = Modifier,
    size: Dp = MinoProfileAvatarDefaults.GridSize,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
)
```

| 항목 | 규칙 |
|---|---|
| 목록 | 앱이 번들한 고정 12종. 서버에서 내려받지 않는다(spec §4) |
| 크기 | Figma 기준 그리드 70dp · 상단 썸네일 120dp. 두 값을 `MinoProfileAvatarDefaults`에 둔다 |
| 선택 표시 | `selected`가 참일 때의 표시는 컴포넌트가 그린다. 화면이 테두리를 덧그리지 않는다 |
| 클릭 | `onClick`이 있으면 선택 시맨틱을 노출한다 — `Modifier.rippleSingleSelectable`([README §6.3](../../../../core/design-system/README.md)) |
| 에셋 | `ImageVector` 변환이 가능하면 벡터, 아니면 밀도별 WebP([ADR](../../../adr/2026-08-01-webp-for-raster-images.md)) |

- enum은 그림만 안다. 서버·저장 식별자, "미선택", 그리드 배치는 갖지 않는다([data-model.md §4](../data-model.md)).
- 기존 `MinoAvatar`·`MinoAvatarSize`는 수정하지 않는다. 두 컴포넌트가 나뉘어 있는 이유는 research.md D5에 있다.

## 2. 상단 내비게이션 (`component/topnavigation/`)

```kotlin
@Composable
fun MinoTopNavigation(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    backEnabled: Boolean = true,
)
```

| 항목 | 규칙 |
|---|---|
| 구성 | 좌측 뒤로가기 + 가운데 제목. Figma `Top Navigation/Top Navigation`의 이 구성만 만든다 |
| 뒤로가기 없음 | `onBackClick == null`이면 버튼 자리를 비운다 |
| 뒤로가기 비활성 | `backEnabled == false`면 버튼을 보이되 누를 수 없게 한다(FR-010 온보딩 진입) |
| 인셋 | 상태 표시줄 인셋은 셸(`MinoScaffold`)이 이미 처리한다. 컴포넌트가 다시 적용하지 않는다 |

- 나머지 variant(액션 아이콘·검색 등)는 이번에 만들지 않는다. 필요한 화면이 자기 작업에서 축을 넓힌다(research.md D6).

## 3. 소비 규칙

- `:feature:profile`은 위 두 표면과 기존 컴포넌트(`MinoTextField`·`MinoActionArea`)만 쓴다. 색·치수를 직접 상수로 들지 않는다(헌법 §기술 표준).
- 아바타 그리드의 **배치**(4열 × 3행, 간격, 제목 `프로필 이미지 선택`)는 화면이 소유한다. 컴포넌트는 자기 한 칸만 안다.
