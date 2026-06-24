# core:common:android

MinoAndroid의 **Android SDK 기반 공용 유틸리티** 모듈. Context 확장·Intent 헬퍼 등 Android 의존 공용 코드와, 화면 공통 **MVI 아키텍처 기반**을 제공한다.

> 모듈 책임·경계·의존 방향(레이어 그래프)은 [`docs/architecture/modularization.md`](../../../docs/architecture/modularization.md)를 단일 출처로 한다. 이 문서는 이 모듈의 **API·사용법·확장 규칙**만 다룬다.

---

## 1. 개요

| | |
|---|---|
| **무엇을** | Android 의존 공용 유틸 + ViewModel이 상속해 쓰는 MVI 컨테이너(`MviContainer`)와 MVI 마커 인터페이스를 제공한다. |
| **빌드 타입** | Android Library (`mino.android.library`) |

> [!IMPORTANT]
> 이 모듈은 **Compose/UI를 의존하지 않는다.** Composable·Effect 헬퍼 등 UI 코드는 [`core:common:ui`](../ui/README.md)에 둔다. (모듈 경계 상세는 [`modularization.md`](../../../docs/architecture/modularization.md))

---

## 2. 핵심 API — MVI 아키텍처

화면 상태 관리는 MVI(Model-View-Intent) 패턴을 따른다. 구성 요소는 모두 `architecture` 패키지에 있다.

### 마커 인터페이스

```kotlin
interface UiState     // 화면 상태 — data class로 구현
interface Intent      // 사용자 의도/이벤트
interface SideEffect  // 일회성 이벤트(Toast·네비게이션 등)
```

### `MviContainer` — ViewModel이 위임받는 컨테이너

```kotlin
interface MviContainer<S : UiState, E : SideEffect> {
    val state: StateFlow<S>
    val sideEffect: Flow<E>
    fun updateState(reducer: S.() -> S)
    suspend fun postSideEffect(effect: E)
}
```

`state`는 `StateFlow`, `sideEffect`는 `Channel` 기반 `Flow`(버퍼링)로 구현된다([`MviContainerImpl`](src/main/java/team/mino/core/common/android/architecture/MviContainerImpl.kt)).

### 사용 예시

`mviContainer()` 팩토리로 컨테이너를 만들고 **위임(by)** 으로 ViewModel에 합성한다.

```kotlin
data class CounterUiState(val count: Int = 0) : UiState
sealed interface CounterSideEffect : SideEffect {
    data object ShowMaxReached : CounterSideEffect
}

class CounterViewModel(
    container: MviContainer<CounterUiState, CounterSideEffect> = mviContainer(CounterUiState()),
) : ViewModel(), MviContainer<CounterUiState, CounterSideEffect> by container {

    fun onIncrease() {
        updateState { copy(count = count + 1) }            // 상태 갱신
        viewModelScope.launch {
            if (state.value.count >= 10) {
                postSideEffect(CounterSideEffect.ShowMaxReached)  // 일회성 이벤트
            }
        }
    }
}
```

- **상태 변경**: `updateState { copy(...) }` — 현재 상태 기준 reducer로 안전하게 갱신
- **일회성 이벤트**: `postSideEffect(...)` — UI에서 [`CollectSideEffect`](../ui/README.md)로 수집

> [!NOTE]
> `SideEffect`는 **포그라운드 UI 이벤트 전용**이다. 백그라운드에서도 처리돼야 하거나 프로세스 사망을 넘어 살아남아야 하는 신호는 `SideEffect`가 아니라 상태(`StateFlow`)나 OS 알림으로 모델링한다. 수집 동작 상세는 [`core:common:ui`](../ui/README.md)의 `CollectSideEffect` 참고.

---

## 3. 디렉토리 구조

```
core/common/android/src/main/java/team/mino/core/common/android/
├── architecture/
│   ├── MviComponent.kt        # UiState / Intent / SideEffect 마커 인터페이스
│   ├── MviContainer.kt        # MVI 인터페이스 + mviContainer() 팩토리
│   └── MviContainerImpl.kt    # StateFlow + Channel 기반 구현체
├── extension/                 # (예약 — Context/Intent 등 Android 확장)
└── util/                      # (예약 — Android 유틸)
```

`extension`·`util`은 현재 비어 있는 **예약 패키지**다.

---

## 4. 확장 규칙 — 어디에 둘지 결정

| 패키지 | 두는 것 | 예시 |
|---|---|---|
| `architecture` | 화면 아키텍처 기반 타입 | MVI 컨테이너·마커 |
| `extension` | **기존 Android 타입에 붙는** 확장 함수·프로퍼티 | `Context.dp()`, `Intent.putGeoPoint()`, `Activity.hideKeyboard()` |
| `util` | **타입에 종속되지 않는** 독립 헬퍼·래퍼 | 권한 체크 헬퍼, 네트워크 상태 모니터, 날짜·전화번호 포맷터 |

