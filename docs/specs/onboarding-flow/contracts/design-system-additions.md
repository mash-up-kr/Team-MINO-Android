# 계약: 디자인 시스템·공용 UI 변경

**대상 스펙 경로**: `docs/specs/onboarding-flow` · **부속 문서**: [plan.md](../plan.md)

이 feature가 `:core:design-system`·`:core:common:ui`에 요구하는 것. **소속 모듈 판정**은 [`component-asset-placement.md`](../../../conventions/component-asset-placement.md), **컴포넌트 구현 패턴**(Defaults·Colors·토큰)은 [`core:design-system` README §6.1](../../../../core/design-system/README.md), **값 대조 절차**는 [`figma-design-fidelity.md`](../../../conventions/figma-design-fidelity.md)가 소유한다. 여기서는 **무엇이 왜 필요한지와 그 소유 모듈**만 정한다.

---

## 1. 판정 결과 한눈에

| 자산 | Figma | 판정 | 이 계획이 하는 일 |
|---|---|---|---|
| `Pagination/Dots` | **MU_Wanted Design System (Community)** 라이브러리의 컴포넌트셋 | `:core:design-system` | **신설한다** (§2) |
| `Top Navigation/Top Navigation` | 같은 라이브러리의 컴포넌트셋 | `:core:design-system` | **축을 넓힌다** — 컴포넌트는 이미 있고 우측 아이콘 액션이 없다 (§4) |
| `Action Area/Action Area` | 같은 라이브러리 | `:core:design-system` | 소비만 한다 — `MinoActionArea`가 이미 있다 |
| `Snackbar/Snackbar` | 같은 라이브러리 | `:core:design-system` | 소비만 한다 — `MinoSnackbar`가 이미 있다. 다만 셸이 그것을 쓰지 않는다 (§3) |
| 튜토리얼 예시 이미지 5종 · 친구 초대 일러스트 | 디자인 시스템 컴포넌트가 아니다 | `:feature:onboarding` | feature가 갖는다 (§5) |

판정 근거는 [`component-asset-placement.md`](../../../conventions/component-asset-placement.md) §1.2의 한 문장이다 — **"디자인 시스템의 표면은 Figma가 정한다."** 사용처 개수는 판정에 들어가지 않는다.

---

## 2. 신설 — `MinoPaginationDots`

`:core:design-system/component/pagination/`

| 파일 | 역할 |
|---|---|
| `MinoPaginationDots.kt` | 컴포넌트 |
| `MinoPaginationDotsDefaults.kt` | 색·치수 기본값 |
| `PaginationDotsPreview.kt` | 프리뷰 |
| `token/PaginationDotsTokens.kt` | 컴포넌트 토큰 |

**필요한 표면**

| 무엇 | 왜 |
|---|---|
| 점 **개수**를 받는다 | 튜토리얼은 5개(FR-015)지만 컴포넌트는 개수를 도메인 규칙으로 갖지 않는다 |
| **선택된 인덱스**를 받는다 | 현재 스텝 하나만 선택 상태여야 한다(FR-015·TS-025) |
| 점 **탭 콜백**을 받는다 | dot으로 임의 스텝 이동(FR-016·TS-027·TS-028). 콜백이 `null`이면 표시 전용 |

- 컴포넌트는 **캐러셀을 모른다.** `PagerState`를 받지 않고 인덱스와 콜백만 받는다 — 디자인 시스템이 Compose Foundation의 특정 타입에 묶이지 않게 한다.
- variant·size 축이 이 컴포넌트셋에 있는지, 선택/비선택 색이 어느 토큰인지는 **구현 착수 시 노드 대조로 판정한다.** 이 계획은 컴포넌트의 존재·역할·표면까지만 정한다.
- 조회 정보: `componentKey` `3bbad7281bae579e77f3dcdc20019f8541f97957`, 라이브러리 **MU_Wanted Design System (Community)**. 컴포넌트셋 정의 노드는 클라우드 MCP로 직접 열 수 없으므로([figma-design-fidelity.md §1.1](../../../conventions/figma-design-fidelity.md)) 화면 인스턴스(`3798-167083`)로 값을 읽거나 사용자에게 컴포넌트셋 링크를 받는다.

