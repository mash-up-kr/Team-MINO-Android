# 계약: 온보딩 진입 (`OnboardingLauncher`)

**대상 스펙 경로**: `docs/specs/onboarding-flow` · **부속 문서**: [plan.md](../plan.md)

온보딩 밖에서 온보딩을 여는 유일한 표면이다. 계약이 놓이는 자리와 작성 규칙은 [`feature-navigation.md`](../../../architecture/feature-navigation.md) 1장, API는 [`core:navigation` README](../../../../core/navigation/README.md) §2.1이 소유한다.

---

## 1. 계약 인터페이스

`:core:navigation` — `activity/launcher/OnboardingLauncher.kt`

```
interface OnboardingLauncher : ActivityLauncher
```

구현은 `:feature:onboarding`의 `di/OnboardingLauncherImpl.kt`가 갖고, `createIntent`에서 `OnboardingActivity`만 지목한다.

---

## 2. 진입 인자 — 없다

**`ExtraTag.kt`에 추가하는 상수가 없다.** 온보딩은 어디서 열리든 같은 플로우이고, 어느 스텝부터 시작할지는 호출자가 아니라 이 설치에 저장된 진행 상태가 정한다(FR-023 · [contracts/onboarding-progress.md](onboarding-progress.md) §3).

```
// 유일한 호출 형태
onboardingLauncher.launch(activity, withFinish = true)
```

- `withFinish = true`인 이유: 호출자(스플래시)는 되돌아올 대상이 아니다. 온보딩에서 뒤로가기로 스플래시가 다시 보이면 FR-006·FR-007이 뚫린다.
- 결과를 받지 않으므로 `resultLauncher`를 넘기지 않는다 — 온보딩이 끝나면 스스로 메인 탭을 연다(§3).

---

## 3. 결과 계약 — 없다

**온보딩은 `setResult`를 호출하지 않는다.** 온보딩의 종착지는 호출자가 아니라 홈 탭이고, 그 전환은 온보딩이 직접 `MainLauncher`로 한다(FR-019·FR-021 · [research.md R-019](../research.md)).

```
// OnboardingActivity — 완료 표시를 기록한 뒤
mainLauncher.launch(this, withFinish = true)
```

호출자가 결과를 기다리지 않으므로, 온보딩이 도중에 프로세스와 함께 죽어도 호출자가 매달려 있지 않는다.

---

## 4. 이 계약이 소비하는 다른 feature의 계약

온보딩은 두 스텝을 다른 feature에 위임한다([research.md R-002](../research.md)). **아래 표의 상수·결과 값은 각 계약 문서가 소유한다** — 여기서는 온보딩이 무엇을 넘기고 무엇을 읽는지만 적는다.

| 스텝 | 계약 (`:core:navigation`) | 온보딩이 넘기는 것 | 온보딩이 읽는 것 | 온보딩의 반응 |
|---|---|---|---|---|
| 프로필 설정 | `ProfileLauncher` | `EXTRA_PROFILE_ENTRY_POINT` = `PROFILE_ENTRY_POINT_ONBOARDING` | `RESULT_OK` | `ROOM_FORM` 스텝으로 (FR-002) |
| 공동방 생성 | `RoomFormLauncher` | `EXTRA_ROOM_FORM_ONBOARDING` = `true` | `EXTRA_ROOM_FORM_RESULT_OUTCOME` = `ROOM_FORM_OUTCOME_CREATED` + `EXTRA_ROOM_FORM_RESULT_ROOM_ID` | `INVITE` 스텝으로 (FR-004) |
| 〃 | 〃 | 〃 | `ROOM_FORM_OUTCOME_SKIPPED` | `TUTORIAL` 스텝으로 — 친구 초대를 거른다 (FR-003) |
| 〃 | 〃 | 〃 | `RESULT_CANCELED` | 같은 스텝을 다시 연다 ([research.md R-020](../research.md)) |
| 홈 진입 | `MainLauncher` | 없음 | 없음 (`withFinish = true`) | — |

두 계약 모두 **결과를 받아야 하므로** `launch(activity, resultLauncher = …)` 형태로 부르고 `withFinish`를 쓰지 않는다.

**세 계약 모두 코드로 존재하고 온보딩 진입을 이미 지원한다(2026-08-29 확인).**

| 상수·값 | 어디에 있는가 | 대상 feature의 처리 |
|---|---|---|
| `PROFILE_ENTRY_POINT_ONBOARDING` | `activity/launcher/ProfileLauncher.kt` | `ProfileEntryPoint.Onboarding` — 진입 시 서버 갱신을 걸지 않고 뒤로가기를 막는다 |
| `EXTRA_ROOM_FORM_ONBOARDING` | `activity/launcher/ExtraTag.kt` | `RoomForm(isOnboarding = true)` — [건너뛰기]를 노출하고 뒤로가기를 막는다 |
| `ROOM_FORM_OUTCOME_CREATED` + `EXTRA_ROOM_FORM_RESULT_ROOM_ID` | 〃 | `RoomFormOutcome.Created(roomId)` → `RESULT_OK` |
| `ROOM_FORM_OUTCOME_SKIPPED` | 〃 | `RoomFormOutcome.Skipped` → `RESULT_OK` |
| — | 〃 | `RoomFormOutcome.Cancelled` → `RESULT_CANCELED`(extra 없음) |

