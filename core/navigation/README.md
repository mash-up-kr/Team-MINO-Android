# core:navigation

MinoAndroid의 **화면 전환(네비게이션) 공용 인프라** 모듈. feature **간** Activity 전환과 feature **내부** type-safe Route 전환의 표준 기반을 제공한다.

> feature가 이 인프라로 화면 전환을 **구성하는 규약**(Launcher 작성·인자 전달과 복원·탭 그래프 편입)은 [`docs/architecture/feature-navigation.md`](../../docs/architecture/feature-navigation.md)를, 모듈 경계·의존 방향은 [`docs/architecture/modularization.md`](../../docs/architecture/modularization.md)를 단일 출처로 한다. 이 문서는 이 모듈이 노출하는 **API·사용법·확장 규칙**만 다룬다.

---

## 1. 개요

| | |
|---|---|
| **무엇을** | feature 간 Activity 전환(`ActivityLauncher`)과 feature 내부 type-safe Route 전환(`MinoNavHost`·`Route`)의 공용 인프라, 그리고 두 전환 축 어디에도 담기지 않는 **전환 요청 공유 상태**(`entry/`)를 제공한다. |
| **빌드 타입** | Android Library + Compose + Hilt (`mino.android.library`, `mino.android.compose`, `mino.android.hilt`) |

