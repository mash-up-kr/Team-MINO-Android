# core:error-handling

MinoAndroid의 **에러 처리 기반 모듈**. 도메인 예외 계층(`MinoDomainException`)과 소비 헬퍼(`runCatchingDomain`·`onDomainFailure`), 에러 전달 채널(`DomainErrorEmitter`·`UncaughtErrorHandler`)을 제공한다.

> 모듈 책임·경계·의존 방향(레이어 그래프)은 [`docs/architecture/modularization.md`](../../docs/architecture/modularization.md)를, 에러 처리 **정책**(2단 분류 기준·레이어별 책임·리뷰 규약)은 [`docs/conventions/error_handling.md`](../../docs/conventions/error_handling.md)를 단일 출처로 한다. 이 문서는 이 모듈의 **API·사용법·확장 규칙**만 다룬다.

---

## 1. 개요

| | |
|---|---|
| **무엇을** | 예상 가능한 실패를 표현하는 도메인 예외 sealed 계층과, ViewModel이 그것만 잡아 소비하게 하는 헬퍼·채널을 제공한다. |
| **빌드 타입** | Kotlin JVM (`mino.kotlin.jvm`) + `kotlinx-coroutines-core` |

> [!IMPORTANT]
> **Android Library가 아니다.** `core:domain`(Kotlin JVM)이 의존할 수 있어야 하기 때문이다. 따라서 Android 의존 코드는 여기 두지 않는다 — CEH 결합 `launchSafely`는 [`core:common:android`](../common/android/README.md), Compose 수집 컴포저블(`CollectDomainError`·`CollectUncaughtError`)은 [`core:common:ui`](../common/ui/README.md)에 둔다.

---

## 2. 핵심 API

| API | 역할 |
|---|---|
| `MinoDomainException` | 예상 가능한 실패의 sealed 계층. 초기 리프는 `Network`(연결 불가·타임아웃)·`Http(code)`(non-2xx) 2개. |
| `runCatchingDomain(block)` | 예외 → `Result` 변환 지점(규약상 유일). **`MinoDomainException`만** `Result.failure`로 잡고, 버그·`CancellationException`은 통과시켜 CEH로 보낸다. |
| `Result.onDomainFailure(action)` | 표준 `onFailure` 대신 쓰는 소비 확장. failure에 `MinoDomainException`만 담긴다는 사실을 타입으로 복원해 sealed `when` 분기를 가능하게 한다. |
| `DomainErrorEmitter` / `domainErrorEmitter()` | **ViewModel 인스턴스별** 도메인 에러 채널(`Channel(BUFFERED)`). `mviContainer`처럼 `by` 위임으로 합성하고, 액션 일회성 실패를 `emitDomainError`로 방출한다. |
| `UncaughtErrorHandler` | CEH 도달 예외(버그) 전용 **전역** 이벤트 버스(`object`). 도메인 예외는 여기로 오지 않는다. |

### `MinoDomainException` 정의

```kotlin
sealed class MinoDomainException(
    message: String? = null,   // 디버깅용. 사용자 노출 문구를 담지 않는다
    cause: Throwable,
) : RuntimeException(message, cause) {
    /** 연결 불가·타임아웃 등 네트워크 단절 */
    class Network(cause: Throwable) : MinoDomainException(cause = cause)

    /** non-2xx HTTP 응답. 상태 코드는 세분화하지 않고 code로 통일 */
    class Http(val code: Int, cause: Throwable) : MinoDomainException(cause = cause)
}
```

### 사용 예시 (A) — ViewModel: 위임 + 두 소비 경로

```kotlin
@HiltViewModel
class SampleViewModel @Inject constructor(
    private val githubRepository: GithubRepository,
) : ViewModel(),
    MviContainer<SampleUiState, SampleSideEffect> by mviContainer(SampleUiState()),
    DomainErrorEmitter by domainErrorEmitter() {

    // 주 데이터 로드 실패 → State에 리프를 담는다 (문구 매핑은 화면이)
    private fun loadTeamMembers() {
        launchSafely {
            updateState { copy(status = Loading) }
            runCatchingDomain { githubRepository.getUser("mash-up-kr") }
                .onSuccess { updateState { copy(status = Success(it)) } }
                .onDomainFailure { e -> updateState { copy(status = Error(e)) } }
        }
    }

    // 사용자 액션의 일회성 실패 → 매핑 없이 리프 그대로 방출
    private fun follow(login: String) {
        launchSafely {
            runCatchingDomain { githubRepository.follow(login) }
                .onSuccess { postSideEffect(ShowToast("팔로우 완료")) }
                .onDomainFailure(::emitDomainError)
        }
    }
}
```

