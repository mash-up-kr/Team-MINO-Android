# 계약: 디자인 시스템 신설 표면 (`:core:design-system`)

이 기능이 `:core:design-system`에 새로 여는 공개 API. 컴포넌트 구조는 [M3 컴포넌트 패턴](../../../adr/2026-07-25-design-system-component-m3-pattern.md)(`Defaults`·`token/`)을 따르고, 값의 토큰·실측 판정은 [figma-design-fidelity.md](../../../conventions/figma-design-fidelity.md)의 절차로 구현 단계에서 한다.

## 1. 프로필 아바타 (`component/profileavatar/`)

```kotlin
enum class MinoProfileAvatar { /* 12항목 */ }

// 지름과 테두리 두께는 자리마다 한 쌍으로 정해지고 서로 비례하지 않는다. 자리를 열거해 두 값을 함께 든다.
enum class MinoProfileAvatarSize(val diameter: Dp) { Grid, Thumbnail }

@Composable
fun MinoProfileAvatarImage(
    avatar: MinoProfileAvatar,
    modifier: Modifier = Modifier,
    size: MinoProfileAvatarSize = MinoProfileAvatarSize.Grid,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
)
```

| 항목 | 규칙 |
|---|---|
| 목록 | 앱이 번들한 고정 12종. 서버에서 내려받지 않는다(spec §4) |
| 크기 | 그리드 70dp · 상단 썸네일 120dp(원본 대조 확인). 테두리는 각각 1.25dp·5dp이며 **비례하지 않아** 자리마다 값을 따로 든다. 네 값을 `token/ProfileAvatarTokens`에 두고 `MinoProfileAvatarSize`가 자리별로 묶는다 — **지름을 자유롭게 받지 않는다.** 디자인에 없는 크기를 열면 그 자리의 테두리 두께를 지어내야 한다 |
| 선택 표시 | **시각 표시를 그리지 않는다.** 원본에 선택된 칸을 구별하는 표현이 없다([research.md D28](../research.md#d28-아바타-선택-상태의-시각-표시를-만들지-않는다)). `selected`는 `Modifier.rippleSingleSelectable`의 접근성 시맨틱으로만 나가고, 화면도 테두리를 덧그리지 않는다 |
| 클릭 | `onClick`이 있으면 선택 시맨틱을 노출한다 — `Modifier.rippleSingleSelectable`([README §6.3](../../../../core/design-system/README.md)) |
| 에셋 | 배경 원과 캐릭터가 래스터로 합성돼 있어 밀도별 WebP다([ADR](../../../adr/2026-08-01-webp-for-raster-images.md)). **테두리는 에셋에 없으므로 컴포넌트가 그린다** |
| 소유 근거 | 에셋을 이 모듈에 두는 배경은 [ADR — 프로필 아바타 12종의 에셋과 컴포넌트는 `:core:design-system`이 소유한다](../../../adr/2026-08-25-profile-avatar-assets-in-design-system.md) |

- enum은 그림만 안다. 저장 식별자, "미선택", 그리드 배치는 갖지 않는다([data-model.md §4](../data-model.md)). 이 모듈은 도메인·데이터 레이어를 모르므로 원격 연동이 붙어도 이 표면은 그대로다([research.md D24](../research.md#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)).
- 기존 `MinoAvatar`·`MinoAvatarSize`는 수정하지 않는다. 두 컴포넌트가 나뉘어 있는 이유는 research.md D5에 있다.

## 2. 상단 내비게이션 (`component/topnavigation/`)

```kotlin
@Composable
fun MinoTopNavigation(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
)
```

**구현 대상은 Figma 컴포넌트셋 `Top Navigation/Top Navigation`(`16215-20432`)의 `Platform=iOS` variant**(`16215-20433`)다. 화면 목업 010-1·010-2·010-3이 붙인 인스턴스(`2314-95704`)가 그것이며, 근거는 [research.md D27](../research.md#d27-상단-바는-화면-목업이-쓰는-ios-variant를-따른다)에 있다.

| 항목 | 규칙 |
|---|---|
| 구성 | 좌측 뒤로가기(셰브런) + **가운데 제목**. 제목은 바 전폭 기준 중앙이고 뒤로가기가 그 위에 겹친다 |
| 높이 | 44dp. **상태 표시줄 인셋을 갖지 않는다** — 인셋은 셸(`MinoScaffold`)이 처리하고, 원본의 `Spacing/Status`(54)는 `Bar`의 형제 노드라 여기 포함되지 않는다 |
| 아이콘 | `MinoIcons.ChevronLeft`(24dp). 화살표(`ArrowLeft`)가 아니다 |
| 뒤로가기 없음 | `onBackClick == null`이면 **버튼을 그리지 않되 leading 슬롯 24dp 자리는 남긴다.** 온보딩 진입이 이 경로다([research.md D29](../research.md#d29-온보딩-진입에서-뒤로가기를-노출하지-않는다)) |
| 비활성 상태 | **파라미터를 두지 않는다.** 호출부가 없고 비활성 시각이 원본 대조된 적이 없다([research.md D34](../research.md#d34-minotopnavigation에-backenabled-파라미터를-두지-않는다)). 필요해지면 디폴트 인자로 소스 호환 추가한다 |
| 긴 제목 | 1줄 말줄임. 전폭 중앙 정렬이라 **긴 제목은 뒤로가기 아래로 흘러 들어간 뒤 잘린다** — 원본 구조 그대로이며 임의로 여백을 넣지 않는다 |

- 이 컴포넌트셋의 축은 `Platform`(iOS·Android·Web) **하나뿐**이다. 액션 아이콘·검색 같은 축은 존재하지 않는다 — plan 2.0.0까지의 [D6](../research.md) 서술이 사실과 달랐다.
- 다른 플랫폼 variant는 만들지 않는다.

## 3. 소비 규칙

- `:feature:profile`은 위 두 표면과 기존 컴포넌트(`MinoTextField`·`MinoActionArea`)만 쓴다. **색은 토큰으로만 접근한다.** 치수는 다르다 — 이 저장소에는 간격·치수 토큰 foundation이 없고 [헌법](../../../constitution.md) §기술 표준이 "값이 일치하는 토큰이 있으면 토큰으로, **없으면 디자인 실측값을 직접 쓴다**"로 정하므로, 화면의 실측 dp는 위반이 아니다.
- 아바타 그리드의 **배치**(4열 × 3행, 간격, 제목 `프로필 이미지 선택`)는 화면이 소유한다. 컴포넌트는 자기 한 칸만 안다([research.md D26](../research.md#d26-아바타-그리드의-배치--화면이-소유하고-lazyverticalgrid를-쓰지-않는다)).
