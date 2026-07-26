# core:common:ui

MinoAndroid의 **feature 간 재사용 공통 UI** 모듈. 공용 Composable 컴포넌트와 Compose 유틸리티(Modifier 확장, Effect 헬퍼 등)를 제공한다.

> 모듈 책임·경계·의존 방향(레이어 그래프)은 [`docs/architecture/modularization.md`](../../../docs/architecture/modularization.md)를 단일 출처로 한다. 이 문서는 이 모듈의 **API·사용법·확장 규칙**만 다룬다.

---

## 1. 개요

| | |
|---|---|
| **무엇을** | 여러 feature가 공유하는 Composable과 Compose 헬퍼를 제공한다. MVI `SideEffect` 수집(`CollectSideEffect`)과 에러 소비(`CollectDomainError`·`CollectUncaughtError`). |
| **빌드 타입** | Android Library + Compose (`mino.android.library` + `mino.android.compose`) |

> [!IMPORTANT]
> **디자인 토큰/테마는 이 모듈이 아니라 [`core:design-system`](../../design-system/README.md)에 둔다.** 이 모듈은 "어떻게 보이는가(토큰)"가 아니라 "어떻게 동작하는가(Effect 수집·Modifier 확장 등)"를 담당한다.

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
- `CollectUncaughtError` — **각 Activity가 `setContent` 바로 아래(NavHost 밖)** 에서 선언한다 (리뷰 규약). 버그 안내 문구는 `R.string.error_unknown`.
- 두 컴포저블 모두 `RESUMED`에서만 수집하고, 수집 공백 중 이벤트는 채널 버퍼가 보존한다.

---

## 3. 디렉토리 구조

```
core/common/ui/src/main/java/team/mino/core/common/ui/
├── architecture/
│   └── CollectSideEffect.kt            # SideEffect를 lifecycle 기준으로 수집하는 Composable
└── error/
    ├── CollectDomainError.kt           # DomainErrorEmitter 수집 (Route 선언)
    ├── CollectUncaughtError.kt         # UncaughtErrorHandler 수집 (Activity 루트 선언)
    └── CollectOnResumed.kt             # 두 수집 컴포저블의 공통 골격 (internal)
```

공용 Composable 컴포넌트나 Modifier 확장이 늘어나면 성격별 패키지(`component`, `modifier` 등)를 추가한다.

---

## 4. 확장 규칙 — 어디에 둘지 결정

이 모듈은 **동작/구조**(공통 Composable·Effect 헬퍼·Modifier 확장)를 담는다. 인접 모듈과 헷갈리기 쉬운 경계만 짚는다.

| 헷갈리는 대상 | 그건 여기가 아니라 |
|---|---|
| 색·타이포·셰이프 토큰, 테마, 기본 디자인 컴포넌트 | [`core:design-system`](../../design-system/README.md) |
| 특정 feature 전용 화면/컴포넌트 | 해당 feature `impl` (공유되면 [§5](#5-feature--corecommonui-승격)로 승격) |

그 외 모듈 경계 판단은 [`modularization.md`](../../../docs/architecture/modularization.md)를 따른다.

---

## 5. feature → core:common:ui 승격

UI 컴포넌트·Composable·화면 단위는 **처음부터 이 모듈에 만들지 않는다.** 특정 feature 안에서 먼저 만들고, **둘 이상의 feature가 실제로 공유하게 될 때** 이 모듈로 끌어올린다(승격). 쓰일지 모를 공용 컴포넌트를 선제적으로 여기 두면, 검증되지 않은 API가 공용 표면으로 굳어진다.

### 승격 기준 — 아래를 모두 만족할 때

- **2개 이상의 feature가 실제로 같은 컴포넌트를 필요로 한다.** ("언젠가 쓸 것 같다"는 제외 — 두 번째 사용처가 생긴 시점이 신호)
- **특정 feature의 도메인/네비게이션에 묶여 있지 않다.** 화면 단위라도 feature 고유 ViewModel·Route·도메인 모델에 의존하면 승격 대상이 아니다. 순수 표현(상태를 인자로 받고 콜백을 올리는 stateless 형태)으로 분리 가능해야 한다.
- **토큰이 아니라 동작/구조다.** 색·타이포·셰이프면 [`core:design-system`](../../design-system/README.md)으로 간다. ([§4](#4-확장-규칙--어디에-둘지-결정))

> [!NOTE]
> 화면(Screen) 단위 승격은 컴포넌트보다 드물다. 화면은 보통 feature의 Route·ViewModel·인자에 강하게 묶이기 때문이다. 승격한다면 **상태·콜백만 받는 stateless Screen**으로 분리하고, feature 쪽에는 ViewModel·Route를 잇는 Route 컴포저블만 남긴다.

### 승격 절차

1. 대상 컴포넌트에서 **feature 의존(ViewModel·Route·도메인 모델·feature 리소스)을 걷어내고** stateless로 다듬는다. 필요한 데이터는 파라미터로, 동작은 콜백(람다)으로 노출한다.
2. 성격에 맞는 패키지로 이동한다 (`component`, `modifier` 등 — [§3](#3-디렉토리-구조)).
3. 디자인 값은 [`core:design-system`](../../design-system/README.md) 토큰을 쓰도록 정리한다. 하드코딩된 색·치수·텍스트 스타일을 토큰으로 교체.
4. 기존 사용처(feature)를 새 공용 컴포넌트 호출로 교체하고, 중복 정의를 제거한다.
5. 공용 컴포넌트는 `@UiModePreviews` 프리뷰를 함께 둔다. (라이트/다크 확인 — design-system 규칙)

> [!TIP]
> 승격은 **"두 번째 사용처가 생겼을 때 리팩토링"** 으로 접근한다. 한 곳에서만 쓰는 동안은 그 feature에 두고, 중복이 실제로 발생한 뒤 공용화하면 잘못된 추상화를 피할 수 있다.

---

## 6. 의존성 추가 가이드

`build.gradle.kts`에 추가:

```kotlin
dependencies {
    implementation(project(":core:common:ui"))
}
```

주로 feature `impl` 모듈(화면을 그리는 쪽)이 의존한다.

---

## 7. 컨벤션

| 항목 | 규칙 |
|---|---|
| **lifecycle 인지** | UI 이벤트 수집은 `CollectSideEffect`처럼 lifecycle을 존중해 백그라운드 처리·유실을 통제한다. |
| **공용성** | 둘 이상의 feature가 실제로 공유할 때만 이 모듈에 올린다. 단일 feature 전용은 그 feature에. ([§5](#5-feature--corecommonui-승격)) |
| **stateless** | 공용 컴포넌트는 feature 의존 없이 상태를 인자로 받고 콜백을 올리는 형태로 둔다. |
| **프리뷰** | 공용 컴포넌트는 `@UiModePreviews`로 라이트/다크 프리뷰를 함께 둔다. |
