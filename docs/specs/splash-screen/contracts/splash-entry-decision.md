# 계약: 앱 진입 판정 (`ResolveSplashEntryUseCase`)

**소유 모듈**: `:core:domain` (`usecase/`)

**대응 요구사항**: FR-002, FR-003, FR-004, SC-002

**판정 기준의 소유자**: 이 문서가 **아니다.** `docs/specs/onboarding-flow`(FR-021·FR-022)가 조건을 정하고 이 계약은 그것을 실행 가능한 형태로 옮긴다 — 소유권 규칙은 [ADR 2026-08-29 앱 진입 화면 판정](../../../adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md)이 갖는다.

> **plan 4.0.0에서 신설된 문서다.** 3.0.2까지 이 판정은 근거가 하나뿐이라 [data-model.md §1.2](../data-model.md)의 표 한 줄로 충분했다. 근거가 둘로 늘고 **호출 순서 제약**과 **교차 스펙 소유권**이 붙으면서 계약으로 세운다.

---

## 1. 인터페이스

```kotlin
class ResolveSplashEntryUseCase @Inject constructor(
    private val profileRegistrationRepository: ProfileRegistrationRepository,
    private val onboardingProgressRepository: OnboardingProgressRepository,
) {
    suspend operator fun invoke(): SplashEntry
}
```

| 주입 | 계약 소유 | 조회 성격 |
|---|---|---|
| `ProfileRegistrationRepository` | **이 스펙** — [profile-registration.md](profile-registration.md) | 서버 (`GET /api/v1/users/me`) |
| `OnboardingProgressRepository` | `docs/specs/onboarding-flow` — [contracts/onboarding-progress.md](../../onboarding-flow/contracts/onboarding-progress.md) | 이 설치의 로컬 저장소 |

- **세션을 확보하지 않는다.** 순서는 호출자(`SplashViewModel`)가 보장한다 — [research.md R-012](../research.md).
- **`:feature:onboarding`을 의존하지 않는다.** 읽는 것은 `:core:domain`의 인터페이스이며 모듈 경계가 그대로다(헌법 원칙 II).

## 2. 판정 규칙

| # | `isRegistered()` | `getProgress().isCompleted` | 반환 | 대응 |
|---|---|---|---|---|
| 1 | `false` | — | `SplashEntry.Onboarding` | FR-003 / TS-002 |
| 2 | `true` | `false` | `SplashEntry.Onboarding` | **FR-003 / TS-003-1** |
| 3 | `true` | `true` | `SplashEntry.Main` | FR-004 / TS-003 |

**둘 중 하나라도 없으면 온보딩이다.** 프로필 저장은 온보딩 네 스텝 중 첫 스텝일 뿐이므로, 그것만으로 완료를 판정하면 중단한 사용자가 메인 탭으로 밀려나 남은 스텝을 영영 보지 못한다(SC-002).

**어느 스텝부터 여는지는 이 계약이 정하지 않는다.** 재개 지점 판정은 온보딩의 몫이며(그 spec FR-023), 그래서 `SplashEntry.Onboarding`을 더 가르지 않는다 — [research.md R-018](../research.md).

## 3. 호출 순서 — 계약의 일부다

**`isRegistered()`를 먼저 호출한다.**

그 판정이 미등록일 때 **프로필 로컬 캐시를 비우는 부수 효과**를 갖고(→ [`ProfileRegistrationRepository`](../../../../core/domain/src/main/kotlin/team/mino/core/domain/repository/ProfileRegistrationRepository.kt) 문서 주석), `:feature:profile`의 등록/수정 분기가 그 보장에 기댄다(`ProfileEntryPoint.needsRefresh`).

**이 제약은 타입에 드러나지 않는다.** `isRegistered(): Boolean`은 순수 조회로 읽히므로, 완료 표시를 먼저 읽고 단축 평가로 이 호출을 건너뛰는 최적화가 **컴파일도 이 UseCase의 테스트도 통과한다.** 깨지는 것은 프로필 화면의 저장 경로이고, 증상은 저장이 `PATCH`로 나가 서버가 거절하는 것으로 나타난다. 근거와 구속력은 [ADR 2026-08-29](../../../adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md) §결정 3항이 소유한다.

규칙 #1에서 완료 표시를 읽지 않아도 결과는 같지만, 위 순서 제약 때문에 얻는 것이 없으므로 단축 여부는 구현 재량이다.

## 4. 실패

| 상황 | 동작 | 근거 |
|---|---|---|
| `isRegistered()`가 `MinoDomainException`을 던짐 | 그대로 전파한다. `Onboarding`으로 뭉개지 않는다 | EC-004 · SC-002 |
| `getProgress()`가 예외를 던짐 | 그대로 전파한다 | 〃 |

- **어느 근거의 실패도 `SplashEntry`를 만들지 않는다.** 실패는 반환값이 아니라 예외이며, 소비는 `SplashViewModel`의 `runCatchingDomain`이 한다([research.md R-016](../research.md)).
- **이 성질이 오프라인 재실행을 막는다.** `isRegistered()`가 서버 조회라 오프라인에서는 반드시 실패하고, 실패가 `SplashEntry`를 만들지 않으므로 화면이 재시도 루프에 머문다. spec 5.0.0 `§5 TBD-5`가 이 결과를 승인했다 — **실패를 `Main`으로 뭉개는 폴백을 넣지 않는다**([research.md R-020](../research.md)).
- 로컬 저장 읽기 실패는 `MinoDomainException`으로 매핑되지 않는 **버그**이므로 CEH로 간다 — 온보딩 계약이 그렇게 정했다([contracts/onboarding-progress.md §1](../../onboarding-flow/contracts/onboarding-progress.md)). 재시도 루프가 그 실패에도 끊기지 않아야 한다는 요구는 [research.md R-013](../research.md)이 이미 든다.

## 5. 이 계약의 구현 소유

**설계는 이 계획이, 코드 변경은 온보딩 작업이 갖는다** — [research.md R-019](../research.md).

`OnboardingProgressRepository`가 온보딩 작업에서 처음 생기므로, 두 변경을 나누면 이 파일이 존재하지 않는 타입을 참조해 빌드가 깨진다. `docs/specs/onboarding-flow` plan 2.0.1이 이 변경을 자기 범위에 올려 두었다.

## 6. 이 계약이 지켜지는지 보는 법

| 확인 | 방법 |
|---|---|
| 판정이 두 근거를 본다 | `ResolveSplashEntryUseCase`가 Repository 둘을 주입받는다 |
| 순서가 지켜진다 | 함수 본문에서 `isRegistered()` 호출이 `getProgress()`보다 앞선다 |
| 스텝 판정이 새어 들어오지 않았다 | `SplashEntry`의 리프가 둘이고 `OnboardingStep`을 참조하지 않는다 |
| 모듈 경계가 그대로다 | `feature/splash/build.gradle.kts`에 `:feature:onboarding`이 없다 |
| 실패가 뭉개지지 않는다 | 함수 본문에 `try`·`catch`·`runCatching`이 없다 |
