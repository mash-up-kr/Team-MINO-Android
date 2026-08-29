# core:common:ui

MinoAndroid의 **feature 간 재사용 공통 UI** 모듈. 공용 Composable 컴포넌트와 Compose 유틸리티(Modifier 확장, Effect 헬퍼 등), 여러 feature가 공유하는 이미지 에셋을 제공한다.

> 모듈 책임·경계·의존 방향(레이어 그래프)은 [`docs/architecture/modularization.md`](../../../docs/architecture/modularization.md)를 단일 출처로 한다. 이 문서는 이 모듈의 **API·사용법·확장 규칙**만 다룬다.

---

## 1. 개요

| | |
|---|---|
| **무엇을** | 여러 feature가 공유하는 Composable과 Compose 헬퍼를 제공한다. MVI `SideEffect` 수집(`CollectSideEffect`), 에러 소비(`CollectDomainError`·`CollectUncaughtError`), 네비게이션 셸(`MinoScaffold`). 둘 이상의 feature가 쓰는 이미지 에셋도 이 모듈이 갖는다. |
| **빌드 타입** | Android Library + Compose (`mino.android.library` + `mino.android.compose`) |

> [!IMPORTANT]
> **디자인 토큰/테마는 이 모듈이 아니라 [`core:design-system`](../../design-system/README.md)에 둔다.** 이 모듈은 "어떻게 보이는가"의 **기준**(토큰·테마)을 갖지 않고, 여러 feature가 공유하는 동작·구조와 이미지 에셋을 담는다.

---

## 2. 핵심 API

### `CollectSideEffect`

MVI [`SideEffect`](../android/README.md)를 **화면 lifecycle에 맞춰** 수집하는 Composable. `minActiveState` 이상에서만 수집하므로, 화면이 백그라운드일 때 Toast·네비게이션 같은 일회성 이벤트가 잘못 처리되지 않는다.

```kotlin
@Composable
fun <T : SideEffect> CollectSideEffect(
    sideEffect: Flow<T>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEvent: (T) -> Unit,
)
```

#### 사용 예시

```kotlin
@Composable
fun CounterRoute(viewModel: CounterViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CollectSideEffect(sideEffect = viewModel.sideEffect) { effect ->
        when (effect) {
            CounterSideEffect.ShowMaxReached ->
                Toast.makeText(context, "최대값 도달", Toast.LENGTH_SHORT).show()
        }
    }

    CounterScreen(state = state, onIncrease = viewModel::onIncrease)
}
```

동작 특성:

- **defer**: 수집이 멈춘 동안(백그라운드)의 이벤트는 `SideEffect` 채널 버퍼에 쌓였다가 **화면 복귀 시 이어서** 처리된다. 유실이 필요하면 채널이 아닌 컨테이너(생산자) 쪽에서 정한다.
- **최신 람다 참조**: recomposition으로 `onEvent`가 바뀌어도 collect를 재시작하지 않고 `rememberUpdatedState`로 최신 람다를 참조한다.
- **즉시 디스패치**: 수집 재개 직후 방출된 이벤트도 놓치지 않도록 `Dispatchers.Main.immediate`로 수집한다.

> [!NOTE]
> 여기서 다루는 건 **수집(소비) 동작**이다. `SideEffect`를 무엇에 쓰고 무엇에 쓰지 말지(포그라운드 전용·영속 신호는 상태로 모델링)는 생산자 쪽 규칙이라 [`core:common:android`](../android/README.md) §2가 단일 출처다.

### 에러 소비 — `CollectDomainError` · `CollectUncaughtError`

에러 처리 규약의 UI 소비 담당이다. State/이벤트 분류 기준·수집 위치 정책은 [`docs/conventions/error_handling.md`](../../../docs/conventions/error_handling.md) §5·§6이 단일 출처다.

```kotlin
@Composable
fun CollectDomainError(emitter: DomainErrorEmitter, onError: (MinoDomainException) -> Unit)

@Composable
fun CollectUncaughtError(onError: (Throwable) -> Unit)
```

- `CollectDomainError` — `DomainErrorEmitter`를 위임한 ViewModel의 **Route**가 선언한다. 리프 → 사용자 문구 매핑은 Route가 수행한다 (문구 정책이 미정이라 공통 매퍼는 두지 않는다 — 규약 §8).
- `CollectUncaughtError` — 직접 선언할 일은 없다. **`MinoScaffold`가 이미 호출**하므로 셸을 쓰면 규약이 만족된다. 버그 안내 문구는 `R.string.error_unknown`.
- 두 컴포저블 모두 `RESUMED`에서만 수집하고, 수집 공백 중 이벤트는 채널 버퍼가 보존한다.

### 네비게이션 셸 — `MinoScaffold`

feature의 셸(`XShell`)이 여는 프로젝트 표준 `Scaffold`. 셸이 무엇을 소유하는지(그래프당 하나·화면은 `Scaffold`를 열지 않음·셸과 그래프 분리)는 [`feature-module.md`](../../../docs/architecture/feature-module.md) 4장이 단일 출처다.

```kotlin
@Composable
fun MinoScaffold(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    containerColor: Color = MinoScaffoldDefaults.containerColor,
    content: @Composable (PaddingValues) -> Unit,
)
```

M3 `Scaffold`를 그대로 노출하는 대신 **프로젝트 표준을 안에 넣은** 래퍼다:

- **미처리 예외 안내** — `CollectUncaughtError` + `SnackbarHost`를 셸이 소유한다. `snackbarHost`를 파라미터로 뚫지 않는 이유이며, 이 때문에 **Activity당 하나만** 열어야 한다(둘이면 같은 예외로 스낵바가 두 번 뜬다).
- **스낵바 호스트 제공** — `LocalSnackbarHostState`로 하위에 내려준다. Route가 도메인 에러를 표시할 때 쓰고, stateless한 `XScreen`에서는 읽지 않는다. 셸 밖에서 읽으면 즉시 `error`로 실패한다.
- **토스트 표출 규칙** — 호스트가 M3 기본 스낵바 대신 `MinoSnackbar`를 그리고, 하단 바 유무와 무관하게 **스크린 하단**을 기준으로 띄운다. 그래서 M3 `Scaffold`의 `snackbarHost` 슬롯은 비워 두고 셸이 직접 얹는다 — 그 슬롯은 호스트를 `bottomBar` 위에 놓아 화면마다 기준선이 갈린다. 화면은 오프셋도 스낵바 컴포저블도 다루지 않는다([ADR](../../../docs/adr/2026-08-24-snackbar-host-owned-by-mino-scaffold.md)).
- **배경 표준** — 기본값은 `MinoScaffoldDefaults`가 공급한다(design-system 토큰). 화면은 배경을 다시 칠하지 않는다.

파라미터는 실제 호출부가 생길 때 디폴트 인자로 늘린다([배치 규약 §3](../../../docs/conventions/component-asset-placement.md#3-컴포넌트-신설-vs-기존-확장)). 지금 `topBar`·`contentWindowInsets`가 없는 이유다 — 화면 고유 topBar는 화면이 직접 배치하고, 인셋을 무시해야 하는 화면은 M3 `Scaffold`를 직접 연다.

```kotlin
// 화면 전환이 있는 feature — content 안에서 그래프(XNavHost)를 연다
MinoScaffold(modifier = modifier, bottomBar = { XBottomBar(...) }) { innerPadding ->
    XNavHost(navController, startDestination, Modifier.padding(innerPadding))
}

// 단일 화면 — 화면 컴포저블을 직접 그린다
MinoScaffold(modifier = modifier) { innerPadding -> XScreen(Modifier.padding(innerPadding)) }
```

> [!NOTE]
> 슬롯은 `content` 하나다. NavHost용 슬롯을 따로 두지 않는 이유(배타성을 타입으로 강제할 수 없고 `navController` 소유가 흐려진다)는 [ADR](../../../docs/adr/2026-07-31-common-shell-mino-scaffold.md) 참조. 인셋 패딩은 셸이 적용하지 않고 `PaddingValues`로 넘긴다 — 리스트가 하단 바 뒤로 스크롤되는 화면이 `contentPadding`으로 받아야 하기 때문이다.

---

## 3. 디렉토리 구조

```
core/common/ui/src/main/java/team/mino/core/common/ui/
├── architecture/
│   ├── CollectFlowWithLifecycle.kt     # 수집 컴포저블 3종의 공통 골격 (internal)
│   └── CollectSideEffect.kt            # SideEffect를 lifecycle 기준으로 수집하는 Composable
├── component/                          # 여러 feature가 공유하는 Composable 컴포넌트
├── error/
│   ├── CollectDomainError.kt           # DomainErrorEmitter 수집 (Route 선언)
│   └── CollectUncaughtError.kt         # UncaughtErrorHandler 수집 (셸이 호출)
└── scaffold/
    ├── MinoScaffold.kt                 # 네비게이션 셸이 여는 표준 Scaffold
    ├── MinoScaffoldDefaults.kt         # 배경·인셋 기본값
    └── LocalSnackbarHostState.kt       # 셸이 소유한 스낵바 호스트 제공
```

공용 Composable 컴포넌트나 Modifier 확장이 늘어나면 성격별 패키지(`component`, `modifier` 등)를 추가한다. 여러 feature가 공유하는 이미지 에셋은 `src/main/res/drawable-*`에 둔다(포맷·밀도 규칙은 [배치 규약 §1.1](../../../docs/conventions/component-asset-placement.md#11-이미지-에셋)).

---

## 4. 확장 규칙 — 어디에 둘지 결정

이 모듈은 **동작/구조**(공통 Composable·Effect 헬퍼·Modifier 확장)를 담는다. 인접 모듈과 헷갈리기 쉬운 경계만 짚는다.

| 헷갈리는 대상 | 그건 여기가 아니라 |
|---|---|
| 색·타이포·그림자 토큰, 테마 | [`core:design-system`](../../design-system/README.md) |

컴포넌트·이미지 에셋을 이 모듈에 둘지 feature나 `:core:design-system`에 둘지, 이미 만든 것을 언제 여기로 올릴지는 [`docs/conventions/component-asset-placement.md`](../../../docs/conventions/component-asset-placement.md)가 단일 출처다.

그 외 모듈 경계 판단은 [`modularization.md`](../../../docs/architecture/modularization.md)를 따른다.

---

## 5. 의존성 추가 가이드

`build.gradle.kts`에 추가:

```kotlin
dependencies {
    implementation(project(":core:common:ui"))
}
```

주로 feature 모듈(화면을 그리는 쪽)이 의존한다.

---

## 6. 컨벤션

| 항목 | 규칙 |
|---|---|
| **lifecycle 인지** | UI 이벤트 수집은 `CollectSideEffect`처럼 lifecycle을 존중해 백그라운드 처리·유실을 통제한다. |
| **공용성** | 배치·승격 판정은 [`component-asset-placement.md`](../../../docs/conventions/component-asset-placement.md)를 단일 출처로 한다. |
| **stateless** | 공용 컴포넌트는 feature 의존 없이 상태를 인자로 받고 콜백을 올리는 형태로 둔다. |
| **프리뷰** | 공용 컴포넌트는 `@UiModePreviews`로 라이트/다크 프리뷰를 함께 둔다. |
