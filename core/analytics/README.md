# core:analytics

MinoAndroid의 **분석(Analytics) 공용 모듈**. Firebase Analytics를 감싸 SDK-무관 트래킹 계약(`AnalyticsTracker`)과 화면 전환 자동 로깅 컴포저블(`TrackScreenViews`)을 제공한다.

> 모듈 책임·경계·의존 방향(레이어 그래프)은 [`docs/architecture/modularization.md`](../../docs/architecture/modularization.md)를 단일 출처로 한다. 이 문서는 이 모듈의 **API·사용법·확장 규칙**만 다룬다.

---

## 1. 개요

| | |
|---|---|
| **무엇을** | 이벤트·화면 조회 트래킹 계약(`AnalyticsTracker`)과 Route 전환마다 화면 조회를 자동 기록하는 `TrackScreenViews`를 제공한다. |
| **빌드 타입** | Android Library + Compose + Hilt (`mino.android.library`, `mino.android.compose`, `mino.android.hilt`) |

> [!IMPORTANT]
> **Firebase Analytics SDK를 아는 곳은 이 모듈뿐이다.** feature는 `AnalyticsTracker` 인터페이스에만 의존하고, `FirebaseAnalytics` 구현체(`FirebaseAnalyticsTracker`)는 `internal`로 갇혀 밖으로 새지 않는다. 이렇게 두면 분석 SDK 교체가 feature로 번지지 않는다.

> [!NOTE]
> Firebase 인프라 자체(google-services 플러그인·`google-services.json`·BOM)는 이 모듈 밖(앱 모듈) 책임이다. 이 모듈은 `firebase-analytics`만 끌어와 로깅 API를 감싼다.

---

## 2. 핵심 API

| API | 역할 |
|---|---|
| `AnalyticsTracker` | 트래킹 공통 계약(interface). feature는 이 타입만 주입받아 `logEvent`로 커스텀 이벤트를 기록한다. |
| `AnalyticsTracker.logEvent(name, params)` | 커스텀 이벤트 기록. `params` 값은 `String`/`Int`/`Long`/`Double`/`Float`/`Boolean`만 그대로 전달되고, 그 외 타입은 `toString()`으로 떨어진다. |
| `TrackScreenViews(navController)` | `navController`의 목적지 전환을 감지해 화면 조회 이벤트를 **자동** 기록하는 컴포저블. feature의 NavHost 진입점에서 한 번 호출한다. |

> [!NOTE]
> `AnalyticsTracker` 바인딩·`FirebaseAnalytics` 인스턴스 제공은 `di/AnalyticsModule`이 `SingletonComponent`에 등록한다. feature는 Hilt로 `AnalyticsTracker`를 주입받거나, 화면 로깅은 `TrackScreenViews`만 호출하면 된다(내부에서 `hiltViewModel`로 트래커를 끌어온다).

### 사용 예시 (A) — 화면 자동 로깅

feature NavHost 진입점에서 `MinoNavHost`와 나란히 `TrackScreenViews`를 한 번 호출해두면, 이후 화면이 늘어나도 각 화면에서 로깅을 직접 부를 필요가 없다.

```kotlin
@Composable
internal fun XNavHost(
    startDestination: Route,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    TrackScreenViews(navController)
    MinoNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        screen<XMain> { XRoute() }
    }
}
```

### 사용 예시 (B) — 커스텀 이벤트

ViewModel 등에서 `AnalyticsTracker`를 주입받아 이벤트를 기록한다.

```kotlin
@HiltViewModel
class XViewModel @Inject constructor(
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {
    fun onCtaClick(itemId: String) {
        analyticsTracker.logEvent(
            name = "cta_click",
            params = mapOf("item_id" to itemId, "position" to 1),
        )
    }
}
```

---

## 3. 디렉토리 구조

```
team/mino/core/analytics/
├── AnalyticsTracker.kt              # 트래킹 공통 계약(interface)
├── FirebaseAnalyticsTracker.kt      # Firebase 구현(internal) — SDK를 아는 유일한 곳
├── di/
│   └── AnalyticsModule.kt           # AnalyticsTracker 바인딩 + FirebaseAnalytics 제공
└── screen/
    ├── TrackScreenViews.kt          # Route 전환 감지 → 화면 조회 자동 로깅 컴포저블
    └── ScreenViewTrackerViewModel.kt # 트래커 주입·화면 이름 파싱(internal)
```

---

## 4. 확장 규칙 — 어디에 둘지 결정

| 두는 것 | 위치 |
|---|---|
| 트래킹 공개 계약(이벤트·화면 로깅 API) | `AnalyticsTracker` — feature가 보는 유일한 표면 |
| 분석 SDK를 만지는 구현 | `FirebaseAnalyticsTracker`처럼 `internal` 구현 — **SDK 타입을 다루는 코드는 이 경계로 모은다** |
| Compose/Navigation 연동 헬퍼 | `screen/` — `TrackScreenViews`처럼 화면 계층과 붙는 로깅 |

- 새 코드가 **Firebase SDK 타입을 만지면** `internal` 구현에, **feature가 불러야 하는 계약이면** `AnalyticsTracker`에 둔다.
- 분석 SDK를 교체할 때는 `AnalyticsTracker` 새 구현과 `di/AnalyticsModule` 바인딩만 바꾼다 — feature 코드는 건드리지 않는다.

---

## 5. 의존성 추가 가이드

feature `:impl`은 **컨벤션 플러그인(`mino.android.feature.impl`)이 이 모듈을 이미 의존**하므로 별도 추가가 필요 없다. 그 밖의 모듈이 직접 트래킹해야 하면:

```kotlin
dependencies {
    implementation(project(":core:analytics"))
}
```

이 모듈이 끌어오는 주요 라이브러리: `firebase-analytics`(BOM 버전), `androidx-navigation-compose`, `androidx-hilt-navigation-compose`. Firebase 플러그인·구성 파일은 앱 모듈이 책임진다.

---

## 6. 컨벤션

| 항목 | 규칙 |
|---|---|
| **SDK 은닉** | feature는 `AnalyticsTracker`만 안다. `FirebaseAnalytics` 등 SDK 타입은 이 모듈 `internal` 구현에서만 다룬다. |
| **화면 로깅** | 화면 진입 로깅은 각 화면에서 개별 호출하지 않고, NavHost 진입점의 `TrackScreenViews` 한 곳으로 모은다. |
| **이벤트 파라미터** | `params`는 Firebase가 지원하는 원시 타입(`String`/`Int`/`Long`/`Double`/`Float`/`Boolean`)만 그대로 전달된다. |
| **DI** | 트래커 바인딩·`FirebaseAnalytics` 제공은 `di/AnalyticsModule`(`SingletonComponent`)에 둔다. |
