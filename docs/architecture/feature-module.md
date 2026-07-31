# Feature 모듈 컨벤션

새 feature 모듈을 추가하거나 화면을 작성할 때의 **구조·역할** 규약. **이 문서가 단일 출처(SSOT)** 이며, 아래 인라인 스켈레톤을 그대로 본떠 작성한다.

> **화면 전환(feature 간 Activity / feature 내부 Route)** 규약은 → `docs/architecture/feature-navigation.md`.
>
> placeholder는 `X`(feature 이름), `XMain`/`XDetail`(화면)로 표기한다. 새 feature가 `profile`이면 `ProfileActivity`, `ProfileMain` 식으로 치환한다. MVI 기반 타입(`UiState`/`Intent`/`SideEffect`/`MviContainer`)은 `:core:common:android`의 `architecture` 패키지를 단일 출처로 한다.

---

## 1. 모듈 분리: `api` / `impl`

각 feature는 **두 모듈**로 나뉜다.

| 모듈 | 노출하는 것 | 의존 규칙 |
|---|---|---|
| `:feature:x:api` | 전환 계약만 — `interface XLauncher : ActivityLauncher` + `EXTRA_*` 상수 | 다른 feature는 **이 `api`에만** 의존한다 |
| `:feature:x:impl` | Activity·화면·ViewModel·Launcher 구현 | 자신의 `api`에 의존. **다른 feature의 `impl`에는 의존 금지** |

```mermaid
flowchart LR
    subgraph x[":feature:x"]
        xApi[":x:api<br/>XLauncher · EXTRA_*"]
        xImpl[":x:impl<br/>Activity · Screen · ViewModel"]
    end
    subgraph y[":feature:y"]
        yApi[":y:api<br/>YLauncher · EXTRA_*"]
        yImpl[":y:impl"]
    end
    xImpl -- implementation --> xApi
    yImpl -- implementation --> yApi
    xImpl -- "전환은 상대 api에만" --> yApi
    yImpl -- "전환은 상대 api에만" --> xApi
    xApi --> nav[":core:navigation"]
    yApi --> nav
    xImpl -. "의존 금지" .-> yImpl
```

**핵심**: feature 간 결합은 `impl`이 상대 `api`(Launcher 인터페이스 + 키 상수)에만 의존한다. Hilt가 `impl`의 `XLauncherImpl`을 상대 `api`의 `XLauncher`로 주입해주므로 `impl`끼리 직접 알 필요가 없다. (전환 메커니즘 → `feature-navigation.md`)

---

## 2. 패키지 구조

**화면 단위 우선** 배치다. 그래프 레벨 파일은 모듈 루트, DI는 `di/`, 각 화면은 자기 이름의 디렉터리(`main`, `detail`, …) 아래 `screen·vm·model·component`를 갖는다.

```
:feature:x:api  —  team/mino/feature/x/api/
├── XLauncher.kt         # interface XLauncher : ActivityLauncher
└── XExtras.kt           # const val EXTRA_* = "..."

:feature:x:impl —  team/mino/feature/x/
├── XActivity.kt         # @AndroidEntryPoint, 셸 호스팅 + feature 간 전환
├── XDestinations.kt     # @Serializable Route 정의(XMain, XDetail) + typeMap
├── XShell.kt            # MinoScaffold + chrome, navController 보유·화면 로깅
├── XNavHost.kt          # MinoNavHost + screen<T> 등록 (그래프만)
├── di/
│   ├── XLauncherImpl.kt        # BaseActivityLauncher 구현
│   └── XNavigationModule.kt    # @Binds XLauncherImpl → XLauncher
├── main/                # 첫 화면(screen-feature)
│   ├── screen/  XRoute.kt(stateful) · XScreen.kt(stateless)
│   ├── vm/      XViewModel · XUiState · XSideEffect · (XIntent: 액션 있을 때만)
│   ├── args/    화면 진입 인자(Route 프로퍼티) 타입 (없으면 생략)
│   ├── model/   UiState를 구성하는 UiModel (없으면 생략)
│   └── component/ Screen 구성용 컴포저블 단위 (없으면 생략)
└── detail/              # 두 번째 화면 — main과 동일 구조
    ├── model/XQuery.kt        # 인자이자 UiState 구성요소 → UiModel로 model에 (아래 규칙)
    ├── screen/  XDetailRoute · XDetailScreen
    └── vm/      XDetailViewModel · XDetailUiState · XDetailSideEffect
```