---

## 3. 변경 — `MinoScaffold`의 스낵바 호스트

`:core:common:ui/scaffold/MinoScaffold.kt` **한 파일**의 변경이다.

| # | 지금 | 이 계획이 요구하는 것 | 근거 |
|---|---|---|---|
| 1 | `SnackbarHost(snackbarHostState)` — M3 기본 스낵바를 그린다 | 호스트가 `MinoSnackbar`를 그린다 | Figma `Snackbar/Snackbar`(`2370-112921`) |
| 2 | 오프셋 없음 — Scaffold 기본 위치 | 스크린 하단에서 40dp 띄운다 | UX-003 |

**왜 셸인가**: 근거의 소유자는 [토스트 소유자 ADR](../../../adr/2026-08-24-snackbar-host-owned-by-mino-scaffold.md)이다 — 스낵바 호스트를 소유한 곳이 `MinoScaffold` 하나뿐이고, 40dp는 이 화면만의 값이 아니라 앱 공통 토스트 표출 규칙이다. 이 계획이 그 결정에 이른 경위는 [research.md R-017](../research.md).

**이 변경이 닿는 다른 화면**: 미처리 예외 안내(`CollectUncaughtError`)와 도메인 에러 스낵바 전부. 표출 위치와 모양이 앱 전체에서 한 번에 바뀐다 — 그것이 의도다.

**현재 코드 상태(2026-08-29 확인)**: `MinoScaffold`가 여전히 `SnackbarHost(snackbarHostState)`를 그리고 오프셋도 없다. ADR이 요구하는 상태가 코드에 아직 없으며, **이 계획이 그 변경을 내는 첫 작업이다.**

