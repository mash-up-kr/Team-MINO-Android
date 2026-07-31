# core:navigation

MinoAndroid의 **화면 전환(네비게이션) 공용 인프라** 모듈. feature **간** Activity 전환과 feature **내부** type-safe Route 전환의 표준 기반을 제공한다.

> feature가 이 인프라로 화면 전환을 **구성하는 규약**(api/impl 분리·Launcher 작성·인자 전달과 복원)은 [`docs/architecture/feature-navigation.md`](../../docs/architecture/feature-navigation.md)를, 모듈 경계·의존 방향은 [`docs/architecture/modularization.md`](../../docs/architecture/modularization.md)를 단일 출처로 한다. 이 문서는 이 모듈이 노출하는 **API·사용법·확장 규칙**만 다룬다.

---

## 1. 개요

| | |
|---|---|
| **무엇을** | feature 간 Activity 전환(`ActivityLauncher`)과 feature 내부 type-safe Route 전환(`MinoNavHost`·`Route`)의 공용 인프라를 제공한다. |
| **빌드 타입** | Android Library + Compose (`mino.android.library`, `mino.android.compose`) |

> [!IMPORTANT]
> 이 모듈은 **전환 인프라만** 제공한다. 화면 Composable·ViewModel·각 feature의 `Route`/`Launcher` **구현**은 여기 두지 않고 해당 feature `:impl`에 둔다(→ [`feature-navigation.md`](../../docs/architecture/feature-navigation.md)).

---

## 2. 핵심 API

화면 전환은 범위에 따라 두 축으로 갈리고, 패키지도 그에 맞춰 둘로 나뉜다.

| | feature 간 (Activity) | feature 내부 (Route) |
|---|---|---|
| 패키지 | `activity/` | `screen/` |
| 진입점 | `ActivityLauncher` / `BaseActivityLauncher` | `MinoNavHost` + `screen<T>` |
| 인자 | Intent extra (`intentOf`) | Route 프로퍼티 (custom은 `serializableNavType`) |
| 뒤로·결과 | `resultLauncher` + `setResult` | `popBackStackIfResumed` |

### 2.1 feature 간 — Activity 전환 (`activity/`)

| API | 역할 |
|---|---|
| `ActivityLauncher` | feature 간 전환 진입점의 공통 **계약**(interface). feature `:api`가 `interface XLauncher : ActivityLauncher`로만 노출한다. |
| `BaseActivityLauncher` | `ActivityLauncher` 공통 동작(Intent 생성·인자 주입·실행) 제공. `:impl` 구현체는 `createIntent`에서 **대상 Activity만** 지정한다. |
| `Context.intentOf<T>()` | 대상 Activity `T`로의 `Intent`를 생성하는 확장. `builder`로 extra를 덧붙인다. |

```kotlin
// :impl — 대상 Activity만 지정
internal class XLauncherImpl @Inject constructor() : BaseActivityLauncher(), XLauncher {
    override fun createIntent(context: Context): Intent = context.intentOf<XActivity>()
}

// 호출부 — 인자는 intentBuilder로, 결과가 필요하면 resultLauncher로
xLauncher.launch(context) { putExtra(EXTRA_SOMETHING, value) }
```

### 2.2 feature 내부 — Route 전환 (`screen/`)

| API | 역할 |
|---|---|
| `Route` | feature 내부 type-safe 라우트의 공통 상위 타입(marker). 구현체는 `@Serializable` 이어야 한다. |
| `MinoNavHost` | 프로젝트 표준 `NavHost`. `Route`를 시작 목적지로 받는다. |
| `NavGraphBuilder.screen<T>(typeMap)` | `Route` 타입 `T`를 목적지로 등록. androidx의 `composable<T>`를 `Route`로 제약한다. |
| `serializableNavType<T>()` / `MinoNavJson` | custom `@Serializable` 인자를 라우트에 싣기 위한 `NavType`. 등록·복원이 공유하는 단일 `Json`. |
| `NavController.popBackStackIfResumed(entry)` | 현재 화면이 `RESUMED`일 때만 pop — 전환 중 빠른 중복 탭의 **이중 pop을 방지**. |