`api` 모듈은 가볍다 — Launcher 인터페이스와 `EXTRA_*` 상수만 둔다(compose·hilt 미적용).

### 디렉터리 역할 (`<screen>/` 하위)

| 디렉터리 | 역할 |
|---|---|
| `screen/` | `XRoute`(stateful) + `XScreen`(stateless) 컴포저블 |
| `vm/` | `XViewModel` · `XUiState` · `XSideEffect` · (`XIntent`) |
| `args/` | **화면 진입 인자**(Route 프로퍼티)로 쓰는 타입의 **기본 위치** |
| `model/` | **UiState를 구성하는 UiModel** |
| `component/` | **Screen을 구성하는 컴포저블 단위**들의 모음 |

**인자 배치 규칙**: 진입 인자 타입은 기본적으로 `args/`에 둔다. **단 그 인자가 `UiState`에도 쓰이면**(= UiModel 겸용) `model/`에 두고 거기서 가져다 쓴다.
예) `XQuery`가 `XDetail(query)`의 인자이면서 `XDetailUiState.query`이기도 하면 → `model/XQuery.kt`.

### 객체별 역할

| 객체 | 역할 |
|---|---|
| `XActivity` | feature의 단일 진입 Activity. `setContent`로 `XShell` 호스팅. feature 간 전환·Intent 처리(→ `feature-navigation.md`) |
| `XDestinations`(`XMain`/`XDetail`) | feature 내부 화면의 type-safe `@Serializable` Route (→ `feature-navigation.md`) |
| `XShell` | feature의 **셸**. `MinoScaffold`로 chrome·insets를 열고, `navController`를 만들어 화면 조회 로깅(`TrackScreenViews`)까지 담당한다(4장) |
| `XNavHost` | `MinoNavHost` + `screen<T>`로 **화면 그래프만** 구성. `navController`는 셸에서 받는다 (→ `feature-navigation.md`) |
| `XLauncher`(api) / `XLauncherImpl`(impl) | 다른 feature가 이 feature로 전환하는 계약/구현 (→ `feature-navigation.md`) |
| `XRoute` | **stateful** 컴포저블 — VM·state·sideEffect를 Screen에 연결 (4장) |
| `XScreen` | **stateless** 컴포저블 — state·콜백만으로 그리는 순수 UI (4장) |
| `XViewModel` | `MviContainer<XUiState, XSideEffect>` 위임. 라우트 인자는 `savedStateHandle.toRoute<T>()`로 복원 |

---

## 3. 진입점·MVI 스켈레톤

> 전환 관련 스켈레톤(`XDestinations`·`XShell`·`XNavHost`·`XLauncherImpl`·DI)은 → `feature-navigation.md`.

**XActivity — 진입점 (셸 호스팅)**
```kotlin
@AndroidEntryPoint
class XActivity : ComponentActivity() {
    @Inject lateinit var yLauncher: YLauncher   // 다른 feature 전환용 (선택)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val arg = intent.getStringExtra(EXTRA_SOMETHING)
        setContent {
            MinoAndroidAppTheme {
                XShell(
                    startDestination = XMain(arg),                 // 진입 인자는 시작 라우트로 (전환 문서 2장)
                    onNavigateToY = { yLauncher.launch(this) { putExtra(EXTRA_Y, …) } },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
```

**XViewModel — MVI 컨테이너**
```kotlin
@HiltViewModel
class XViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel(), MviContainer<XUiState, XSideEffect> by mviContainer(XUiState()) {
    init {
        val route = savedStateHandle.toRoute<XMain>()             // 라우트 인자 복원 (전환 문서 2장)
        updateState { copy(arg = route.arg.orEmpty()) }
    }

    fun processIntent(intent: XIntent) { /* when (intent) { … } */ }
}
```
- `XUiState : UiState`, `XSideEffect : SideEffect`, (`XIntent : Intent` — 사용자 액션이 있을 때만).
- 인자가 없으면 `SavedStateHandle`·`init`을 두지 않는다(→ `feature-navigation.md`의 "인자가 없는 화면").