**남은 후속**: 스플래시 스펙·계획이 같은 40dp를 아직 **Screen 컴포저블의 몫**으로 적고 있고 `SplashRoute`가 그렇게 구현되어 있다(자체 토스트 상태·표출 시간을 든다). 소유자 판정 자체는 닫혔고, 그 문서의 개정과 구현 정리만 남았다 — [열린 항목 E](../research.md#열린-항목).

---

## 4. 확장 — `MinoTopNavigation`의 우측 아이콘 액션

> **plan 2.0.0에서 판정이 바뀌었다.** 1.0.x는 "신설 소유자가 둘이라 소비만 한다"였는데, 컴포넌트가 머지되어 그 다툼은 끝났다. 대신 **필요한 축 하나가 없다는 사실**이 드러났다([research.md R-025](../research.md)).

두 화면의 상단 바가 모두 `Top Navigation/Top Navigation` 인스턴스다(`2314-95568` · `3798-167080`).

| 화면 | 필요한 구성 | 현재 API로 가능한가 | 근거 |
|---|---|---|---|
| 친구 초대 | 제목 없음 · 우측 **[X] 아이콘** | **아니오** — 우측이 `actionLabel: String?`만 받는다 | FR-013·TS-022 |
| 튜토리얼 스텝 1~4 | 제목 `튜토리얼` · 우측 텍스트 [건너뛰기] | 예 (`title` + `actionLabel`) | FR-017·TS-029 |
| 튜토리얼 스텝 5 | 제목 `튜토리얼` · 우측 액션 없음 | 예 (`actionLabel = null`) | FR-018·TS-030 |

### 4.1 지금 API

`:core:design-system/component/topnavigation/MinoTopNavigation.kt`

```
fun MinoTopNavigation(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
)
```

그 파일의 KDoc이 **"액션 아이콘·검색 등 나머지 구성은 필요한 화면이 나올 때 축을 넓힌다"** 고 적어 두었다. 친구 초대가 그 화면이다.

### 4.2 이 계획이 요구하는 것

우측 슬롯이 **텍스트 액션과 아이콘 액션 중 하나**를 그릴 수 있어야 한다.

| 요구 | 이유 |
|---|---|
| 아이콘 액션을 지정하는 축이 생긴다 | 친구 초대의 [X](FR-013) |
| 기존 파라미터의 의미와 기본값이 그대로다 | 프로필·공동방 폼이 이미 호출 중이라 깨지면 안 된다 |
| 텍스트와 아이콘이 동시에 그려지지 않는다 | Figma 컴포넌트셋에 그런 구성이 없다 |
| 제목 없는 구성이 가능하다 | 친구 초대는 제목이 없다 — 빈 문자열로 되는지 노드 대조로 확인한다 |

**표면의 구체적 형태(파라미터 이름·타입·둘 중 하나를 강제하는 방식)는 이 계획이 정하지 않는다.** [`design-system README`](../../../../core/design-system/README.md) §6.1의 M3 패턴(Defaults·컴포넌트 토큰)을 따르되, 어느 축으로 표현할지는 **구현 착수 시 Figma 컴포넌트셋의 속성 축을 보고 정한다**([figma-design-fidelity.md](../../../conventions/figma-design-fidelity.md) §2). 슬롯을 `@Composable () -> Unit`으로 열어 아무것이나 넣게 하는 형태는 기각했다 — 디자인 시스템이 정한 구성 축이 무너진다([research.md R-025](../research.md)).

### 4.3 회귀 범위

이 컴포넌트를 쓰는 화면 전부가 확인 대상이다 — 최소한 `:feature:profile`·`:feature:roomform`. 기존 호출부가 컴파일되고 화면이 그대로 보이는 것으로 확인한다.

**아이콘 자산**: [X]가 `MinoIcons`에 이미 있는지 구현 착수 시 확인한다. 없으면 아이콘은 `:core:design-system`이 소유하므로([`design-system README`](../../../../core/design-system/README.md) §5) 그 모듈에 더한다 — feature에 두지 않는다.

---

## 5. 에셋 — `:feature:onboarding`이 갖는다

| 에셋 | 노드 | 비고 |
|---|---|---|
| 튜토리얼 예시 이미지 스텝 1~4 | `3798-167084` 및 각 스텝의 대응 프레임 | — |
| 튜토리얼 예시 이미지 스텝 5 | `4396-184972`의 `MAP 4` | 스텝 5 프레임이 다시 그려지며 자리표시자가 채워졌다 |
| 친구 초대 일러스트 | `5073-101129` | 한 장이다. **구름 배경은 없다** — 확정 브랜딩에서 노드가 사라졌다 |

- 위치는 `feature/onboarding/src/main/res/drawable-{mdpi,xhdpi,xxhdpi}/`, 포맷은 **WebP**다 — [래스터 이미지 배치·포맷 ADR](../../../adr/2026-08-19-raster-image-placement-and-format.md).
- **`:core:design-system`에 두지 않는다.** 그 모듈은 이미지 에셋을 받지 않는다([`component-asset-placement.md`](../../../conventions/component-asset-placement.md) §1.1).
- **`:core:common:ui`에도 두지 않는다.** 두 번째 사용처가 없다. 선제 승격은 같은 문서가 금지한다.
- export 절차는 [`figma-design-fidelity.md`](../../../conventions/figma-design-fidelity.md) §1.3(클라우드 `download_assets` → 밀도별 1·2·3배).

---

## 6. 이 계약이 지켜지는지 보는 법

| 확인 | 방법 |
|---|---|
| feature가 디자인 시스템 컴포넌트를 다시 그리지 않았다 | `:feature:onboarding`에 점·상단 바·액션 영역을 직접 그리는 컴포저블이 없다 |
| 이미지가 디자인 시스템에 들어가지 않았다 | `core/design-system/src/main/res/drawable-*`에 온보딩 에셋이 없다 |
| 밀도·포맷 규칙을 지켰다 | feature의 `drawable/`(밀도 없는 디렉터리)에 래스터가 없고, 확장자가 전부 `.webp`다 |
| 토큰·실측 판정을 했다 | 구현 착수 시 노드 대조 기록이 있다([figma-design-fidelity.md](../../../conventions/figma-design-fidelity.md) §2·§6) |
| 상단 바를 확장했지 복제하지 않았다 | `:feature:onboarding`에 자체 상단 바 컴포저블이 없고, `MinoTopNavigation` 호출만 있다 |
| 기존 호출부가 깨지지 않았다 | `:feature:profile`·`:feature:roomform`의 `MinoTopNavigation` 호출이 수정 없이 컴파일된다 |