```kotlin
MinoNavHost(navController, startDestination) {
    screen<XMain> { XRoute(onNavigateToDetail = { navController.navigate(XDetail(query)) }) }
    screen<XDetail>(typeMap = XDetail.typeMap) { entry ->
        XDetailRoute(onBack = { navController.popBackStackIfResumed(entry) })
    }
}
```

> [!NOTE]
> custom 인자 타입은 등록(`screen<T>`)과 복원(`SavedStateHandle.toRoute<T>`)이 **같은 `typeMap`** 을 참조해야 round-trip이 어긋나지 않는다. Route `companion`에 한 번 정의해 공유한다. 전체 인자 전달·복원 흐름은 [`feature-navigation.md`](../../docs/architecture/feature-navigation.md) 참조.

---

## 3. 디렉토리 구조

```
team/mino/core/navigation/
├── activity/   # feature 간 Activity 전환 인프라
│   ├── ActivityLauncher.kt       # 전환 진입점 공통 계약(interface)
│   ├── BaseActivityLauncher.kt   # 공통 동작 제공(abstract) — createIntent만 구현
│   └── ActivityIntent.kt         # Context.intentOf<T> 확장
└── screen/     # feature 내부 type-safe Route 전환 인프라
    ├── Route.kt                  # 라우트 공통 상위 타입(marker)
    ├── MinoNavHost.kt            # 표준 NavHost + screen<T> 등록 함수
    ├── SerializableNavType.kt    # custom 인자용 NavType + MinoNavJson
    └── NavLifecycle.kt           # popBackStackIfResumed
```

---

## 4. 확장 규칙 — 어디에 둘지 결정

| 패키지 | 두는 것 | 예시 |
|---|---|---|
| `activity` | feature **간** Activity 전환에 쓰는 공통 계약·헬퍼 | 결과 전달 헬퍼, Intent 확장 |
| `screen` | feature **내부** Route(Compose Navigation) 전환 인프라 | 공통 transition, NavType, 백스택 헬퍼 |

- 두 전환 축 중 **어디에 공통으로 쓰이는 인프라인가**로 패키지를 고른다.
- 특정 feature 고유의 `Route`·`XShell`·`XNavHost`·`XLauncherImpl`은 **이 모듈이 아니라 그 feature `:impl`** 에 둔다(→ [`feature-navigation.md`](../../docs/architecture/feature-navigation.md)). 이 모듈엔 여러 feature가 공유하는 기반만 올린다.

---

## 5. 의존성 추가 가이드

`build.gradle.kts`에 추가:

```kotlin
dependencies {
    implementation(project(":core:navigation"))
}
```

이 모듈이 끌어오는 주요 라이브러리: `androidx-navigation-compose`, `androidx-activity-compose`, `kotlinx-serialization-json` (모두 `implementation`이라 전이되지 않음). 주로 Activity·`NavHost`·`Launcher`를 구현하는 feature `:impl`이 의존한다.

---

## 6. 컨벤션

| 항목 | 규칙 |
|---|---|
| **Route** | `@Serializable` 필수. 각 feature는 자신의 `Route`를 `:impl` 내부에 정의한다(화면 전환은 feature 내부 관심사). |
| **typeMap 공유** | custom 인자는 등록(`screen<T>`)·복원(`toRoute<T>`)이 같은 `typeMap`을 참조 — Route `companion`에 한 번 정의해 공유. |
| **뒤로가기** | 직접 `popBackStack` 대신 `popBackStackIfResumed`로 이중 pop을 막는다. |
| **Launcher 노출** | feature `:api`는 `interface XLauncher : ActivityLauncher`만 노출하고, `:impl`은 `BaseActivityLauncher`를 상속해 대상 Activity만 지정한다. |