버그성 예외는 별도 코드가 없다 — `runCatchingDomain`을 통과해 `launchSafely`의 CEH에 도달하고, CEH가 `Timber.e` 리포팅 후 `UncaughtErrorHandler.dispatch`로 전역 안내를 보낸다. 예시의 `launchSafely`는 이 모듈이 아니라 [`core:common:android`](../common/android/README.md)의 확장이다.

### 사용 예시 (B) — 수집 측

방출된 에러의 수집은 [`core:common:ui`](../common/ui/README.md)의 컴포저블이 담당한다: `DomainErrorEmitter`는 해당 ViewModel의 Route가 `CollectDomainError`로, `UncaughtErrorHandler`는 네비게이션 셸 `MinoScaffold`가 `CollectUncaughtError`로 수집한다.

---

## 3. 디렉토리 구조

```
core/error-handling/src/main/kotlin/team/mino/core/errorhandling/
├── MinoDomainException.kt     # 도메인 예외 sealed 계층
├── RunCatchingDomain.kt       # runCatchingDomain + Result.onDomainFailure
├── DomainErrorEmitter.kt      # 인터페이스 + domainErrorEmitter() 팩토리 + private 구현
└── UncaughtErrorHandler.kt    # 버그 전용 전역 이벤트 버스 (object)
```

---

## 4. 확장 규칙 — 리프 추가

새 `MinoDomainException` 리프를 추가할 때의 형태 규칙:

| 규칙 | 이유 |
|---|---|
| 반드시 `class` (`object` 금지) | `Throwable` 싱글턴은 stacktrace가 최초 생성 시점 한 번으로 고정·공유되어 발생 지점을 추적할 수 없다 |
| 원본 예외를 기본값 없는 필수 파라미터 `cause`로 받는다 | 보존을 잊는 실수를 구조적으로 차단, 크래시 리포팅 시 원인 추적 근거 |
| 사용자 노출 문구를 담지 않는다 | UI 문구는 presentation에서 리프 타입 → string resource로 매핑 |
| `core:data`의 validator 화이트리스트와 **짝으로** 추가한다 | 매핑되지 않는 리프는 죽은 타입이고, 리프 없는 매핑은 컴파일이 깨진다 |

- **탈출구 리프(`Unknown`류) 금지** — 매핑 규칙에 걸리지 않는 예외는 버그이며 CEH 소관이다. 탈출구가 있으면 버그가 도메인 예외로 오분류되어 조용히 소비된다.
- feature 고유 비즈니스 에러(서버 에러 코드 체계)의 리프 확장 정책은 API 스펙 확정 시 재논의한다.
- 새 코드가 Android API를 만지면 [`core:common:android`](../common/android/README.md), Composable이면 [`core:common:ui`](../common/ui/README.md)로 — 이 모듈은 순수 Kotlin 에러 인프라만 담는다.

---

## 5. 의존성 추가 가이드

feature 모듈은 **컨벤션 플러그인(`mino.android.feature`)이 이 모듈을 이미 의존**하므로 별도 추가가 필요 없다. 그 밖의 모듈은 `build.gradle.kts`에 추가:

```kotlin
dependencies {
    implementation(project(":core:error-handling"))
}
```


---

## 6. 컨벤션

| 항목 | 규칙 |
|---|---|
| **예외 잡기** | `kotlin.runCatching` 금지 → `runCatchingDomain`. 버그·`CancellationException`을 삼키면 안 된다. |
| **failure 소비** | 표준 `onFailure` 금지 → `onDomainFailure`. 모든 `runCatchingDomain` 결과에 필수 (리뷰 규약). |
| **`UncaughtErrorHandler`** | 버그 전용 통로. 도메인 예외를 `dispatch`하지 않는다. |
| **`DomainErrorEmitter`** | 인스턴스별 채널. 전역 싱글턴으로 만들지 않는다 — 다른 화면으로의 이벤트 오배달 방지. |
| **`when` 분기** | 리프 소비 `when`에서 `else` 허용. |