---

## 4. Composable 연결: Route ↔ Screen

화면 한 장은 **Route(연결부) + Screen(UI)** 두 컴포저블로 나눈다.

```mermaid
sequenceDiagram
    participant Nav as XNavHost
    participant Rt as XRoute (stateful)
    participant VM as XViewModel
    participant Scr as XScreen (stateless)
    Nav->>Rt: screen<XMain> { XRoute(콜백) }
    Rt->>VM: hiltViewModel()
    VM-->>Rt: state / sideEffect
    Rt->>Scr: XScreen(state, onIntent, 콜백, modifier)
    Scr->>Rt: onIntent(XIntent)
    Rt->>VM: processIntent(intent)
    VM-->>Rt: 새 state 방출 → 재구성
```

**`XRoute` — stateful (연결 담당)**
```kotlin
@Composable
internal fun XRoute(
    onNavigateToY: () -> Unit,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: XViewModel = hiltViewModel(),     // VM 주입
) {
    val state by viewModel.state.collectAsStateWithLifecycle()   // state 구독
    // 1회성 효과가 있으면 viewModel.sideEffect 를 수집해 Toast·이벤트 처리

    // 액션 실패(도메인 에러)를 스낵바로 알릴 때. 호스트는 셸이 소유한다 → error_handling.md §5·§6
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    CollectDomainError(viewModel) { error ->
        // 리프 → 문구 매핑은 이 화면이 한다. 공통 매퍼는 두지 않는다(error_handling.md §8)
        scope.launch { snackbarHostState.showSnackbar(context.getString(messageResOf(error))) }
    }

    XScreen(
        state = state,
        onIntent = viewModel::processIntent,
        onNavigateToY = onNavigateToY,
        onNavigateToDetail = onNavigateToDetail,
        modifier = modifier,
    )
}
```
- VM 획득·state 구독·sideEffect 수집을 담당하고, NavHost가 넘긴 **네비게이션 콜백**을 Screen에 전달한다.
- `XNavHost`가 `screen<T> { XRoute(...) }`로 호출하는 유일한 진입점.
- `DomainErrorEmitter`는 ViewModel 인스턴스별 채널이라 셸이 대신 수집할 수 없다 — 수집은 Route가 하고 표시할 호스트만 셸에서 받는다. `LocalSnackbarHostState`는 Route에서만 읽고 `XScreen`으로 내려보내지 않는다.
- `messageResOf`는 그 화면이 자기 파일에 두는 `when(error)` 매핑이다. 리프별 문구 정책이 미정이라 **공통 매퍼를 만들지 않는다**(→ `error_handling.md` §8).

**`XScreen` — stateless (UI 담당)**
```kotlin
@Composable
fun XScreen(
    state: XUiState,
    onIntent: (XIntent) -> Unit,
    onNavigateToY: () -> Unit,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) { /* state로 UI 구성 */ }           // Scaffold를 열지 않는다
}
```
- `state`와 콜백**만** 받는다. ViewModel·navController를 모른다 → `@Preview` 단독 렌더 가능.
- `Scaffold`·insets는 화면이 아니라 **셸이 소유**한다(아래).

> **왜 나누나**: 네비게이션·VM 결합(테스트 어려움)을 Route에 격리하고, Screen은 입력→출력이 명확한 순수 함수로 유지해 프리뷰·재사용·테스트가 쉬워진다.

### 셸(`XShell`)과 그래프(`XNavHost`) 분리

**chrome을 여는 셸과 화면 그래프는 컴포저블을 나눈다.** 셸은 `Scaffold`·insets·`navController`·화면 로깅을, 그래프는 `screen<T>` 등록만 맡는다. `Scaffold`는 **그래프당 하나**이고 셸이 소유한다 — 셸은 M3 `Scaffold`를 직접 열지 않고 [`MinoScaffold`](../../core/common/ui/README.md)를 쓰며, 화면은 셸이 계산한 영역 안을 그린다.