### 예약 패키지에 무엇을 넣나

`extension`·`util`은 현재 비어 있다. 둘의 구분은 **"기존 타입에 붙는가, 독립적으로 호출하는가"**:

- **`extension`** — 수신자(receiver)가 있는 확장. `fun X.doSomething()` 형태로 기존 Android 타입을 꾸민다.
  - `Context`/`Activity`/`Fragment` 확장 — `Context.dp()`, `Context.getColorCompat()`, `Activity.hideKeyboard()`
  - `Intent`/`Bundle` 확장 — `Intent.putGeoPoint()`, `Bundle.getParcelableCompat()`
  - `View` 확장 — `View.setVisible(Boolean)`, `View.showSnackbar()`
- **`util`** — 수신자 없이 독립적으로 쓰는 헬퍼·상수·얇은 래퍼.
  - 권한 체크 — `PermissionChecker.hasCameraPermission(context)`
  - 시스템 상태 헬퍼 — 네트워크 연결 상태, 배터리 등
  - 포맷/파서 중 **Android API에 의존하는 것** (datetime·문자열 등 순수 Kotlin이면 [`core:common:kotlin`](../kotlin/README.md)으로)

### 파일명 컨벤션

- **`architecture` 등 타입 정의** — 파일명은 타입/인터페이스 이름 그대로 (`MviContainer.kt`, `MviContainerImpl.kt`). 단, 묶음으로 다루는 마커 인터페이스는 한 파일에 모은다 (`MviComponent.kt` = `UiState`/`Intent`/`SideEffect`).
- **`extension`** — 파일명은 **리시버(수신자) 타입 이름**으로 짓고, 같은 타입의 확장은 그 파일에 모은다.

  | 확장 대상 | 파일명 |
  |---|---|
  | `Context` 확장 | `Context.kt` |
  | `Intent` 확장 | `Intent.kt` |
  | `View` 확장 | `View.kt` |

  ```
  extension/
  ├── Context.kt   # Context.dp(), Context.getColorCompat() ...
  ├── Intent.kt    # Intent.putGeoPoint() ...
  └── View.kt      # View.setVisible(), View.showSnackbar() ...
  ```

- **`util`** — 파일/타입명은 **행동을 나타내는 접미사**(`Util`, `Manager`, `Checker`, `Formatter` 등)로 짓는다. 객체가 무엇을 하는지에 따라 적절한 접미사를 고른다.

  | 역할 | 접미사 | 예시 |
  |---|---|---|
  | 정적 헬퍼 함수 모음 | `Util` | `ResourceUtil.kt` |
  | 상태·리소스를 관리하는 객체 | `Manager` | `NetworkManager.kt` |
  | 조건 검사 | `Checker` | `PermissionChecker.kt` |
  | 포맷 변환 | `Formatter` | `PhoneNumberFormatter.kt` |

> [!NOTE]
> 넣기 전 두 가지를 확인한다. ① **Android API가 정말 필요한가** — 순수 Kotlin이면 [`core:common:kotlin`](../kotlin/README.md). ② **Compose/Composable인가** — 그렇다면 [`core:common:ui`](../ui/README.md). 둘 다 아니고 여러 모듈이 공유하면 여기다.

위 표는 **이 모듈 안에서** 패키지를 고르는 기준이다. 이 모듈에 둘지, 다른 모듈(`core:common:kotlin`·`core:common:ui`·feature 등)에 둘지 — 즉 모듈 경계 판단은 [`modularization.md`](../../../docs/architecture/modularization.md)를 따른다.

---

## 5. 의존성 추가 가이드

`build.gradle.kts`에 추가:

```kotlin
dependencies {
    implementation(project(":core:common:android"))
}
```

MVI 타입(`UiState`/`Intent`/`SideEffect`/`MviContainer`)이 모두 이 모듈에 있으므로, ViewModel을 두는 feature `impl` 모듈이 이 모듈을 의존한다.

---

## 6. 컨벤션

| 항목 | 규칙 |
|---|---|
| **의존성** | Android SDK까지만. **Compose/UI는 의존 금지** (그건 `core:common:ui`). |
| **MVI 합성** | ViewModel은 `mviContainer()`를 `by`로 위임받아 사용. 직접 `MviContainerImpl`을 new 하지 않는다. |
| **SideEffect 범위** | 포그라운드 UI 일회성 이벤트 전용. 영속/백그라운드 신호는 상태로 모델링. |
