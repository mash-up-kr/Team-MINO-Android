# 계약: 스플래시 화면 (UI · 진입 · 전환)

**소유 모듈**: `:feature:splash`

**대응 요구사항**: FR-001, FR-005, FR-006, FR-007, FR-010, UX-001~UX-007

---

## 1. 공개 표면

진입형 feature의 공개 표면은 `XActivity` 하나뿐이다([feature-module.md §1 공개 범위](../../../architecture/feature-module.md)).

```kotlin
class SplashActivity : ComponentActivity()   // public
```

- `SplashShell`·`SplashRoute`·`SplashScreen`·`SplashViewModel`·`SplashUiState`는 모두 `internal`.
- **스플래시는 `Launcher` 계약을 갖지 않는다.** 다른 feature가 스플래시를 여는 일이 없기 때문이다 — OS 런처만이 진입점이다.

## 2. 매니페스트 계약

```xml
<!-- :feature:splash -->
<activity android:name=".SplashActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

**동반 변경**: `:feature:main`의 `MainActivity`에서 `intent-filter`(MAIN·LAUNCHER)를 **제거**하고 `android:exported`를 재검토한다. LAUNCHER가 두 개면 런처에 아이콘이 둘 뜬다.

## 3. 화면 계약

| 요소 | 규칙 | 근거 |
|---|---|---|
| 브랜드 레이어 | 캐릭터 5종·`gguk` 워드마크·태그라인·구름 배경. **항상** 노출되며 상태와 무관하다 | FR-001, UX-004 |
| 로딩 스피너 | `SplashUiState.isSpinnerVisible`일 때만 브랜드 레이어 **위에** 얹는다 | FR-006, UX-005 |
| 오류 토스트 | `MinoSnackbar`. 좌우 **20dp**, 화면 하단에서 **40dp** 띄운 위치 | UX-003, R-006 |
| 전환 버튼·안내 문구 | **두지 않는다.** 재시도 버튼도 없다 | UX-001 |
| 터치 처리 | 소비하지 않는다. 어떤 제스처도 전환에 영향을 주지 않는다 | FR-005 |
| 에러 전용 화면 | **없다.** 지연·실패에도 브랜드 화면을 유지한다 | UX-004 |

- `40px`(Figma) → `40dp`(Android) 환산 근거는 [spec.md §4 가정](../spec.md)이 소유한다.
- 토큰 사용 여부는 코드가 아니라 Figma 원본과의 대조로 판정한다([figma-design-fidelity.md](../../../conventions/figma-design-fidelity.md)).

## 4. MVI 계약

```kotlin
internal data class SplashUiState(val isSpinnerVisible: Boolean = false)

internal sealed interface SplashIntent {
    data object Start : SplashIntent
}

internal sealed interface SplashSideEffect {
    data class NavigateTo(val entry: SplashEntry) : SplashSideEffect
    data class ShowToast(val toast: SplashToast) : SplashSideEffect
}
```

상세 전이표는 [data-model.md §2](../data-model.md)가 소유한다.

## 5. 전환 계약

| 목적지 | 수단 | 상태 |
|---|---|---|
| `SplashEntry.Main` | `MainLauncher.launch(activity, withFinish = true)` | 계약 존재 ([MainLauncher.kt](../../../../core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/MainLauncher.kt)) |
| `SplashEntry.Onboarding` | **온보딩 진입 계약을 `withFinish = true`로 호출한다** | 아래 참고 |

- `withFinish = true`로 스플래시를 종료해 뒤로가기로 되돌아오지 못하게 한다.
- `withFinish`와 `resultLauncher`는 함께 쓰지 않는다([core/navigation README §2.1](../../../../core/navigation/README.md)).
- **스플래시는 새 전환 계약을 만들지 않는다.** `:core:navigation`에 이미 있거나 온보딩이 만드는 것을 소비한다.

### 온보딩 진입 계약의 대상 — 이 계획이 정하지 않는다

현재 구현은 `ProfileLauncher`를 직접 부르며 온보딩의 첫 스텝을 여는 형태다([ProfileLauncher.kt](../../../../core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/ProfileLauncher.kt) + `PROFILE_ENTRY_POINT_ONBOARDING`). 이는 `:feature:onboarding`이 없던 시점의 임시 배선이며, **`docs/specs/onboarding-flow`가 그것을 `OnboardingLauncher`로 대체하기로 정했다**([contracts/onboarding-launcher.md §8](../../onboarding-flow/contracts/onboarding-launcher.md)).

이 문서가 계약으로 고정하는 것은 **성질 셋**이고, 어느 `Launcher`인지는 온보딩이 정한다.

| # | 성질 | 근거 |
|---|---|---|
| 1 | `withFinish = true` — 스플래시로 되돌아올 수 없다 | FR-010 · 온보딩 spec FR-006 |
| 2 | 결과를 받지 않는다 — `resultLauncher`를 넘기지 않는다 | 온보딩의 종착지는 스플래시가 아니다 |
| 3 | **어느 스텝부터 여는지를 스플래시가 지정하지 않는다** | spec §3.2 · 온보딩 spec FR-023 |

3번이 `ProfileLauncher` 직접 호출을 대체해야 하는 이유다 — 그 호출은 **프로필 설정 스텝을 지정**하고 있어, 재개해야 할 사용자에게도 첫 스텝을 연다.

## 6. 상위 계약 준수 — `anonymous-auth-session` 호출자 계약

세션 확보를 소비하는 진입 화면은 그 스펙의 **호출자 계약 C-1~C-8**을 지켜야 한다. 대조 결과는 [plan.md §호출자 계약 대조](../plan.md)가 소유한다. 이 화면이 특히 코드로 보장해야 하는 둘:

- **C-2** — `EnsureAnonymousSessionUseCase`가 정상 반환하기 전에는 어떤 전환도 하지 않는다(FR-010).
- **C-5** — 재시도 루프를 **도메인 예외 수신에만 종속시키지 않는다.** 예외 매핑은 화이트리스트라 열거 밖 실패는 CEH로 가는데, 루프가 `runCatchingDomain`의 실패 콜백에만 걸려 있으면 그때 루프가 조용히 끝나고 화면이 안내도 재시도도 없이 영구히 멈춘다(→ [research.md R-013](../research.md)).

## 7. 재시도 계약

- 실패 후 **`EnsureAnonymousSessionUseCase`와 `ResolveSplashEntryUseCase`를 한 묶음으로** 자동 재호출한다. 사용자 조작을 요구하지 않는다(FR-010). 두 호출 모두 멱등이라 반복이 안전하다.
- **재시도 대상이 세션 확보만이 아니다.** 판정이 실패해도 같은 루프가 돈다 — 그래서 오프라인 재실행이 이 루프에 갇힌다(spec `EC-002`·`TS-016`). spec 5.0.0이 승인한 동작이며, 루프를 빠져나가는 유일한 조건은 `SplashEntry`가 만들어지는 것이다([research.md R-020](../research.md)).
- 성공하는 순간 표출 중인 토스트와 무관하게 즉시 전환한다(EC-005).
- 토스트는 직전 표출로부터 **최소 10초** 간격을 두고 반복한다(UX-006).
- 재시도 **횟수 상한은 두지 않는다**(호출자 계약 C-4, spec §4 가정). 간격은 구현 단계에서 정하고 `research.md`에 덧붙인다.
- 13초 임계(FR-007)를 `withTimeout`으로 걸지 않는다 — `TimeoutCancellationException`이 `CancellationException`이라 도메인 예외 경로를 타지 않고 CEH로 샌다(→ [research.md R-004·R-013](../research.md)).