```kotlin
// XShell — chrome·insets·navController·로깅
@Composable
internal fun XShell(startDestination: Route, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    TrackScreenViews(navController)

    MinoScaffold(modifier = modifier, bottomBar = { /* 그래프 전체에 걸리는 chrome */ }) { innerPadding ->
        XNavHost(navController, startDestination, Modifier.padding(innerPadding))
    }
}

// XNavHost — 그래프만
@Composable
internal fun XNavHost(navController: NavHostController, startDestination: Route, modifier: Modifier = Modifier) {
    MinoNavHost(navController, startDestination, modifier) { /* screen<T> 등록 */ }
}
```

- **`Activity`가 호출하는 것은 `XShell`이다.** `XNavHost`는 셸만 호출한다.
- `navController`는 **셸이 만들어** 그래프에 넘긴다. chrome(탭 바 등)이 현재 목적지를 읽어야 하고 화면 로깅도 `navController`에 붙으므로, 소유자를 셸로 못박아 두 관심사가 그래프 안에 섞이지 않게 한다.
- `MinoScaffold`의 슬롯은 `content` **하나**다. 화면 전환이 있으면 그 안에서 `XNavHost`를, 단일 화면이면 화면 컴포저블을 직접 그린다. 화면이 하나여도 인자 복원(`toRoute`)·화면 조회 로깅이 NavHost에 딸려 오므로 **NavHost 유지가 기본**이고, VM·인자·로깅이 모두 없는 정적 화면만 `XNavHost` 없이 셸이 화면을 직접 그린다.
- 여러 화면에 걸치는 chrome(bottomBar)은 **셸의 slot**에 둔다. 탭 전환 화면도 이 형태의 `XShell`일 뿐 별도 구조가 아니다.
- 스낵바 호스트와 미처리 예외 안내는 `MinoScaffold`가 이미 갖고 있다 — feature가 배선하지 않는다(→ `error_handling.md` §6). **Activity당 `MinoScaffold`는 하나**다.
- 화면 고유 chrome(topBar 등)은 그 화면이 자기 컨테이너 최상단에 **직접 배치**한다.
- **예외** — `TopAppBar` 스크롤 연동처럼 slot API가 꼭 필요하거나, 인셋을 무시하고 full-bleed로 그려야 하는 화면은 그 화면이 **M3 `Scaffold`를 직접** 열 수 있다(`MinoScaffold`가 아니다 — 미처리 예외 수집이 중복된다). 이때 인셋 이중 적용을 막기 위해 안쪽은 `contentWindowInsets = WindowInsets(0)`으로 둔다.

> 화면마다 `Scaffold`를 열면 인셋이 중복 적용되고, 여러 화면에 공통으로 걸리는 chrome을 둘 자리가 없다. 배경은 → [Scaffold·insets는 화면이 아니라 네비게이션 셸이 소유한다](../adr/2026-07-29-scaffold-ownership-navhost.md)(당시 셸 이름은 `XNavHost`였다), 셸을 공통 컴포저블로 승격하고 `XShell`/`XNavHost`로 나눈 배경은 → [공통 셸 `MinoScaffold`](../adr/2026-07-31-common-shell-mino-scaffold.md).

---

## 5. 새 feature 추가 체크리스트

1. `:feature:x:api`, `:feature:x:impl` 생성 후 `settings.gradle.kts` 등록. 컨벤션 플러그인 `mino.android.feature.api` / `mino.android.feature.impl` 적용.
2. `api`: `interface XLauncher : ActivityLauncher` + 필요한 `EXTRA_*` 상수.
3. `impl` 루트: `XActivity` · `XDestinations` · `XShell` · `XNavHost`.
4. `impl/di`: `XLauncherImpl`(`BaseActivityLauncher`) + `@Binds` 모듈.
5. 화면마다 `<screen>/screen`(`XRoute`+`XScreen`) · `<screen>/vm`(`XViewModel`·`XUiState`·`XSideEffect`, 액션 있으면 `XIntent`). 인자·UiModel·컴포저블 조각은 `args`/`model`/`component`(2장 규칙).
6. **화면 전환**(feature 간 Launcher / 내부 Route·인자 복원)은 → `docs/architecture/feature-navigation.md`.
7. Route↔Screen 분리·셸/그래프 분리(4장)를 지킨다.

> 참고: 현재 `:feature:sample`이 이 구조의 구현 예시다. 단 **데모용이라 추후 제거될 수 있으므로** 규약의 기준은 이 문서이며, sample 존재에 의존하지 않는다.
