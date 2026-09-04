# 계약: 알림 탭 딥링크

**대상**: FR-009·FR-010·FR-012·FR-013 — 시스템 알림을 눌렀을 때 도착지로 이동한다.

**plan 1.1.0에서 대체됨, 1.2.0에서 §2·§3·§4 보정** — 1.0.x는 항상 `SplashActivity`를 거쳤다([research.md D6·D7](../research.md), 재검토됨). 지금은 `MainActivity`를 겨냥하고 게이트 통과 여부를 탭 시점에 수신자가 판정한다([D13](../research.md#d13-알림-탭은-mainactivity를-겨냥하고-게이트-통과-여부는-탭-시점에-수신자가-판정한다)·[D14](../research.md#d14-mainactivity는-singletask이고-딥링크-intent는-new_task만-건다)·[D15](../research.md#d15-웜-경로의-탭-전환은-명령형이고-대기-중인-도착지-탭을-mainactivity가-상태로-든다)).

이 계약은 세 구간을 잇는다 — (A) `:core:notification`이 알림을 만들 때 `PendingIntent`에 도착지를 싣는다, (B) 프로세스가 죽어 있던 경우에만 `MainActivity`가 값을 `SplashActivity`로 넘기고 스플래시가 세션 확보 뒤 다시 `MainActivity`로 돌려준다, (C) `MainActivity`가 값을 소비해 탭·화면을 연다.

```
알림 탭
  → MainActivity (D13·D14 — 항상 이 Activity를 겨냥, singleTask)
    ├─ 앱 실행 중(Main 살아 있음): 기존 인스턴스의 onNewIntent → (C)                   [스플래시 없음, 상태 보존]
    ├─ 프로세스 있음·Main 없음(뒤로가기로 종료): onCreate, 게이트 통과 상태 → 우회 없이 (C) startTab
    └─ 프로세스 없음: onCreate, setContent 전에 MainEntryGate 미통과 + 푸시 extra 있음
         → SplashActivity (extra 그대로 전달, CLEAR_TOP — D13·D16)                    [FR-010 — 시작 경로 그대로]
           ├─ 스플래시가 이미 진행 중이면 그 인스턴스가 onNewIntent로 extra를 이어받는다(singleTop, D16)
           → 세션 확보(EnsureAnonymousSessionUseCase, 기존 경로) → MainEntryGate.markPassed()
           → MainActivity 신규 생성 (extra 그대로) → onCreate → (C)
(C) 소비:
  → PlaceDetailRequestHolder.request(pinId, NOTIFICATION) + 저장 탭            [PushDestination.PlaceDetail]
  → RoomDetailRequestHolder.request(roomId) + 저장 탭                          [PushDestination.RoomDetail]
  → 알림 탭                                                                   [PushDestination.NotificationTab]
  탭 전환: 콜드는 startTab(D9), 웜은 pendingTab + navigateToTab(D15)
```

---

## 1. Intent extra 키 (`:core:navigation` — `activity/launcher/ExtraTag.kt`에 추가)

```kotlin
const val EXTRA_PUSH_DESTINATION_TYPE = "push_destination_type"
const val EXTRA_PUSH_DESTINATION_ID = "push_destination_id"

// EXTRA_PUSH_DESTINATION_TYPE에 실리는 값
const val PUSH_DESTINATION_TYPE_PLACE = "place"
const val PUSH_DESTINATION_TYPE_ROOM = "room"
const val PUSH_DESTINATION_TYPE_NOTIFICATION_TAB = "notification_tab"
```

기존 명명 규칙(`EXTRA_<대상 feature>_<이름>`)과 달리 대상 feature 접두어를 붙이지 않는다 — 이 extra는 `SplashActivity`와 `MainActivity` 둘 다 읽고 그대로 전달하는 값이라 특정 feature 하나에 속하지 않는다. `PushDestination`(sealed) 자체를 직렬화해 싣지 않고 문자열 타입 + id 두 값으로 푸는 이유는 Intent extra가 primitive를 선호하는 기존 관례(`docs/architecture/feature-navigation.md` 1장)를 따르기 위해서다.

**만드는 쪽**: `:core:notification`의 `MinoFirebaseMessagingService`가 `PushDestination`을 이 두 extra로 인코딩해 (A) 구간의 `Intent`에 싣는다.

**읽는 쪽**: `MainActivity`가 `onCreate`(콜드) 또는 `onNewIntent`(웜)에서 읽어 (C)에서 소비한 뒤 지운다(재사용 방지 — 회전 등 재생성 시 같은 요청이 중복 소비되지 않도록 `intent.removeExtra`로 소비 표시). 콜드 우회 구간(B)에서는 `MainActivity`가 읽지 않고 그대로 `SplashActivity`에 실어 보내고, `SplashActivity`도 읽지 않고 그대로 다시 `MainActivity`에 싣는다 — 값을 해석하는 곳은 (C) 하나다.

---

## 2. Intent 팩토리 둘 (`:core:navigation` — `deeplink/`)

```kotlin
// core:navigation — deeplink/MainDeepLinkIntentFactory.kt
interface MainDeepLinkIntentFactory {
    /** [MainActivity]를 대상으로 하는 Intent를 만든다. Activity 없이 Context만으로 호출 가능해야 한다. */
    fun create(context: Context): Intent
}

// core:navigation — deeplink/SplashDeepLinkIntentFactory.kt
interface SplashDeepLinkIntentFactory {
    /** [SplashActivity]를 대상으로 하는 Intent를 만든다. Activity 없이 Context만으로 호출 가능해야 한다. */
    fun create(context: Context): Intent
}
```

`ActivityLauncher`(`fun launch(activity: Activity, ...)`)를 재사용하지 않는 이유: 호출부 중 하나(`:core:notification`의 `FirebaseMessagingService`)는 `Activity`가 아니라 `Service` Context만 가진다. 기존 `XLauncher` 계약은 살아 있는 Activity에서 즉시 `startActivity`하는 형태라 이 상황에 맞지 않는다.

| 팩토리 | 구현·바인딩 | 소비자 | 플래그 |
|---|---|---|---|
| `MainDeepLinkIntentFactory` | `:feature:main/di/MainDeepLinkIntentFactoryImpl.kt` + `MainDeepLinkModule.kt`, `SingletonComponent` | `:core:notification` (A) | `FLAG_ACTIVITY_NEW_TASK` (D14 — `CLEAR_TOP` 없음) |
| `SplashDeepLinkIntentFactory` | `:feature:splash/di/SplashDeepLinkIntentFactoryImpl.kt` + `SplashDeepLinkModule.kt`, `SingletonComponent` | `:feature:main` (B, 콜드 우회) | `FLAG_ACTIVITY_CLEAR_TOP` ([D16](../research.md#d16-스플래시가-진행-중일-때의-알림-탭은-그-스플래시가-이어받는다) — 진행 중인 Splash가 있으면 위의 우회용 Main을 정리하고 그 Splash가 `onNewIntent`로 받는다. `NEW_TASK`는 불필요, Activity 안에서 부른다) |

두 모듈이 `SingletonComponent`인 이유는 Service에서 쓰이거나 Activity 생애주기 밖에서 만들어지기 때문이다(`ActivityRetainedComponent`가 아니다). feature 간 직접 의존은 생기지 않는다 — 인터페이스는 `:core:navigation`, 구현은 각 대상 feature가 갖는다([DI 규칙](../../../conventions/dependency-injection.md), 기존 `XLauncherImpl` 패턴과 같은 자리).

**호출** (`:core:notification`, (A) 구간):

```kotlin
val intent = mainDeepLinkIntentFactory.create(context).apply {
    putExtra(EXTRA_PUSH_DESTINATION_TYPE, type)
    putExtra(EXTRA_PUSH_DESTINATION_ID, id)  // NotificationTab이면 생략
}
val pendingIntent = PendingIntent.getActivity(
    context, requestCode, intent,
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
)
```

`requestCode`는 알림마다 달라야 서로 다른 도착지의 `PendingIntent`가 캐시로 뭉개지지 않는다 — `targetId`(없으면 `type`)의 해시를 쓴다(EC-011: 여러 알림이 각각 별개로 쌓여야 한다).

---

## 3. `MainEntryGate` (`:core:navigation` — `entry/`)

```kotlin
// core:navigation — entry/MainEntryGate.kt
@Singleton
class MainEntryGate @Inject constructor() {
    /** 이 프로세스에서 스플래시가 Main 진입을 확정했는가. 프로세스가 죽으면 함께 사라진다. */
    val isPassed: Boolean
    fun markPassed()
}
```

- **켜는 쪽**: `SplashActivity`가 `onNavigateToMain`에서 `mainLauncher.launch(...)` 직전에 `markPassed()`한다. 온보딩 갈래(`SplashEntry.Onboarding`)에서는 켜지 않는다 — 온보딩을 마친 뒤 Main으로 갈 때도 스플래시를 다시 지나므로 그 시점에 켜진다.
- **읽는 쪽**: `MainActivity.onCreate`만 읽는다(§4). 온보딩·프로필 판정을 담지 않는다 — 스플래시가 이미 내린 결론을 기억할 뿐이다([D13](../research.md#d13-알림-탭은-mainactivity를-겨냥하고-게이트-통과-여부는-탭-시점에-수신자가-판정한다), [ADR 2026-08-29](../../../adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md)).
- 배치 기준은 [core/navigation README §2.3](../../../../core/navigation/README.md) — 홀더들과 같은 "두 축 밖 공유 상태"다. 스코프만 `ActivityRetained`가 아니라 `Singleton`이다(Activity 하나가 아니라 프로세스의 사실).

`SplashActivity`는 `feature/splash/src/main/AndroidManifest.xml`에서 `android:launchMode="singleTop"`이다(D16). 푸시 extra를 받아 `MainLauncher`로 그대로 실어 넘기는 역할(1.0.x 계약 §3)은 유지하고, 진행 중에 우회 Intent가 오면 `onNewIntent`로 extra를 갱신한다:

```kotlin
// 필드로 보관한다 — onNewIntent가 갱신할 수 있어야 한다(D16)
private var pushDestinationType: String? = null
private var pushDestinationId: String? = null

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readPushDestination(intent)
    ...
    onNavigateToMain = {
        mainEntryGate.markPassed()
        mainLauncher.launch(this, withFinish = true) {
            pushDestinationType?.let { putExtra(EXTRA_PUSH_DESTINATION_TYPE, it) }
            pushDestinationId?.let { putExtra(EXTRA_PUSH_DESTINATION_ID, it) }
        }
    }
}

/** 스플래시가 진행 중에 콜드 우회(§4)가 도착한 경우. 진행 중인 세션 확보는 그대로 두고 도착지만 바꾼다. */
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    readPushDestination(intent)
}

private fun readPushDestination(intent: Intent) {
    intent.getStringExtra(EXTRA_PUSH_DESTINATION_TYPE)?.let {
        pushDestinationType = it
        pushDestinationId = intent.getStringExtra(EXTRA_PUSH_DESTINATION_ID)
    }
}
```

`onNewIntent`는 `SplashViewModel`을 건드리지 않는다 — 세션 확보·최소 노출·재시도는 이미 돌고 있고, 바뀌는 것은 전환 시점에 실을 extra뿐이다(D16). 알림 없이 켜진 스플래시에 우회가 도착하면 extra가 새로 생기고, 알림으로 켜진 스플래시에 다른 알림의 우회가 도착하면 나중 것이 이긴다.

온보딩으로 갈라지는 경우는 extra를 버린다 — 프로필이 없는 설치는 애초에 알림 수신 대상이 아니다(spec 1.0.3 §4 가정, [push-token-api.md §1](push-token-api.md#1-엔드포인트) `USER_NOT_REGISTERED`).

---

## 4. `MainActivity` — extra를 소비해 요청 홀더·도착지 탭을 정한다

```kotlin
@Inject lateinit var placeDetailRequestHolder: PlaceDetailRequestHolder
@Inject lateinit var roomDetailRequestHolder: RoomDetailRequestHolder   // D8
@Inject lateinit var mainEntryGate: MainEntryGate                        // §3
@Inject lateinit var splashDeepLinkIntentFactory: SplashDeepLinkIntentFactory  // §2, 콜드 우회

/** 웜 경로(onNewIntent)가 남기는 대기 중인 탭. MainShell이 소비한다(§5). */
private var pendingTab by mutableStateOf<MainTab?>(null)

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // (B) 콜드 우회 — 반드시 setContent 전에. 푸시 extra가 있을 때만 판정한다(D13 범위).
    val pushType = intent.getStringExtra(EXTRA_PUSH_DESTINATION_TYPE)
    if (pushType != null && !mainEntryGate.isPassed) {
        // 팩토리가 CLEAR_TOP을 걸어 두었다(§2 표, D16) — 진행 중인 스플래시가 있으면 그쪽이 onNewIntent로 받는다
        startActivity(splashDeepLinkIntentFactory.create(this).apply {
            putExtra(EXTRA_PUSH_DESTINATION_TYPE, pushType)
            intent.getStringExtra(EXTRA_PUSH_DESTINATION_ID)?.let { putExtra(EXTRA_PUSH_DESTINATION_ID, it) }
        })
        finish()
        return
    }
    enableEdgeToEdge()
    val startTab = resolvePendingPushDestination() ?: MainTab.HOME   // 콜드 (C), D9
    setContent {
        MinoAndroidAppTheme {
            MainShell(startTab = startTab, pendingTab = pendingTab, onPendingTabConsumed = { pendingTab = null }, ...)
        }
    }
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    resolvePendingPushDestination()?.let { pendingTab = it }        // 웜 (C), D15
}

/** extra를 읽고 지운 뒤, 홀더에 요청을 남기고 갈 탭을 돌려준다. extra가 없으면 null. */
private fun resolvePendingPushDestination(): MainTab? {
    val type = intent.getStringExtra(EXTRA_PUSH_DESTINATION_TYPE) ?: return null
    val id = intent.getStringExtra(EXTRA_PUSH_DESTINATION_ID)
    intent.removeExtra(EXTRA_PUSH_DESTINATION_TYPE)
    intent.removeExtra(EXTRA_PUSH_DESTINATION_ID)
    return when (type) {
        PUSH_DESTINATION_TYPE_PLACE -> id?.let {
            placeDetailRequestHolder.request(it, PlaceDetailEntryOrigin.NOTIFICATION)
            MainTab.SAVED
        } ?: MainTab.NOTIFICATION
        PUSH_DESTINATION_TYPE_ROOM -> id?.let {
            roomDetailRequestHolder.request(it)
            MainTab.SAVED
        } ?: MainTab.NOTIFICATION
        else -> MainTab.NOTIFICATION  // PUSH_DESTINATION_TYPE_NOTIFICATION_TAB·모르는 값 공통 낙하
    }
}
```

- 콜드 우회 분기는 **`setContent` 전**에 있어야 한다. 그 뒤로 미루면 ViewModel이 만들어지고 첫 서버 요청이 세션 없이 나간다(D13의 안전 조건).
- 분기 조건에 `pushType != null`(TYPE extra 존재)이 들어가는 이유는 범위를 푸시 탭으로 한정하기 위해서다. 넘기는 extra도 TYPE·ID 둘뿐이다. 프로세스 종료 후 최근 앱에서 복원되는 `MainActivity`는 extra가 없어 지금과 같은 경로를 탄다 — 세션은 SDK 복원 + ViewModel의 `ensureSession()` 선행으로 확보되므로 이 feature가 다룰 것이 없다([D13](../research.md#d13-알림-탭은-mainactivity를-겨냥하고-게이트-통과-여부는-탭-시점에-수신자가-판정한다) 1.2.2 정정).
- `PlaceDetailEntryOrigin.NOTIFICATION`은 이미 `core/navigation/entry/PlaceDetailRequestHolder.kt`에 예약돼 있던 값이다 — 이번 구현이 그 값을 처음 실사용한다.
- `id == null`인데 `type`은 `PLACE`/`ROOM`인 경우(EC-009)도 `NotificationTab` 낙하로 흡수한다. `MinoFirebaseMessagingService`가 [`PushDestination`](../data-model.md#3-pushdestination) 해석 시점에 이미 걸러내므로 방어적 이중 처리다.
- `onNewIntent`는 `singleTask`(D14)여야 온다. `standard`면 새 인스턴스가 `onCreate`로 뜬다.

---

## 5. `MainShell`/`MainNavHost` — 시작 탭 파라미터와 대기 탭 소비

```kotlin
// feature:main — MainShell.kt
internal fun MainShell(
    startTab: MainTab = MainTab.HOME,          // 콜드 (D9)
    pendingTab: MainTab? = null,               // 웜 (D15)
    onPendingTabConsumed: () -> Unit = {},
    onRequestPlaceDetail: (pinId: String, origin: PlaceDetailEntryOrigin) -> Unit,
    ...
) {
    val navController = rememberNavController()
    LaunchedEffect(pendingTab) {
        pendingTab?.let { navController.navigateToTab(it); onPendingTabConsumed() }
    }
    ... MainNavHost(navController, startTab = startTab, ...) ...
}

// feature:main — MainNavHost.kt
internal fun MainNavHost(navController: NavHostController, startTab: MainTab = MainTab.HOME, ...) {
    MinoNavHost(navController = navController, startDestination = startTab.route, ...) { ... }
}
```

콜드에서 `startDestination`을 쓰는 이유는 [D9](../research.md#d9-알림-탭-목록-도착지는-maintabnotification을-그대로-쓴다), 웜에서 명령형을 쓰는 이유는 [D15](../research.md#d15-웜-경로의-탭-전환은-명령형이고-대기-중인-도착지-탭을-mainactivity가-상태로-든다)가 소유한다. 홀더 `request()`는 두 경로 모두 탭 전환 **전에** 끝나 있어야 저장 탭이 들어오면서 요청을 본다 — §4의 소비 함수가 그 순서를 보장한다.

---

## 6. `RoomDetailRequestHolder` 소비 (`:feature:room` — `RoomListViewModel`)

`PlaceDetailRequestHolder`의 `observePlaceDetailRequests()`(`RoomListViewModel.kt`)와 같은 자리에 대칭으로 추가한다.

```kotlin
private fun observeRoomDetailRequests() {
    launchSafely {
        roomDetailRequestHolder.pending
            .filterNotNull()
            .collect { roomId ->
                roomDetailRequestHolder.consume()
                updateState { copy(selectedRoomId = roomId) }
            }
    }
}
```

방 조회를 다시 하지 않는다 — `observeMyRooms()`가 이미 실시간으로 관찰 중인 방 목록에 그 `roomId`가 있으면 방 상세가 그 데이터를 그대로 그린다. 아직 로드되지 않았거나 접근할 수 없는 경우의 처리는 이 문서 범위 밖이다(spec EC-010 — "도착지 화면이 자신의 규칙으로 처리한다").