**이 계획이 두 계약에 더하는 것은 없다.** 넷 다 두 feature가 온보딩 호출자를 예상하고 미리 만들어 둔 것이고, 이 계획이 그 첫 소비자다.

`ProfileActivity`가 저장 시 `setResult(RESULT_OK)`만 하고 프로필 값을 싣지 않는 것도 그대로 쓴다 — 값의 원천은 `ProfileRepository.observeProfile()`이고, 온보딩은 "저장됐다"는 사실만 필요하다.

**온보딩은 두 feature 모듈을 의존하지 않는다.** `:feature:onboarding`의 `build.gradle.kts`에 `:feature:profile`·`:feature:roomform`이 없다는 것이 그 확인 방법이다.

---

## 5. DI 배선

| 무엇 | 어디 | 스코프 |
|---|---|---|
| `OnboardingLauncherImpl` → `OnboardingLauncher` `@Binds` | `:feature:onboarding`의 `di/OnboardingNavigationModule.kt` | `ActivityRetainedComponent` + `@ActivityRetainedScoped` |

구현을 가진 모듈이 바인딩을 소유한다 — [`dependency-injection.md`](../../../conventions/dependency-injection.md). `:app`은 `implementation(project(":feature:onboarding"))`으로 그래프에 넣기만 한다.

---

## 6. 매니페스트

`:feature:onboarding`의 `AndroidManifest.xml`에 `OnboardingActivity`를 `android:exported="false"`로 선언한다. **런처 intent-filter를 두지 않는다** — 런처 자격은 `:feature:splash`가 갖는다.

---

## 7. 이 계약이 지켜지는지 보는 법

| 확인 | 방법 |
|---|---|
| feature 간 의존이 없다 | `feature/onboarding/build.gradle.kts`에 다른 `:feature:*`가 없다 |
| 온보딩이 결과를 돌려주지 않는다 | `OnboardingActivity`에 `setResult` 호출이 없다 |
| 진입 인자가 없다 | `OnboardingActivity`에 `intent.get*Extra` 호출이 없다 |
| 재개 지점을 호출자가 정하지 않는다 | 시작 스텝의 원천이 `ResolveOnboardingStepUseCase` 한 곳이다 |

---

## 8. 이 계약이 스플래시에 요구하는 변경

**지금 스플래시는 온보딩이 아니라 프로필을 직접 연다.**

```kotlin
// SplashActivity.kt — 현재
onNavigateToOnboarding = {
    profileLauncher.launch(this, withFinish = true) {
        putExtra(EXTRA_PROFILE_ENTRY_POINT, PROFILE_ENTRY_POINT_ONBOARDING)
    }
}
```

1.0.x 시점에는 `:feature:onboarding`이 없어 스플래시 계획이 `TBD-P4`("`OnboardingLauncher` 대상 없음")로 열어 두었고, 그 자리를 프로필 직접 호출로 메운 상태로 머지됐다.

### 8.1 이대로 두면 깨지는 것

| # | 무엇 | 근거 |
|---|---|---|
| 1 | **프로필 저장 후 갈 곳이 없다.** `ProfileActivity`가 `setResult(RESULT_OK); finish()`로 닫히는데 호출자인 스플래시는 `withFinish = true`로 이미 죽어 있다 — 태스크가 비어 앱이 내려간다 | FR-001·TS-002 |
| 2 | 공동방 생성·친구 초대·튜토리얼 스텝에 도달할 경로가 없다 | FR-001·FR-003·FR-004 |
| 3 | 진행 상태를 기록·복원하는 주체가 없어 재개가 성립하지 않는다 | FR-023·FR-024 |

### 8.2 요구하는 형태

```kotlin
// SplashActivity.kt — 이 계획이 요구하는 것
onNavigateToOnboarding = { onboardingLauncher.launch(this, withFinish = true) }
```

- `ProfileLauncher` 주입과 두 extra 상수 import가 스플래시에서 빠지고, `OnboardingLauncher` 주입이 들어온다. **프로필 진입 인자를 싣는 주체는 `OnboardingActivity`로 옮겨간다**(§4).
- `:core:navigation`의 두 계약 모두 그 모듈에 있으므로 **모듈 의존 그래프는 바뀌지 않는다.**
- `SplashShell`·`SplashNavHost`·`SplashRoute`·`SplashViewModel`은 손대지 않는다. 콜백 이름(`onNavigateToOnboarding`)이 이미 이 의미다.

### 8.3 스플래시 스펙과 충돌하지 않는 이유

`docs/specs/splash-screen` FR-003은 *"프로필 설정([SYS-011])으로 시작하는 온보딩([SCR-002])으로 이동한다"* 이고, 같은 문서의 비목표가 *"온보딩([SCR-002])과 프로필 설정([SYS-011])의 화면 구성·진행 로직은 이 스펙이 정의하지 않는다. 이 스펙은 스플래시에서 그 화면으로의 진입 시점까지만 다룬다"* 로 적었다. **어느 Activity가 그 진입을 받는지는 온보딩의 몫**이라는 뜻이므로, 이 변경은 그 스펙을 어기지 않는다. 근거는 [research.md R-023](../research.md).

진입 **판정**(어떤 조건에서 온보딩을 여는가)의 변경은 별도이며 [onboarding-progress.md §4](onboarding-progress.md)가 소유한다.