> Hilt는 `entry/`의 공유 상태를 요청하는 쪽과 받는 쪽이 **같은 인스턴스**로 보게 하려고 붙었다(→ [2.3](#23-두-축-밖--전환-요청-공유-상태-entry)). `activity`·`screen`의 API는 Hilt를 쓰지 않는다.

> [!IMPORTANT]
> 이 모듈은 **전환 인프라와 feature 간 전환 계약**을 제공한다. 화면 Composable·ViewModel·각 feature의 `Route`와 `Launcher` **구현**은 여기 두지 않고 해당 feature 모듈에 둔다(→ [`feature-navigation.md`](../../docs/architecture/feature-navigation.md)).

---

## 2. 핵심 API

화면 전환은 범위에 따라 두 축으로 갈리고, 패키지도 그에 맞춰 나뉜다. 두 축 **어느 쪽의 API도 아니면서** 전환을 성립시키는 공유 상태는 `entry/`에 따로 둔다(→ [2.3](#23-두-축-밖--전환-요청-공유-상태-entry)).

| | feature 간 (Activity) | feature 내부 (Route) |
|---|---|---|
| 패키지 | `activity/` | `screen/` |
| 진입점 | `ActivityLauncher` / `BaseActivityLauncher` | `MinoNavHost` + `screen<T>` |
| 인자 | Intent extra (`intentOf`) | Route 프로퍼티 (custom은 `serializableNavType`) |
| 뒤로·결과 | `resultLauncher` + `setResult` | `popBackStackIfResumed` |

### 2.1 feature 간 — Activity 전환 (`activity/`)

| API | 역할 |
|---|---|
| `ActivityLauncher` | feature 간 전환 진입점의 공통 **계약**(interface). `launch(activity, resultLauncher, withFinish, intentBuilder)` 하나를 갖는다. |
| `BaseActivityLauncher` | `ActivityLauncher` 공통 동작(Intent 생성·인자 주입·실행·`withFinish` 처리) 제공. 각 feature의 구현체는 `createIntent`에서 **대상 Activity만** 지정한다. |
| `Context.intentOf<T>()` | 대상 Activity `T`로의 `Intent`를 생성하는 확장. `builder`로 extra를 덧붙인다. |
| `activity/launcher/` | **feature별 전환 계약**. `interface XLauncher : ActivityLauncher`와 Intent extra 키(`ExtraTag.kt`)를 모아 둔다. |

```kotlin
// 대상 feature — 대상 Activity만 지정
internal class XLauncherImpl @Inject constructor() : BaseActivityLauncher(), XLauncher {
    override fun createIntent(context: Context): Intent = context.intentOf<XActivity>()
}

// 호출부 — 인자는 intentBuilder로, 결과가 필요하면 resultLauncher로
xLauncher.launch(activity) { putExtra(EXTRA_X_SOMETHING, value) }
```

> [!NOTE]
> `launch`의 첫 인자는 `Context`가 아니라 `Activity`다. 비-Activity `Context`로 `startActivity`를 호출하면 `FLAG_ACTIVITY_NEW_TASK` 없이 실패하고, `withFinish`(전환 후 호출자 종료)도 성립하지 않는다. `withFinish`와 `resultLauncher`는 함께 쓰지 않는다 — 종료된 Activity는 결과를 받을 수 없다.

### 2.2 feature 내부 — Route 전환 (`screen/`)

| API | 역할 |
|---|---|
| `Route` | feature 내부 type-safe 라우트의 공통 상위 타입(marker). 구현체는 `@Serializable` 이어야 한다. |
| `MinoNavHost` | 프로젝트 표준 `NavHost`. `Route`를 시작 목적지로 받는다. |
| `NavGraphBuilder.screen<T>(typeMap)` | `Route` 타입 `T`를 목적지로 등록. androidx의 `composable<T>`를 `Route`로 제약한다. |
| `NavGraphBuilder.graph<T>(startDestination, typeMap)` | `Route` 타입 `T`를 진입점으로 하는 **중첩 그래프** 등록. androidx의 `navigation<T>`를 `Route`로 제약한다. |
| `serializableNavType<T>()` / `MinoNavJson` | custom `@Serializable` 인자를 라우트에 싣기 위한 `NavType`. 등록·복원이 공유하는 단일 `Json`. |
| `NavController.popBackStackIfResumed(entry)` | 현재 화면이 `RESUMED`일 때만 pop — 전환 중 빠른 중복 탭의 **이중 pop을 방지**. |
| `ImmersiveRoute` | 바텀 네비게이션을 숨겨야 하는 몰입 화면임을 표시하는 빈 마커 인터페이스. 몰입 화면을 만드는 feature는 자신의 `Route`에 이 마커를 함께 구현한다. |
| `ImmersiveRouteRegistry` | `ImmersiveRoute`를 구현하는 `Route`를 모아 두는 레지스트리. 탭 셸이 `isImmersive(destination)`으로 현재 목적지가 몰입 화면인지(구체 `Route` 타입을 몰라도) 판정할 수 있게 한다. |

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

> [!IMPORTANT]
> `screen<T>`·`graph<T>`는 등록 시점에 `T`가 `ImmersiveRoute`를 구현하는지 검사해, 구현하면 `ImmersiveRouteRegistry`에 자동으로 등록하는 **부수효과**를 갖는다. 몰입 화면을 만드는 feature는 `Route`에 `ImmersiveRoute`를 함께 구현하기만 하면 되고, 레지스트리에 직접 등록하는 코드를 작성할 필요가 없다.

### 2.3 두 축 밖 — 전환 요청 공유 상태 (`entry/`)

전환을 주고받는 두 쪽이 **서로의 구체 타입을 모르는** 자리가 있다. 탭 셸 아래에서 한 탭이 다른 탭의 화면을 여는 경우가 그렇다. 이때 여는 값은 Intent extra도 Route 인자도 아닌 **공유 상태**로 오간다.

| API | 역할 |
|---|---|
| `PlaceDetailRequestHolder` | 다른 탭이 저장 탭에 「이 핀의 장소 상세를 열어 달라」고 남기는 요청 자리. 여는 쪽은 `request(pinId)`로 싣고, 받는 탭은 `pending`을 구독해 처리한 뒤 `consume()`으로 비운다. |

```kotlin
// 여는 쪽 — 요청을 남기고 대상 탭으로 전환한다
placeDetailRequestHolder.request(pinId)

// 받는 쪽 — 처리하고 반드시 비운다
placeDetailRequestHolder.pending.filterNotNull().collect { pinId ->
    open(pinId)
    placeDetailRequestHolder.consume()
}
```

이 패키지의 API는 요청하는 쪽과 받는 쪽이 같은 인스턴스를 봐야 하므로 **주입으로 공유한다** — 이 모듈에서 Hilt를 쓰는 유일한 자리다. 인터페이스 없는 생성자 주입 구체 클래스라 바인딩 모듈은 두지 않고, 스코프는 `ActivityRetainedComponent`(`@ActivityRetainedScoped`)다 — 탭 전환은 같은 Activity 안의 일이고 구성 변경에도 요청이 살아남아야 한다(스코프 규칙 → [`dependency-injection.md`](../../docs/conventions/dependency-injection.md)).

> [!IMPORTANT]
> 왜 Route 인자로 나르지 않는가 — 탭 전환은 떠난 탭의 백스택을 `saveState`/`restoreState`로 저장·복원하고, 복원된 목적지는 **저장 당시의 인자를 그대로 들고 되살아난다.** 새로 실은 값이 무시된다. 탭 경계를 건너는 값은 백스택 밖에 두어야 한다. 배경 → [ADR](../../docs/adr/2026-09-02-immersive-map-screen-shares-one-map-in-tab-feature.md).

> [!NOTE]
> 받는 쪽이 `consume()`을 빠뜨리면 사용자가 닫은 화면이 탭을 오갈 때마다 다시 열린다 — 남아 있는 요청은 탭에 들어올 때마다 유효한 요청으로 읽히기 때문이다.

---

## 3. 디렉토리 구조

```
team/mino/core/navigation/
├── activity/   # feature 간 Activity 전환 인프라
│   ├── ActivityLauncher.kt       # 전환 진입점 공통 계약(interface)
│   ├── BaseActivityLauncher.kt   # 공통 동작 제공(abstract) — createIntent만 구현
│   ├── ActivityIntent.kt         # Context.intentOf<T> 확장
│   └── launcher/                 # feature별 전환 계약
│       ├── ExtraTag.kt           # Intent extra 키 (EXTRA_<대상 feature>_<이름>)
│       └── XLauncher.kt          # interface XLauncher : ActivityLauncher (feature마다 하나)
├── entry/      # 두 전환 축 밖 — 전환 요청 공유 상태
│   └── PlaceDetailRequestHolder.kt  # 다른 탭 → 저장 탭 장소 상세 열기 요청(@ActivityRetainedScoped)
└── screen/     # feature 내부 type-safe Route 전환 인프라
    ├── Route.kt                  # 라우트 공통 상위 타입(marker)
    ├── MinoNavHost.kt            # 표준 NavHost + screen<T>·graph<T> 등록 함수
    ├── SerializableNavType.kt    # custom 인자용 NavType + MinoNavJson
    ├── NavLifecycle.kt           # popBackStackIfResumed
    ├── ImmersiveRoute.kt         # 바텀 네비게이션을 숨길 몰입 화면 마커(interface)
    └── ImmersiveRouteRegistry.kt # ImmersiveRoute 구현 Route를 모아 두는 레지스트리(screen<T>·graph<T>가 등록)
```

---

## 4. 확장 규칙 — 어디에 둘지 결정

| 패키지 | 두는 것 | 예시 |
|---|---|---|
| `activity` | feature **간** Activity 전환에 쓰는 공통 계약·헬퍼 | 결과 전달 헬퍼, Intent 확장 |
| `activity/launcher` | feature**별** 전환 계약 — `XLauncher` 인터페이스와 Intent extra 키 | 새 진입형 feature의 `XLauncher`, `EXTRA_*` |
| `screen` | feature **내부** Route(Compose Navigation) 전환 인프라 | 공통 transition, NavType, 백스택 헬퍼 |
| `entry` | 어느 전환 축의 API도 아니면서, 전환을 주고받는 두 쪽이 **서로의 구체 타입을 모른 채 합의하는 상태** | 탭 간 화면 열기 요청 홀더 |

- 인프라는 먼저 두 전환 축 중 **어디에 공통으로 쓰이는가**로 패키지를 고른다(`activity`·`screen`). 어느 축에도 담기지 않는 것만 `entry`로 간다 — 축에 담기는 것을 `entry`에 두면 그 축의 사용자가 API를 두 곳에서 찾게 된다.
- 이 모듈이 feature 이름을 알게 되는 것은 의도된 비용이다. 계약을 각 feature에 두면 서로를 여는 두 feature가 생기는 순간 Gradle 모듈 순환이 되어 빌드가 막힌다. 배경 → [ADR](../../docs/adr/2026-08-01-single-module-navigation-contract.md).

---

## 5. 의존성 추가 가이드

`build.gradle.kts`에 추가:

```kotlin
dependencies {
    implementation(project(":core:navigation"))
}
```

이 모듈이 끌어오는 주요 라이브러리(모두 `implementation`이라 전이되지 않음): `androidx-navigation-compose`, `androidx-activity-compose`, `kotlinx-serialization-json`, `kotlinx-coroutines-core`(`entry/`의 공유 상태가 `StateFlow`다). 주로 Activity·`NavHost`·`Launcher`를 구현하는 feature 모듈이 의존한다.

---

## 6. 컨벤션

| 항목 | 규칙 |
|---|---|
| **Route** | `@Serializable` 필수. 각 feature는 자신의 `Route`를 자기 모듈 안에 정의한다(화면 전환은 feature 내부 관심사). 탭 feature만 그래프 진입 Route 하나를 `public`으로 연다. |
| **typeMap 공유** | custom 인자는 등록(`screen<T>`)·복원(`toRoute<T>`)이 같은 `typeMap`을 참조 — Route `companion`에 한 번 정의해 공유. |
| **뒤로가기** | 직접 `popBackStack` 대신 `popBackStackIfResumed`로 이중 pop을 막는다. |
| **Launcher 노출** | `activity/launcher`에 `interface XLauncher : ActivityLauncher`만 두고, 대상 feature가 `BaseActivityLauncher`를 상속해 대상 Activity만 지정한다. |
